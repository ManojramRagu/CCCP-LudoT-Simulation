package strategy;

import model.Board;
import model.BoardToken;
import model.Player;
import model.Piece;
import model.PieceState;
import model.MovementDirection;
import java.util.List;

public class AggressiveStrategy implements PlayerStrategy {
    @Override
    public BoardToken selectTokenToMove(Player player, List<BoardToken> movableTokens, Board board, int diceRoll) {
        if (movableTokens == null || movableTokens.isEmpty()) return null;

        // 1. Prioritize capturing opponent pieces (closest to the opponent's home)
        BoardToken bestCaptureToken = null;
        int minDistance = Integer.MAX_VALUE;
        for (BoardToken token : movableTokens) {
            Piece captured = getCapturedOpponent(token, board, diceRoll, player);
            if (captured != null) {
                int dist = distanceToOpponentHome(captured, board);
                if (dist < minDistance) {
                    minDistance = dist;
                    bestCaptureToken = token;
                }
            }
        }
        if (bestCaptureToken != null) return bestCaptureToken;

        // 2. Will not take a piece from base unless it cannot capture with a 6
        if (diceRoll == 6) {
            BoardToken baseToken = getTokenInBase(movableTokens);
            if (baseToken != null) return baseToken;
        }

        // 3. Avoid creating blocks - pick a token that doesn't share a destination with our own pieces.
        for (BoardToken token : movableTokens) {
            if (token.getCurrentPosition() != -1 && !landsOnOwnPiece(token, board, diceRoll, player)) {
                return token;
            }
        }

        return movableTokens.stream().filter(t -> t.getCurrentPosition() != -1).findFirst().orElse(movableTokens.get(0));
    }

    private Piece getCapturedOpponent(BoardToken token, Board board, int diceRoll, Player player) {
        if (token.getCurrentPosition() == -1 || token.getComponentPieces().getFirst().getState() == PieceState.HOME_STRAIGHT) return null;
        
        int actualSteps = token.getTokenSize() > 1 ? (diceRoll / token.getTokenSize()) : diceRoll;
        Piece firstPiece = token.getComponentPieces().getFirst();
        if (firstPiece.isEnergized()) actualSteps *= 2;
        if (firstPiece.isSick()) actualSteps /= 2;
        if (actualSteps <= 0) return null;

        int currentPos = token.getCurrentPosition();
        int targetPos = token.getDirection() == MovementDirection.CLOCKWISE 
                ? (currentPos + actualSteps) % Board.TOTAL_TRACK_CELLS 
                : (currentPos - actualSteps + Board.TOTAL_TRACK_CELLS) % Board.TOTAL_TRACK_CELLS;
        
        List<Piece> occupants = board.getTrackCell(targetPos).getOccupyingPieces();
        if (occupants.isEmpty()) return null;
        if (occupants.getFirst().getColor() != player.getColor() && occupants.size() <= token.getTokenSize()) {
            return occupants.getFirst();
        }
        return null;
    }

    private int distanceToOpponentHome(Piece opponent, Board board) {
        int approach = Board.getApproachIndex(opponent.getColor());
        int currentPos = opponent.getCurrentPosition();
        if (currentPos == -1) return 1000;
        return opponent.getDirection() == MovementDirection.CLOCKWISE 
            ? (approach - currentPos + Board.TOTAL_TRACK_CELLS) % Board.TOTAL_TRACK_CELLS
            : (currentPos - approach + Board.TOTAL_TRACK_CELLS) % Board.TOTAL_TRACK_CELLS;
    }

    private boolean landsOnOwnPiece(BoardToken token, Board board, int diceRoll, Player player) {
        if (token.getCurrentPosition() == -1 || token.getComponentPieces().getFirst().getState() == PieceState.HOME_STRAIGHT) return false;
        
        int actualSteps = token.getTokenSize() > 1 ? (diceRoll / token.getTokenSize()) : diceRoll;
        Piece firstPiece = token.getComponentPieces().getFirst();
        if (firstPiece.isEnergized()) actualSteps *= 2;
        if (firstPiece.isSick()) actualSteps /= 2;
        if (actualSteps <= 0) return false;

        int targetPos = token.getDirection() == MovementDirection.CLOCKWISE 
                ? (token.getCurrentPosition() + actualSteps) % Board.TOTAL_TRACK_CELLS 
                : (token.getCurrentPosition() - actualSteps + Board.TOTAL_TRACK_CELLS) % Board.TOTAL_TRACK_CELLS;
        return board.getTrackCell(targetPos).getOccupyingPieces().stream().anyMatch(p -> p.getColor() == player.getColor());
    }

    private BoardToken getTokenInBase(List<BoardToken> tokens) {
        return tokens.stream().filter(t -> t.getCurrentPosition() == -1).findFirst().orElse(null);
    }
}