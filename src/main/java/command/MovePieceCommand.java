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

        Piece firstPiece = token.getComponentPieces().getFirst();
        if (firstPiece.isCompleted()) return false;

        if (firstPiece.isInBase()) return diceRoll == 6;

        int actualSteps = token.getTokenSize() > 1 ? (diceRoll / token.getTokenSize()) : diceRoll;
        if (actualSteps <= 0) return false;

        if (firstPiece.getState() == PieceState.HOME_STRAIGHT) {
            return (token.getCurrentPosition() + actualSteps) <= Board.HOME_STRAIGHT_LENGTH;
        }

        if (firstPiece.getState() == PieceState.STANDARD_TRACK && token.hasCapturedOpponent()) {
            int approach = Board.getApproachIndex(player.getColor());

            int distToApproach = token.getDirection() == MovementDirection.CLOCKWISE
                    ? (approach - token.getCurrentPosition() + Board.TOTAL_TRACK_CELLS) % Board.TOTAL_TRACK_CELLS
                    : (token.getCurrentPosition() - approach + Board.TOTAL_TRACK_CELLS) % Board.TOTAL_TRACK_CELLS;

            // IntelliJ Warning Fix: Simplified nested if statements into a single return boolean
            return actualSteps <= distToApproach || (actualSteps - distToApproach) <= Board.HOME_STRAIGHT_LENGTH + 1;
        }
        return true;
    }

    @Override
    public void execute() {
        if (!isExecutable()) return;

        Piece firstPiece = token.getComponentPieces().getFirst();
        int actualSteps = token.getTokenSize() > 1 ? (diceRoll / token.getTokenSize()) : diceRoll;

        if (firstPiece.isInBase()) {
            int startIdx = Board.getStartingIndex(player.getColor());
            token.setCurrentPosition(startIdx);
            Cell startCell = board.getTrackCell(startIdx);
            handleCaptureOnCell(startCell);
            for(Piece p : token.getComponentPieces()) {
                p.setInBase(false);
                p.setState(PieceState.STANDARD_TRACK);
                startCell.addPiece(p);
            }
            return;
        }

        if (firstPiece.getState() == PieceState.HOME_STRAIGHT) {
            int targetPos = token.getCurrentPosition() + actualSteps;
            if (targetPos == Board.HOME_STRAIGHT_LENGTH) {
                completeToken();
            } else {
                token.setCurrentPosition(targetPos);
            }
            return;
        }

        int currentPos = token.getCurrentPosition();
        int actualTarget = currentPos;
        int approachIndex = Board.getApproachIndex(player.getColor());
        boolean enteringHome = false;
        int homeStraightTarget = -1;

        for (int i = 1; i <= actualSteps; i++) {
            int prevPos = token.getDirection() == MovementDirection.CLOCKWISE
                    ? (currentPos + i - 1) % Board.TOTAL_TRACK_CELLS
                    : (currentPos - i + 1 + Board.TOTAL_TRACK_CELLS) % Board.TOTAL_TRACK_CELLS;

            int nextPos = token.getDirection() == MovementDirection.CLOCKWISE
                    ? (currentPos + i) % Board.TOTAL_TRACK_CELLS
                    : (currentPos - i + Board.TOTAL_TRACK_CELLS) % Board.TOTAL_TRACK_CELLS;

            if (prevPos == approachIndex && token.hasCapturedOpponent()) {
                enteringHome = true;
                int remainingSteps = actualSteps - i + 1;
                homeStraightTarget = remainingSteps - 1;
                break;
            }

            Cell nextCell = board.getTrackCell(nextPos);

            if (nextCell.isBlocked() && nextCell.hasOpponentPiece(player.getColor())) {
                if (i == actualSteps && nextCell.getOccupyingPieces().size() == token.getTokenSize()) {
                    actualTarget = nextPos;
                }
                break;
            }
            actualTarget = nextPos;
        }

        Cell oldCell = board.getTrackCell(currentPos);
        for(Piece p : token.getComponentPieces()) oldCell.removePiece(p);

        if (enteringHome) {
            if (homeStraightTarget == Board.HOME_STRAIGHT_LENGTH) {
                completeToken();
            } else {
                token.setCurrentPosition(homeStraightTarget);
                for(Piece p : token.getComponentPieces()) {
                    p.setState(PieceState.HOME_STRAIGHT);
                }
            }
        } else if (actualTarget != currentPos) {
            Cell targetCell = board.getTrackCell(actualTarget);
            handleCaptureOnCell(targetCell);
            token.setCurrentPosition(actualTarget);
            for(Piece p : token.getComponentPieces()) targetCell.addPiece(p);
        } else {
            for(Piece p : token.getComponentPieces()) oldCell.addPiece(p);
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

    private void completeToken() {
        token.setCurrentPosition(-2);
        for (Piece p : token.getComponentPieces()) {
            p.setCompleted(true);
            p.setState(PieceState.COMPLETED);
        }
    }

    @Override
    public int getPreviousPosition() { return previousPosition; }

    @Override
    public boolean hasCaptured() { return captured; }
}