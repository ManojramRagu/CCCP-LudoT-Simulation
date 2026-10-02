package model;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class EnumCoverageTest {
    @Test
    void testMovementDirection() {
        assertNotNull(MovementDirection.valueOf("CLOCKWISE"));
        assertNotNull(MovementDirection.valueOf("COUNTER_CLOCKWISE"));
    }

    @Test
    void testPieceColor() {
        assertNotNull(PieceColor.valueOf("RED"));
        assertNotNull(PieceColor.valueOf("GREEN"));
    }

    @Test
    void testCellType() {
        assertNotNull(CellType.valueOf("STANDARD"));
        assertNotNull(CellType.valueOf("MYSTERY"));
    }
}
