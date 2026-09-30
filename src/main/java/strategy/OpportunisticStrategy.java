package strategy;

import model.Board;
import model.BoardToken;
import model.Player;
import java.util.List;

public class OpportunisticStrategy implements PlayerStrategy {
    @Override
    public BoardToken selectTokenToMove(Player player, List<BoardToken> movableTokens, Board board, int diceRoll) {
        if (movableTokens == null || movableTokens.isEmpty()) return null;

        // 1. Prioritize empty base.
        if (diceRoll == 6) {
            BoardToken baseToken = movableTokens.stream().filter(t -> t.getCurrentPosition() == -1).findFirst().orElse(null);
            if (baseToken != null) return baseToken;
        }

        // 2. Prioritize pieces that NEED captures (have 0 captures currently)
        for (BoardToken token : movableTokens) {
            if (token.getCurrentPosition() != -1 && !token.hasCapturedOpponent()) {
                return token;
            }
        }

        // 3. Default opportunistic move
        return movableTokens.get(0);
    }
}