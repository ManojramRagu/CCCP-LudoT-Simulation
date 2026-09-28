package runner;

import facade.LudoTGameFacade;
import gateway.GameLogGateway;
import gateway.InMemoryGameLogGateway;

public class Main {
    public static void main(String[] args) {
        LudoTGameFacade gameFacade = new LudoTGameFacade();
        GameLogGateway logGateway = new InMemoryGameLogGateway();

        GameRunner runner = new GameRunner(gameFacade, logGateway);
        System.out.println("=== Starting LUDO-T Simulation ===");
        runner.runSimulation();
        System.out.println("=== Simulation Finished. Total Events Logged: " + runner.getLoggedEventCount() + " ===");
    }
}
