package model;

import java.util.List;

public interface BoardToken {
    PieceColor getColor();
    int getCurrentPosition();
    MovementDirection getDirection();
    void setCurrentPosition(int position);
    List<Piece> getComponentPieces();
    int getTokenSize();

    boolean hasCapturedOpponent();
    void recordCapture(int amount);
}