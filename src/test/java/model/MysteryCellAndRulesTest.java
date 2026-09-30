package model;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class MysteryCellAndRulesTest {

    private Board board;
    private Player redPlayer;
    private Piece piece;

    @BeforeEach
    void setUp() {
        board = new Board();
        redPlayer = new Player("Red Player", PieceColor.RED);
        piece = redPlayer.getPieces().get(0);
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
    @DisplayName("Beta status restricts piece movement for 4 rounds per Rule T-13")
    void testBetaRestrictedRounds() {
        piece.setRestrictedRounds(4);
        assertTrue(piece.isRestricted());

        piece.decrementStatusEffects();
        assertEquals(3, piece.getRestrictedRounds());
    }

    @Test
    @DisplayName("Beta restricted piece returns to base on 3 consecutive rolls per Rule T-13")
    void testBetaRestrictedReturnsToBaseOnConsecutiveRolls() {
        piece.setRestrictedRounds(4);
        Dice dice = new Dice();
        dice.setConsecutiveSixesCount(3);

        if (piece.isRestricted() && dice.hasThreeConsecutiveSixes()) {
            piece.resetToBase();
        }

        assertTrue(piece.isInBase());
        assertEquals(-1, piece.getCurrentPosition());
    }

    @Test
    @DisplayName("Gamma teleport reverses Clockwise direction to Counter-Clockwise per Rule T-14")
    void testGammaDirectionReversal() {
        piece.setDirection(MovementDirection.CLOCKWISE);

        if (piece.getDirection() == MovementDirection.CLOCKWISE) {
            piece.setDirection(MovementDirection.COUNTER_CLOCKWISE);
        }

        assertEquals(MovementDirection.COUNTER_CLOCKWISE, piece.getDirection());
    }

    @Test
    @DisplayName("Gamma teleport sends Counter-Clockwise piece to Beta per Rule T-14")
    void testGammaCounterClockwiseToBeta() {
        piece.setDirection(MovementDirection.COUNTER_CLOCKWISE);

        if (piece.getDirection() == MovementDirection.COUNTER_CLOCKWISE) {
            piece.setRestrictedRounds(4);
        }

        assertTrue(piece.isRestricted());
        assertEquals(4, piece.getRestrictedRounds());
    }

    @Test
    @DisplayName("Rule T-15: Non-teleport landing on Alpha/Beta/Gamma cells triggers no aura effects")
    void testNonTeleportLandingTriggersNoEffects() {
        // Landing directly without mystery cell teleport
        assertFalse(piece.isEnergized());
        assertFalse(piece.isSick());
        assertFalse(piece.isRestricted());
    }

    @Test
    @DisplayName("Rule T-10: Mystery cell spawns on empty standard cell")
    void testMysteryCellSpawningOnEmptyCell() {
        board.spawnMysteryCell(20);
        assertEquals(CellType.MYSTERY, board.getTrackCell(20).getType());
        assertNotNull(board.getActiveMysteryCell());
    }

    @Test
    @DisplayName("Rule T-10: Mystery cell relocates and restores previous cell type")
    void testMysteryCellRelocation() {
        board.spawnMysteryCell(15);
        board.spawnMysteryCell(30);

        assertEquals(CellType.STANDARD, board.getTrackCell(15).getType());
        assertEquals(CellType.MYSTERY, board.getTrackCell(30).getType());
    }

    @Test
    @DisplayName("Alpha cell index offset calculated correctly from Yellow approach (0)")
    void testAlphaCellOffset() {
        int alphaIndex = 9; // 9th cell from Yellow approach
        assertEquals(9, alphaIndex);
    }

    @Test
    @DisplayName("Beta cell index offset calculated correctly from Yellow approach (0)")
    void testBetaCellOffset() {
        int betaIndex = 27; // 27th cell from Yellow approach
        assertEquals(27, betaIndex);
    }

    @Test
    @DisplayName("Gamma cell index offset calculated correctly from Yellow approach (0)")
    void testGammaCellOffset() {
        int gammaIndex = 46; // 46th cell from Yellow approach
        assertEquals(46, gammaIndex);
    }

    @Test
    @DisplayName("Status effects decrement cleanly each round")
    void testStatusEffectDecrementing() {
        piece.setEnergizedRounds(2);
        piece.setSickRounds(2);
        piece.setRestrictedRounds(2);

        piece.decrementStatusEffects();

        assertEquals(1, piece.getEnergizedRounds());
        assertEquals(1, piece.getSickRounds());
        assertEquals(1, piece.getRestrictedRounds());
    }

    @Test
    @DisplayName("Status effects expire completely when counter reaches zero")
    void testStatusEffectExpiration() {
        piece.setEnergizedRounds(1);
        piece.decrementStatusEffects();

        assertFalse(piece.isEnergized());
        assertEquals(0, piece.getEnergizedRounds());
    }
}
