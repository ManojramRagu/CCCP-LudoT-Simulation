package model;

import org.junit.jupiter.api.Test;
import strategy.AggressiveStrategy;
import static org.junit.jupiter.api.Assertions.*;

class PlayerTest {
    @Test
    void testPlayerInitialization() {
        Player player = new Player("Red", PieceColor.RED, new AggressiveStrategy());
        assertEquals("Red", player.getName());
        assertEquals(PieceColor.RED, player.getColor());
        assertNotNull(player.getStrategy());
        assertEquals(4, player.getPieces().size());
    }
}
