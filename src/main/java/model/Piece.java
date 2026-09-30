package model;

public class Piece {
    private final String id;
    private final PieceColor color;
    private int currentPosition;
    private MovementDirection direction;
    private boolean inBase;
    private boolean completed;
    private boolean hasCapturedOpponent;
    private int approachCellPasses;

    private PieceState state;
    private int energizedRounds;
    private int sickRounds;
    private int restrictedRounds;

    public Piece(String id, PieceColor color) {
        this.id = id;
        this.color = color;
        this.currentPosition = -1;
        this.direction = MovementDirection.CLOCKWISE;
        this.inBase = true;
        this.completed = false;
        this.hasCapturedOpponent = false;
        this.approachCellPasses = 0;
        this.state = PieceState.BASE;
        this.energizedRounds = 0;
        this.sickRounds = 0;
        this.restrictedRounds = 0;
    }

    public String getId() { return id; }
    public PieceColor getColor() { return color; }
    public int getCurrentPosition() { return currentPosition; }
    
    public void setCurrentPosition(int currentPosition) {
        this.currentPosition = currentPosition;
        if (currentPosition != -1 && inBase) {
            this.inBase = false;
            this.state = PieceState.STANDARD_TRACK;
        }
    }

    public MovementDirection getDirection() { return direction; }
    public void setDirection(MovementDirection direction) { this.direction = direction; }

    public boolean isInBase() { return inBase; }
    public void setInBase(boolean inBase) {
        this.inBase = inBase;
        if (inBase) {
            this.state = PieceState.BASE;
            this.currentPosition = -1;
        } else if (this.state == PieceState.BASE) {
            this.state = PieceState.STANDARD_TRACK;
        }
    }

    public boolean isCompleted() { return completed; }
    public void setCompleted(boolean completed) {
        this.completed = completed;
        if (completed) {
            this.state = PieceState.COMPLETED;
            this.inBase = false;
        }
    }

    public PieceState getState() { return state; }
    public void setState(PieceState state) { this.state = state; }

    public void setInHomeStraight(boolean inHomeStraight) {
        if (inHomeStraight) {
            this.state = PieceState.HOME_STRAIGHT;
            this.inBase = false;
        }
    }

    public boolean hasCapturedOpponent() { return hasCapturedOpponent; }
    public void recordCapture() { this.hasCapturedOpponent = true; }

    public int getApproachCellPasses() { return approachCellPasses; }
    public void incrementApproachCellPasses() { this.approachCellPasses++; }

    public boolean isEnergized() { return energizedRounds > 0; }
    public int getEnergizedRounds() { return energizedRounds; }
    public void setEnergizedRounds(int rounds) { this.energizedRounds = rounds; }

    public boolean isSick() { return sickRounds > 0; }
    public int getSickRounds() { return sickRounds; }
    public void setSickRounds(int rounds) { this.sickRounds = rounds; }

    public boolean isRestricted() { return restrictedRounds > 0; }
    public int getRestrictedRounds() { return restrictedRounds; }
    public void setRestrictedRounds(int rounds) { this.restrictedRounds = rounds; }

    public void resetToBase() {
        setInBase(true);
        this.hasCapturedOpponent = false;
        this.approachCellPasses = 0;
        this.energizedRounds = 0;
        this.sickRounds = 0;
        this.restrictedRounds = 0;
    }

    public void decrementStatusEffects() {
        if (energizedRounds > 0) energizedRounds--;
        if (sickRounds > 0) sickRounds--;
        if (restrictedRounds > 0) restrictedRounds--;
    }

    @Override
    public String toString() {
        return id + " (" + color + ", Pos: " + currentPosition + ", Base: " + inBase + ")";
    }
}
