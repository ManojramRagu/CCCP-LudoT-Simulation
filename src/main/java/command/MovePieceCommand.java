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
    private boolean captured;
    private int previousPosition;

    public MovePieceCommand(Piece piece, Player player, Board board, int steps) {
        this.piece = piece;
        this.player = player;
        this.board = board;
        this.steps = steps;
        this.captured = false;
        this.previousPosition = piece != null ? piece.getCurrentPosition() : -1;
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

        this.previousPosition = piece.getCurrentPosition();

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
        Cell oldCell = board.getTrackCell(currentPos);
        oldCell.removePiece(piece);

        int targetPos = (currentPos + steps) % Board.TOTAL_TRACK_CELLS;
        Cell targetCell = board.getTrackCell(targetPos);
        handleCaptureOnCell(targetCell);

        piece.setCurrentPosition(targetPos);
        targetCell.addPiece(piece);
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
    public boolean hasCaptured() {
        return captured;
    }

    @Override
    public int getPreviousPosition() {
        return previousPosition;
    }
}
