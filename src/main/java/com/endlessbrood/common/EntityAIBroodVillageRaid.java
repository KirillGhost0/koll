package com.endlessbrood.common;

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.ai.EntityAIBase;
import net.minecraft.util.ChunkCoordinates;
import net.minecraft.util.MathHelper;
import net.minecraft.village.Village;

/** Night raids use vanilla village data, with cooldown and no forced chunk loading.
 *  Navigation merely approaches the village: vanilla target AI chooses actual prey.
 */
public final class EntityAIBroodVillageRaid extends EntityAIBase {
    private final EntityParasite parasite;
    private int cooldown;
    private int travelTicks;
    private int destinationX;
    private int destinationY;
    private int destinationZ;

    public EntityAIBroodVillageRaid(EntityParasite parasite) {
        this.parasite = parasite;
        this.cooldown = parasite.getRNG().nextInt(400) + 120;
        setMutexBits(1); // do not fight wandering / melee movement tasks
    }

    @Override
    public boolean shouldExecute() {
        if (--cooldown > 0) return false;
        cooldown = BroodConfig.raidSearchInterval + parasite.getRNG().nextInt(200);
        if (parasite.worldObj.isRemote || parasite.worldObj.provider.dimensionId != 0 ||
            !BroodRaidRules.eligible(BroodConfig.villageRaids, !parasite.worldObj.isDaytime(),
                EvolutionData.get(parasite.worldObj).getStage(), BroodConfig.raidUnlockStage) ||
            parasite.getAttackTarget() != null || parasite.worldObj.villageCollectionObj == null) return false;
        int x = MathHelper.floor_double(parasite.posX);
        int y = MathHelper.floor_double(parasite.posY);
        int z = MathHelper.floor_double(parasite.posZ);
        Village village = parasite.worldObj.villageCollectionObj.findNearestVillage(
            x, y, z, BroodConfig.raidSearchRadius);
        if (village == null || village.isAnnihilated() || village.getNumVillagers() < 1) return false;
        ChunkCoordinates center = village.getCenter();
        double dx = center.posX - parasite.posX;
        double dz = center.posZ - parasite.posZ;
        if (!BroodRaidRules.withinRange(dx * dx + dz * dz, BroodConfig.raidSearchRadius)) return false;
        if (!parasite.worldObj.blockExists(center.posX, center.posY, center.posZ)) return false;
        destinationX = center.posX;
        destinationY = center.posY;
        destinationZ = center.posZ;
        return true;
    }

    @Override
    public void startExecuting() {
        travelTicks = 220;
        parasite.getNavigator().tryMoveToXYZ(destinationX + 0.5D, destinationY,
            destinationZ + 0.5D, 0.92D);
    }

    @Override
    public boolean continueExecuting() {
        EntityLivingBase target = parasite.getAttackTarget();
        return travelTicks-- > 0 && target == null && !parasite.worldObj.isDaytime() &&
            !parasite.getNavigator().noPath();
    }

    @Override
    public void resetTask() {
        parasite.getNavigator().clearPathEntity();
    }
}
