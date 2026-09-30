package command;

import model.*;
import java.util.List;

public class MovePieceCommand implements GameCommand {
    private final BoardToken token;
    private final Player player;
    private final Board board;
    private final int diceRoll;
    private final int previousPosition;
    private boolean captured;

    public MovePieceCommand(BoardToken token, Player player, Board board, int diceRoll) {
        this.token = token;
        this.player = player;
        this.board = board;
        this.diceRoll = diceRoll;
        this.previousPosition = token != null ? token.getCurrentPosition() : -1;
        this.captured = false;
    }

    @Override
    public boolean isExecutable() {
        if (token == null || player == null || board == null || diceRoll <= 0) return false;

        if (token.getCurrentPosition() == -1) {
            return diceRoll == 6; // Rule 2: Must roll a 6 to exit base
        }

        int actualSteps = token.getTokenSize() > 1 ? (diceRoll / token.getTokenSize()) : diceRoll;
        return actualSteps > 0;
    }

    @Override
    public void execute() {
        if (!isExecutable()) return;

        if (token.getCurrentPosition() == -1 && diceRoll == 6) {
            int startIdx = Board.getStartingIndex(player.getColor());
            token.setCurrentPosition(startIdx);
            Cell startCell = board.getTrackCell(startIdx);
            handleCaptureOnCell(startCell);
            for(Piece p : token.getComponentPieces()) {
                startCell.addPiece(p);
            }
            return;
        }

        int actualSteps = token.getTokenSize() > 1 ? (diceRoll / token.getTokenSize()) : diceRoll;
        int currentPos = token.getCurrentPosition();
        int actualTarget = currentPos;

        for (int i = 1; i <= actualSteps; i++) {
            int nextPos = (currentPos + i) % Board.TOTAL_TRACK_CELLS;
            Cell nextCell = board.getTrackCell(nextPos);

            if (nextCell.isBlocked() && nextCell.hasOpponentPiece(player.getColor())) {
                if (i == actualSteps && nextCell.getOccupyingPieces().size() == token.getTokenSize()) {
                    actualTarget = nextPos;
                }
                break;
            }
            actualTarget = nextPos;
        }

        if (actualTarget != currentPos) {
            Cell oldCell = board.getTrackCell(currentPos);
            for(Piece p : token.getComponentPieces()) oldCell.removePiece(p);

            Cell targetCell = board.getTrackCell(actualTarget);
            handleCaptureOnCell(targetCell);

            token.setCurrentPosition(actualTarget);
            for(Piece p : token.getComponentPieces()) targetCell.addPiece(p);
        }
    }

    private void handleCaptureOnCell(Cell targetCell) {
        if (targetCell.hasOpponentPiece(player.getColor())) {
            List<Piece> occupants = targetCell.getOccupyingPieces();
            if (occupants.size() == token.getTokenSize()) {
                for (Piece occupant : occupants) {
                    if (occupant.getColor() != player.getColor()) {
                        occupant.resetToBase();
                        token.recordCapture(1);
                        this.captured = true;
                    }
                }
                targetCell.clearPieces();
            }
        }
    }

    @Override
    public int getPreviousPosition() { return previousPosition; }

    @Override
    public boolean hasCaptured() { return captured; }
}