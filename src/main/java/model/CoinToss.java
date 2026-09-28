package model;

import java.util.Objects;
import java.util.Random;

public class CoinToss {
    private final Random random;

    public CoinToss() {
        this(new Random());
    }

    public CoinToss(Random random) {
        this.random = Objects.requireNonNull(random, "Random generator cannot be null");
    }

    public MovementDirection flip() {
        return random.nextBoolean() ? MovementDirection.CLOCKWISE : MovementDirection.COUNTER_CLOCKWISE;
    }
}
