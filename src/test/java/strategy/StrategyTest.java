package strategy;

import model.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

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

    @Test
    @DisplayName("Red (Aggressive): Prioritizes capture closest to opponent's home")
    void testRedCapturePriorityClosestToHome() {
        Piece r1 = redPlayer.getPieces().get(0);
        Piece r2 = redPlayer.getPieces().get(1);
        r1.setCurrentPosition(10); r1.setDirection(MovementDirection.CLOCKWISE); r1.setInBase(false); r1.setState(PieceState.STANDARD_TRACK);
        r2.setCurrentPosition(20); r2.setDirection(MovementDirection.CLOCKWISE); r2.setInBase(false); r2.setState(PieceState.STANDARD_TRACK);

        Player greenPlayerOpp = new Player("Green", PieceColor.GREEN, null);
        Piece g1 = greenPlayerOpp.getPieces().get(0);
        Piece g2 = greenPlayerOpp.getPieces().get(1);
        
        g1.setCurrentPosition(15); g1.setDirection(MovementDirection.CLOCKWISE); g1.setInBase(false); g1.setState(PieceState.STANDARD_TRACK);
        g2.setCurrentPosition(25); g2.setDirection(MovementDirection.CLOCKWISE); g2.setInBase(false); g2.setState(PieceState.STANDARD_TRACK);

        board.getTrackCell(15).addPiece(g1);
        board.getTrackCell(25).addPiece(g2);

        AggressiveStrategy strategy = new AggressiveStrategy();
        // Roll 5 allows r1->15 (captures g1) OR r2->25 (captures g2)
        // Green approach is 37. g1 is at 15 (dist=22). g2 is at 25 (dist=12).
        // r2 capturing g2 is closest to Green's home.
        BoardToken selected = strategy.selectTokenToMove(redPlayer, List.of(r1, r2), board, 5);
        assertEquals(r2, selected);
    }

    @Test
    @DisplayName("Red (Aggressive): Exits base if piece on track but no captures possible")
    void testRedExitsBaseIfNoCaptures() {
        Piece r1 = redPlayer.getPieces().get(0);
        Piece r2 = redPlayer.getPieces().get(1);
        r1.setCurrentPosition(10); r1.setInBase(false); r1.setState(PieceState.STANDARD_TRACK);
        r2.setCurrentPosition(-1); r2.setInBase(true);

        AggressiveStrategy strategy = new AggressiveStrategy();
        BoardToken selected = strategy.selectTokenToMove(redPlayer, List.of(r1, r2), board, 6);
        assertEquals(r2, selected); // No captures available, so pick base piece on 6
    }

    @Test
    @DisplayName("Green (Blocking): Prioritizes creating block over exiting base")
    void testGreenCreatesBlockOverBaseExit() {
        Piece g1 = greenPlayer.getPieces().get(0);
        Piece g2 = greenPlayer.getPieces().get(1);
        Piece g3 = greenPlayer.getPieces().get(2);
        
        g1.setCurrentPosition(10); g1.setInBase(false); g1.setState(PieceState.STANDARD_TRACK); g1.setDirection(MovementDirection.CLOCKWISE);
        g2.setCurrentPosition(16); g2.setInBase(false); g2.setState(PieceState.STANDARD_TRACK);
        board.getTrackCell(16).addPiece(g2);
        g3.setCurrentPosition(-1); g3.setInBase(true);

        BlockingStrategy strategy = new BlockingStrategy();
        // Roll 6 allows g1 to move to 16 (creating a block with g2) OR g3 to exit base.
        BoardToken selected = strategy.selectTokenToMove(greenPlayer, List.of(g1, g3), board, 6);
        assertEquals(g1, selected);
    }

    @Test
    @DisplayName("Yellow (Opportunistic): Validates capture actually happens, else moves piece closest to its home")
    void testYellowCaptureValidationAndFallback() {
        Piece y1 = yellowPlayer.getPieces().get(0);
        Piece y2 = yellowPlayer.getPieces().get(1);
        
        y1.setCurrentPosition(10); y1.setInBase(false); y1.setState(PieceState.STANDARD_TRACK); y1.setDirection(MovementDirection.CLOCKWISE);
        y2.setCurrentPosition(40); y2.setInBase(false); y2.setState(PieceState.STANDARD_TRACK); y2.setDirection(MovementDirection.CLOCKWISE);
        
        // Neither has captures.
        // Roll 4 -> y1 goes to 14, y2 goes to 44. No opponents at 14 or 44.
        OpportunisticStrategy strategy = new OpportunisticStrategy();
        BoardToken selected = strategy.selectTokenToMove(yellowPlayer, List.of(y1, y2), board, 4);
        
        // Fallback: move piece closest to its home (Yellow approach is 50).
        // y2 at 40 (dist 10) is closer than y1 at 10 (dist 40).
        assertEquals(y2, selected);
    }

    @Test
    @DisplayName("Blue (Chaotic): Avoids mystery cell when CW by picking NEXT piece in cycle")
    void testBlueAvoidsMysteryCell() {
        Piece b1 = bluePlayer.getPieces().get(0);
        Piece b2 = bluePlayer.getPieces().get(1);
        b1.setCurrentPosition(10); b1.setInBase(false); b1.setState(PieceState.STANDARD_TRACK); b1.setDirection(MovementDirection.CLOCKWISE);
        b2.setCurrentPosition(20); b2.setInBase(false); b2.setState(PieceState.STANDARD_TRACK); b2.setDirection(MovementDirection.CLOCKWISE);

        board.spawnMysteryCell(15);
        ChaoticStrategy strategy = new ChaoticStrategy();
        
        // Target cyclic is 0 (b1). Roll is 5. b1 goes to 15 (Mystery!). It should avoid it and pick b2.
        BoardToken selected = strategy.selectTokenToMove(bluePlayer, List.of(b1, b2), board, 5);
        assertEquals(b2, selected);
    }

    @ParameterizedTest
    @CsvSource({
            "6, -1",
            "5, 10"
    })
    @DisplayName("Aggressive picks from base on 6 or track otherwise")
    void testAggressivePicksFromBaseOrTrack(int roll, int position) {
        Piece piece1 = redPlayer.getPieces().get(0);
        piece1.setCurrentPosition(position);
        piece1.setInBase(position == -1);
        if (position != -1) piece1.setState(PieceState.STANDARD_TRACK);

        List<BoardToken> tokens = Collections.singletonList(piece1);
        BoardToken selected = redPlayer.getStrategy().selectTokenToMove(redPlayer, tokens, board, roll);
        assertNotNull(selected);
    }

    @Test
    @DisplayName("Aggressive fallback branch logic when no pieces can capture")
    void testAggressiveFallbackWhenAllChoicesPoor() {
        Piece piece1 = redPlayer.getPieces().get(0);
        piece1.setInBase(false);
        piece1.setState(PieceState.STANDARD_TRACK);
        piece1.setCurrentPosition(10);
        Piece opponent = new Piece("G1", PieceColor.GREEN);
        opponent.setInBase(false);
        opponent.setState(PieceState.STANDARD_TRACK);
        opponent.setCurrentPosition(12);
        board.getTrackCell(12).addPiece(opponent);
        
        List<BoardToken> tokens = Collections.singletonList(piece1);
        BoardToken selected = redPlayer.getStrategy().selectTokenToMove(redPlayer, tokens, board, 2);
        assertEquals(piece1, selected);
    }
}