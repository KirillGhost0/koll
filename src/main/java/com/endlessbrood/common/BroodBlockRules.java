package com.endlessbrood.common;

import java.math.BigInteger;
import net.minecraft.block.Block;
import net.minecraft.init.Blocks;
import net.minecraft.world.World;

/** Server-only block breaking. NTM and all unlisted modded blocks are protected. */
public final class BroodBlockRules {
    private BroodBlockRules() { }

    public static boolean canBreak(World world, int x, int y, int z,
                                   BigInteger stage, int gene) {
        if (world.isRemote || !BroodConfig.allowTerrainDamage ||
            !world.getGameRules().getGameRuleBooleanValue("mobGriefing") ||
            y < 1 || y > 254 || !world.blockExists(x, y, z)) return false;
        Block block = world.getBlock(x, y, z);
        if (block == null || block == Blocks.air || block == Blocks.obsidian ||
            block == Blocks.bedrock || block == Blocks.end_portal_frame) return false;
        if (block.getBlockHardness(world, x, y, z) < 0.0F ||
            block.hasTileEntity(world.getBlockMetadata(x, y, z)) ||
            world.getTileEntity(x, y, z) != null) return false;
        Object registryName = Block.blockRegistry.getNameForObject(block);
        return BroodBlockPolicy.permits(registryName == null ? null : registryName.toString(),
            stage, gene, BroodConfig.protectedBlocks,
            BroodConfig.softDigStage, BroodConfig.woodenDigStage, BroodConfig.stoneDigStage);
    }

    public static int digDuration(World world, int x, int y, int z) {
        Object name = Block.blockRegistry.getNameForObject(world.getBlock(x, y, z));
        return name != null && BroodBlockPolicy.isStone(name.toString())
            ? BroodConfig.blockDigTicks * 2 : BroodConfig.blockDigTicks;
    }
}
