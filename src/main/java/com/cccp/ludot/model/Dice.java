package com.cccp.ludot.model;

import java.util.Objects;
import java.util.Random;

public class Dice {
    private final Random random;
    private int lastRoll;

    public Dice() {
        this(new Random());
    }

    public Dice(Random random) {
        this.random = Objects.requireNonNull(random, "Random generator cannot be null");
        this.lastRoll = 1;
    }

    /**
     * Rolls the die and returns a value between 1 and 6.
     * @return integer between 1 and 6
     */
    public int roll() {
        this.lastRoll = random.nextInt(6) + 1;
        return this.lastRoll;
    }

    public int getLastRoll() {
        return lastRoll;
    }
}