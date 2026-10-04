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
    private String capturedOpponentName;
    private PieceColor capturedOpponentColor;

    public MovePieceCommand(BoardToken token, Player player, Board board, int diceRoll) {
        this.token = token;
        this.player = player;
        this.board = board;
        this.diceRoll = diceRoll;
        this.previousPosition = token != null ? token.getCurrentPosition() : -1;
        this.captured = false;
        this.capturedOpponentName = "";
        this.capturedOpponentColor = null;
    }

    @Override
    public boolean isExecutable() {
        if (token == null || player == null || board == null || diceRoll <= 0) return false;

        Piece firstPiece = token.getComponentPieces().getFirst();
        if (firstPiece.isCompleted()) return false;
        
        if (firstPiece.isRestricted()) return false;

        if (firstPiece.isInBase()) return diceRoll == 6;

        int actualSteps = token.getTokenSize() > 1 ? (diceRoll / token.getTokenSize()) : diceRoll;
        if (firstPiece.isEnergized()) actualSteps *= 2;
        if (firstPiece.isSick()) actualSteps /= 2;
        if (actualSteps <= 0) return false;

        if (firstPiece.getState() == PieceState.HOME_STRAIGHT) {
            return (token.getCurrentPosition() + actualSteps) <= Board.HOME_STRAIGHT_LENGTH;
        }

        if (firstPiece.getState() == PieceState.STANDARD_TRACK && token.hasCapturedOpponent()) {
            int approach = Board.getApproachIndex(player.getColor());
            int distToApproach = token.getDirection() == MovementDirection.CLOCKWISE
                    ? (approach - token.getCurrentPosition() + Board.TOTAL_TRACK_CELLS) % Board.TOTAL_TRACK_CELLS
                    : (token.getCurrentPosition() - approach + Board.TOTAL_TRACK_CELLS) % Board.TOTAL_TRACK_CELLS;

            if (token.getDirection() == MovementDirection.COUNTER_CLOCKWISE && firstPiece.getApproachPassCount() < 1) {
                return true;
            }

            return actualSteps <= distToApproach || (actualSteps - distToApproach) <= (Board.HOME_STRAIGHT_LENGTH + 1);
        }
        return true;
    }

    @Override
    public void execute() {
        if (!isExecutable()) return;

        Piece firstPiece = token.getComponentPieces().getFirst();
        int actualSteps = token.getTokenSize() > 1 ? (diceRoll / token.getTokenSize()) : diceRoll;
        if (firstPiece.isEnergized()) actualSteps *= 2;
        if (firstPiece.isSick()) actualSteps /= 2;

        if (firstPiece.isInBase()) {
            int startIdx = Board.getStartingIndex(player.getColor());
            token.setCurrentPosition(startIdx);
            Cell startCell = board.getTrackCell(startIdx);
            handleCaptureOnCell(startCell, startIdx);

            CoinToss toss = new CoinToss();
            MovementDirection chosenDirection = toss.flip();

            for(Piece p : token.getComponentPieces()) {
                p.setInBase(false);
                p.setState(PieceState.STANDARD_TRACK);
                p.setDirection(chosenDirection);
                p.setOriginalDirection(chosenDirection);
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

            if (prevPos == approachIndex) {
                if (token.getDirection() == MovementDirection.COUNTER_CLOCKWISE) {
                    firstPiece.incrementApproachPassCount();
                    if (firstPiece.getApproachPassCount() >= 2 && token.hasCapturedOpponent()) {
                        enteringHome = true;
                        homeStraightTarget = (actualSteps - i + 1) - 1;
                        break;
                    }
                } else if (token.hasCapturedOpponent()) {
                    enteringHome = true;
                    homeStraightTarget = (actualSteps - i + 1) - 1;
                    break;
                }
            }

            Cell nextCell = board.getTrackCell(nextPos);

            if (nextCell.isBlocked() && nextCell.hasOpponentPiece(player.getColor())) {
                if (i == actualSteps && nextCell.getOccupyingPieces().size() == token.getTokenSize()) {
                    actualTarget = nextPos;
                } else {
                    String blockPieceName = nextCell.getOccupyingPieces().getFirst().getId();
                    String blockColorName = nextCell.getOccupyingPieces().getFirst().getColor().name().toLowerCase();
                    String myPieceName = token.getComponentPieces().getFirst().getId();
                    int intendedTarget = token.getDirection() == MovementDirection.CLOCKWISE
                            ? (currentPos + actualSteps) % Board.TOTAL_TRACK_CELLS
                            : (currentPos - actualSteps + Board.TOTAL_TRACK_CELLS) % Board.TOTAL_TRACK_CELLS;
                    System.out.println(String.format("[%s] piece %s is blocked from moving from L%d to L%d by [%s] piece %s.", 
                        player.getColor().name().toLowerCase(), myPieceName, currentPos, intendedTarget, blockColorName, blockPieceName));
                    
                    long piecesOnBoard = player.getPieces().stream().filter(p -> !p.isInBase() && !p.isCompleted()).count();
                    if (piecesOnBoard <= token.getTokenSize()) {
                        System.out.println(String.format("[%s] does not have other pieces in the board to move instead of the blocked piece.", player.getColor().name().toLowerCase()));
                    }
                    
                    if (actualTarget == currentPos) {
                        System.out.println("Ignoring the throw and moving on to the next player.");
                    } else {
                        System.out.println(String.format("Moved the piece to square L%d which is the cell before the block.", actualTarget));
                    }
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
            handleCaptureOnCell(targetCell, actualTarget);
            token.setCurrentPosition(actualTarget);
            for(Piece p : token.getComponentPieces()) targetCell.addPiece(p);
        } else {
            for(Piece p : token.getComponentPieces()) oldCell.addPiece(p);
        }
    }

    private void handleCaptureOnCell(Cell targetCell, int pos) {
        if (targetCell.hasOpponentPiece(player.getColor())) {
            List<Piece> occupants = targetCell.getOccupyingPieces();
            if (occupants.size() == token.getTokenSize()) {
                capturedOpponentName = occupants.getFirst().getId();
                capturedOpponentColor = occupants.getFirst().getColor();
                List<Piece> toRemove = new java.util.ArrayList<>();
                for (Piece occupant : occupants) {
                    if (occupant.getColor() != player.getColor()) {
                        occupant.resetToBase();
                        token.recordCapture(1);
                        this.captured = true;
                        toRemove.add(occupant);
                    }
                }
                for (Piece p : toRemove) {
                    targetCell.removePiece(p);
                }
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
    
    @Override
    public String getCapturedOpponentName() { return capturedOpponentName; }
    @Override
    public PieceColor getCapturedOpponentColor() { return capturedOpponentColor; }
}