package model;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

public class Board {
    private static Board instance;

    public static final int TOTAL_TRACK_CELLS = 52;
    public static final int HOME_STRAIGHT_LENGTH = 5;

    public static final int YELLOW_START_INDEX = 0;
    public static final int BLUE_START_INDEX = 13;
    public static final int RED_START_INDEX = 26;
    public static final int GREEN_START_INDEX = 39;

    public static final int YELLOW_APPROACH_INDEX = 51;
    public static final int BLUE_APPROACH_INDEX = 12;
    public static final int RED_APPROACH_INDEX = 25;
    public static final int GREEN_APPROACH_INDEX = 38;

    // Based on Rule T-11: 9th, 27th, and 46th from Yellow Approach (cell 51 = 0)
    public static final int ALPHA_CELL_INDEX = 8;
    public static final int BETA_CELL_INDEX = 26;
    public static final int GAMMA_CELL_INDEX = 45;

    private final List<Cell> trackCells;
    private final Map<PieceColor, List<Cell>> homeStraights;
    private Cell activeMysteryCell;

    // Private constructor prevents external instantiation
    private Board() {
        this.trackCells = new ArrayList<>(TOTAL_TRACK_CELLS);
        this.homeStraights = new EnumMap<>(PieceColor.class);
        initializeBoard();
    }

    // Thread-safe Singleton accessor
    public static synchronized Board getInstance() {
        if (instance == null) {
            instance = new Board();
        }
        return instance;
    }

    // For testing purposes: allows resetting the board state between JUnit tests
    public static synchronized void resetInstance() {
        instance = null;
    }

    private void initializeBoard() {
        for (int i = 0; i < TOTAL_TRACK_CELLS; i++) {
            trackCells.add(new Cell(i, determineCellType(i)));
        }

        for (PieceColor color : PieceColor.values()) {
            List<Cell> straight = new ArrayList<>(HOME_STRAIGHT_LENGTH);
            for (int i = 0; i < HOME_STRAIGHT_LENGTH; i++) {
                straight.add(new Cell(i, CellType.HOME_STRAIGHT));
            }
            homeStraights.put(color, straight);
        }
    }

    private CellType determineCellType(int index) {
        if (index == YELLOW_START_INDEX || index == BLUE_START_INDEX ||
                index == RED_START_INDEX || index == GREEN_START_INDEX) {
            return CellType.STARTING_X;
        }
        if (index == YELLOW_APPROACH_INDEX || index == BLUE_APPROACH_INDEX ||
                index == RED_APPROACH_INDEX || index == GREEN_APPROACH_INDEX) {
            return CellType.APPROACH;
        }
        return CellType.STANDARD;
    }

    public Cell getTrackCell(int index) {
        int normalizedIndex = (index % TOTAL_TRACK_CELLS + TOTAL_TRACK_CELLS) % TOTAL_TRACK_CELLS;
        return trackCells.get(normalizedIndex);
    }

    public List<Cell> getHomeStraight(PieceColor color) {
        return homeStraights.get(color);
    }

    public void spawnMysteryCell(int index) {
        removeMysteryCell();
        Cell target = getTrackCell(index);
        target.setType(CellType.MYSTERY);
        activeMysteryCell = target;
    }

    public void removeMysteryCell() {
        if (activeMysteryCell != null) {
            activeMysteryCell.setType(determineCellType(activeMysteryCell.getIndex()));
            activeMysteryCell = null;
        }
    }

    public Cell getActiveMysteryCell() {
        return activeMysteryCell;
    }

    public static int getStartingIndex(PieceColor color) {
        return switch (color) {
            case YELLOW -> YELLOW_START_INDEX;
            case BLUE -> BLUE_START_INDEX;
            case RED -> RED_START_INDEX;
            case GREEN -> GREEN_START_INDEX;
        };
    }
}