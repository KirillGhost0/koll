package com.endlessbrood.common;

/** Pure math for low-cost, home-nest colony guard behavior. */
public final class BroodTerritoryRules {
    private BroodTerritoryRules() { }

    public static boolean shouldReturn(double x, double z, int homeX, int homeZ, int radius) {
        if (radius <= 0 || !Double.isFinite(x) || !Double.isFinite(z)) return false;
        double dx = x - (homeX + 0.5D);
        double dz = z - (homeZ + 0.5D);
        return dx * dx + dz * dz > (double) radius * radius;
    }
}
