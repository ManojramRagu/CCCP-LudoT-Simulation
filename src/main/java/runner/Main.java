package runner;

import facade.LudoTGameFacade;
import gateway.GameLogGateway;
import gateway.InMemoryGameLogGateway;

public class Main {
    public static void main(String[] args) {
        System.out.println("=== LUDO-T AUTONOMOUS SIMULATION ===");
        System.out.println("The red player has four (04) pieces named R1, R2, R3, and R4.");
        System.out.println("The green player has four (04) pieces named G1, G2, G3, and G4.");
        System.out.println("The yellow player has four (04) pieces named Y1, Y2, Y3, and Y4.");
        System.out.println("The blue player has four (04) pieces named B1, B2, B3, and B4.\n");

        LudoTGameFacade gameFacade = new LudoTGameFacade();
        GameLogGateway gateway = new InMemoryGameLogGateway();
        gameFacade.registerObserver(gateway);

        GameRunner runner = new GameRunner(gameFacade, gateway);
        runner.runSimulation();

        System.out.println("\n=== SIMULATION COMPLETE ===");
        System.out.println("Total Game Events Executed: " + gateway.getAllEvents().size());
        if (gameFacade.isGameOver()) {
            System.out.println("Status: GAME OVER (Winner determined)");
        } else {
            System.out.println("Status: Safety limit reached (1000 turns)");
        }
    }
}
