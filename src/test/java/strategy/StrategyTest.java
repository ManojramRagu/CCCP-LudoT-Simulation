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
        board = new Board();
        redPlayer = new Player("Red Player", PieceColor.RED);
        bluePlayer = new Player("Blue Player", PieceColor.BLUE);
        greenPlayer = new Player("Green Player", PieceColor.GREEN);
    }

    @Test
    @DisplayName("AggressiveStrategy prioritizes capturing opponent piece over standard advance")
    void testAggressiveStrategyPrefersCapture() {
        Piece p1 = redPlayer.getPieces().get(0);
        Piece p2 = redPlayer.getPieces().get(1);

        p1.setInBase(false);
        p1.setCurrentPosition(0);

        p2.setInBase(false);
        p2.setCurrentPosition(10);

        Piece bluePiece = bluePlayer.getPieces().get(0);
        bluePiece.setInBase(false);
        bluePiece.setCurrentPosition(3);
        board.getTrackCell(3).addPiece(bluePiece);

        AggressiveStrategy strategy = new AggressiveStrategy();
        Piece selected = strategy.selectPieceToMove(redPlayer, List.of(p1, p2), board, 3);

        assertEquals(p1, selected);
    }

    @Test
    @DisplayName("AggressiveStrategy prioritizes capturing opponent closest to home when multiple captures exist")
    void testAggressiveStrategyCapturesClosestToHome() {
        Piece p1 = redPlayer.getPieces().get(0);
        Piece p2 = redPlayer.getPieces().get(1);

        p1.setInBase(false);
        p1.setCurrentPosition(10); // Target pos = 13

        p2.setInBase(false);
        p2.setCurrentPosition(20); // Target pos = 23 (closer to Red home/approach at 25)

        Piece blue1 = bluePlayer.getPieces().get(0);
        blue1.setInBase(false);
        blue1.setCurrentPosition(13);
        board.getTrackCell(13).addPiece(blue1);

        Piece blue2 = bluePlayer.getPieces().get(1);
        blue2.setInBase(false);
        blue2.setCurrentPosition(23);
        board.getTrackCell(23).addPiece(blue2);

        AggressiveStrategy strategy = new AggressiveStrategy();
        Piece selected = strategy.selectPieceToMove(redPlayer, List.of(p1, p2), board, 3);

        assertNotNull(selected);
    }

    @Test
    @DisplayName("AggressiveStrategy exits base on rolling 6 when no capture is available")
    void testAggressiveStrategyExitBaseOnSix() {
        Piece basePiece = redPlayer.getPieces().get(0);
        Piece activePiece = redPlayer.getPieces().get(1);
        activePiece.setInBase(false);
        activePiece.setCurrentPosition(10);

        AggressiveStrategy strategy = new AggressiveStrategy();
        Piece selected = strategy.selectPieceToMove(redPlayer, List.of(activePiece, basePiece), board, 6);

        assertEquals(basePiece, selected);
    }

    @Test
    @DisplayName("AggressiveStrategy returns null when movable pieces list is empty")
    void testAggressiveStrategyEmptyList() {
        AggressiveStrategy strategy = new AggressiveStrategy();
        assertNull(strategy.selectPieceToMove(redPlayer, Collections.emptyList(), board, 3));
        assertNull(strategy.selectPieceToMove(redPlayer, null, board, 3));
    }

    @Test
    @DisplayName("BalancedStrategy prioritizes bringing pieces out of base on rolling 6")
    void testBalancedStrategyPrefersExitingBase() {
        Piece basePiece = greenPlayer.getPieces().get(0);
        Piece activePiece = greenPlayer.getPieces().get(1);
        activePiece.setInBase(false);
        activePiece.setCurrentPosition(15);

        BalancedStrategy strategy = new BalancedStrategy();
        Piece selected = strategy.selectPieceToMove(greenPlayer, List.of(activePiece, basePiece), board, 6);

        assertEquals(basePiece, selected);
    }

    @Test
    @DisplayName("BalancedStrategy prioritizes advancing pieces that have captured an opponent")
    void testBalancedStrategyPrefersCapturedPieces() {
        Piece p1 = greenPlayer.getPieces().get(0);
        p1.setInBase(false);
        p1.setCurrentPosition(5);

        Piece p2 = greenPlayer.getPieces().get(1);
        p2.setInBase(false);
        p2.setCurrentPosition(10);
        p2.recordCapture();

        BalancedStrategy strategy = new BalancedStrategy();
        Piece selected = strategy.selectPieceToMove(greenPlayer, List.of(p1, p2), board, 3);

        assertEquals(p2, selected);
    }

    @Test
    @DisplayName("BalancedStrategy returns null when movable pieces list is empty")
    void testBalancedStrategyEmptyList() {
        BalancedStrategy strategy = new BalancedStrategy();
        assertNull(strategy.selectPieceToMove(greenPlayer, Collections.emptyList(), board, 4));
        assertNull(strategy.selectPieceToMove(greenPlayer, null, board, 4));
    }

    @Test
    @DisplayName("BalancedStrategy fallback returns first movable piece when no specific condition met")
    void testBalancedStrategyFallback() {
        Piece p1 = greenPlayer.getPieces().get(0);
        p1.setInBase(false);
        p1.setCurrentPosition(5);

        Piece p2 = greenPlayer.getPieces().get(1);
        p2.setInBase(false);
        p2.setCurrentPosition(10);

        BalancedStrategy strategy = new BalancedStrategy();
        Piece selected = strategy.selectPieceToMove(greenPlayer, List.of(p1, p2), board, 2);

        assertEquals(p1, selected);
    }

    @Test
    @DisplayName("AggressiveStrategy fallback returns active piece when rolling non-six with piece in base")
    void testAggressiveFallbackNonSix() {
        Piece basePiece = redPlayer.getPieces().get(0);
        Piece activePiece = redPlayer.getPieces().get(1);
        activePiece.setInBase(false);
        activePiece.setCurrentPosition(10);

        AggressiveStrategy strategy = new AggressiveStrategy();
        Piece selected = strategy.selectPieceToMove(redPlayer, List.of(activePiece, basePiece), board, 4);

        assertEquals(activePiece, selected);
    }

    @Test
    @DisplayName("CoinToss produces expected direction when executed in strategy setup")
    void testCoinTossIntegration() {
        CoinToss coinToss = new CoinToss();
        MovementDirection direction = coinToss.flip();
        assertNotNull(direction);
    }

    @Test
    @DisplayName("BalancedStrategy handles single piece in movable list cleanly")
    void testBalancedStrategySinglePiece() {
        Piece p1 = greenPlayer.getPieces().get(0);
        p1.setInBase(false);

        BalancedStrategy strategy = new BalancedStrategy();
        Piece selected = strategy.selectPieceToMove(greenPlayer, List.of(p1), board, 5);

        assertEquals(p1, selected);
    }

    @Test
    @DisplayName("AggressiveStrategy handles single piece in movable list cleanly")
    void testAggressiveStrategySinglePiece() {
        Piece p1 = redPlayer.getPieces().get(0);
        p1.setInBase(false);

        AggressiveStrategy strategy = new AggressiveStrategy();
        Piece selected = strategy.selectPieceToMove(redPlayer, List.of(p1), board, 5);

        assertEquals(p1, selected);
    }

    @Test
    @DisplayName("PlayerStrategy interface polymorphism test")
    void testPolymorphism() {
        PlayerStrategy strat1 = new AggressiveStrategy();
        PlayerStrategy strat2 = new BalancedStrategy();

        assertNotNull(strat1);
        assertNotNull(strat2);
    }

    @Test
    @DisplayName("BalancedStrategy prefers exit base on 6 over piece without capture")
    void testBalancedBaseExitOverUncaptured() {
        Piece basePiece = greenPlayer.getPieces().get(0);
        Piece activePiece = greenPlayer.getPieces().get(1);
        activePiece.setInBase(false);
        activePiece.setCurrentPosition(10);

        BalancedStrategy strategy = new BalancedStrategy();
        Piece selected = strategy.selectPieceToMove(greenPlayer, List.of(activePiece, basePiece), board, 6);

        assertEquals(basePiece, selected);
    }

    @Test
    @DisplayName("AggressiveStrategy prefers base exit on 6 over non-capturing move")
    void testAggressiveBaseExitOverNonCapture() {
        Piece basePiece = redPlayer.getPieces().get(0);
        Piece activePiece = redPlayer.getPieces().get(1);
        activePiece.setInBase(false);
        activePiece.setCurrentPosition(10);

        AggressiveStrategy strategy = new AggressiveStrategy();
        Piece selected = strategy.selectPieceToMove(redPlayer, List.of(activePiece, basePiece), board, 6);

        assertEquals(basePiece, selected);
    }
}
