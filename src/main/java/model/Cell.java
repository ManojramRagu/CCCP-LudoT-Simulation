package model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Cell {
    private final int index;
    private CellType type;
    private final List<Piece> occupyingPieces;

    public Cell(int index, CellType type) {
        this.index = index;
        this.type = type;
        this.occupyingPieces = new ArrayList<>();
    }

    public int getIndex() { return index; }
    public CellType getType() { return type; }
    public void setType(CellType type) { this.type = type; }

    public List<Piece> getOccupyingPieces() {
        return Collections.unmodifiableList(occupyingPieces);
    }

    public void addPiece(Piece piece) {
        if (piece != null && !occupyingPieces.contains(piece)) {
            occupyingPieces.add(piece);
        }
    }

    public void removePiece(Piece piece) {
        occupyingPieces.remove(piece);
    }

    public boolean hasOpponentPiece(PieceColor color) {
        for (Piece p : occupyingPieces) {
            if (p.getColor() != color) {
                return true;
            }
        }
        return false;
    }

    public boolean isBlocked() {
        if (occupyingPieces.size() < 2) return false;
        PieceColor firstColor = occupyingPieces.get(0).getColor();
        for (Piece p : occupyingPieces) {
            if (p.getColor() != firstColor) return false;
        }
        return true;
    }

    public void clearPieces() {
        occupyingPieces.clear();
    }
}