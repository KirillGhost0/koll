package com.endlessbrood.common;

/**
 * Pure server progression gates shared by spawning, nests and passive growth.
 * Game time uses total world ticks, never a mutable day/night clock.
 */
public final class BroodPacingRules {
    public static final long TICKS_PER_DAY = 24000L;

    private BroodPacingRules() { }

    public static boolean graceExpired(long totalWorldTicks, int graceDays) {
        long required = Math.max(0, graceDays) * TICKS_PER_DAY;
        return totalWorldTicks >= required;
    }

    public static boolean shouldAdvance(long totalWorldTicks, int graceDays, int growthInterval) {
        return growthInterval > 0 && graceExpired(totalWorldTicks, graceDays)
            && totalWorldTicks % growthInterval == 0;
    }

    /** Avoids overflow even if a future config increases the grace-day range. */
    public static boolean maySpawn(boolean wardProtects, boolean wardsBlockSpawning,
                                    long totalWorldTicks, int graceDays) {
        return graceExpired(totalWorldTicks, graceDays)
            && (!wardsBlockSpawning || !wardProtects);
    }
}
