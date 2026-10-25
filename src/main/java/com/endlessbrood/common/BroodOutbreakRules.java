package com.endlessbrood.common;

/** Pure geometry and timing used by the distant-outbreak system. */
public final class BroodOutbreakRules {
    private BroodOutbreakRules() { }

    public static boolean graceExpired(long now, long installationTick, int days) {
        if (installationTick < 0L || now < installationTick) return false;
        return now - installationTick >= (long) Math.max(0, days) * 24000L;
    }

    public static boolean shouldGrow(long now, long installationTick, int days, int interval) {
        return interval > 0 && graceExpired(now, installationTick, days)
            && (now - installationTick) % interval == 0L;
    }

    public static boolean outsideRadius(double x, double z, double ax, double az, int radius) {
        if (Double.isNaN(x) || Double.isNaN(z) || Double.isInfinite(x) || Double.isInfinite(z)) return false;
        double dx = x - ax;
        double dz = z - az;
        return dx * dx + dz * dz >= (double) radius * radius;
    }
}
