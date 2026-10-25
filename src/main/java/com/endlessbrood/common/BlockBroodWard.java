package com.endlessbrood.common;

import com.endlessbrood.EndlessBrood;
import java.util.List;
import java.util.Random;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.init.Blocks;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.world.World;

/** Slow, affordable early-game defensive tool that never loads nearby chunks. */
public final class BlockBroodWard extends Block {
    public BlockBroodWard() {
        super(Material.iron);
        setBlockName("endlessbrood.ward");
        setBlockTextureName("endlessbrood:ward");
        setHardness(3.0F);
        setResistance(12.0F);
        setHarvestLevel("pickaxe", 1);
        setCreativeTab(CreativeTabs.tabBlock);
        setLightLevel(0.35F);
    }

    @Override
    public void onBlockAdded(World world, int x, int y, int z) {
        if (!world.isRemote) world.scheduleBlockUpdate(x, y, z, this, tickRate(world));
    }

    @Override
    public int tickRate(World world) {
        return BroodConfig.wardPulseTicks;
    }

    @Override
    @SuppressWarnings("unchecked")
    public void updateTick(World world, int x, int y, int z, Random random) {
        if (world.isRemote || world.getBlock(x, y, z) != this) return;
        world.scheduleBlockUpdate(x, y, z, this, tickRate(world));
        if (!BroodConfig.wardsEnabled) return;
        int radius = BroodConfig.wardRadius;
        AxisAlignedBB area = AxisAlignedBB.getBoundingBox(x - radius, y - 4, z - radius,
            x + radius + 1, y + 5, z + radius + 1);
        List<EntityParasite> parasites = world.getEntitiesWithinAABB(EntityParasite.class, area);
        for (EntityParasite parasite : parasites) {
            if (!parasite.isDead) {
                // Slows enemies, but does not damage them or invalidate uncapped evolution.
                parasite.addPotionEffect(new PotionEffect(Potion.moveSlowdown.id,
                    Math.max(60, tickRate(world) + 20), 1));
            }
        }
        // Reclamation is deliberately small and local (no huge chunk scans).
        for (int i = 0; i < 5; ++i) {
            int nx = x + random.nextInt(radius * 2 + 1) - radius;
            int nz = z + random.nextInt(radius * 2 + 1) - radius;
            int ny = y + random.nextInt(5) - 2;
            if (ny > 0 && ny < 255 && world.blockExists(nx, ny, nz) &&
                world.getBlock(nx, ny, nz) == EndlessBrood.taintedSoil) {
                world.setBlock(nx, ny, nz, Blocks.dirt, 0, 3);
            }
        }
    }
}
