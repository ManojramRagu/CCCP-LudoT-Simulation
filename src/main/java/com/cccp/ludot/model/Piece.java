package com.cccp.ludot.model;

public class Piece {
    private final String id;
    private final PieceColor color;
    private int currentPosition; // -1 represents inside Base
    private MovementDirection direction;
    private boolean inBase;
    private boolean completed;
    private boolean hasCapturedOpponent; // Rule T-2/T-7 requirement
    private int approachCellPasses; // Rule T-1 tracking for Counter-Clockwise

    public Piece(String id, PieceColor color) {
        this.id = id;
        this.color = color;
        this.currentPosition = -1;
        this.direction = MovementDirection.CLOCKWISE; // Default until coin toss
        this.inBase = true;
        this.completed = false;
        this.hasCapturedOpponent = false;
        this.approachCellPasses = 0;
    }

    // --- Getters &amp; Setters ---

    public String getId() {
        return id;
    }

    public PieceColor getColor() {
        return color;
    }

    public int getCurrentPosition() {
        return currentPosition;
    }

    public void setCurrentPosition(int currentPosition) {
        this.currentPosition = currentPosition;
    }

    public MovementDirection getDirection() {
        return direction;
    }

    public void setDirection(MovementDirection direction) {
        this.direction = direction;
    }

    public boolean isInBase() {
        return inBase;
    }

    public void setInBase(boolean inBase) {
        this.inBase = inBase;
    }

    public boolean isCompleted() {
        return completed;
    }

    public void setCompleted(boolean completed) {
        this.completed = completed;
    }

    public boolean hasCapturedOpponent() {
        return hasCapturedOpponent;
    }

    public void recordCapture() {
        this.hasCapturedOpponent = true;
    }

    public int getApproachCellPasses() {
        return approachCellPasses;
    }

    public void incrementApproachCellPasses() {
        this.approachCellPasses++;
    }

    @Override
    public String toString() {
        return color + "-" + id + " @ " + (inBase ? "BASE" : currentPosition);
    }
}