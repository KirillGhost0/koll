package com.endlessbrood.common;

import java.math.BigInteger;
import java.util.Random;

/** Stackable traits; combined phenotypes diversify without increasing mob count. */
public final class ParasiteGenome {
    public static final int PLATES = 1;
    public static final int SWIFT = 2;
    public static final int SPORES = 4;
    public static final int REGENERATION = 8;
    public static final int ALL = PLATES | SWIFT | SPORES | REGENERATION;

    private ParasiteGenome() { }

    public static int roll(BigInteger stage, Random random) {
        int traits = 0;
        double chance = Math.min(0.42D, 0.06D + EvolutionMath.logOnePlus(stage) * 0.026D);
        if (stage.compareTo(BigInteger.valueOf(12)) >= 0 && random.nextDouble() < chance) traits |= PLATES;
        if (stage.compareTo(BigInteger.valueOf(30)) >= 0 && random.nextDouble() < chance) traits |= SWIFT;
        if (stage.compareTo(BigInteger.valueOf(75)) >= 0 && random.nextDouble() < chance) traits |= SPORES;
        if (stage.compareTo(BigInteger.valueOf(150)) >= 0 && random.nextDouble() < chance) traits |= REGENERATION;
        return traits;
    }
}
