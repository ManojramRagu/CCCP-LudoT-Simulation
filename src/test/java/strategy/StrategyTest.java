package strategy;

import model.*;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class StrategyTest {

    @Test
    @DisplayName("AggressiveStrategy prioritizes capturing an opponent piece")
    void testAggressiveStrategyPrefersCapture() {
        Player redPlayer = new Player("Red", PieceColor.RED);
        Player bluePlayer = new Player("Blue", PieceColor.BLUE);
        Board board = new Board();

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
    @DisplayName("BalancedStrategy prioritizes bringing pieces out of base on rolling 6")
    void testBalancedStrategyPrefersExitingBase() {
        Player greenPlayer = new Player("Green", PieceColor.GREEN);
        Board board = new Board();

        Piece basePiece = greenPlayer.getPieces().get(0);
        Piece activePiece = greenPlayer.getPieces().get(1);
        activePiece.setInBase(false);
        activePiece.setCurrentPosition(15);

        BalancedStrategy strategy = new BalancedStrategy();
        Piece selected = strategy.selectPieceToMove(greenPlayer, List.of(activePiece, basePiece), board, 6);

        assertEquals(basePiece, selected);
    }

    @Test
    @DisplayName("CoinToss produces expected direction when executed")
    void testCoinToss() {
        CoinToss coinToss = new CoinToss();
        MovementDirection direction = coinToss.flip();
        assertNotNull(direction);
    }
}
