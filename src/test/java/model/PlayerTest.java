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
    @Test
    void testPlayerWinCondition() {
        Player player = new Player("Red", PieceColor.RED, new AggressiveStrategy());
        
        // Initially, 0 pieces are completed
        assertFalse(player.hasWon(), "Should not win with 0 pieces completed");
        
        // Complete 1 piece
        player.getPieces().get(0).setCompleted(true);
        assertFalse(player.hasWon(), "Should not win with 1 piece completed");
        
        // Complete 2 pieces
        player.getPieces().get(1).setCompleted(true);
        assertFalse(player.hasWon(), "Should not win with 2 pieces completed");
        
        // Complete 3 pieces
        player.getPieces().get(2).setCompleted(true);
        assertFalse(player.hasWon(), "Should not win with 3 pieces completed");
        
        // Complete 4 pieces
        player.getPieces().get(3).setCompleted(true);
        assertTrue(player.hasWon(), "Should win when exactly 4 pieces are completed");
    }
}
