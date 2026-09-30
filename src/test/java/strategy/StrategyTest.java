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

    @BeforeEach
    void setUp() {
        Board.resetInstance();
        board = Board.getInstance();

        redPlayer = new Player("Red Player", PieceColor.RED, new AggressiveStrategy());
        bluePlayer = new Player("Blue Player", PieceColor.BLUE, new ChaoticStrategy());
        greenPlayer = new Player("Green Player", PieceColor.GREEN, new BlockingStrategy());
    }

    @Test
    @DisplayName("AggressiveStrategy prioritizes capturing opponent piece over standard advance")
    void testAggressiveStrategyPrefersCapture() {
        Piece p1 = redPlayer.getPieces().getFirst();
        Piece p2 = redPlayer.getPieces().get(1);
        p1.setInBase(false); p1.setCurrentPosition(0);
        p2.setInBase(false); p2.setCurrentPosition(10);

        Piece bluePiece = bluePlayer.getPieces().getFirst();
        bluePiece.setInBase(false); bluePiece.setCurrentPosition(3);
        board.getTrackCell(3).addPiece(bluePiece);

        AggressiveStrategy strategy = new AggressiveStrategy();
        BoardToken selected = strategy.selectTokenToMove(redPlayer, List.<BoardToken>of(p1, p2), board, 3);

        assertEquals(p1, selected);
    }

    @Test
    @DisplayName("Section 2.1.1: AggressiveStrategy exits base on rolling 6 when NO pieces are on track")
    void testAggressiveStrategyExitBaseWhenEmptyTrack() {
        Piece basePiece1 = redPlayer.getPieces().getFirst();
        Piece basePiece2 = redPlayer.getPieces().get(1);

        AggressiveStrategy strategy = new AggressiveStrategy();
        BoardToken selected = strategy.selectTokenToMove(redPlayer, List.<BoardToken>of(basePiece1, basePiece2), board, 6);

        assertEquals(basePiece1, selected);
    }

    @Test
    @DisplayName("Section 2.1.1: AggressiveStrategy strictly refuses to exit base if it already has a piece on track")
    void testAggressiveStrategyRefusesExitBaseWhenAlreadyOnTrack() {
        Piece basePiece = redPlayer.getPieces().getFirst();
        Piece activePiece = redPlayer.getPieces().get(1);
        activePiece.setInBase(false);
        activePiece.setCurrentPosition(10);

        AggressiveStrategy strategy = new AggressiveStrategy();
        // Roll is 6. The bot MUST pick the active piece, not the base piece.
        BoardToken selected = strategy.selectTokenToMove(redPlayer, List.<BoardToken>of(activePiece, basePiece), board, 6);

        assertEquals(activePiece, selected);
    }

    @Test
    @DisplayName("AggressiveStrategy returns null when movable tokens list is empty")
    void testAggressiveStrategyEmptyList() {
        AggressiveStrategy strategy = new AggressiveStrategy();
        assertNull(strategy.selectTokenToMove(redPlayer, Collections.emptyList(), board, 3));
        assertNull(strategy.selectTokenToMove(redPlayer, null, board, 3));
    }

    @Test
    @DisplayName("Section 2.1.2: BlockingStrategy (Green) prioritizes bringing pieces out of base on rolling 6")
    void testBlockingStrategyPrefersExitingBase() {
        Piece basePiece = greenPlayer.getPieces().getFirst();
        Piece activePiece = greenPlayer.getPieces().get(1);
        activePiece.setInBase(false);
        activePiece.setCurrentPosition(15);

        BlockingStrategy strategy = new BlockingStrategy();
        BoardToken selected = strategy.selectTokenToMove(greenPlayer, List.<BoardToken>of(activePiece, basePiece), board, 6);

        assertEquals(basePiece, selected);
    }

    @Test
    @DisplayName("BlockingStrategy returns null when movable tokens list is empty")
    void testBlockingStrategyEmptyList() {
        BlockingStrategy strategy = new BlockingStrategy();
        assertNull(strategy.selectTokenToMove(greenPlayer, Collections.emptyList(), board, 4));
        assertNull(strategy.selectTokenToMove(greenPlayer, null, board, 4));
    }
}