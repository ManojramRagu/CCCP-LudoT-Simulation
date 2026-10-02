package strategy;

import model.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class StrategyTest {

    private Board board;
    private Player redPlayer;
    private Player bluePlayer;
    private Player greenPlayer;
    private Player yellowPlayer;

    @BeforeEach
    void setUp() {
        Board.resetInstance();
        board = Board.getInstance();

        redPlayer = new Player("Red Player", PieceColor.RED, new AggressiveStrategy());
        bluePlayer = new Player("Blue Player", PieceColor.BLUE, new ChaoticStrategy());
        greenPlayer = new Player("Green Player", PieceColor.GREEN, new BlockingStrategy());
        yellowPlayer = new Player("Yellow Player", PieceColor.YELLOW, new OpportunisticStrategy());
    }

    @Test
    @DisplayName("Section 2.1.1: AggressiveStrategy exits base on rolling 6 when NO pieces are on track")
    void testAggressiveStrategyExitBaseWhenEmptyTrack() {
        Piece basePiece = redPlayer.getPieces().getFirst();
        AggressiveStrategy strategy = new AggressiveStrategy();
        BoardToken selected = strategy.selectTokenToMove(redPlayer, List.<BoardToken>of(basePiece), board, 6);
        assertEquals(basePiece, selected);
    }

    @Test
    @DisplayName("Section 2.1.2: Green Player prioritizes moving Blockades over single pieces")
    void testGreenPrioritizesBlocks() {
        Piece g1 = greenPlayer.getPieces().getFirst();
        Piece g2 = greenPlayer.getPieces().get(1);
        Piece g3 = greenPlayer.getPieces().get(2);

        g1.setCurrentPosition(10); g2.setCurrentPosition(10);
        g3.setCurrentPosition(15);
        Block block = new Block(List.of(g1, g2));

        BlockingStrategy strategy = new BlockingStrategy();
        BoardToken selected = strategy.selectTokenToMove(greenPlayer, List.of(block, g3), board, 4);

        assertInstanceOf(Block.class, selected); // Green chooses the block
    }

    @Test
    @DisplayName("Section 2.1.3: Yellow Player prioritizes pieces that have NOT captured an opponent yet (Rule T-7 Requirement)")
    void testYellowPrioritizesUncapturedPieces() {
        Piece y1 = yellowPlayer.getPieces().getFirst();
        Piece y2 = yellowPlayer.getPieces().get(1);

        y1.setCurrentPosition(10); y1.recordCapture(1); // Already captured, can go home
        y2.setCurrentPosition(15); // Has not captured, cannot go home

        OpportunisticStrategy strategy = new OpportunisticStrategy();
        BoardToken selected = strategy.selectTokenToMove(yellowPlayer, List.of(y1, y2), board, 4);

        assertEquals(y2, selected); // Yellow chooses piece that desperately needs a capture
    }

    @Test
    @DisplayName("Section 2.1.4: Blue Player strictly rotates tokens cyclically regardless of board position")
    void testBlueCyclicRotation() {
        Piece b1 = bluePlayer.getPieces().get(0);
        Piece b2 = bluePlayer.getPieces().get(1);
        b1.setCurrentPosition(10);
        b2.setCurrentPosition(15);

        ChaoticStrategy strategy = new ChaoticStrategy();

        // Simulating 4 consecutive turns
        assertEquals(b1, strategy.selectTokenToMove(bluePlayer, List.of(b1, b2), board, 3));
        assertEquals(b2, strategy.selectTokenToMove(bluePlayer, List.of(b1, b2), board, 3));
    }
}