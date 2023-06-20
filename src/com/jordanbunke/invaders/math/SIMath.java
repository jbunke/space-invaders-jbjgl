package com.jordanbunke.invaders.math;

import java.util.Random;

public final class SIMath {
    private static final Random r = new Random();

    public static boolean flipCoin() {
        return prob(0.5);
    }

    public static boolean prob(final double p) {
        return r.nextDouble() < p;
    }

    public static int randomInRange(final int min, final int max) {
        return min + (int)((max - min) * r.nextDouble());
    }
}
