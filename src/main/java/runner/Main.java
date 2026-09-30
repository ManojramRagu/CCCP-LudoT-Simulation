package runner;

import facade.LudoTGameFacade;
import gateway.GameLogGateway;
import gateway.InMemoryGameLogGateway;
import model.PieceColor;
import model.Dice;

public class Main {

    private static final PieceColor[] CLOCKWISE_ORDER = {
            PieceColor.RED, PieceColor.GREEN, PieceColor.YELLOW, PieceColor.BLUE
    };

    public static void main(String[] args) {
        LudoTGameFacade gameFacade = new LudoTGameFacade();
        GameLogGateway logGateway = new InMemoryGameLogGateway();

        gameFacade.addObserver(logGateway);

        printPreGameMessages();

        PieceColor startingPlayer = determineFirstPlayer();

        GameRunner runner = new GameRunner(gameFacade, logGateway, startingPlayer);
        runner.runSimulation();

        System.out.println("\n=== Simulation Finished. Total Events Logged: " + runner.getLoggedEventCount() + " ===");
    }

    private static void printPreGameMessages() {
        for (PieceColor color : CLOCKWISE_ORDER) {
            String cName = color.name().toLowerCase();
            char initial = color.name().charAt(0);
            System.out.printf("The %s player has four (04) pieces named %s1, %s2, %s3, and %s4.\n",
                    cName, initial, initial, initial, initial);
        }
        System.out.println();
    }

    private static PieceColor determineFirstPlayer() {
        Dice dice = new Dice();
        int maxRoll = -1;
        PieceColor startingPlayer = CLOCKWISE_ORDER[0];

        for (PieceColor color : CLOCKWISE_ORDER) {
            int roll = dice.roll();
            System.out.println(color.name().toLowerCase() + " rolls " + roll);
            if (roll > maxRoll) {
                maxRoll = roll;
                startingPlayer = color;
            }
        }

        int startIndex = 0;
        for (int i = 0; i < CLOCKWISE_ORDER.length; i++) {
            if (CLOCKWISE_ORDER[i] == startingPlayer) {
                startIndex = i;
                break;
            }
        }

        String p1 = CLOCKWISE_ORDER[startIndex].name().toLowerCase();
        String p2 = CLOCKWISE_ORDER[(startIndex + 1) % 4].name().toLowerCase();
        String p3 = CLOCKWISE_ORDER[(startIndex + 2) % 4].name().toLowerCase();
        String p4 = CLOCKWISE_ORDER[(startIndex + 3) % 4].name().toLowerCase();

        System.out.printf("%s player has the highest roll and will begin the game. The order of a single round is %s, %s, %s, and %s.\n\n",
                p1, p1, p2, p3, p4);

        return startingPlayer;
    }
}