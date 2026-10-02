package strategy;

import model.Board;
import model.BoardToken;
import model.Block;
import model.Player;
import model.Piece;
import model.PieceState;
import model.MovementDirection;
import java.util.List;

public class BlockingStrategy implements PlayerStrategy {
    @Override
    public BoardToken selectTokenToMove(Player player, List<BoardToken> movableTokens, Board board, int diceRoll) {
        if (movableTokens == null || movableTokens.isEmpty()) return null;

        // 1. Attempt to move forward using a block move (Rule T-4)
        for (BoardToken token : movableTokens) {
            if (token instanceof Block) {
                return token;
            }
        }

        // 2. Green likes an empty base. Always move to X on a 6, unless moving 6 creates a block.
        if (diceRoll == 6) {
            for (BoardToken token : movableTokens) {
                if (token.getCurrentPosition() != -1 && token.getTokenSize() == 1 && landsOnOwnPiece(token, board, diceRoll, player)) {
                    return token;
                }
            }
            BoardToken baseToken = movableTokens.stream().filter(t -> t.getCurrentPosition() == -1).findFirst().orElse(null);
            if (baseToken != null) return baseToken;
        }

        // 3. Move other pieces home before breaking a block (avoid picking pieces that are part of a block)
        for (BoardToken token : movableTokens) {
            if (token.getTokenSize() == 1 && token.getCurrentPosition() != -1) {
                return token; // Move a free piece
            }
        }

        return movableTokens.get(0);
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
}