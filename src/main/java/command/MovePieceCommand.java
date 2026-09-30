package command;

import model.Board;
import model.Cell;
import model.Piece;
import model.Player;

public class MovePieceCommand implements GameCommand {
    private final Piece piece;
    private final Player player;
    private final Board board;
    private final int steps;
    private int previousPosition;
    private boolean captured;

    public MovePieceCommand(Piece piece, Player player, Board board, int steps) {
        this.piece = piece;
        this.player = player;
        this.board = board;
        this.steps = steps;
        this.previousPosition = piece != null ? piece.getCurrentPosition() : -1;
        this.captured = false;
    }

    @Override
    public boolean isExecutable() {
        if (piece == null || player == null || board == null || steps <= 0) {
            return false;
        }
        if (piece.isInBase()) {
            return steps == 6;
        }
        return !piece.isCompleted();
    }

    @Override
    public void execute() {
        if (!isExecutable()) {
            return;
        }

        if (piece.isInBase() && steps == 6) {
            int startIdx = Board.getStartingIndex(player.getColor());
            piece.setInBase(false);
            piece.setCurrentPosition(startIdx);
            Cell startCell = board.getTrackCell(startIdx);
            handleCaptureOnCell(startCell);
            startCell.addPiece(piece);
            return;
        }

        int currentPos = piece.getCurrentPosition();
        int actualTarget = currentPos;

        for (int i = 1; i <= steps; i++) {
            int nextPos = (currentPos + i) % Board.TOTAL_TRACK_CELLS;
            Cell nextCell = board.getTrackCell(nextPos);
            if (nextCell.isBlocked() && nextCell.hasOpponentPiece(player.getColor())) {
                break;
            }
            actualTarget = nextPos;
        }

        if (actualTarget != currentPos) {
            Cell oldCell = board.getTrackCell(currentPos);
            oldCell.removePiece(piece);

            Cell targetCell = board.getTrackCell(actualTarget);
            handleCaptureOnCell(targetCell);

            piece.setCurrentPosition(actualTarget);
            targetCell.addPiece(piece);
        }
    }

    private void handleCaptureOnCell(Cell targetCell) {
        if (targetCell.hasOpponentPiece(player.getColor())) {
            for (Piece occupant : targetCell.getOccupyingPieces()) {
                if (occupant.getColor() != player.getColor()) {
                    occupant.setInBase(true);
                    occupant.setCurrentPosition(-1);
                    piece.recordCapture();
                    this.captured = true;
                }
            }
            targetCell.clearPieces();
        }
    }

    @Override
    public int getPreviousPosition() {
        return previousPosition;
    }

    @Override
    public boolean hasCaptured() {
        return captured;
    }
}
