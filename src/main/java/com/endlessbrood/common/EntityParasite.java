package com.endlessbrood.common;

import java.math.BigInteger;
import java.util.List;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.EntityAIAttackOnCollide;
import net.minecraft.entity.ai.EntityAIHurtByTarget;
import net.minecraft.entity.ai.EntityAILookIdle;
import net.minecraft.entity.ai.EntityAINearestAttackableTarget;
import net.minecraft.entity.ai.EntityAISwimming;
import net.minecraft.entity.ai.EntityAIWander;
import net.minecraft.entity.ai.EntityAIWatchClosest;
import net.minecraft.entity.monster.EntityMob;
import net.minecraft.entity.passive.EntityAnimal;
import net.minecraft.entity.passive.EntityVillager;
import net.minecraft.entity.monster.EntityZombie;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.MathHelper;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionEffect;
import net.minecraft.world.World;

/** Original parasite with mutations and deliberately limited, slow tunnelling AI. */
public class EntityParasite extends EntityMob {
    private int gene = ParasiteGenes.STANDARD;
    private boolean geneAssigned;
    private int genome;
    private int lastAppliedMorph = -1;
    // Position of the nest that spawned this parasite; only stored for nest-born mobs.
    private boolean hasBroodHome;
    private int broodHomeX;
    private int broodHomeY;
    private int broodHomeZ;
    private long nextDefenseAlarmTick;

    public void setBroodHome(int x, int y, int z) {
        if (worldObj.isRemote) return;
        hasBroodHome = true;
        broodHomeX = x;
        broodHomeY = y;
        broodHomeZ = z;
    }

    public void clearBroodHome() { hasBroodHome = false; }
    public boolean hasBroodHome() { return hasBroodHome; }
    public int getBroodHomeX() { return broodHomeX; }
    public int getBroodHomeY() { return broodHomeY; }
    public int getBroodHomeZ() { return broodHomeZ; }

    /** Called when an already-spawned leader is attacked. Rate-limited locally. */
    @SuppressWarnings("unchecked")
    public void raiseLeaderDefenseAlarm(EntityLivingBase attacker) {
        if (worldObj.isRemote || !BroodConfig.leaderDefenseAlarm ||
            getMorph() != BroodColonyRules.QUEEN_MORPH ||
            attacker == null || attacker.isDead || attacker instanceof EntityParasite ||
            (attacker instanceof EntityPlayer &&
             ((EntityPlayer) attacker).capabilities.isCreativeMode)) return;
        long now = worldObj.getTotalWorldTime();
        if (now < nextDefenseAlarmTick) return;
        nextDefenseAlarmTick = now + 200L;
        int radius = BroodConfig.leaderDefenseRadius;
        List<EntityParasite> nearby = worldObj.getEntitiesWithinAABB(EntityParasite.class,
            boundingBox.expand(radius, 6.0D, radius));
        int assigned = 0;
        for (EntityParasite guard : nearby) {
            if (guard == this || guard.isDead || guard.getAttackTarget() != null ||
                getDistanceSqToEntity(guard) > (double) radius * radius) continue;
            guard.setAttackTarget(attacker);
            if (++assigned >= BroodConfig.leaderDefenseLimit) break;
        }
    }

    /** DataWatcher synchronizes appearance for multiplayer clients. */
    @Override
    protected void entityInit() {
        super.entityInit();
        this.dataWatcher.addObject(20, Byte.valueOf((byte) 0)); // body type
        this.dataWatcher.addObject(21, Byte.valueOf((byte) 0)); // genetic adaptation
        this.dataWatcher.addObject(22, Byte.valueOf((byte) 0)); // stackable extra traits
    }

    public int getMorph() {
        return this.dataWatcher.getWatchableObjectByte(20);
    }

    public int getGene() {
        return this.dataWatcher.getWatchableObjectByte(21);
    }

    public int getGenome() {
        return this.dataWatcher.getWatchableObjectByte(22) & 15;
    }

    private void setMorph(int morph) {
        this.dataWatcher.updateObject(20, Byte.valueOf((byte) morph));
        updateSizeForMorph();
    }

    /** Called by custom spawn eggs on the authoritative server before spawning. */
    public void configureSpawnMorph(int morph) {
        if (this.worldObj.isRemote) return;
        BigInteger stage = EvolutionData.get(this.worldObj).getStage();
        gene = ParasiteGenes.roll(stage, this.rand);
        genome = ParasiteGenome.roll(stage, this.rand);
        this.dataWatcher.updateObject(22, Byte.valueOf((byte) genome));
        this.dataWatcher.updateObject(21, Byte.valueOf((byte) gene));
        setMorph(morph >= 0 && morph <= BroodColonyRules.QUEEN_MORPH ? morph : 0);
        geneAssigned = true;
        updateAttributesFromEvolution();
    }

    private void updateSizeForMorph() {
        int kind = getMorph();
        if (lastAppliedMorph == kind) return;
        lastAppliedMorph = kind;
        // Modest hitboxes to avoid getting large models stuck in caves.
        if (kind == 5) setSize(1.45F, 2.1F);
        else if (kind == 4) setSize(1.9F, 2.5F);
        else if (kind == 2) setSize(1.0F, 1.5F);
        else if (kind == 1) setSize(0.7F, 0.75F);
        else setSize(0.85F, 1.2F);
    }
    private int digX;
    private int digY;
    private int digZ;
    private int digProgress;
    private boolean digging;

    public EntityParasite(World world) {
        super(world);
        this.setSize(0.85F, 1.2F);
        this.tasks.addTask(0, new EntityAISwimming(this));
        this.tasks.addTask(2, new EntityAIAttackOnCollide(this, 1.12D, true));
        // Optional bounded navigation toward a registered village on late-stage nights.
        this.tasks.addTask(3, new EntityAIBroodNestGuard(this));
        this.tasks.addTask(4, new EntityAIBroodVillageRaid(this));
        this.tasks.addTask(5, new EntityAIWander(this, 0.8D));
        this.tasks.addTask(6, new EntityAIWatchClosest(this, EntityPlayer.class, 8.0F));
        this.tasks.addTask(7, new EntityAILookIdle(this));
        this.targetTasks.addTask(1, new EntityAIHurtByTarget(this, true));
        // Parasites can track nearby victims behind obstacles, but dig only while close.
        this.targetTasks.addTask(2, new EntityAINearestAttackableTarget(this, EntityPlayer.class, 0, false));
        this.targetTasks.addTask(3, new EntityAINearestAttackableTarget(this, EntityAnimal.class, 0, true));
        this.targetTasks.addTask(4, new EntityAINearestAttackableTarget(this, EntityVillager.class, 0, true));
        this.targetTasks.addTask(5, new EntityAINearestAttackableTarget(this, EntityZombie.class, 0, true));
    }

    @Override
    protected boolean isAIEnabled() {
        return true;
    }

    @Override
    protected String getLivingSound() {
        return "mob.spider.say";
    }

    @Override
    protected String getHurtSound() {
        return "mob.spider.say";
    }

    @Override
    protected String getDeathSound() {
        return "mob.spider.death";
    }

    @Override
    protected void applyEntityAttributes() {
        super.applyEntityAttributes();
        this.getEntityAttribute(SharedMonsterAttributes.maxHealth).setBaseValue(20.0D);
        this.getEntityAttribute(SharedMonsterAttributes.movementSpeed).setBaseValue(0.23D);
        this.getEntityAttribute(SharedMonsterAttributes.attackDamage).setBaseValue(3.0D);
    }

    @Override
    public boolean getCanSpawnHere() {
        if (!super.getCanSpawnHere()) return false;
        BroodOutbreakData outbreak = BroodOutbreakData.get(worldObj);
        if (!outbreak.graceExpired(worldObj.getTotalWorldTime()) ||
            (BroodConfig.blockNaturalSpawnInHomeZones &&
             !outbreak.farFromBases(posX, posZ))) return false;
        boolean ward = BroodConfig.wardsBlockNaturalSpawns && BroodWardRules.protects(worldObj,
            MathHelper.floor_double(posX), MathHelper.floor_double(posY),
            MathHelper.floor_double(posZ));
        if (BroodConfig.wardsBlockNaturalSpawns && ward) return false;
        return this.worldObj.getEntitiesWithinAABB(EntityParasite.class,
            this.boundingBox.expand(24.0D, 12.0D, 24.0D)).size() < BroodConfig.localPopulationLimit;
    }

    @Override
    public void onLivingUpdate() {
        super.onLivingUpdate();
        updateSizeForMorph(); // Also runs client-side after DataWatcher updates.
        if (this.worldObj.isRemote) return;
        if (!geneAssigned) {
            BigInteger stage = EvolutionData.get(this.worldObj).getStage();
            gene = ParasiteGenes.roll(stage, this.rand);
            genome = ParasiteGenome.roll(stage, this.rand);
            this.dataWatcher.updateObject(21, Byte.valueOf((byte) gene));
            this.dataWatcher.updateObject(22, Byte.valueOf((byte) genome));
            // Both early-game stalkers and scuttlers have unique 3D meshes.
            // More heavily mutated bodies unlock as the world evolves.
            int morph = rand.nextInt(100) < 42 ? 1 : 0;
            if (stage.compareTo(BigInteger.valueOf(3)) >= 0 && rand.nextInt(8) == 0) morph = 2;
            if (stage.compareTo(BigInteger.valueOf(10)) >= 0 && rand.nextInt(6) == 0) morph = 3;
            setMorph(morph);
            geneAssigned = true;
            updateAttributesFromEvolution();
        }
        if (BroodConfig.scuttlerPounce && getMorph() == 1 &&
            (this.ticksExisted + this.getEntityId()) % 120 == 0) pounceAtPrey();
        if (getMorph() == BroodColonyRules.QUEEN_MORPH &&
            (this.ticksExisted + this.getEntityId()) % BroodConfig.leaderRallyTicks == 0) {
            rallyNearbyBrood();
        }
        if (BroodConfig.colossusRoar && getMorph() == 4 &&
            (this.ticksExisted + this.getEntityId()) % 200 == 0) colossusRoar();
        if (this.ticksExisted % 100 == 0 && (genome & ParasiteGenome.REGENERATION) != 0 &&
            this.getHealth() > 0.0F && this.getHealth() < this.getMaxHealth()) {
            this.heal((float) Math.min(2.0D, this.getMaxHealth() * 0.015D));
        }
        if ((this.ticksExisted + this.getEntityId()) % 160 == 0 && getAttackTarget() == null) {
            shareColonyTarget();
        }
        if ((this.ticksExisted + this.getEntityId()) % 200 == 1) {
            updateAttributesFromEvolution();
        }
        if (this.ticksExisted % 10 == 0) {
            tryDigTowardVictim();
        }
    }

    private void updateAttributesFromEvolution() {
        double power = EvolutionMath.logOnePlus(EvolutionData.get(this.worldObj).getStage());
        int kind = getMorph();
        double health = (20.0D + 5.0D * power) *
            (gene == ParasiteGenes.BRUTE ? 1.35D : 1.0D) *
            (kind == 4 ? 4.0D : kind == 5 ? 2.6D : kind == 2 ? 1.7D : kind == 1 ? 0.65D : 1.0D) *
            ((genome & ParasiteGenome.PLATES) != 0 ? 1.22D : 1.0D);
        double attack = (3.0D + 1.5D * power) *
            (gene == ParasiteGenes.BRUTE ? 1.25D : 1.0D) *
            (kind == 4 ? 2.3D : kind == 5 ? 1.65D : kind == 2 ? 1.4D : 1.0D);
        double speed = Math.min(0.38D, 0.23D + 0.005D * power +
            (gene == ParasiteGenes.HUNTER ? 0.055D : 0.0D) +
            (kind == 1 ? 0.065D : kind == 2 ? -0.05D : kind == 4 ? -0.08D : kind == 5 ? -0.055D : 0.0D) +
            ((genome & ParasiteGenome.SWIFT) != 0 ? 0.035D : 0.0D));
        double oldMax = this.getMaxHealth();
        this.getEntityAttribute(SharedMonsterAttributes.maxHealth).setBaseValue(health);
        this.getEntityAttribute(SharedMonsterAttributes.attackDamage).setBaseValue(attack);
        this.getEntityAttribute(SharedMonsterAttributes.movementSpeed).setBaseValue(speed);
        if (this.getHealth() > 0.0F && health > oldMax) {
            this.heal((float) Math.min(health - oldMax, 100.0D));
        } else if (this.getHealth() > health) {
            this.setHealth((float) health);
        }
    }

    /** Distinct Scuttler combat role: a short, capped leap, no teleportation or block damage. */
    private void pounceAtPrey() {
        EntityLivingBase target = this.getAttackTarget();
        if (target == null || target.isDead || !this.onGround || !this.canEntityBeSeen(target)) return;
        double distance = this.getDistanceSqToEntity(target);
        if (distance < 9.0D || distance > 64.0D) return;
        double x = target.posX - this.posX;
        double z = target.posZ - this.posZ;
        double norm = Math.sqrt(x * x + z * z);
        if (norm < 0.01D) return;
        this.motionX += x / norm * 0.34D;
        this.motionY = 0.31D;
        this.motionZ += z / norm * 0.34D;
        this.velocityChanged = true;
    }

    /** Colossus shockwave is status-only, never destroys blocks or creates explosions. */
    @SuppressWarnings("unchecked")
    private void colossusRoar() {
        List<EntityLivingBase> victims = this.worldObj.getEntitiesWithinAABB(EntityLivingBase.class,
            this.boundingBox.expand(8.0D, 4.0D, 8.0D));
        boolean threatened = false;
        for (EntityLivingBase victim : victims) {
            if (victim != this && !(victim instanceof EntityParasite) &&
                !victim.isDead && this.getDistanceSqToEntity(victim) <= 64.0D) {
                victim.addPotionEffect(new PotionEffect(Potion.moveSlowdown.id, 65, 0));
                victim.addPotionEffect(new PotionEffect(Potion.weakness.id, 65, 0));
                threatened = true;
            }
        }
        if (threatened) this.worldObj.playSoundEffect(this.posX, this.posY, this.posZ,
            "mob.enderdragon.growl", 1.2F, 0.65F);
    }

    /** Matriarch: short-lived, capped buffs for a handful of nearby followers.
     *  Buffs do not bypass wards, damage caps or block protection.
     */
    @SuppressWarnings("unchecked")
    private void rallyNearbyBrood() {
        int r = BroodConfig.leaderRallyRadius;
        List<EntityParasite> nearby = this.worldObj.getEntitiesWithinAABB(EntityParasite.class,
            this.boundingBox.expand(r, 5.0D, r));
        EntityLivingBase sharedTarget = this.getAttackTarget();
        int influenced = 0;
        int healed = 0;
        for (EntityParasite member : nearby) {
            if (member == this || member.isDead ||
                this.getDistanceSqToEntity(member) > r * r) continue;
            member.addPotionEffect(new PotionEffect(Potion.moveSpeed.id,
                BroodConfig.leaderRallyTicks + 30, 0));
            // First six closest peers get an attack boost; never unlimited stacks.
            if (influenced++ < 6) member.addPotionEffect(new PotionEffect(
                Potion.damageBoost.id, BroodConfig.leaderRallyTicks + 30, 0));
            if (BroodConfig.queenHeals && healed < 4 &&
                member.getHealth() > 0.0F && member.getHealth() < member.getMaxHealth()) {
                member.heal(1.0F); // small, predictable local healing, not immortality
                healed++;
            }
            if (sharedTarget != null && !sharedTarget.isDead &&
                member.getAttackTarget() == null &&
                member.getDistanceSqToEntity(sharedTarget) <= 256.0D) {
                member.setAttackTarget(sharedTarget);
            }
        }
    }

    /** Nearby brood members coordinate without an expensive world-wide hive scan. */
    @SuppressWarnings("unchecked")
    private void shareColonyTarget() {
        if (EvolutionData.get(this.worldObj).getStage().compareTo(BigInteger.valueOf(12)) < 0) return;
        List<EntityParasite> peers = this.worldObj.getEntitiesWithinAABB(EntityParasite.class,
            this.boundingBox.expand(16.0D, 5.0D, 16.0D));
        for (EntityParasite peer : peers) {
            EntityLivingBase victim = peer.getAttackTarget();
            if (victim != null && !victim.isDead && victim.getHealth() > 0.0F &&
                this.getDistanceSqToEntity(victim) <= 576.0D) {
                this.setAttackTarget(victim);
                return;
            }
        }
    }

    @Override
    public boolean attackEntityAsMob(Entity victim) {
        boolean success = super.attackEntityAsMob(victim);
        if (success && !this.worldObj.isRemote &&
            (genome & ParasiteGenome.SPORES) != 0 && victim instanceof EntityLivingBase) {
            ((EntityLivingBase) victim).addPotionEffect(new PotionEffect(Potion.weakness.id, 70, 0));
        }
        return success;
    }

    @Override
    protected void dropFewItems(boolean recentlyHit, int lootingLevel) {
        super.dropFewItems(recentlyHit, lootingLevel);
        if (recentlyHit && com.endlessbrood.EndlessBrood.chitin != null) {
            int amount = (getMorph() == 4 ? 4 : getMorph() == 5 ? 3 : 1) + this.rand.nextInt(2 + Math.max(0, lootingLevel));
            for (int i = 0; i < amount; ++i) this.dropItem(com.endlessbrood.EndlessBrood.chitin, 1);
        }
    }

    private void clearDigging() {
        digging = false;
        digProgress = 0;
    }

    private void tryDigTowardVictim() {
        EntityLivingBase target = this.getAttackTarget();
        if (target == null || target.isDead || this.canEntityBeSeen(target) ||
            this.getDistanceSqToEntity(target) > 64.0D ||
            !BroodConfig.allowTerrainDamage ||
            !this.worldObj.getGameRules().getGameRuleBooleanValue("mobGriefing")) {
            clearDigging();
            return;
        }
        double dx = target.posX - this.posX;
        double dz = target.posZ - this.posZ;
        double horizontal = Math.sqrt(dx * dx + dz * dz);
        if (horizontal < 0.1D || Math.abs(target.posY - this.posY) > 3.0D) {
            clearDigging();
            return;
        }
        int x = MathHelper.floor_double(this.posX + dx / horizontal * 0.85D);
        int y = MathHelper.floor_double(this.boundingBox.minY + 0.2D);
        int z = MathHelper.floor_double(this.posZ + dz / horizontal * 0.85D);
        BigInteger stage = EvolutionData.get(this.worldObj).getStage();
        if (!BroodBlockRules.canBreak(this.worldObj, x, y, z, stage, gene)) {
            y += 1;
            if (!BroodBlockRules.canBreak(this.worldObj, x, y, z, stage, gene)) {
                clearDigging();
                return;
            }
        }
        if (!digging || digX != x || digY != y || digZ != z) {
            digX = x;
            digY = y;
            digZ = z;
            digProgress = 0;
            digging = true;
        }
        digProgress += 10;
        if (digProgress < BroodBlockRules.digDuration(this.worldObj, x, y, z)) return;
        // Revalidate right before actually removing a block; no expensive area scans.
        if (BroodBlockRules.canBreak(this.worldObj, x, y, z, stage, gene) &&
            !BroodWardRules.protects(this.worldObj, x, y, z)) {
            this.worldObj.func_147480_a(x, y, z, BroodConfig.dropDestroyedBlocks);
        }
        clearDigging();
    }

    @Override
    public void writeEntityToNBT(NBTTagCompound tag) {
        super.writeEntityToNBT(tag);
        tag.setInteger("EndlessBroodGene", gene);
        tag.setInteger("EndlessBroodGenome", genome);
        tag.setInteger("EndlessBroodMorph", getMorph());
        tag.setBoolean("EndlessBroodHasHome", hasBroodHome);
        if (hasBroodHome) {
            tag.setInteger("EndlessBroodHomeX", broodHomeX);
            tag.setInteger("EndlessBroodHomeY", broodHomeY);
            tag.setInteger("EndlessBroodHomeZ", broodHomeZ);
        }
    }

    @Override
    public void readEntityFromNBT(NBTTagCompound tag) {
        super.readEntityFromNBT(tag);
        hasBroodHome = tag.getBoolean("EndlessBroodHasHome");
        if (hasBroodHome) {
            broodHomeX = tag.getInteger("EndlessBroodHomeX");
            broodHomeY = tag.getInteger("EndlessBroodHomeY");
            broodHomeZ = tag.getInteger("EndlessBroodHomeZ");
        }
        if (tag.hasKey("EndlessBroodGene")) {
            gene = tag.getInteger("EndlessBroodGene");
            if (gene < ParasiteGenes.STANDARD || gene > ParasiteGenes.CORROSIVE) {
                gene = ParasiteGenes.STANDARD;
            }
            geneAssigned = true;
            this.dataWatcher.updateObject(21, Byte.valueOf((byte) gene));
        }
        if (tag.hasKey("EndlessBroodGenome")) {
            genome = tag.getInteger("EndlessBroodGenome") & ParasiteGenome.ALL;
            this.dataWatcher.updateObject(22, Byte.valueOf((byte) genome));
        }
        if (tag.hasKey("EndlessBroodMorph")) {
            int morph = tag.getInteger("EndlessBroodMorph");
            setMorph(morph >= 0 && morph <= BroodColonyRules.QUEEN_MORPH ? morph : 0);
        }
    }
}
