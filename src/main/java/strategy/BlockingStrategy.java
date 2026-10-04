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

        for (BoardToken token : movableTokens) {
            if (token instanceof Block) {
                return token;
            }
        }

        if (diceRoll == 6) {
            for (BoardToken token : movableTokens) {
                if (token.getCurrentPosition() != -1 && token.getTokenSize() == 1 && landsOnOwnPiece(token, board, diceRoll, player)) {
                    return token;
                }
            }
            BoardToken baseToken = movableTokens.stream().filter(t -> t.getCurrentPosition() == -1).findFirst().orElse(null);
            if (baseToken != null) return baseToken;
        }

        BoardToken blockToBreak = null;
        for (BoardToken token : movableTokens) {
            if (token.getTokenSize() == 1 && token.getCurrentPosition() != -1) {
                long piecesOfMyColor = board.getTrackCell(token.getCurrentPosition()).getOccupyingPieces().stream()
                        .filter(p -> p.getColor() == player.getColor()).count();
                if (piecesOfMyColor > 1) {
                    blockToBreak = token;
                }
            }
        }
        
        if (blockToBreak != null) {
            boolean hasMovablePieceInFront = false;
            for (BoardToken token : movableTokens) {
                if (token.getTokenSize() == 1 && token.getCurrentPosition() != -1) {
                    long piecesOfMyColor = board.getTrackCell(token.getCurrentPosition()).getOccupyingPieces().stream()
                            .filter(p -> p.getColor() == player.getColor()).count();
                    if (piecesOfMyColor == 1 && isPieceInFront(token, blockToBreak, player)) {
                        return token;
                    }
                }
            }
            return blockToBreak;
        }

        for (BoardToken token : movableTokens) {
            if (token.getTokenSize() == 1 && token.getCurrentPosition() != -1) {
                long piecesOfMyColor = board.getTrackCell(token.getCurrentPosition()).getOccupyingPieces().stream()
                        .filter(p -> p.getColor() == player.getColor()).count();
                if (piecesOfMyColor == 1) {
                    return token;
                }
            }
        }

        return movableTokens.get(0);
    }

    private boolean isPieceInFront(BoardToken piece, BoardToken block, Player player) {
        if (piece.getComponentPieces().getFirst().getState() == PieceState.HOME_STRAIGHT) return true;
        if (block.getComponentPieces().getFirst().getState() == PieceState.HOME_STRAIGHT) return false;
        
        int approach = Board.getApproachIndex(player.getColor());
        int pieceDist = piece.getDirection() == MovementDirection.CLOCKWISE 
            ? (approach - piece.getCurrentPosition() + Board.TOTAL_TRACK_CELLS) % Board.TOTAL_TRACK_CELLS
            : (piece.getCurrentPosition() - approach + Board.TOTAL_TRACK_CELLS) % Board.TOTAL_TRACK_CELLS;
            
        int blockDist = block.getDirection() == MovementDirection.CLOCKWISE 
            ? (approach - block.getCurrentPosition() + Board.TOTAL_TRACK_CELLS) % Board.TOTAL_TRACK_CELLS
            : (block.getCurrentPosition() - approach + Board.TOTAL_TRACK_CELLS) % Board.TOTAL_TRACK_CELLS;
            
        return pieceDist < blockDist;
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