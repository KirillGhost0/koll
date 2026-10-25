package com.endlessbrood.common;

import java.math.BigInteger;

/** Pure, predictable gates for late-game village raids. */
public final class BroodRaidRules {
    private BroodRaidRules() { }

    public static boolean eligible(boolean enabled, boolean isNight,
                                   BigInteger stage, int unlockStage) {
        return enabled && isNight && stage != null &&
            stage.compareTo(BigInteger.valueOf(unlockStage)) >= 0;
    }

    /** Never seek villages more than the bounded radius away. */
    public static boolean withinRange(double distanceSquared, int maxDistance) {
        return maxDistance > 0 && distanceSquared >= 36.0D &&
            distanceSquared <= (double) maxDistance * maxDistance;
    }
}
