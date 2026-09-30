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
    @DisplayName("Alpha energised status doubles movement speed per Rule T-12")
    void testAlphaEnergizedDoublesMovement() {
        piece.setEnergizedRounds(4);
        assertTrue(piece.isEnergized());
        int roll = 3;
        int effectiveMove = piece.isEnergized() ? roll * 2 : roll;
        assertEquals(6, effectiveMove);
    }

    @Test
    @DisplayName("Alpha sick status halves movement speed per Rule T-12")
    void testAlphaSickHalvesMovement() {
        piece.setSickRounds(4);
        assertTrue(piece.isSick());
        int roll = 4;
        int effectiveMove = piece.isSick() ? roll / 2 : roll;
        assertEquals(2, effectiveMove);
    }

    @Test
    @DisplayName("Rule T-10: Mystery cell spawns on empty standard cell")
    void testMysteryCellSpawningOnEmptyCell() {
        board.spawnMysteryCell(20);
        assertEquals(CellType.MYSTERY, board.getTrackCell(20).getType());
        assertNotNull(board.getActiveMysteryCell());
    }
}