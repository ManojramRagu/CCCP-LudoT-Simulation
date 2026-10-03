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
}