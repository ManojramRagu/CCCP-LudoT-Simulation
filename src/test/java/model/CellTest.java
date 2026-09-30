package model;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class CellTest {

    private Cell cell;

    @BeforeEach
    void setUp() {
        cell = new Cell(10, CellType.STANDARD);
    }

    @Test
    @DisplayName("Cell initializes with correct index, type, and empty piece list")
    void testInitialCellState() {
        assertEquals(10, cell.getIndex());
        assertEquals(CellType.STANDARD, cell.getType());
        assertTrue(cell.getOccupyingPieces().isEmpty());
    }

    @Test
    @DisplayName("Cell allows adding and retrieving occupying pieces")
    void testAddPiece() {
        Piece piece = new Piece("R1", PieceColor.RED);
        cell.addPiece(piece);
        assertEquals(1, cell.getOccupyingPieces().size());
        assertTrue(cell.getOccupyingPieces().contains(piece));
    }

    @Test
    @DisplayName("Cell safely ignores null piece addition")
    void testAddNullPiece() {
        cell.addPiece(null);
        assertTrue(cell.getOccupyingPieces().isEmpty());
    }

    @Test
    @DisplayName("Cell allows removing pieces cleanly")
    void testRemovePiece() {
        Piece piece = new Piece("R1", PieceColor.RED);
        cell.addPiece(piece);
        cell.removePiece(piece);
        assertTrue(cell.getOccupyingPieces().isEmpty());
    }

    @Test
    @DisplayName("Cell creates defensive block when 2+ pieces of same color occupy it per Rule T-3")
    void testBlockDetectionSameColor() {
        Piece r1 = new Piece("R1", PieceColor.RED);
        Piece r2 = new Piece("R2", PieceColor.RED);

        cell.addPiece(r1);
        assertFalse(cell.isBlocked());

        cell.addPiece(r2);
        assertTrue(cell.isBlocked());
    }

    @Test
    @DisplayName("Cell does not create block when pieces belong to different colors")
    void testBlockDetectionDifferentColors() {
        Piece r1 = new Piece("R1", PieceColor.RED);
        Piece g1 = new Piece("G1", PieceColor.GREEN);

        cell.addPiece(r1);
        cell.addPiece(g1);
        assertFalse(cell.isBlocked());
    }

    @Test
    @DisplayName("Cell detects opponent piece presence accurately")
    void testHasOpponentPiece() {
        Piece r1 = new Piece("R1", PieceColor.RED);
        cell.addPiece(r1);

        assertFalse(cell.hasOpponentPiece(PieceColor.RED));
        assertTrue(cell.hasOpponentPiece(PieceColor.GREEN));
    }

    @Test
    @DisplayName("Cell clearPieces removes all occupying pieces")
    void testClearPieces() {
        cell.addPiece(new Piece("R1", PieceColor.RED));
        cell.addPiece(new Piece("R2", PieceColor.RED));
        cell.clearPieces();
        assertTrue(cell.getOccupyingPieces().isEmpty());
    }

    @Test
    @DisplayName("Cell type mutation updates cleanly for mystery cells")
    void testSetType() {
        cell.setType(CellType.MYSTERY);
        assertEquals(CellType.MYSTERY, cell.getType());
    }

    @Test
    @DisplayName("Cell unmodifiable list prevents external direct modification")
    void testUnmodifiableOccupyingPieces() {
        Piece r1 = new Piece("R1", PieceColor.RED);
        cell.addPiece(r1);
        assertThrows(UnsupportedOperationException.class, () -> {
            cell.getOccupyingPieces().clear();
        });
    }
}
