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
        Piece piece = player.getPieces().getFirst(); // Replaced .get(0) with .getFirst()

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
        Piece g1 = player.getPieces().getFirst(); // Replaced .get(0) with .getFirst()
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
    @DisplayName("Rule T-3 & T-8: Moving token stops before a larger defensive block, does not capture")
    void testDefensiveBlockStopping() {
        Player red = new Player("Red", PieceColor.RED, null);
        Player blue = new Player("Blue", PieceColor.BLUE, null);

        Piece redPiece = red.getPieces().getFirst(); // Replaced .get(0) with .getFirst()
        redPiece.setCurrentPosition(0);
        board.getTrackCell(0).addPiece(redPiece);

        Piece b1 = blue.getPieces().getFirst(); // Replaced .get(0) with .getFirst()
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
        Piece basePiece = player.getPieces().getFirst(); // Replaced .get(0) with .getFirst()

        GameCommand command = CommandFactory.createMoveCommand(basePiece, player, board, 4);
        assertFalse(command.isExecutable());
        assertInstanceOf(NullCommand.class, command);
    }
}