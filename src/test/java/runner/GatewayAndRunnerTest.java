package runner;

import facade.LudoTGameFacade;
import gateway.GameLogGateway;
import gateway.InMemoryGameLogGateway;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class GatewayAndRunnerTest {

    @Test
    @DisplayName("InMemoryGameLogGateway stores and retrieves logged events")
    void testGatewayLogging() {
        GameLogGateway gateway = new InMemoryGameLogGateway();
        assertEquals(0, gateway.getAllEvents().size());

        LudoTGameFacade game = new LudoTGameFacade();
        GameRunner runner = new GameRunner(game, gateway);
        runner.runSimulation();

        assertTrue(gateway.getAllEvents().size() > 0);
    }

    @Test
    @DisplayName("GameRunner terminates simulation when maximum turns or game win occurs")
    void testGameRunnerExecution() {
        LudoTGameFacade game = new LudoTGameFacade();
        GameLogGateway gateway = new InMemoryGameLogGateway();
        GameRunner runner = new GameRunner(game, gateway);

        runner.runSimulation();

        assertTrue(runner.getLoggedEventCount() <= GameRunner.MAX_TURNS);
        assertTrue(runner.getLoggedEventCount() > 0);
    }
}
