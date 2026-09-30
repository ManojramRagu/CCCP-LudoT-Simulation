package model;

import java.util.Collections;
import java.util.List;

public class Piece implements BoardToken {
    private final String id;
    private final PieceColor color;
    private int currentPosition;
    private MovementDirection direction;
    private boolean inBase;
    private boolean completed;
    private int capturesMade;

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
        this.capturesMade = 0;
        this.state = PieceState.BASE;
    }

    public String getId() { return id; }

    @Override
    public PieceColor getColor() { return color; }

    @Override
    public int getCurrentPosition() { return currentPosition; }

    @Override
    public void setCurrentPosition(int currentPosition) {
        this.currentPosition = currentPosition;
        if (currentPosition != -1 && inBase) {
            this.inBase = false;
            this.state = PieceState.STANDARD_TRACK;
        }
    }

    @Override
    public MovementDirection getDirection() { return direction; }

    public void setDirection(MovementDirection direction) { this.direction = direction; }

    @Override
    public List<Piece> getComponentPieces() {
        return Collections.singletonList(this);
    }

    @Override
    public int getTokenSize() { return 1; }

    @Override
    public boolean hasCapturedOpponent() { return capturesMade > 0; }

    @Override
    public void recordCapture(int amount) { this.capturesMade += amount; }

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

    public boolean isEnergized() { return energizedRounds > 0; }
    public void setEnergizedRounds(int rounds) { this.energizedRounds = rounds; }

    public boolean isSick() { return sickRounds > 0; }
    public void setSickRounds(int rounds) { this.sickRounds = rounds; }

    public boolean isRestricted() { return restrictedRounds > 0; }
    public void setRestrictedRounds(int rounds) { this.restrictedRounds = rounds; }

    public void resetToBase() {
        setInBase(true);
        this.capturesMade = 0;
        this.energizedRounds = 0;
        this.sickRounds = 0;
        this.restrictedRounds = 0;
        this.direction = MovementDirection.CLOCKWISE;
    }

    public void decrementStatusEffects() {
        if (energizedRounds > 0) energizedRounds--;
        if (sickRounds > 0) sickRounds--;
        if (restrictedRounds > 0) restrictedRounds--;
    }
}