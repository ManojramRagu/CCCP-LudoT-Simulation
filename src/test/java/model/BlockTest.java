package model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import java.util.Arrays;
import static org.junit.jupiter.api.Assertions.*;

class BlockTest {

    @Test
    @DisplayName("Block initialization aggregates multiple pieces into a single token")
    void testBlockInitialization() {
        Piece p1 = new Piece("G1", PieceColor.GREEN);
        Piece p2 = new Piece("G2", PieceColor.GREEN);
        p1.setCurrentPosition(5);
        p2.setCurrentPosition(5);

        Block block = new Block(Arrays.asList(p1, p2));

        assertEquals(PieceColor.GREEN, block.getColor());
        assertEquals(5, block.getCurrentPosition());
        assertEquals(2, block.getTokenSize());
        assertEquals(2, block.getComponentPieces().size());
    }

    @Test
    @DisplayName("Block applies capture records to all component pieces per Rule T-8")
    void testBlockCaptureRecording() {
        Piece p1 = new Piece("G1", PieceColor.GREEN);
        Piece p2 = new Piece("G2", PieceColor.GREEN);
        Block block = new Block(Arrays.asList(p1, p2));

        block.recordCapture(1);

        assertTrue(p1.hasCapturedOpponent());
        assertTrue(p2.hasCapturedOpponent());
    }
}