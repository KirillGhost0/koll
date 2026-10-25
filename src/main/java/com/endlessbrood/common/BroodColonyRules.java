package com.endlessbrood.common;

import java.math.BigInteger;
import java.util.Random;

/** Pure decisions for nest tiers and limited mother/leader population. */
public final class BroodColonyRules {
    public static final int SEED = 0;
    public static final int MATURE = 1;
    public static final int CITADEL = 2;
    public static final int QUEEN_MORPH = 5;

    private BroodColonyRules() { }

    /** Returning false does not downgrade an already-established nest. */
    public static boolean canUpgrade(int currentTier, BigInteger stage,
                                     int matureUnlock, int citadelUnlock) {
        if (currentTier == SEED) return stage.compareTo(BigInteger.valueOf(matureUnlock)) >= 0;
        if (currentTier == MATURE) return stage.compareTo(BigInteger.valueOf(citadelUnlock)) >= 0;
        return false;
    }

    /** One attempt per nest pulse; defaults make ascension measured in hours. */
    public static int upgradedTier(int currentTier, BigInteger stage,
                                   int matureUnlock, int citadelUnlock,
                                   int chanceDenominator, Random random) {
        if (canUpgrade(currentTier, stage, matureUnlock, citadelUnlock)
                && random.nextInt(Math.max(1, chanceDenominator)) == 0) {
            return currentTier + 1;
        }
        return currentTier;
    }

    public static boolean maySpawnQueen(int tier, BigInteger stage, int queenUnlock) {
        return tier >= CITADEL && stage.compareTo(BigInteger.valueOf(queenUnlock)) >= 0;
    }

    public static boolean mayEstablishDaughter(int tier, BigInteger stage) {
        return tier >= CITADEL && stage.compareTo(BigInteger.valueOf(40)) >= 0;
    }

    /** Weighted roles limited to existing six model variants. Caller gates queen population. */
    public static int chooseMorph(int tier, BigInteger stage, Random random) {
        int roll = random.nextInt(100);
        if (tier <= SEED || stage.compareTo(BigInteger.valueOf(12)) < 0) {
            return roll < 55 ? 0 : 1;
        }
        if (tier == MATURE) {
            return roll < 35 ? 0 : roll < 60 ? 1 : roll < 85 ? 2 : 3;
        }
        return roll < 15 ? 0 : roll < 30 ? 1 : roll < 56 ? 2 : roll < 89 ? 3 : 4;
    }
}
