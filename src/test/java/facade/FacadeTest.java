package facade;

import dto.GameEventDTO;
import model.Board;
import model.PieceColor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class FacadeTest {

    @BeforeEach
    void setUp() {
        Board.resetInstance();
    }

    @Test
    @DisplayName("Facade initializes game components cleanly")
    void testGameInitialization() {
        LudoTGameFacade game = new LudoTGameFacade();
        assertNotNull(game.getBoard());
        assertEquals(4, game.getPlayers().size());
        assertFalse(game.isGameOver());
    }

    @Test
    @DisplayName("Single turn execution returns non-null GameEventDTO")
    void testPlayTurn() {
        LudoTGameFacade game = new LudoTGameFacade();
        GameEventDTO event = game.playTurn(PieceColor.RED);

        assertNotNull(event);
        assertEquals(PieceColor.RED, event.playerColor());
    }

    @Test
    @DisplayName("Facade playTurn behaves predictably with Mocked Dice")
    void testPlayTurnWithMockDice() throws Exception {
        LudoTGameFacade game = new LudoTGameFacade();
        
        // Mock the internal Dice via Reflection to isolate Facade logic from randomness
        model.Dice mockDice = org.mockito.Mockito.mock(model.Dice.class);
        org.mockito.Mockito.when(mockDice.roll()).thenReturn(6);
        
        java.lang.reflect.Field diceField = LudoTGameFacade.class.getDeclaredField("dice");
        diceField.setAccessible(true);
        diceField.set(game, mockDice);

        GameEventDTO event = game.playTurn(PieceColor.RED);

        assertNotNull(event);
        assertEquals(6, event.diceRoll(), "Dice roll should be strictly 6 as dictated by the Mock");
        
        // Verify the mock was called
        org.mockito.Mockito.verify(mockDice, org.mockito.Mockito.atLeastOnce()).roll();
    }

    @Test
    @DisplayName("Phantom Block Resolution: standard track and home straight pieces don't mix")
    void testPhantomBlockResolution() {
        LudoTGameFacade game = new LudoTGameFacade();
        model.Player redPlayer = game.getPlayers().get(PieceColor.RED);
        
        model.Piece p1 = redPlayer.getPieces().get(0);
        p1.setInBase(false);
        p1.setState(model.PieceState.STANDARD_TRACK);
        p1.setCurrentPosition(2);
        
        model.Piece p2 = redPlayer.getPieces().get(1);
        p2.setInBase(false);
        p2.setState(model.PieceState.HOME_STRAIGHT);
        p2.setCurrentPosition(2);
        
        assertFalse(game.hasBlockade(PieceColor.RED), "Pieces on different tracks but same position should NOT form a block");
    }

    @Test
    @DisplayName("Base-to-Start captures grant bonus roll and log correctly")
    void testBaseToStartCaptureBonus() throws Exception {
        LudoTGameFacade game = new LudoTGameFacade();
        
        // Mock opponent piece on Yellow's start (0)
        model.Player yellow = game.getPlayers().get(PieceColor.YELLOW);
        model.Player red = game.getPlayers().get(PieceColor.RED);
        model.Piece opponent = red.getPieces().get(0);
        
        opponent.setInBase(false);
        opponent.setState(model.PieceState.STANDARD_TRACK);
        opponent.setCurrentPosition(0); // Yellow's starting cell
        game.getBoard().getTrackCell(0).addPiece(opponent);
        
        // Mock Dice to roll a 6 for Yellow
        model.Dice mockDice = org.mockito.Mockito.mock(model.Dice.class);
        org.mockito.Mockito.when(mockDice.roll()).thenReturn(6);
        java.lang.reflect.Field diceField = LudoTGameFacade.class.getDeclaredField("dice");
        diceField.setAccessible(true);
        diceField.set(game, mockDice);
        
        GameEventDTO event = game.playTurn(PieceColor.YELLOW);
        
        assertTrue(event.capturedOpponent(), "Capture flag must be true");
        assertTrue(event.description().contains("captures"), "Capture string must be logged");
        assertTrue(opponent.isInBase(), "Opponent must be returned to base");
    }
}