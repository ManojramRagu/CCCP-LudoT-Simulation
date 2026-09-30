package command;

import model.Board;
import model.Piece;
import model.PieceColor;
import model.Player;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class CommandTest {

    @Test
    @DisplayName("MovePieceCommand moves piece out of base on rolling 6")
    void testMovePieceCommandExitBase() {
        Player player = new Player("Red", PieceColor.RED);
        Board board = new Board();
        Piece piece = player.getPieces().get(0);

        GameCommand command = CommandFactory.createMoveCommand(piece, player, board, 6);
        assertTrue(command.isExecutable());
        command.execute();

        assertFalse(piece.isInBase());
        assertEquals(Board.RED_START_INDEX, piece.getCurrentPosition());
        assertEquals(1, board.getTrackCell(Board.RED_START_INDEX).getOccupyingPieces().size());
    }

    @Test
    @DisplayName("MovePieceCommand advances piece and executes capture on opponent")
    void testMovePieceCommandCapture() {
        Player red = new Player("Red", PieceColor.RED);
        Player blue = new Player("Blue", PieceColor.BLUE);
        Board board = new Board();

        Piece redPiece = red.getPieces().get(0);
        redPiece.setInBase(false);
        redPiece.setCurrentPosition(0);
        board.getTrackCell(0).addPiece(redPiece);

        Piece bluePiece = blue.getPieces().get(0);
        bluePiece.setInBase(false);
        bluePiece.setCurrentPosition(3);
        board.getTrackCell(3).addPiece(bluePiece);

        GameCommand command = CommandFactory.createMoveCommand(redPiece, red, board, 3);
        command.execute();

        assertEquals(3, redPiece.getCurrentPosition());
        assertTrue(bluePiece.isInBase());
        assertTrue(redPiece.hasCapturedOpponent());
        assertEquals(1, board.getTrackCell(3).getOccupyingPieces().size());
    }

    @Test
    @DisplayName("MovePieceCommand stops piece before a defensive block per Rule T-3")
    void testDefensiveBlockStopping() {
        Player red = new Player("Red", PieceColor.RED);
        Player blue = new Player("Blue", PieceColor.BLUE);
        Board board = new Board();

        Piece redPiece = red.getPieces().get(0);
        redPiece.setInBase(false);
        redPiece.setCurrentPosition(0);
        board.getTrackCell(0).addPiece(redPiece);

        Piece blue1 = blue.getPieces().get(0);
        blue1.setInBase(false);
        blue1.setCurrentPosition(3);
        board.getTrackCell(3).addPiece(blue1);

        Piece blue2 = blue.getPieces().get(1);
        blue2.setInBase(false);
        blue2.setCurrentPosition(3);
        board.getTrackCell(3).addPiece(blue2);

        assertTrue(board.getTrackCell(3).isBlocked());

        GameCommand command = CommandFactory.createMoveCommand(redPiece, red, board, 5);
        command.execute();

        assertEquals(2, redPiece.getCurrentPosition());
    }

    @Test
    @DisplayName("CommandFactory creates NullCommand when move is invalid")
    void testNullCommandExecution() {
        Player player = new Player("Red", PieceColor.RED);
        Board board = new Board();
        Piece basePiece = player.getPieces().get(0);

        GameCommand command = CommandFactory.createMoveCommand(basePiece, player, board, 4);
        assertFalse(command.isExecutable());
        assertInstanceOf(NullCommand.class, command);

        command.execute();
        assertTrue(basePiece.isInBase());
    }
}
