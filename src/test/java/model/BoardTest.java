package model;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class BoardTest {

    private Board board;

    @BeforeEach
    void setUp() {
        board = new Board();
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
    @DisplayName("Board starting index legend compliance: Yellow=0, Blue=13, Red=26, Green=39")
    void testStartingIndicesLegendCompliance() {
        assertEquals(0, Board.getStartingIndex(PieceColor.YELLOW));
        assertEquals(13, Board.getStartingIndex(PieceColor.BLUE));
        assertEquals(26, Board.getStartingIndex(PieceColor.RED));
        assertEquals(39, Board.getStartingIndex(PieceColor.GREEN));
    }

    @Test
    @DisplayName("Board approach index compliance: Yellow=51, Blue=12, Red=25, Green=38")
    void testApproachIndicesCompliance() {
        assertEquals(51, Board.getApproachIndex(PieceColor.YELLOW));
        assertEquals(12, Board.getApproachIndex(PieceColor.BLUE));
        assertEquals(25, Board.getApproachIndex(PieceColor.RED));
        assertEquals(38, Board.getApproachIndex(PieceColor.GREEN));
    }

    @Test
    @DisplayName("Board track wrapping handles indices beyond 51 cleanly")
    void testTrackIndexWrapping() {
        Cell cell0 = board.getTrackCell(0);
        Cell cell52 = board.getTrackCell(52);
        assertSame(cell0, cell52);
    }

    @Test
    @DisplayName("Board track wrapping handles negative indices cleanly")
    void testNegativeTrackIndexWrapping() {
        Cell cell51 = board.getTrackCell(51);
        Cell cellNeg1 = board.getTrackCell(-1);
        assertSame(cell51, cellNeg1);
    }

    @Test
    @DisplayName("Board starting squares are assigned CellType.STARTING_X")
    void testStartingCellTypes() {
        assertEquals(CellType.STARTING_X, board.getTrackCell(0).getType());
        assertEquals(CellType.STARTING_X, board.getTrackCell(13).getType());
        assertEquals(CellType.STARTING_X, board.getTrackCell(26).getType());
        assertEquals(CellType.STARTING_X, board.getTrackCell(39).getType());
    }

    @Test
    @DisplayName("Board approach squares are assigned CellType.APPROACH")
    void testApproachCellTypes() {
        assertEquals(CellType.APPROACH, board.getTrackCell(51).getType());
        assertEquals(CellType.APPROACH, board.getTrackCell(12).getType());
        assertEquals(CellType.APPROACH, board.getTrackCell(25).getType());
        assertEquals(CellType.APPROACH, board.getTrackCell(38).getType());
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

    @Test
    @DisplayName("Board replacing mystery cell cleans up previous location")
    void testReplaceMysteryCell() {
        board.spawnMysteryCell(10);
        board.spawnMysteryCell(20);

        assertEquals(CellType.STANDARD, board.getTrackCell(10).getType());
        assertEquals(CellType.MYSTERY, board.getTrackCell(20).getType());
        assertEquals(20, board.getActiveMysteryCell().getIndex());
    }

    @Test
    @DisplayName("Board instance is non-null")
    void testBoardInstantiation() {
        assertNotNull(board);
    }
}
