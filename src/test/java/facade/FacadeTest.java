package facade;

import dto.GameEventDTO;
import model.PieceColor;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class FacadeTest {

    @Test
    @DisplayName("Facade initializes game components cleanly")
    void testGameInitialization() {
        LudoTGameFacade game = new LudoTGameFacade();
        assertNotNull(game.getBoard());
        assertEquals(4, game.getPlayers().size());
        assertFalse(game.isGameOver());
    }

    @Test
    @DisplayName("Single turn execution returns non-null GameEventDTO and updates game log")
    void testPlayTurn() {
        LudoTGameFacade game = new LudoTGameFacade();
        GameEventDTO event = game.playTurn(PieceColor.RED);

        assertNotNull(event);
        assertEquals(PieceColor.RED, event.playerColor());
        assertEquals(1, game.getGameLog().size());
    }
}
