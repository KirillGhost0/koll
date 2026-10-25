package com.endlessbrood.common;

import com.endlessbrood.EndlessBrood;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import java.math.BigInteger;
import java.util.List;
import java.util.Random;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.init.Blocks;
import net.minecraft.item.Item;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.IIcon;
import net.minecraft.world.World;

/** Three colony types in ONE saved block ID: metadata 0=seed, 1=mature, 2=citadel. */
public final class BlockBroodNest extends Block {
    @SideOnly(Side.CLIENT)
    private IIcon[] tierTextures;

    public BlockBroodNest() {
        super(Material.rock);
        setBlockName("endlessbrood.nest");
        setBlockTextureName("endlessbrood:nest");
        setHardness(7.0F);
        setResistance(20.0F);
        setHarvestLevel("pickaxe", 2);
        setCreativeTab(CreativeTabs.tabBlock);
        setTickRandomly(false);
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void registerBlockIcons(IIconRegister register) {
        tierTextures = new IIcon[] {
            register.registerIcon("endlessbrood:nest"),
            register.registerIcon("endlessbrood:nest_mature"),
            register.registerIcon("endlessbrood:nest_citadel")
        };
        blockIcon = tierTextures[0];
    }

    @Override
    @SideOnly(Side.CLIENT)
    public IIcon getIcon(int side, int meta) {
        return tierTextures[Math.max(0, Math.min(BroodColonyRules.CITADEL, meta))];
    }

    @Override
    public int tickRate(World world) {
        return BroodConfig.nestPulseTicks;
    }

    @Override
    public void onBlockAdded(World world, int x, int y, int z) {
        if (!world.isRemote) world.scheduleBlockUpdate(x, y, z, this, tickRate(world));
    }

    @Override
    @SuppressWarnings("unchecked")
    public void updateTick(World world, int x, int y, int z, Random random) {
        if (world.isRemote || !BroodConfig.nestsEnabled || world.getBlock(x, y, z) != this) return;
        world.scheduleBlockUpdate(x, y, z, this, tickRate(world));
        // Existing-world grace also pauses colony upgrading and spreading.
        if (!BroodOutbreakData.get(world).graceExpired(world.getTotalWorldTime())) return;
        BigInteger stage = EvolutionData.get(world).getStage();
        int tier = Math.max(0, Math.min(BroodColonyRules.CITADEL, world.getBlockMetadata(x, y, z)));
        // No mutation of blocks in a player's ward radius.
        if (BroodWardRules.protects(world, x, y, z)) return;
        if (BroodConfig.colonyTiers) {
            int advanced = BroodColonyRules.upgradedTier(tier, stage,
                BroodConfig.matureNestStage, BroodConfig.citadelNestStage,
                BroodConfig.nestUpgradeChance, random);
            if (advanced != tier) {
                tier = advanced;
                world.setBlockMetadataWithNotify(x, y, z, tier, 3);
            }
        }

        if (BroodConfig.nestExpansion &&
            BroodColonyRules.mayEstablishDaughter(tier, stage) &&
            random.nextInt(BroodConfig.nestExpansionChance) == 0 &&
            world.getGameRules().getGameRuleBooleanValue("mobGriefing")) {
            tryEstablishDaughter(world, x, y, z, random);
        }
        if (BroodConfig.taintedSoilSpread &&
            world.getGameRules().getGameRuleBooleanValue("mobGriefing") &&
            stage.compareTo(BigInteger.valueOf(2)) >= 0) {
            trySpreadSoil(world, x, y, z, random);
        }
        if (stage.compareTo(BigInteger.valueOf(BroodConfig.nestSpawnUnlockStage)) < 0 ||
            !world.getGameRules().getGameRuleBooleanValue("doMobSpawning")) return;
        AxisAlignedBB near = AxisAlignedBB.getBoundingBox(x - 20, y - 10, z - 20,
            x + 21, y + 11, z + 21);
        List<EntityParasite> population = world.getEntitiesWithinAABB(EntityParasite.class, near);
        if (population.size() >= BroodConfig.nestPopulationLimit) return;

        // Reuse local list for the ordinary spawn; only query a broader radius for leaders.
        int morph = BroodColonyRules.chooseMorph(tier, stage, random);
        if (BroodColonyRules.maySpawnQueen(tier, stage, BroodConfig.queenUnlockStage) &&
            random.nextInt(BroodConfig.queenSpawnChance) == 0 &&
            !leaderWithinRange(world, x, y, z)) {
            morph = BroodColonyRules.QUEEN_MORPH;
        }
        int sx = x + random.nextInt(7) - 3;
        int sz = z + random.nextInt(7) - 3;
        int sy = y + random.nextInt(3);
        if (!world.blockExists(sx, sy, sz) || !world.blockExists(sx, sy - 1, sz) ||
            !world.isAirBlock(sx, sy, sz) || !world.isAirBlock(sx, sy + 1, sz) ||
            ((morph == 4 || morph == BroodColonyRules.QUEEN_MORPH) &&
                !world.isAirBlock(sx, sy + 2, sz)) ||
            !world.getBlock(sx, sy - 1, sz).getMaterial().isSolid() ||
            BroodWardRules.protects(world, sx, sy, sz)) return;
        EntityParasite parasite = new EntityParasite(world);
        parasite.setLocationAndAngles(sx + .5D, sy, sz + .5D, random.nextFloat() * 360F, 0F);
        parasite.configureSpawnMorph(morph);
        parasite.setBroodHome(x, y, z);
        if (world.checkNoEntityCollision(parasite.boundingBox) &&
            world.getCollidingBoundingBoxes(parasite, parasite.boundingBox).isEmpty()) {
            world.spawnEntityInWorld(parasite);
        }
    }

    private void trySpreadSoil(World world, int x, int y, int z, Random random) {
        // Fixed 3 attempts per pulse, regardless of world progression or nest tier.
        for (int attempt = 0; attempt < 3; ++attempt) {
            int px = x + random.nextInt(9) - 4;
            int pz = z + random.nextInt(9) - 4;
            int py = y + random.nextInt(3) - 1;
            if (py <= 0 || py >= 254 || !world.blockExists(px, py, pz) ||
                !world.blockExists(px, py + 1, pz) ||
                BroodWardRules.protects(world, px, py, pz)) continue;
            Block ground = world.getBlock(px, py, pz);
            if ((ground == Blocks.dirt || ground == Blocks.grass) &&
                world.isAirBlock(px, py + 1, pz)) {
                world.setBlock(px, py, pz, EndlessBrood.taintedSoil, 0, 3);
                break;
            }
        }
    }

    @SuppressWarnings("unchecked")
    private boolean leaderWithinRange(World world, int x, int y, int z) {
        List<EntityParasite> nearby = world.getEntitiesWithinAABB(EntityParasite.class,
            AxisAlignedBB.getBoundingBox(x - 30, y - 14, z - 30,
                x + 31, y + 15, z + 31));
        for (EntityParasite candidate : nearby) {
            if (!candidate.isDead && candidate.getMorph() == BroodColonyRules.QUEEN_MORPH) {
                return true;
            }
        }
        return false;
    }

    private void tryEstablishDaughter(World world, int x, int y, int z, Random random) {
        int dx = x + (random.nextBoolean() ? 1 : -1) * (12 + random.nextInt(6));
        int dz = z + (random.nextBoolean() ? 1 : -1) * (12 + random.nextInt(6));
        // This is a rare action (~ once every four loaded-world hours per citadel).
        // Abort instantly if one sampled position touches an unloaded chunk.
        for (int xx = dx - 10; xx <= dx + 10; xx += 2) {
            for (int zz = dz - 10; zz <= dz + 10; zz += 2) {
                for (int yy = y - 2; yy <= y + 2; ++yy) {
                    if (yy <= 0 || yy >= 254 || !world.blockExists(xx, yy, zz)) return;
                    if (world.getBlock(xx, yy, zz) == this) return;
                }
            }
        }
        for (int yy = y - 2; yy <= y + 2; ++yy) {
            if (yy <= 0 || yy >= 253 || !world.blockExists(dx, yy, dz) ||
                !world.blockExists(dx, yy + 2, dz)) return;
            if ((world.getBlock(dx, yy, dz) == Blocks.grass ||
                 world.getBlock(dx, yy, dz) == Blocks.dirt) &&
                world.isAirBlock(dx, yy + 1, dz) && world.isAirBlock(dx, yy + 2, dz) &&
                !BroodWardRules.protects(world, dx, yy + 1, dz)) {
                world.setBlock(dx, yy + 1, dz, this, BroodColonyRules.SEED, 3);
                return;
            }
        }
    }

    /** Guards react only to a real, nearby player disturbing the nest.
     *  This does not alter blocks, force chunks or create extra parasites.
     */
    @Override
    public void onBlockClicked(World world, int x, int y, int z, EntityPlayer player) {
        alertGuards(world, x, y, z, player);
    }

    @Override
    public void onBlockHarvested(World world, int x, int y, int z, int metadata,
                                 EntityPlayer player) {
        alertGuards(world, x, y, z, player);
        super.onBlockHarvested(world, x, y, z, metadata, player);
    }

    @SuppressWarnings("unchecked")
    private void alertGuards(World world, int x, int y, int z, EntityPlayer player) {
        if (world.isRemote || !BroodConfig.nestDefense || player == null ||
            player.capabilities.isCreativeMode || player.isDead ||
            !BroodConfig.nestsEnabled) return;
        int radius = BroodConfig.nestGuardRadius;
        if (player.getDistanceSq(x + 0.5D, y + 0.5D, z + 0.5D) >
            (double) radius * radius) return;
        List<EntityParasite> nearby = world.getEntitiesWithinAABB(EntityParasite.class,
            AxisAlignedBB.getBoundingBox(x - radius, y - 7, z - radius,
                x + radius + 1, y + 8, z + radius + 1));
        int assigned = 0;
        for (EntityParasite guard : nearby) {
            if (!guard.isDead && guard.getAttackTarget() == null &&
                guard.getDistanceSqToEntity(player) <= (double) radius * radius) {
                guard.setAttackTarget(player);
                if (++assigned >= BroodConfig.nestGuardLimit) break;
            }
        }
    }

    @Override
    public Item getItemDropped(int meta, Random random, int fortune) {
        return EndlessBrood.chitin;
    }

    @Override
    public int quantityDropped(Random random) {
        return 2 + random.nextInt(4);
    }
}
