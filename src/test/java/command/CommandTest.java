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
        piece.setCurrentPosition(50); // Yellow approach is 51
        piece.setDirection(MovementDirection.CLOCKWISE);
        piece.recordCapture(1);

        // Distance to approach = 1. Home straight length = 5. Total exactly = 6. 7 overshoots.
        GameCommand command = CommandFactory.createMoveCommand(piece, player, board, 7);
        assertFalse(command.isExecutable());
    }

    @Test
    @DisplayName("Rule T-7: Piece attempting to enter Home Straight without capturing continues on standard track")
    void testEnteringHomeStraightWithoutCaptureFails() {
        Player player = new Player("Yellow", PieceColor.YELLOW, null);
        Piece piece = player.getPieces().getFirst();
        piece.setInBase(false);
        piece.setState(PieceState.STANDARD_TRACK);
        piece.setCurrentPosition(50);
        piece.setDirection(MovementDirection.CLOCKWISE);

        // NO capture recorded
        GameCommand command = CommandFactory.createMoveCommand(piece, player, board, 2);
        command.execute();

        assertEquals(0, piece.getCurrentPosition()); // Continued past approach (51) to cell 0
        assertEquals(PieceState.STANDARD_TRACK, piece.getState());
    }
}