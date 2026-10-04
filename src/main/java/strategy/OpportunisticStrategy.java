package strategy;

import model.Board;
import model.BoardToken;
import model.Player;
import java.util.List;

public class OpportunisticStrategy implements PlayerStrategy {
    @Override
    public BoardToken selectTokenToMove(Player player, List<BoardToken> movableTokens, Board board, int diceRoll) {
        if (movableTokens == null || movableTokens.isEmpty()) return null;

        if (diceRoll == 6) {
            BoardToken baseToken = movableTokens.stream().filter(t -> t.getCurrentPosition() == -1).findFirst().orElse(null);
            if (baseToken != null) return baseToken;
        }

        for (BoardToken token : movableTokens) {
            if (token.getCurrentPosition() != -1 && !token.hasCapturedOpponent() && landsOnOpponent(token, board, diceRoll, player)) {
                return token;
            }
        }

        BoardToken closestToken = null;
        int minDistance = Integer.MAX_VALUE;
        for (BoardToken token : movableTokens) {
            if (token.getCurrentPosition() == -1) continue;
            int dist = distanceToOwnHome(token, player);
            if (dist < minDistance) {
                minDistance = dist;
                closestToken = token;
            }
        }
        
        return closestToken != null ? closestToken : movableTokens.get(0);
    }

    private boolean landsOnOpponent(BoardToken token, Board board, int diceRoll, Player player) {
        if (token.getCurrentPosition() == -1 || token.getComponentPieces().getFirst().getState() == model.PieceState.HOME_STRAIGHT) return false;
        int actualSteps = token.getTokenSize() > 1 ? (diceRoll / token.getTokenSize()) : diceRoll;
        model.Piece firstPiece = token.getComponentPieces().getFirst();
        if (firstPiece.isEnergized()) actualSteps *= 2;
        if (firstPiece.isSick()) actualSteps /= 2;
        if (actualSteps <= 0) return false;

        int targetPos = token.getDirection() == model.MovementDirection.CLOCKWISE 
                ? (token.getCurrentPosition() + actualSteps) % Board.TOTAL_TRACK_CELLS 
                : (token.getCurrentPosition() - actualSteps + Board.TOTAL_TRACK_CELLS) % Board.TOTAL_TRACK_CELLS;
        
        List<model.Piece> occupants = board.getTrackCell(targetPos).getOccupyingPieces();
        return !occupants.isEmpty() && occupants.getFirst().getColor() != player.getColor() && occupants.size() <= token.getTokenSize();
    }

    private int distanceToOwnHome(BoardToken token, Player player) {
        if (token.getComponentPieces().getFirst().getState() == model.PieceState.HOME_STRAIGHT) {
            return Board.HOME_STRAIGHT_LENGTH - token.getCurrentPosition();
        }
        int approach = Board.getApproachIndex(player.getColor());
        int currentPos = token.getCurrentPosition();
        return token.getDirection() == model.MovementDirection.CLOCKWISE 
            ? (approach - currentPos + Board.TOTAL_TRACK_CELLS) % Board.TOTAL_TRACK_CELLS
            : (currentPos - approach + Board.TOTAL_TRACK_CELLS) % Board.TOTAL_TRACK_CELLS;
    }
}