package command;

import model.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.*;

class CommandTest {

    private Board board;

    @BeforeEach
    void setUp() {
        Board.resetInstance();
        board = Board.getInstance();
    }

    @Test
    @DisplayName("MovePieceCommand moves token out of base on rolling 6 and flips coin (Rule T-1)")
    void testMovePieceCommandExitBaseAndCoinToss() {
        Player player = new Player("Red", PieceColor.RED, null);
        Piece piece = player.getPieces().getFirst();

        GameCommand command = CommandFactory.createMoveCommand(piece, player, board, 6);
        assertTrue(command.isExecutable());
        command.execute();

        assertFalse(piece.isInBase());
        assertEquals(Board.RED_START_INDEX, piece.getCurrentPosition());
        assertNotNull(piece.getDirection()); // Proves Rule T-1 coin toss executed
    }

    @Test
    @DisplayName("Rule T-4: Block movement distance is dice roll divided by token size")
    void testBlockMovementRuleT4() {
        Player player = new Player("Green", PieceColor.GREEN, null);
        Piece g1 = player.getPieces().getFirst();
        Piece g2 = player.getPieces().get(1);
        g1.setCurrentPosition(10);
        g2.setCurrentPosition(10);
        g1.setDirection(MovementDirection.CLOCKWISE);
        g2.setDirection(MovementDirection.CLOCKWISE);

        Block block = new Block(Arrays.asList(g1, g2));
        GameCommand command = CommandFactory.createMoveCommand(block, player, board, 4);
        command.execute();

        assertEquals(12, block.getCurrentPosition()); // Moved 4 / 2 = 2 spaces
    }

    @Test
    @DisplayName("Rule T-8: Blockade captures an identically sized opponent blockade")
    void testBlockadeCapturesIdenticalBlockade() {
        Player red = new Player("Red", PieceColor.RED, null);
        Player blue = new Player("Blue", PieceColor.BLUE, null);

        Piece r1 = red.getPieces().getFirst();
        Piece r2 = red.getPieces().get(1);
        r1.setCurrentPosition(0); r2.setCurrentPosition(0);
        r1.setDirection(MovementDirection.CLOCKWISE); r2.setDirection(MovementDirection.CLOCKWISE);
        Block redBlock = new Block(Arrays.asList(r1, r2));

        Piece b1 = blue.getPieces().getFirst();
        Piece b2 = blue.getPieces().get(1);
        b1.setCurrentPosition(3); b2.setCurrentPosition(3);
        board.getTrackCell(3).addPiece(b1);
        board.getTrackCell(3).addPiece(b2);

        // Move 6 / 2 = 3 steps (lands exactly on blue block)
        GameCommand command = CommandFactory.createMoveCommand(redBlock, red, board, 6);
        command.execute();

        assertTrue(b1.isInBase()); // Captured
        assertTrue(b2.isInBase()); // Captured
        assertTrue(redBlock.hasCapturedOpponent());
    }

    @Test
    @DisplayName("Rule 10: Piece in Home Straight cannot overshoot target and rejects invalid move")
    void testHomeStraightOvershootRejection() {
        Player player = new Player("Red", PieceColor.RED, null);
        Piece piece = player.getPieces().getFirst();
        piece.setInBase(false);
        piece.setState(PieceState.HOME_STRAIGHT);
        piece.setCurrentPosition(3); // 2 steps away from Home

        GameCommand command = CommandFactory.createMoveCommand(piece, player, board, 3);
        assertFalse(command.isExecutable());
    }

    @Test
    @DisplayName("Rule 10 & T-7: Moving from track to Home Straight rejects if roll overshoots Home")
    void testEnteringHomeStraightOvershoot() {
        Player player = new Player("Yellow", PieceColor.YELLOW, null);
        Piece piece = player.getPieces().getFirst();
        piece.setInBase(false);
        piece.setState(PieceState.STANDARD_TRACK);
        piece.setCurrentPosition(49); // Yellow approach is 50
        piece.setDirection(MovementDirection.CLOCKWISE);
        piece.recordCapture(1);

        // Distance to approach = 1. Home straight length = 5. Total exactly = 6. 7 overshoots.
        GameCommand command = CommandFactory.createMoveCommand(piece, player, board, 7);
        assertFalse(command.isExecutable());
    }

    @Test
    @DisplayName("testHomeStraightEntryDeniedNoCaptures: CW piece without captures bypasses approach")
    void testHomeStraightEntryDeniedNoCaptures() {
        Player player = new Player("Yellow", PieceColor.YELLOW, null);
        Piece piece = player.getPieces().getFirst();
        piece.setInBase(false);
        piece.setState(PieceState.STANDARD_TRACK);
        piece.setCurrentPosition(49);
        piece.setDirection(MovementDirection.CLOCKWISE);

        // NO capture recorded. Approach is at 50. Roll 2 should move to 51.
        GameCommand command = CommandFactory.createMoveCommand(piece, player, board, 2);
        command.execute();

        assertEquals(51, piece.getCurrentPosition()); // Bypasses 50, lands on 51.
        assertEquals(PieceState.STANDARD_TRACK, piece.getState());
    }

    @Test
    @DisplayName("testHomeStraightEntryGrantedWithCapture: CW piece with capture enters home straight")
    void testHomeStraightEntryGrantedWithCapture() {
        Player player = new Player("Yellow", PieceColor.YELLOW, null);
        Piece piece = player.getPieces().getFirst();
        piece.setInBase(false);
        piece.setState(PieceState.STANDARD_TRACK);
        piece.setCurrentPosition(49);
        piece.setDirection(MovementDirection.CLOCKWISE);
        piece.recordCapture(1);

        // Approach is at 50. Distance to approach is 1. Roll 3 means it enters home straight by 2 steps.
        // Target index in home straight is (3 - 1) - 1 = 1.
        GameCommand command = CommandFactory.createMoveCommand(piece, player, board, 3);
        command.execute();

        assertEquals(1, piece.getCurrentPosition());
        assertEquals(PieceState.HOME_STRAIGHT, piece.getState());
    }

    @Test
    @DisplayName("testHomeStraightCounterClockwiseFirstPass: CCW piece on first pass bypasses approach even with capture")
    void testHomeStraightCounterClockwiseFirstPass() {
        Player player = new Player("Yellow", PieceColor.YELLOW, null);
        Piece piece = player.getPieces().getFirst();
        piece.setInBase(false);
        piece.setState(PieceState.STANDARD_TRACK);
        piece.setCurrentPosition(51);
        piece.setDirection(MovementDirection.COUNTER_CLOCKWISE);
        piece.recordCapture(1);

        // CCW moving from 51 -> 50 (approach). Roll 2 means it goes 51 -> 50 -> 49.
        GameCommand command = CommandFactory.createMoveCommand(piece, player, board, 2);
        
        // This is its first pass, so it should bypass and land on 49.
        assertTrue(command.isExecutable());
        command.execute();

        assertEquals(49, piece.getCurrentPosition());
        assertEquals(PieceState.STANDARD_TRACK, piece.getState());
        assertEquals(1, piece.getApproachPassCount());
    }

    @Test
    @DisplayName("T-12: Alpha energized doubles steps; Alpha sick halves steps")
    void testAlphaEnergizedAndSick() {
        Player player = new Player("Yellow", PieceColor.YELLOW, null);
        Piece piece = player.getPieces().getFirst();
        piece.setInBase(false);
        piece.setState(PieceState.STANDARD_TRACK);
        piece.setCurrentPosition(10);
        piece.setDirection(MovementDirection.CLOCKWISE);

        piece.setEnergizedRounds(4);
        GameCommand energizedCmd = CommandFactory.createMoveCommand(piece, player, board, 3);
        assertTrue(energizedCmd.isExecutable());
        energizedCmd.execute();
        assertEquals(16, piece.getCurrentPosition(), "Energized piece rolling 3 should move 6 cells from L10 to L16");

        piece.setEnergizedRounds(0);
        piece.setCurrentPosition(10);
        piece.setSickRounds(4);

        GameCommand sickCmd = CommandFactory.createMoveCommand(piece, player, board, 4);
        assertTrue(sickCmd.isExecutable());
        sickCmd.execute();
        assertEquals(12, piece.getCurrentPosition(), "Sick piece rolling 4 should move 2 cells from L10 to L12");
    }

    @Test
    @DisplayName("Blocked piece with NO other movable pieces prints 'Ignoring the throw and moving on to the next player.'")
    void testAlternateBlockResolution() {
        Player red = new Player("Red", PieceColor.RED, null);
        Player blue = new Player("Blue", PieceColor.BLUE, null);
        Piece r1 = red.getPieces().getFirst();
        r1.setInBase(false);
        r1.setState(PieceState.STANDARD_TRACK);
        r1.setCurrentPosition(5);
        r1.setDirection(MovementDirection.CLOCKWISE);

        Piece b1 = blue.getPieces().get(0);
        Piece b2 = blue.getPieces().get(1);
        b1.setInBase(false); b2.setInBase(false);
        b1.setState(PieceState.STANDARD_TRACK); b2.setState(PieceState.STANDARD_TRACK);
        b1.setCurrentPosition(6); b2.setCurrentPosition(6);
        b1.setDirection(MovementDirection.CLOCKWISE); b2.setDirection(MovementDirection.CLOCKWISE);
        board.getTrackCell(6).addPiece(b1);
        board.getTrackCell(6).addPiece(b2);

        java.io.ByteArrayOutputStream outputCapture = new java.io.ByteArrayOutputStream();
        java.io.PrintStream originalOut = System.out;
        System.setOut(new java.io.PrintStream(outputCapture));

        GameCommand command = CommandFactory.createMoveCommand(r1, red, board, 1);
        assertTrue(command.isExecutable()); 
        command.execute();

        assertEquals(5, r1.getCurrentPosition(), "Piece should stay at original position when blocked at step 1");

        String output = outputCapture.toString();
        assertTrue(output.contains("is blocked from moving"), "Should contain block notification");
        assertTrue(output.contains("Ignoring the throw and moving on to the next player."),
                "Should print 'Ignoring the throw' when piece is blocked at its very first step (no cells before block)");

        System.setOut(originalOut);
    }
}