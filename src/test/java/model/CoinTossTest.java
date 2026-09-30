package model;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class CoinTossTest {

    private CoinToss coinToss;

    @BeforeEach
    void setUp() {
        coinToss = new CoinToss();
    }

    @Test
    @DisplayName("CoinToss produces non-null MovementDirection on flip")
    void testFlipNonNull() {
        MovementDirection direction = coinToss.flip();
        assertNotNull(direction);
    }

    @Test
    @DisplayName("CoinToss flip returns valid enum values CLOCKWISE or COUNTER_CLOCKWISE")
    void testFlipValidDirection() {
        MovementDirection direction = coinToss.flip();
        assertTrue(direction == MovementDirection.CLOCKWISE || direction == MovementDirection.COUNTER_CLOCKWISE);
    }

    @Test
    @DisplayName("CoinToss flip produces both heads and tails over large sample")
    void testFlipDistribution() {
        boolean sawClockwise = false;
        boolean sawCounterClockwise = false;

        for (int i = 0; i < 200; i++) {
            MovementDirection dir = coinToss.flip();
            if (dir == MovementDirection.CLOCKWISE) sawClockwise = true;
            if (dir == MovementDirection.COUNTER_CLOCKWISE) sawCounterClockwise = true;
        }

        assertTrue(sawClockwise && sawCounterClockwise);
    }

    @Test
    @DisplayName("CoinToss instance is non-null")
    void testInstantiation() {
        assertNotNull(coinToss);
    }
}
