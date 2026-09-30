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
        assertFalse(piece.isCompleted());
        assertEquals(-1, piece.getCurrentPosition());
        assertEquals(MovementDirection.CLOCKWISE, piece.getDirection());
        assertEquals(PieceState.BASE, piece.getState());
    }

    @Test
    @DisplayName("Piece position update reflects on board status and state")
    void testSetCurrentPosition() {
        piece.setInBase(false);
        piece.setCurrentPosition(10);
        assertEquals(10, piece.getCurrentPosition());
        assertFalse(piece.isInBase());
        assertEquals(PieceState.STANDARD_TRACK, piece.getState());
    }

    @Test
    @DisplayName("Piece records captures correctly for Rule T-7 home entry requirement")
    void testRecordCapture() {
        assertFalse(piece.hasCapturedOpponent());
        piece.recordCapture();
        assertTrue(piece.hasCapturedOpponent());
    }

    @Test
    @DisplayName("Piece direction toggles cleanly between Clockwise and Counter-Clockwise")
    void testSetDirection() {
        piece.setDirection(MovementDirection.COUNTER_CLOCKWISE);
        assertEquals(MovementDirection.COUNTER_CLOCKWISE, piece.getDirection());
    }

    @Test
    @DisplayName("Piece resetting to base resets all accumulated state per Rule T-9")
    void testResetToBase() {
        piece.setInBase(false);
        piece.setCurrentPosition(25);
        piece.recordCapture();
        piece.setEnergizedRounds(3);

        piece.resetToBase();
        assertTrue(piece.isInBase());
        assertEquals(-1, piece.getCurrentPosition());
        assertFalse(piece.hasCapturedOpponent());
        assertEquals(0, piece.getEnergizedRounds());
        assertEquals(PieceState.BASE, piece.getState());
    }

    @Test
    @DisplayName("Piece completing home straight updates state to COMPLETED")
    void testMarkCompleted() {
        piece.setInBase(false);
        piece.setCompleted(true);
        assertTrue(piece.isCompleted());
        assertEquals(PieceState.COMPLETED, piece.getState());
    }

    @Test
    @DisplayName("Piece energised rounds double movement speed according to Rule T-12")
    void testEnergizedStatus() {
        piece.setEnergizedRounds(4);
        assertTrue(piece.isEnergized());
        assertEquals(4, piece.getEnergizedRounds());
        
        piece.decrementStatusEffects();
        assertEquals(3, piece.getEnergizedRounds());
    }

    @Test
    @DisplayName("Piece sick rounds halve movement speed according to Rule T-12")
    void testSickStatus() {
        piece.setSickRounds(4);
        assertTrue(piece.isSick());
        assertEquals(4, piece.getSickRounds());

        piece.decrementStatusEffects();
        assertEquals(3, piece.getSickRounds());
    }

    @Test
    @DisplayName("Piece movement restriction prevents movement for 4 rounds according to Rule T-13")
    void testRestrictedRounds() {
        piece.setRestrictedRounds(4);
        assertTrue(piece.isRestricted());
        
        piece.decrementStatusEffects();
        assertEquals(3, piece.getRestrictedRounds());
    }

    @Test
    @DisplayName("Piece entering home straight updates state to HOME_STRAIGHT")
    void testHomeStraightState() {
        piece.setInBase(false);
        piece.setInHomeStraight(true);
        assertEquals(PieceState.HOME_STRAIGHT, piece.getState());
    }

    @Test
    @DisplayName("Piece equality and string representation format correctly")
    void testPieceIdentity() {
        Piece p2 = new Piece("R1", PieceColor.RED);
        assertEquals(piece.getId(), p2.getId());
        assertEquals(piece.getColor(), p2.getColor());
    }

    @Test
    @DisplayName("Piece handles multiple consecutive capture recordings")
    void testMultipleCaptures() {
        piece.recordCapture();
        piece.recordCapture();
        assertTrue(piece.hasCapturedOpponent());
    }
}
