package command;

import model.Board;
import model.BoardToken;
import model.Player;

public class CommandFactory {
    public static GameCommand createMoveCommand(BoardToken token, Player player, Board board, int diceRoll) {
        if (token == null || player == null || board == null) {
            return new NullCommand();
        }
        MovePieceCommand command = new MovePieceCommand(token, player, board, diceRoll);
        if (!command.isExecutable()) {
            return new NullCommand();
        }
        return command;
    }
}