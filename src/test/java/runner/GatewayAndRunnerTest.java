package runner;

import facade.LudoTGameFacade;
import gateway.GameLogGateway;
import gateway.InMemoryGameLogGateway;
import model.Board;
import model.PieceColor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class GatewayAndRunnerTest {

    @BeforeEach
    void setUp() {
        // Ensure a clean board state before running full simulations
        Board.resetInstance();
    }

    @Test
    @DisplayName("InMemoryGameLogGateway stores and retrieves logged events")
    void testGatewayLogging() {
        GameLogGateway gateway = new InMemoryGameLogGateway();
        LudoTGameFacade game = new LudoTGameFacade();

        // CRITICAL FIX: The test must register the Observer just like Main.java does
        game.addObserver(gateway);

        GameRunner runner = new GameRunner(game, gateway, PieceColor.RED);
        runner.runSimulation();

        assertTrue(gateway.getAllEvents().size() > 0, "Gateway should have captured broadcasted events.");
    }

    @Test
    @DisplayName("GameRunner terminates simulation when maximum turns or game win occurs")
    void testGameRunnerExecution() {
        LudoTGameFacade game = new LudoTGameFacade();
        GameLogGateway gateway = new InMemoryGameLogGateway();

        // CRITICAL FIX: Register the Observer
        game.addObserver(gateway);

        GameRunner runner = new GameRunner(game, gateway, PieceColor.RED);
        runner.runSimulation();

        assertTrue(runner.getLoggedEventCount() <= GameRunner.MAX_TURNS,
                "Logged event count should be <= MAX_TURNS (" + GameRunner.MAX_TURNS + ") but was " + runner.getLoggedEventCount());
        assertTrue(runner.getLoggedEventCount() > 0,
                "Logged event count should be > 0");
    }

    @Test
    @DisplayName("GameRunner correctly interacts with Facade (Isolated using Mockito)")
    void testGameRunnerIsolated() {
        LudoTGameFacade mockFacade = org.mockito.Mockito.mock(LudoTGameFacade.class);
        GameLogGateway mockGateway = org.mockito.Mockito.mock(GameLogGateway.class);
        
        // Mock facade to immediately return game over after one turn
        org.mockito.Mockito.when(mockFacade.isGameOver()).thenReturn(false, true);
        org.mockito.Mockito.when(mockFacade.playTurn(org.mockito.ArgumentMatchers.any())).thenReturn(
            new dto.GameEventDTO(1, PieceColor.RED, 1, "R1", 26, 27, false, "Test")
        );
        
        GameRunner runner = new GameRunner(mockFacade, mockGateway, PieceColor.RED);
        runner.runSimulation();
        
        // Verify Facade was interacted with
        org.mockito.Mockito.verify(mockFacade, org.mockito.Mockito.atLeastOnce()).playTurn(PieceColor.RED);
    }
}