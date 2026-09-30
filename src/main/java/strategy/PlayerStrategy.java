package strategy;

import model.Board;
import model.BoardToken;
import model.Player;

import java.util.List;

public interface PlayerStrategy {
    BoardToken selectTokenToMove(Player player, List<BoardToken> movableTokens, Board board, int diceRoll);
}