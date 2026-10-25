package com.endlessbrood.common;

import com.endlessbrood.EndlessBrood;
import java.util.Random;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionEffect;
import net.minecraft.world.World;

/** Saturation metadata 0..3 bounds propagation from a single nest seed. */
public final class BlockTaintedSoil extends Block {
    public BlockTaintedSoil() {
        super(Material.ground);
        setBlockName("endlessbrood.taintedSoil");
        setBlockTextureName("endlessbrood:tainted_soil");
        setHardness(0.65F);
        setStepSound(soundTypeGravel);
        setCreativeTab(CreativeTabs.tabBlock);
        setTickRandomly(true);
    }

    @Override
    public void updateTick(World world, int x, int y, int z, Random random) {
        if (world.isRemote || !BroodConfig.taintedSoilSpread ||
            !world.getGameRules().getGameRuleBooleanValue("mobGriefing") ||
            random.nextInt(7) != 0) return;
        int age = world.getBlockMetadata(x, y, z);
        if (age >= 3) return;
        int nx = x + random.nextInt(3) - 1;
        int nz = z + random.nextInt(3) - 1;
        if (nx == x && nz == z || !world.blockExists(nx, y, nz)) return;
        if (BroodWardRules.protects(world, nx, y, nz)) return;
        if ((world.getBlock(nx, y, nz) == Blocks.grass || world.getBlock(nx, y, nz) == Blocks.dirt) &&
            world.isAirBlock(nx, y + 1, nz)) {
            world.setBlock(nx, y, nz, EndlessBrood.taintedSoil, age + 1, 3);
        }
    }

    @Override
    public void onEntityWalking(World world, int x, int y, int z, Entity entity) {
        if (!world.isRemote && entity instanceof EntityPlayer && world.rand.nextInt(100) == 0) {
            ((EntityPlayer) entity).addPotionEffect(new PotionEffect(Potion.moveSlowdown.id, 50, 0));
        }
    }
}
