package model;

import java.util.Objects;
import java.util.Random;

public class Dice {
    private final Random random;
    private int lastRoll;

    public Dice() {
        this(new Random());
    }

    // Constructor injection allows injecting a mocked Random object for 100% deterministic JUnit tests
    public Dice(Random random) {
        this.random = Objects.requireNonNull(random, "Random generator cannot be null");
        this.lastRoll = 0;
    }

    public int roll() {
        this.lastRoll = random.nextInt(6) + 1;
        return this.lastRoll;
    }

    public int getLastRoll() {
        return lastRoll;
    }
}