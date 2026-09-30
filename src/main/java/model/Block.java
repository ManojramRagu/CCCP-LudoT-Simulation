package model;

import java.util.ArrayList;
import java.util.List;

public class Block implements BoardToken {
    private final List<Piece> pieces;
    private final PieceColor color;
    private int currentPosition;

    public Block(List<Piece> pieces) {
        if (pieces == null || pieces.size() < 2) {
            throw new IllegalArgumentException("A block must consist of at least 2 pieces.");
        }
        this.pieces = new ArrayList<>(pieces);
        this.color = pieces.get(0).getColor();
        this.currentPosition = pieces.get(0).getCurrentPosition();
    }

    @Override
    public PieceColor getColor() { return color; }

    @Override
    public int getCurrentPosition() { return currentPosition; }

    @Override
    public void setCurrentPosition(int position) {
        this.currentPosition = position;
        for (Piece p : pieces) {
            p.setCurrentPosition(position);
        }
    }

    @Override
    public MovementDirection getDirection() {
        return pieces.get(0).getDirection();
    }

    @Override
    public List<Piece> getComponentPieces() {
        return new ArrayList<>(pieces);
    }

    @Override
    public int getTokenSize() {
        return pieces.size();
    }

    @Override
    public boolean hasCapturedOpponent() {
        return pieces.get(0).hasCapturedOpponent();
    }

    @Override
    public void recordCapture(int amount) {
        // Rule T-8: Capture count increments for every piece participating in the capturing blockade
        for (Piece p : pieces) {
            p.recordCapture(amount);
        }
    }
}