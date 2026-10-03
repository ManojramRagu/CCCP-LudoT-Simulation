package model;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class BoardTest {

    private Board board;

    @BeforeEach
    void setUp() {
        Board.resetInstance(); // Prevents test pollution
        board = Board.getInstance(); // Uses the Singleton
    }

    @Test
    @DisplayName("Board initializes with 52 standard track cells")
    void testTrackCellsCount() {
        assertNotNull(board.getTrackCell(0));
        assertNotNull(board.getTrackCell(51));
    }

    @Test
    @DisplayName("Board initializes Home Straights for all 4 colors with 5 cells each")
    void testHomeStraightsInitialization() {
        for (PieceColor color : PieceColor.values()) {
            List<Cell> straight = board.getHomeStraight(color);
            assertNotNull(straight);
            assertEquals(Board.HOME_STRAIGHT_LENGTH, straight.size());
            for (Cell cell : straight) {
                assertEquals(CellType.HOME_STRAIGHT, cell.getType());
            }
        }
    }

    @Test
    @DisplayName("Board track wrapping handles indices beyond 51 cleanly")
    void testTrackIndexWrapping() {
        Cell cell0 = board.getTrackCell(0);
        Cell cell52 = board.getTrackCell(52);
        assertSame(cell0, cell52);
    }

    @Test
    @DisplayName("Board spawns mystery cell at requested location according to Rule T-10")
    void testSpawnMysteryCell() {
        board.spawnMysteryCell(15);
        assertNotNull(board.getActiveMysteryCell());
        assertEquals(15, board.getActiveMysteryCell().getIndex());
        assertEquals(CellType.MYSTERY, board.getTrackCell(15).getType());
    }

    @Test
    @DisplayName("Board removes active mystery cell and restores original cell type")
    void testRemoveMysteryCell() {
        board.spawnMysteryCell(0);
        assertEquals(CellType.MYSTERY, board.getTrackCell(0).getType());

        board.removeMysteryCell();
        assertNull(board.getActiveMysteryCell());
        assertEquals(CellType.STARTING_X, board.getTrackCell(0).getType());
    }

    @ParameterizedTest
    @CsvSource({
            "0, YELLOW",
            "13, BLUE",
            "26, RED",
            "39, GREEN"
    })
    @DisplayName("Starting indices correctly defined per piece color")
    void testStartingIndices(int expectedIndex, PieceColor color) {
        assertEquals(expectedIndex, Board.getStartingIndex(color));
    }

    @ParameterizedTest
    @CsvSource({
            "50, YELLOW",
            "11, BLUE",
            "24, RED",
            "37, GREEN"
    })
    @DisplayName("Approach indices correctly defined per piece color")
    void testApproachIndices(int expectedIndex, PieceColor color) {
        assertEquals(expectedIndex, Board.getApproachIndex(color));
    }

    @Test
    @DisplayName("Alpha, Beta, Gamma cell calculations are exactly 9, 27, 46 from yellow approach")
    void testAlphaBetaGammaCalculations() {
        int yellowApproach = Board.YELLOW_APPROACH_INDEX;
        assertEquals((yellowApproach + 9) % Board.TOTAL_TRACK_CELLS, Board.ALPHA_CELL_INDEX);
        assertEquals((yellowApproach + 27) % Board.TOTAL_TRACK_CELLS, Board.BETA_CELL_INDEX);
        assertEquals((yellowApproach + 46) % Board.TOTAL_TRACK_CELLS, Board.GAMMA_CELL_INDEX);
    }
}