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

    @Test
    @DisplayName("Rule T-4: Block with opposing directions calculates direction by longest distance from home")
    void testBlockOpposingDirectionsLongestDistance() {
        Piece p1 = new Piece("R1", PieceColor.RED);
        Piece p2 = new Piece("R2", PieceColor.RED);
        p1.setDirection(MovementDirection.CLOCKWISE);
        p2.setDirection(MovementDirection.COUNTER_CLOCKWISE);
        
        // Red approach is 24.
        // At pos 20: CW distance is 24-20=4. CCW distance is (20-24+52)%52=48. CCW is longer.
        p1.setCurrentPosition(20);
        p2.setCurrentPosition(20);
        Block block1 = new Block(Arrays.asList(p1, p2));
        assertEquals(MovementDirection.COUNTER_CLOCKWISE, block1.getDirection());

        // At pos 30: CW distance is (24-30+52)%52=46. CCW distance is 30-24=6. CW is longer.
        p1.setCurrentPosition(30);
        p2.setCurrentPosition(30);
        Block block2 = new Block(Arrays.asList(p1, p2));
        assertEquals(MovementDirection.CLOCKWISE, block2.getDirection());
    }
}