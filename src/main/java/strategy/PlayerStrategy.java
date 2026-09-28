package strategy;

import model.Board;
import model.Piece;
import model.Player;

import java.util.List;

public interface PlayerStrategy {
    Piece selectPieceToMove(Player player, List<Piece> movablePieces, Board board, int diceRoll);
}
