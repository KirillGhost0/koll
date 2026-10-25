package com.endlessbrood.common;

import java.math.BigInteger;

/** Works even when an evolution stage cannot fit in long or double. */
public final class EvolutionMath {
    private EvolutionMath() { }

    public static double logOnePlus(BigInteger value) {
        if (value.signum() <= 0) return 0.0D;
        BigInteger shiftedValue = value.add(BigInteger.ONE);
        int shift = Math.max(0, shiftedValue.bitLength() - 52);
        return Math.log(shiftedValue.shiftRight(shift).doubleValue())
            + shift * Math.log(2.0D);
    }
}
