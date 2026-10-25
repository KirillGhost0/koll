package com.endlessbrood.common;

import com.endlessbrood.EndlessBrood;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.gameevent.TickEvent;
import java.util.Random;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.util.MathHelper;
import net.minecraft.world.World;

/**
 * Rare, limited attempts to seed old, ALREADY LOADED distant terrain.
 * No extra chunk loading, no scan across all saved chunks, no forced tickets.
 */
public final class BroodOutbreakTicker {
    @SubscribeEvent
    public void worldTick(TickEvent.WorldTickEvent event) {
        if (event.phase != TickEvent.Phase.END || event.world.isRemote ||
            event.world.provider.dimensionId != 0) return;
        World world = event.world;
        BroodOutbreakData data = BroodOutbreakData.get(world);
        data.rememberPlayers(world);
        if (!BroodConfig.nestsEnabled || !BroodConfig.retroactiveDistantNests ||
            !data.graceExpired(world.getTotalWorldTime()) ||
            data.retroSeedCount() >= BroodConfig.retroSeedLimit ||
            world.getTotalWorldTime() % BroodConfig.retroSeedIntervalTicks != 0) return;
        // One potential nest per interval; only loaded land near a player exploring FAR from bases.
        for (Object entry : world.playerEntities) {
            if (!(entry instanceof EntityPlayer)) continue;
            EntityPlayer player = (EntityPlayer) entry;
            if (player.dimension != 0) continue;
            int px = MathHelper.floor_double(player.posX);
            int pz = MathHelper.floor_double(player.posZ);
            Random random = world.rand;
            for (int attempt = 0; attempt < 16; ++attempt) {
                int x = px + random.nextInt(289) - 144;
                int z = pz + random.nextInt(289) - 144;
                // Data checks both initial bases and a buffer around *all* current players.
                if (!data.maySeedAt(world, x, z) || !data.farFromExistingRetroSeeds(x, z) ||
                    !world.blockExists(x, 64, z)) continue;
                int y = world.getHeightValue(x, z);
                if (y <= 55 || y >= 190 || !world.blockExists(x, y + 1, z) ||
                    !world.isAirBlock(x, y, z) || !world.isAirBlock(x, y + 1, z)) continue;
                if (world.getBlock(x, y - 1, z) != Blocks.grass &&
                    world.getBlock(x, y - 1, z) != Blocks.dirt) continue;
                if (BroodWardRules.protects(world, x, y, z)) continue;
                if (world.setBlock(x, y, z, EndlessBrood.nest, 0, 3)) {
                    data.recordRetroSeed(x, z);
                    return;
                }
            }
        }
    }
}
