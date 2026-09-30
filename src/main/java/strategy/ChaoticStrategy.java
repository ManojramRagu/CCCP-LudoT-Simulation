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

        // 1. Cyclic priority (B1 -> B2 -> B3 -> B4)
        int targetCyclicIndex = player.getAndIncrementCyclicIndex();

        // Find the specific piece matching the cyclic index, if it is movable
        for (BoardToken token : movableTokens) {
            // Simplified check: checking if the token contains the specific cyclic piece
            if (token.getComponentPieces().stream().anyMatch(p -> p.getId().endsWith(String.valueOf(targetCyclicIndex + 1)))) {

                // 2. If CCW, prioritize landing on Mystery Cell
                if (token.getDirection() == MovementDirection.COUNTER_CLOCKWISE && landsOnMysteryCell(token, board, diceRoll)) {
                    return token;
                }

                // 3. If CW, avoid mystery cell. If it lands on it, skip this piece.
                if (token.getDirection() == MovementDirection.CLOCKWISE && landsOnMysteryCell(token, board, diceRoll)) {
                    continue;
                }
                return token;
            }
        }

        // Fallback if cyclic piece isn't movable
        return movableTokens.get(0);
    }

    private boolean landsOnMysteryCell(BoardToken token, Board board, int diceRoll) {
        if (board.getActiveMysteryCell() == null || token.getCurrentPosition() == -1) return false;
        int targetPos = (token.getCurrentPosition() + diceRoll) % Board.TOTAL_TRACK_CELLS;
        return targetPos == board.getActiveMysteryCell().getIndex();
    }
}