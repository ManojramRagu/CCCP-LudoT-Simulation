package strategy;

import model.Board;
import model.BoardToken;
import model.Player;
import model.PieceState;
import java.util.List;

public class AggressiveStrategy implements PlayerStrategy {
    @Override
    public BoardToken selectTokenToMove(Player player, List<BoardToken> movableTokens, Board board, int diceRoll) {
        if (movableTokens == null || movableTokens.isEmpty()) return null;

        // 1. Prioritize capturing opponent pieces (closest to home logic would require complex pathfinding, simplified to first available capture here to maintain clean architecture).
        for (BoardToken token : movableTokens) {
            if (token.getCurrentPosition() != -1 && landsOnOpponent(token, board, diceRoll, player)) {
                return token;
            }
        }

        // 2. Will not take a piece from base unless it cannot capture with a 6, AND it doesn't already have one on track.
        long piecesOnTrack = player.getPieces().stream().filter(p -> p.getState() == PieceState.STANDARD_TRACK).count();
        if (diceRoll == 6 && piecesOnTrack == 0) {
            return getTokenInBase(movableTokens);
        }

        // 3. Avoid creating blocks - pick a token that doesn't share a destination with our own pieces.
        for (BoardToken token : movableTokens) {
            if (token.getCurrentPosition() != -1 && !landsOnOwnPiece(token, board, diceRoll, player)) {
                return token;
            }
        }

        return movableTokens.get(0);
    }

    private boolean landsOnOpponent(BoardToken token, Board board, int diceRoll, Player player) {
        // Simplified prediction logic for architecture phase
        int targetPos = (token.getCurrentPosition() + diceRoll) % Board.TOTAL_TRACK_CELLS;
        return board.getTrackCell(targetPos).hasOpponentPiece(player.getColor());
    }

    private boolean landsOnOwnPiece(BoardToken token, Board board, int diceRoll, Player player) {
        int targetPos = (token.getCurrentPosition() + diceRoll) % Board.TOTAL_TRACK_CELLS;
        return board.getTrackCell(targetPos).getOccupyingPieces().stream().anyMatch(p -> p.getColor() == player.getColor());
    }

    private BoardToken getTokenInBase(List<BoardToken> tokens) {
        return tokens.stream().filter(t -> t.getCurrentPosition() == -1).findFirst().orElse(tokens.get(0));
    }
}