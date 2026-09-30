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
}