package model;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class PieceTest {

    private Piece piece;

    @BeforeEach
    void setUp() {
        piece = new Piece("R1", PieceColor.RED);
    }

    @Test
    @DisplayName("Piece initializes in base with default attributes")
    void testInitialState() {
        assertEquals("R1", piece.getId());
        assertEquals(PieceColor.RED, piece.getColor());
        assertTrue(piece.isInBase());
        assertEquals(-1, piece.getCurrentPosition());
        assertEquals(MovementDirection.CLOCKWISE, piece.getDirection());
        assertEquals(PieceState.BASE, piece.getState());
        assertEquals(1, piece.getTokenSize(), "A single piece must have a token size of 1");
    }

    @Test
    @DisplayName("Piece position update reflects on state")
    void testSetCurrentPosition() {
        piece.setInBase(false);
        piece.setCurrentPosition(10);
        assertEquals(10, piece.getCurrentPosition());
        assertFalse(piece.isInBase());
        assertEquals(PieceState.STANDARD_TRACK, piece.getState());
    }

    @Test
    @DisplayName("Piece records captures correctly for Rule T-7")
    void testRecordCapture() {
        assertFalse(piece.hasCapturedOpponent());
        piece.recordCapture(1);
        assertTrue(piece.hasCapturedOpponent());
    }

    @Test
    @DisplayName("Piece resetting to base resets all accumulated state per Rule T-9")
    void testResetToBase() {
        piece.setInBase(false);
        piece.setCurrentPosition(25);
        piece.recordCapture(1);
        piece.setEnergizedRounds(3);

        piece.resetToBase();
        assertTrue(piece.isInBase());
        assertEquals(-1, piece.getCurrentPosition());
        assertFalse(piece.hasCapturedOpponent());

        // Corrected: Verifying the boolean state instead of the raw integer
        assertFalse(piece.isEnergized());
        assertEquals(PieceState.BASE, piece.getState());
    }
}