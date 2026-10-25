package com.endlessbrood.common;

import java.math.BigInteger;
import java.util.Random;

/** Simple hereditary-style phenotypes. A gene is rolled at spawn and persisted in NBT. */
public final class ParasiteGenes {
    public static final int STANDARD = 0;
    public static final int HUNTER = 1;
    public static final int BRUTE = 2;
    public static final int CORROSIVE = 3;

    private ParasiteGenes() { }

    public static int roll(BigInteger worldStage, Random random) {
        if (worldStage.compareTo(BigInteger.valueOf(6)) < 0) return STANDARD;
        // More stages unlock more frequent mutations, but don't spawn excessive entities.
        int mutationChance = Math.min(70, 18 + (int) Math.min(52,
            Math.floor(EvolutionMath.logOnePlus(worldStage) * 8.0D)));
        if (random.nextInt(100) >= mutationChance) return STANDARD;
        if (worldStage.compareTo(BigInteger.valueOf(30)) >= 0 && random.nextInt(4) == 0) {
            return CORROSIVE;
        }
        if (worldStage.compareTo(BigInteger.valueOf(15)) >= 0 && random.nextBoolean()) {
            return BRUTE;
        }
        return HUNTER;
    }
}
