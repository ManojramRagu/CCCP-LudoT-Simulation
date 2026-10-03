package model;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.EnumSource;

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

    @ParameterizedTest
    @EnumSource(PieceColor.class)
    @DisplayName("Piece initializes correctly for all colors")
    void testPieceCreationAllColors(PieceColor color) {
        Piece p = new Piece("TEST", color);
        assertEquals(color, p.getColor());
        assertEquals("TEST", p.getId());
        assertEquals(PieceState.BASE, p.getState());
        assertEquals(-1, p.getCurrentPosition());
        assertFalse(p.isCompleted());
        assertFalse(p.isEnergized());
        assertFalse(p.isSick());
        assertFalse(p.isRestricted());
        assertFalse(p.isArrivedViaTeleport());
        assertEquals(MovementDirection.CLOCKWISE, p.getOriginalDirection());
    }

    @ParameterizedTest
    @CsvSource({
            "0, false",
            "1, false",
            "2, true",
            "5, true"
    })
    @DisplayName("Piece correctly decrements energized rounds")
    void testDecrementEnergizedRounds(int initialRounds, boolean isEnergized) {
        piece.setEnergizedRounds(initialRounds);
        piece.decrementStatusEffects();
        assertEquals(isEnergized, piece.isEnergized());
    }

    @ParameterizedTest
    @CsvSource({
            "0, false",
            "1, false",
            "3, true"
    })
    @DisplayName("Piece correctly decrements sick rounds")
    void testDecrementSickRounds(int initialRounds, boolean isSick) {
        piece.setSickRounds(initialRounds);
        piece.decrementStatusEffects();
        assertEquals(isSick, piece.isSick());
    }

    @ParameterizedTest
    @CsvSource({
            "0, false",
            "1, false",
            "4, true"
    })
    @DisplayName("Piece correctly decrements restricted rounds")
    void testDecrementRestrictedRounds(int initialRounds, boolean isRestricted) {
        piece.setRestrictedRounds(initialRounds);
        piece.decrementStatusEffects();
        assertEquals(isRestricted, piece.isRestricted());
    }

    @ParameterizedTest
    @EnumSource(PieceState.class)
    @DisplayName("Piece state can be arbitrarily set and retrieved")
    void testSetState(PieceState state) {
        piece.setState(state);
        assertEquals(state, piece.getState());
    }

    @Test
    @DisplayName("Piece sets completed status correctly")
    void testCompletedPiece() {
        piece.setCompleted(true);
        assertTrue(piece.isCompleted());
        assertEquals(PieceState.COMPLETED, piece.getState());
        assertFalse(piece.isInBase());
    }

    @Test
    @DisplayName("Piece teleport flag sets and retrieves correctly")
    void testTeleportFlag() {
        assertFalse(piece.isArrivedViaTeleport());
        piece.setArrivedViaTeleport(true);
        assertTrue(piece.isArrivedViaTeleport());
    }
}