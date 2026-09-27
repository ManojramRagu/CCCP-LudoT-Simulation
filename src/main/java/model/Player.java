package model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Player {
    public static final int PIECES_PER_PLAYER = 4;

    private final String name;
    private final PieceColor color;
    private final List<Piece> pieces;

    public Player(String name, PieceColor color) {
        this.name = name;
        this.color = color;
        this.pieces = new ArrayList<>(PIECES_PER_PLAYER);
        for (int i = 1; i <= PIECES_PER_PLAYER; i++) {
            pieces.add(new Piece(color.name() + "-" + i, color));
        }
    }

    public String getName() {
        return name;
    }

    public PieceColor getColor() {
        return color;
    }

    public List<Piece> getPieces() {
        return Collections.unmodifiableList(pieces);
    }

    public long getPiecesInBaseCount() {
        return pieces.stream().filter(Piece::isInBase).count();
    }

    public long getActivePiecesOnBoardCount() {
        return pieces.stream().filter(p -> !p.isInBase() && !p.isCompleted()).count();
    }

    public long getCompletedPiecesCount() {
        return pieces.stream().filter(Piece::isCompleted).count();
    }

    public boolean hasWon() {
        return getCompletedPiecesCount() == PIECES_PER_PLAYER;
    }
}
