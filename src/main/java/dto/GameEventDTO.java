package dto;

import model.PieceColor;

public record GameEventDTO(
        int turnNumber,
        PieceColor playerColor,
        int diceRoll,
        String tokenId,
        int startPosition,
        int endPosition,
        boolean capturedOpponent,
        String eventDescription
) {}