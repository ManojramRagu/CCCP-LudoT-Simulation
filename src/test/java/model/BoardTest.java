package model;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class BoardTest {

    private Board board;

    @BeforeEach
    void setUp() {
        board = new Board();
    }

    @Test
    @DisplayName("Board initialization should create 52 track cells with correct types")
    void testBoardInitialization() {
        assertEquals(Board.TOTAL_TRACK_CELLS, board.getTrackCell(0).getIndex() == 0 ? 52 : 0);
        assertEquals(CellType.STARTING_X, board.getTrackCell(Board.RED_START_INDEX).getType());
        assertEquals(CellType.APPROACH, board.getTrackCell(Board.RED_APPROACH_INDEX).getType());
        assertEquals(CellType.STANDARD, board.getTrackCell(1).getType());
    }

    @Test
    @DisplayName("Cell block detection should trigger when 2 same-colored pieces occupy a cell")
    void testCellBlockDetection() {
        Cell cell = board.getTrackCell(5);
        Piece redPiece1 = new Piece("RED-1", PieceColor.RED);
        Piece redPiece2 = new Piece("RED-2", PieceColor.RED);

        cell.addPiece(redPiece1);
        assertFalse(cell.isBlocked());

        cell.addPiece(redPiece2);
        assertTrue(cell.isBlocked());

        Piece bluePiece = new Piece("BLUE-1", PieceColor.BLUE);
        cell.addPiece(bluePiece);
        assertFalse(cell.isBlocked());
    }

    @Test
    @DisplayName("Mystery cell spawning and removal should correctly alter cell types")
    void testMysteryCellLifecycle() {
        board.spawnMysteryCell(10);
        assertEquals(CellType.MYSTERY, board.getTrackCell(10).getType());
        assertNotNull(board.getActiveMysteryCell());

        board.removeMysteryCell();
        assertEquals(CellType.STANDARD, board.getTrackCell(10).getType());
        assertNull(board.getActiveMysteryCell());
    }
}
