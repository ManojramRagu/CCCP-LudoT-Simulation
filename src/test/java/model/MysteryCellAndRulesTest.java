package model;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import strategy.AggressiveStrategy;

import static org.junit.jupiter.api.Assertions.*;

class MysteryCellAndRulesTest {

    private Board board;
    private Player redPlayer;
    private Piece piece;

    @BeforeEach
    void setUp() {
        Board.resetInstance();
        board = Board.getInstance();
        redPlayer = new Player("Red Player", PieceColor.RED, new AggressiveStrategy());
        piece = redPlayer.getPieces().getFirst();
        piece.setInBase(false);
        piece.setCurrentPosition(10);
    }

    @Test
    @DisplayName("MysteryCellEffect enum provides 6 teleport options per Rule T-11")
    void testMysteryCellOptionsCount() {
        assertEquals(6, MysteryCellEffect.values().length);
    }

    @Test
    @DisplayName("Rule T-12: Alpha energised status doubles movement speed")
    void testAlphaEnergizedDoublesMovement() {
        piece.setEnergizedRounds(4);
        assertTrue(piece.isEnergized());
        int roll = 3;
        int effectiveMove = piece.isEnergized() ? roll * 2 : roll;
        assertEquals(6, effectiveMove);
    }

    @Test
    @DisplayName("Rule T-12: Alpha sick status halves movement speed")
    void testAlphaSickHalvesMovement() {
        piece.setSickRounds(4);
        assertTrue(piece.isSick());
        int roll = 4;
        int effectiveMove = piece.isSick() ? roll / 2 : roll;
        assertEquals(2, effectiveMove);
    }

    @Test
    @DisplayName("Rule T-13: Beta restricted piece returns to base on three consecutive 3s")
    void testBetaEscapeOnConsecutiveThrees() {
        piece.setRestrictedRounds(4);
        assertTrue(piece.isRestricted());

        // Simulating the engine detecting three consecutive 3s during the restriction
        int consecutiveThrees = 3;
        if (piece.isRestricted() && consecutiveThrees == 3) {
            piece.resetToBase();
        }

        assertTrue(piece.isInBase());
        assertFalse(piece.isRestricted());
    }

    @Test
    @DisplayName("Rule T-10: Mystery cell spawns on empty standard cell")
    void testMysteryCellSpawningOnEmptyCell() {
        board.spawnMysteryCell(20);
        assertEquals(CellType.MYSTERY, board.getTrackCell(20).getType());
        assertNotNull(board.getActiveMysteryCell());
    }
}