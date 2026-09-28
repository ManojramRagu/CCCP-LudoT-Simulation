package strategy;

import model.Board;
import model.Piece;
import model.Player;

import java.util.List;

public class BalancedStrategy implements PlayerStrategy {
    @Override
    public Piece selectPieceToMove(Player player, List<Piece> movablePieces, Board board, int diceRoll) {
        if (movablePieces == null || movablePieces.isEmpty()) {
            return null;
        }

        for (Piece piece : movablePieces) {
            if (piece.isInBase() && diceRoll == 6) {
                return piece;
            }
        }

        for (Piece piece : movablePieces) {
            if (!piece.isInBase() && piece.hasCapturedOpponent()) {
                return piece;
            }
        }

        return movablePieces.get(0);
    }
}
