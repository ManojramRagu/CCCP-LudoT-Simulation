package model;

import java.util.Objects;
import java.util.Random;

/**
 * Represents a standard 6-sided die with Rule T-6 tracking.
 */
public class Dice {
    private final Random random;
    private int lastRoll;
    private int consecutiveSixesCount;

    public Dice() {
        this(new Random());
    }

    public Dice(Random random) {
        this.random = Objects.requireNonNull(random, "Random generator cannot be null");
        this.lastRoll = 0;
        this.consecutiveSixesCount = 0;
    }

    public int roll() {
        this.lastRoll = random.nextInt(6) + 1;
        if (this.lastRoll == 6) {
            this.consecutiveSixesCount++;
        } else {
            this.consecutiveSixesCount = 0;
        }
        return this.lastRoll;
    }

    public int getLastRoll() {
        return lastRoll;
    }

    public int getLastRolledValue() {
        return lastRoll;
    }

    public void setLastRolledValue(int val) {
        this.lastRoll = val;
    }

    public int getConsecutiveSixesCount() {
        return consecutiveSixesCount;
    }

    public void setConsecutiveSixesCount(int count) {
        this.consecutiveSixesCount = count;
    }

    public boolean hasThreeConsecutiveSixes() {
        return consecutiveSixesCount >= 3;
    }

    public void resetConsecutiveSixes() {
        this.consecutiveSixesCount = 0;
    }

    public void reset() {
        this.lastRoll = 0;
        this.consecutiveSixesCount = 0;
    }
}
