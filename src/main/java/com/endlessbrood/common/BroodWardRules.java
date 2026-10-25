package com.endlessbrood.common;

import com.endlessbrood.EndlessBrood;
import net.minecraft.world.World;

/** Bounded guard check. Unloaded chunks are skipped, never accessed. */
public final class BroodWardRules {
    private BroodWardRules() { }

    public static boolean protects(World world, int x, int y, int z) {
        if (!BroodConfig.wardsEnabled) return false;
        int radius = BroodConfig.wardRadius;
        // Nest spawning is rare: scan only when a nest is already ready to spawn.
        // Sample only 1 block vertically above/below the intended nest.
        for (int dx = -radius; dx <= radius; ++dx) {
            for (int dz = -radius; dz <= radius; ++dz) {
                if (dx * dx + dz * dz > radius * radius) continue;
                for (int dy = -3; dy <= 3; ++dy) {
                    int px = x + dx, py = y + dy, pz = z + dz;
                    if (py > 0 && py < 255 && world.blockExists(px, py, pz) &&
                        world.getBlock(px, py, pz) == EndlessBrood.ward) return true;
                }
            }
        }
        return false;
    }
}
