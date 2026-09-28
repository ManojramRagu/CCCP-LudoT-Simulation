package strategy;

import model.Board;
import model.Cell;
import model.Piece;
import model.Player;

import java.util.List;

public class AggressiveStrategy implements PlayerStrategy {
    @Override
    public Piece selectPieceToMove(Player player, List<Piece> movablePieces, Board board, int diceRoll) {
        if (movablePieces == null || movablePieces.isEmpty()) {
            return null;
        }

        for (Piece piece : movablePieces) {
            if (!piece.isInBase() && landsOnOpponent(piece, board, diceRoll, player)) {
                return piece;
            }
        }

        for (Piece piece : movablePieces) {
            if (piece.isInBase() && diceRoll == 6) {
                return piece;
            }
        }

        return movablePieces.get(0);
    }

    private boolean landsOnOpponent(Piece piece, Board board, int diceRoll, Player player) {
        int targetPos = (piece.getCurrentPosition() + diceRoll) % Board.TOTAL_TRACK_CELLS;
        Cell targetCell = board.getTrackCell(targetPos);
        return targetCell.getOccupyingPieces().stream()
                .anyMatch(p -> p.getColor() != player.getColor());
    }
}
