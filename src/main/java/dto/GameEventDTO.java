package dto;

import model.PieceColor;

public record GameEventDTO(
    int turnNumber,
    PieceColor playerColor,
    int diceRoll,
    String pieceMovedId,
    int startPosition,
    int endPosition,
    boolean capturedOpponent,
    String eventDescription
) {
    public int turnNumber() { return turnNumber; }
    public PieceColor playerColor() { return playerColor; }
    public int diceRoll() { return diceRoll; }
    public String pieceMovedId() { return pieceMovedId; }
    public int startPosition() { return startPosition; }
    public int endPosition() { return endPosition; }
    public boolean capturedOpponent() { return capturedOpponent; }
    public String eventDescription() { return eventDescription; }
    public String description() { return eventDescription; }
}
