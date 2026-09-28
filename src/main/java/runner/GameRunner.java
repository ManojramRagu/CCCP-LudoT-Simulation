package runner;

import dto.GameEventDTO;
import facade.LudoTGameFacade;
import gateway.GameLogGateway;
import model.Piece;
import model.PieceColor;
import model.Player;

public class GameRunner {
    public static final int MAX_TURNS = 1000;

    private final LudoTGameFacade gameFacade;
    private final GameLogGateway logGateway;

    public GameRunner(LudoTGameFacade gameFacade, GameLogGateway logGateway) {
        this.gameFacade = gameFacade;
        this.logGateway = logGateway;
    }

    public void runSimulation() {
        PieceColor[] turnOrder = new PieceColor[]{PieceColor.RED, PieceColor.GREEN, PieceColor.YELLOW, PieceColor.BLUE};
        int turnIndex = 0;
        int totalTurnsExecuted = 0;

        while (!gameFacade.isGameOver() && totalTurnsExecuted < MAX_TURNS) {
            PieceColor activeColor = turnOrder[turnIndex];
            GameEventDTO event = gameFacade.playTurn(activeColor);
            logGateway.logEvent(event);

            turnIndex = (turnIndex + 1) % turnOrder.length;
            totalTurnsExecuted++;

            if (totalTurnsExecuted % 4 == 0) {
                printRoundSummary(totalTurnsExecuted / 4);
            }
        }
    }

    private void printRoundSummary(int roundNumber) {
        System.out.println("\n--- END OF ROUND " + roundNumber + " ---");
        for (PieceColor color : PieceColor.values()) {
            Player p = gameFacade.getPlayers().get(color);
            System.out.println(color + " player now has " + p.getActivePiecesOnBoardCount() + "/4 pieces on board and " + p.getPiecesInBaseCount() + "/4 pieces in base.");
            System.out.println("============================ Location of pieces " + color + " ============================");
            for (Piece piece : p.getPieces()) {
                String loc = piece.isInBase() ? "Base" : piece.isCompleted() ? "Home" : "L" + piece.getCurrentPosition();
                System.out.println("Piece " + piece.getId() + " -> " + loc);
            }
        }
        if (gameFacade.getBoard().getActiveMysteryCell() != null) {
            int loc = gameFacade.getBoard().getActiveMysteryCell().getIndex();
            int turns = gameFacade.getBoard().getMysteryCellTurnsRemaining();
            System.out.println("The mystery cell is at L" + loc + " and will be at that location for the next " + turns + " turns.");
        }
        System.out.println("----------------------------------------\n");
    }

    public int getLoggedEventCount() {
        return logGateway.getAllEvents().size();
    }
}
