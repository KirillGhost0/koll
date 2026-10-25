package com.endlessbrood.common;

/**
 * Prevents industrial mob grinders and repeated kill events from advancing the
 * entire server's evolution in seconds. The budget is shared by all players.
 * It renews at 24,000 ticks of total loaded-world time, NOT /time set.
 * Pure Java: unit-testable without Minecraft or Forge.
 */
public final class BroodDailyBudget {
    private long day = -1L;
    private int used;

    public int consume(int requested, long totalWorldTicks, int dailyLimit) {
        if (requested <= 0 || dailyLimit <= 0) return 0;
        long today = Math.max(0L, totalWorldTicks) / 24000L;
        if (day != today) {
            day = today;
            used = 0;
        }
        int granted = Math.min(requested, Math.max(0, dailyLimit - used));
        used += granted;
        return granted;
    }

    public int getUsed(long totalWorldTicks) {
        return day == Math.max(0L, totalWorldTicks) / 24000L ? used : 0;
    }

    public long getDay() { return day; }
    public int getPersistedUsed() { return used; }

    public void restore(long persistedDay, int persistedUsed) {
        day = Math.max(-1L, persistedDay);
        used = Math.max(0, persistedUsed);
    }
}
