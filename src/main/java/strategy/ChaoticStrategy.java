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

        // Try up to 4 times (for each piece) to find a valid one according to rules
        for (int i = 0; i < 4; i++) {
            int targetCyclicIndex = player.getAndIncrementCyclicIndex();

            for (BoardToken token : movableTokens) {
                if (token.getComponentPieces().stream().anyMatch(p -> p.getId().endsWith(String.valueOf(targetCyclicIndex + 1)))) {
                    
                    // 2. If CCW, prioritize landing on Mystery Cell
                    if (token.getDirection() == MovementDirection.COUNTER_CLOCKWISE && landsOnMysteryCell(token, board, diceRoll)) {
                        return token;
                    }

                    // 3. If CW, avoid mystery cell. If it lands on it, try the next piece in cycle.
                    if (token.getDirection() == MovementDirection.CLOCKWISE && landsOnMysteryCell(token, board, diceRoll)) {
                        break; // Breaks inner loop, moves to next piece in cycle
                    }
                    
                    return token;
                }
            }
        }

        // Fallback if all cyclic pieces are skipped (e.g., all CW land on mystery cell)
        return movableTokens.get(0);
    }

    private boolean landsOnMysteryCell(BoardToken token, Board board, int diceRoll) {
        if (board.getActiveMysteryCell() == null || token.getCurrentPosition() == -1) return false;
        int targetPos = (token.getCurrentPosition() + diceRoll) % Board.TOTAL_TRACK_CELLS;
        return targetPos == board.getActiveMysteryCell().getIndex();
    }
}