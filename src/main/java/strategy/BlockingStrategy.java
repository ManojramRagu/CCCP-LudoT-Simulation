package strategy;

import model.Board;
import model.BoardToken;
import model.Block;
import model.Player;
import java.util.List;

public class BlockingStrategy implements PlayerStrategy {
    @Override
    public BoardToken selectTokenToMove(Player player, List<BoardToken> movableTokens, Board board, int diceRoll) {
        if (movableTokens == null || movableTokens.isEmpty()) return null;

        // 1. Green likes an empty base. Always move to X on a 6, unless it creates a block (simplified to always move for now).
        if (diceRoll == 6) {
            BoardToken baseToken = movableTokens.stream().filter(t -> t.getCurrentPosition() == -1).findFirst().orElse(null);
            if (baseToken != null) return baseToken;
        }

        // 2. Attempt to move forward using a block move (Rule T-4)
        for (BoardToken token : movableTokens) {
            if (token instanceof Block) {
                return token;
            }
        }

        // 3. Move other pieces home before breaking a block (avoid picking pieces that are part of a block)
        for (BoardToken token : movableTokens) {
            if (token.getTokenSize() == 1 && token.getCurrentPosition() != -1) {
                return token; // Move a free piece
            }
        }

        return movableTokens.get(0);
    }
}