package model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.Random;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

class DiceTest {

    @Test
    @DisplayName("Dice roll should remain within bounds 1 to 6")
    void testDiceRollWithinBounds() {
        Dice dice = new Dice();
        for (int i = 0; i < 100; i++) {
            int roll = dice.roll();
            assertTrue(roll >= 1 && roll <= 6, "Roll must be between 1 and 6");
        }
    }

    @Test
    @DisplayName("Dice initial value should be 0 before any roll")
    void testInitialValueZero() {
        Dice dice = new Dice();
        assertEquals(0, dice.getLastRoll());
    }

    @Test
    @DisplayName("Dice roll should return deterministic value when Random is mocked")
    void testMockedDiceRoll() {
        // ARRANGE: Inject a test double to guarantee a roll of 6
        Random mockRandom = Mockito.mock(Random.class);
        when(mockRandom.nextInt(6)).thenReturn(5); // nextInt(6) returns 0-5. 5 + 1 = 6.

        Dice dice = new Dice(mockRandom);

        // ACT
        int roll = dice.roll();

        // ASSERT
        assertEquals(6, roll, "Mocked die should return exactly 6");
        assertEquals(6, dice.getLastRoll());
    }
}