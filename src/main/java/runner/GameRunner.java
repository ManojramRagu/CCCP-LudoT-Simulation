package runner;

import dto.GameEventDTO;
import facade.LudoTGameFacade;
import gateway.GameLogGateway;
import model.PieceColor;

public class GameRunner {
    public static final int MAX_TURNS = 1000;

    private final LudoTGameFacade gameFacade;
    private final GameLogGateway logGateway;

    public GameRunner(LudoTGameFacade gameFacade, GameLogGateway logGateway) {
        this.gameFacade = gameFacade;
        this.logGateway = logGateway;
    }

    public void runSimulation() {
        PieceColor[] turnOrder = PieceColor.values();
        int turnIndex = 0;
        int totalTurnsExecuted = 0;

        while (!gameFacade.isGameOver() && totalTurnsExecuted < MAX_TURNS) {
            PieceColor activeColor = turnOrder[turnIndex];
            GameEventDTO event = gameFacade.playTurn(activeColor);
            logGateway.logEvent(event);

            turnIndex = (turnIndex + 1) % turnOrder.length;
            totalTurnsExecuted++;
        }
    }

    public int getLoggedEventCount() {
        return logGateway.getAllEvents().size();
    }
}
