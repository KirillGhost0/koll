package com.endlessbrood.common;

import java.math.BigInteger;
import java.util.Locale;

/** No HBM classes are linked: damage categories are detected generically. */
public final class BroodAdaptiveMath {
    public static final int FIRE = 0;
    public static final int EXPLOSIVE = 1;
    public static final int RANGED = 2;
    public static final int RADIATION = 3;
    public static final int CATEGORY_COUNT = 4;

    private BroodAdaptiveMath() { }

    public static int category(String type, boolean fire, boolean explosion, boolean projectile) {
        String id = type == null ? "" : type.toLowerCase(Locale.ROOT);
        if (id.contains("radiat") || id.contains("nuclear") || id.contains("rad_poison")) return RADIATION;
        if (fire || id.contains("flame") || id.contains("burn")) return FIRE;
        if (explosion || id.contains("explos") || id.contains("blast")) return EXPLOSIVE;
        if (projectile || id.contains("bullet") || id.contains("laser") || id.contains("arrow") ||
            id.contains("projectile")) return RANGED;
        return -1;
    }

    /** Repeated damage gradually builds resistance, with a hard ceiling. */
    public static double resistance(long experience, BigInteger stage, double cap) {
        if (stage == null || stage.compareTo(BigInteger.valueOf(5)) < 0 || experience < 250L) return 0.0D;
        int tiers = 1 + (int) Math.floor(Math.log(1.0D + experience / 250.0D) / Math.log(2.0D));
        return Math.min(Math.min(0.75D, Math.max(0.0D, cap)), tiers * 0.065D);
    }

    public static long saturatingAdd(long current, long amount) {
        if (amount <= 0) return Math.max(0, current);
        if (current < 0 || current > Long.MAX_VALUE - amount) return Long.MAX_VALUE;
        return current + amount;
    }
}
