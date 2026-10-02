package model;

import strategy.PlayerStrategy;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Player {
    public static final int PIECES_PER_PLAYER = 4;

    private final String name;
    private final PieceColor color;
    private final List<Piece> pieces;
    private final PlayerStrategy strategy;

    private int consecutiveSixes;
    private int blueCyclicIndex;

    // This is the 3-argument constructor your test is looking for!
    public Player(String name, PieceColor color, PlayerStrategy strategy) {
        this.name = name;
        this.color = color;
        this.strategy = strategy;
        this.consecutiveSixes = 0;
        this.blueCyclicIndex = 0;

        this.pieces = new ArrayList<>(PIECES_PER_PLAYER);
        for (int i = 1; i <= PIECES_PER_PLAYER; i++) {
            pieces.add(new Piece(color.name().substring(0, 1) + i, color));
        }
    }

    public String getName() { return name; }
    public PieceColor getColor() { return color; }
    public PlayerStrategy getStrategy() { return strategy; }
    public List<Piece> getPieces() { return Collections.unmodifiableList(pieces); }

    public int getConsecutiveSixes() { return consecutiveSixes; }
    public void incrementConsecutiveSixes() { this.consecutiveSixes++; }
    public void resetConsecutiveSixes() { this.consecutiveSixes = 0; }
    public boolean hasThreeConsecutiveSixes() { return this.consecutiveSixes >= 3; }

    public int getAndIncrementCyclicIndex() {
        int currentIndex = blueCyclicIndex;
        blueCyclicIndex = (blueCyclicIndex + 1) % PIECES_PER_PLAYER;
        return currentIndex;
    }

    public long getCompletedPiecesCount() {
        return pieces.stream().filter(Piece::isCompleted).count();
    }

    public boolean hasWon() {
        return getCompletedPiecesCount() == PIECES_PER_PLAYER;
    }
}