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
    @DisplayName("MovePieceCommand moves token out of base on rolling 6")
    void testMovePieceCommandExitBase() {
        Player player = new Player("Red", PieceColor.RED, null);
        Piece piece = player.getPieces().getFirst();

        GameCommand command = CommandFactory.createMoveCommand(piece, player, board, 6);
        assertTrue(command.isExecutable());
        command.execute();

        assertFalse(piece.isInBase());
        assertEquals(Board.RED_START_INDEX, piece.getCurrentPosition());
        assertEquals(1, board.getTrackCell(Board.RED_START_INDEX).getOccupyingPieces().size());
    }

    @Test
    @DisplayName("Rule T-4: Block movement distance is dice roll divided by token size")
    void testBlockMovementRuleT4() {
        Player player = new Player("Green", PieceColor.GREEN, null);
        Piece g1 = player.getPieces().getFirst();
        Piece g2 = player.getPieces().get(1);
        g1.setCurrentPosition(10);
        g2.setCurrentPosition(10);

        Block block = new Block(Arrays.asList(g1, g2));

        GameCommand command = CommandFactory.createMoveCommand(block, player, board, 4);
        command.execute();

        assertEquals(12, block.getCurrentPosition());
        assertEquals(12, g1.getCurrentPosition());
        assertEquals(12, g2.getCurrentPosition());
    }

    @Test
    @DisplayName("Rule T-5: Piece retains original direction when breaking from a block")
    void testRuleT5BlockBreakDirectionRetention() {
        Player player = new Player("Green", PieceColor.GREEN, null);
        Piece g1 = player.getPieces().getFirst();
        Piece g2 = player.getPieces().get(1);

        g1.setDirection(MovementDirection.CLOCKWISE);
        g2.setDirection(MovementDirection.COUNTER_CLOCKWISE);

        Block block = new Block(Arrays.asList(g1, g2));

        // Simulating the block breaking by moving g1 individually
        assertEquals(MovementDirection.CLOCKWISE, g1.getDirection());
        assertEquals(MovementDirection.COUNTER_CLOCKWISE, g2.getDirection());
    }

    @Test
    @DisplayName("Rule 10: Piece in Home Straight cannot overshoot target and rejects invalid move")
    void testHomeStraightOvershootRejection() {
        Player player = new Player("Red", PieceColor.RED, null);
        Piece piece = player.getPieces().getFirst();
        piece.setInBase(false);
        piece.setState(PieceState.HOME_STRAIGHT);
        piece.setCurrentPosition(3); // 2 steps away from Home (Length 5)

        // Roll 3 overshoots the required 2 steps
        GameCommand command = CommandFactory.createMoveCommand(piece, player, board, 3);
        assertFalse(command.isExecutable());
        assertInstanceOf(NullCommand.class, command);
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

        // Distance to approach = 1. Home straight length = 5. Total steps to reach home exactly = 6.
        // A roll of 7 overshoots.
        GameCommand command = CommandFactory.createMoveCommand(piece, player, board, 7);
        assertFalse(command.isExecutable());
    }

    @Test
    @DisplayName("Rule T-3 & T-8: Moving token stops before a larger defensive block, does not capture")
    void testDefensiveBlockStopping() {
        Player red = new Player("Red", PieceColor.RED, null);
        Player blue = new Player("Blue", PieceColor.BLUE, null);

        Piece redPiece = red.getPieces().getFirst();
        redPiece.setCurrentPosition(0);
        board.getTrackCell(0).addPiece(redPiece);

        Piece b1 = blue.getPieces().getFirst();
        Piece b2 = blue.getPieces().get(1);
        b1.setCurrentPosition(3);
        b2.setCurrentPosition(3);
        board.getTrackCell(3).addPiece(b1);
        board.getTrackCell(3).addPiece(b2);

        GameCommand command = CommandFactory.createMoveCommand(redPiece, red, board, 5);
        command.execute();

        assertEquals(2, redPiece.getCurrentPosition());
        assertFalse(redPiece.hasCapturedOpponent());
    }

    @Test
    @DisplayName("CommandFactory creates NullCommand when move is invalid")
    void testNullCommandExecution() {
        Player player = new Player("Red", PieceColor.RED, null);
        Piece basePiece = player.getPieces().getFirst();

        GameCommand command = CommandFactory.createMoveCommand(basePiece, player, board, 4);
        assertFalse(command.isExecutable());
        assertInstanceOf(NullCommand.class, command);
    }
}