package com.endlessbrood.common;

import com.endlessbrood.EndlessBrood;
import cpw.mods.fml.common.IWorldGenerator;
import java.util.Random;
import net.minecraft.init.Blocks;
import net.minecraft.world.World;
import net.minecraft.world.chunk.IChunkProvider;

/** Rare, single-block surface nest; no large structures or cascading chunk loads. */
public final class BroodWorldGenerator implements IWorldGenerator {
    @Override
    public void generate(Random random, int chunkX, int chunkZ, World world,
                         IChunkProvider chunkGenerator, IChunkProvider chunkProvider) {
        if (world.isRemote || world.provider.dimensionId != 0 || !BroodConfig.nestsEnabled ||
            BroodConfig.nestChancePer10000Chunks <= 0 ||
            random.nextInt(10000) >= BroodConfig.nestChancePer10000Chunks) return;
        int x = chunkX * 16 + 4 + random.nextInt(8);
        int z = chunkZ * 16 + 4 + random.nextInt(8);
        BroodOutbreakData outbreak = BroodOutbreakData.get(world);
        if (!outbreak.maySeedAt(world, x, z)) return;
        int y = world.getHeightValue(x, z);
        if (y <= 55 || y >= 190 || !world.blockExists(x, y, z) ||
            !world.isAirBlock(x, y, z) || !world.isAirBlock(x, y + 1, z)) return;
        if (world.getBlock(x, y - 1, z) != Blocks.grass &&
            world.getBlock(x, y - 1, z) != Blocks.dirt) return;
        world.setBlock(x, y, z, EndlessBrood.nest, 0, 2);
    }
}
