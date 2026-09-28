package command;

import model.Board;
import model.Piece;
import model.Player;

public class CommandFactory {
    public static GameCommand createMoveCommand(Piece piece, Player player, Board board, int steps) {
        if (piece == null || player == null || board == null) {
            return new NullCommand();
        }
        MovePieceCommand command = new MovePieceCommand(piece, player, board, steps);
        if (!command.isExecutable()) {
            return new NullCommand();
        }
        return command;
    }

    public static GameCommand createCommand(Piece piece, int steps, Board board, Player player) {
        return createMoveCommand(piece, player, board, steps);
    }
}
