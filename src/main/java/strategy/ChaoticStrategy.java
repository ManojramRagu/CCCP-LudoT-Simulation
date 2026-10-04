package strategy;

import model.Board;
import model.BoardToken;
import model.Player;
import model.MovementDirection;
import java.util.List;

public class ChaoticStrategy implements PlayerStrategy {
    @Override
    public BoardToken selectTokenToMove(Player player, List<BoardToken> movableTokens, Board board, int diceRoll) {
        if (movableTokens == null || movableTokens.isEmpty()) return null;

        for (int i = 0; i < 4; i++) {
            int targetCyclicIndex = player.getAndIncrementCyclicIndex();

            for (BoardToken token : movableTokens) {
                if (token.getComponentPieces().stream().anyMatch(p -> p.getId().endsWith(String.valueOf(targetCyclicIndex + 1)))) {
                    if (token.getDirection() == MovementDirection.COUNTER_CLOCKWISE && landsOnMysteryCell(token, board, diceRoll)) {
                        return token;
                    }

                    if (token.getDirection() == MovementDirection.CLOCKWISE && landsOnMysteryCell(token, board, diceRoll)) {
                        break;
                    }
                    
                    return token;
                }
            }
        }

        return movableTokens.get(0);
    }

    private boolean landsOnMysteryCell(BoardToken token, Board board, int diceRoll) {
        if (board.getActiveMysteryCell() == null || token.getCurrentPosition() == -1) return false;
        int actualSteps = token.getTokenSize() > 1 ? (diceRoll / token.getTokenSize()) : diceRoll;
        model.Piece firstPiece = token.getComponentPieces().getFirst();
        if (firstPiece.isEnergized()) actualSteps *= 2;
        if (firstPiece.isSick()) actualSteps /= 2;
        if (actualSteps <= 0) return false;

        int targetPos = token.getDirection() == MovementDirection.CLOCKWISE 
                ? (token.getCurrentPosition() + actualSteps) % Board.TOTAL_TRACK_CELLS 
                : (token.getCurrentPosition() - actualSteps + Board.TOTAL_TRACK_CELLS) % Board.TOTAL_TRACK_CELLS;
        return targetPos == board.getActiveMysteryCell().getIndex();
    }
}