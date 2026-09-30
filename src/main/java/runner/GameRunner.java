package runner;

import dto.GameEventDTO;
import facade.LudoTGameFacade;
import gateway.GameLogGateway;
import model.Board;
import model.PieceColor;

public class GameRunner {
    public static final int MAX_TURNS = 1000;

    private final LudoTGameFacade gameFacade;
    private final GameLogGateway logGateway;
    private final PieceColor startingColor;

    public GameRunner(LudoTGameFacade gameFacade, GameLogGateway logGateway, PieceColor startingColor) {
        this.gameFacade = gameFacade;
        this.logGateway = logGateway;
        this.startingColor = startingColor;
    }

    public void runSimulation() {
        PieceColor[] turnOrder = {PieceColor.RED, PieceColor.GREEN, PieceColor.YELLOW, PieceColor.BLUE};

        int turnIndex = 0;
        for (int i = 0; i < turnOrder.length; i++) {
            if (turnOrder[i] == startingColor) {
                turnIndex = i;
                break;
            }
        }

        int totalTurnsExecuted = 0;

        while (!gameFacade.isGameOver() && totalTurnsExecuted < MAX_TURNS) {
            PieceColor activeColor = turnOrder[turnIndex];
            GameEventDTO event = gameFacade.playTurn(activeColor);

            if (logGateway != null) logGateway.logEvent(event);

            if ((totalTurnsExecuted + 1) % 4 == 0) printRoundSummary();

            turnIndex = (turnIndex + 1) % turnOrder.length;
            totalTurnsExecuted++;
        }

        if (gameFacade.isGameOver()) announceWinner();
    }

    private void printRoundSummary() {
        for (PieceColor color : PieceColor.values()) {
            model.Player player = gameFacade.getPlayers().get(color);
            long onBoard = player.getPieces().stream().filter(p -> !p.isInBase() && !p.isCompleted()).count();
            long inBase = player.getPieces().stream().filter(model.Piece::isInBase).count();

            System.out.println("[" + color.name().toLowerCase() + "] player now has " + onBoard + "/4 pieces on the board and " + inBase + "/4 pieces on the base.");
            System.out.println("============================ Location of pieces " + color.name().toLowerCase() + " ============================");
            for (model.Piece p : player.getPieces()) {
                String loc = p.isInBase() ? "Base" : (p.isCompleted() ? "Home" : "L" + p.getCurrentPosition());
                System.out.println("Piece " + p.getId() + " -> " + loc);
            }
        }

        model.Cell mystery = Board.getInstance().getActiveMysteryCell();
        if (mystery != null) {
            System.out.println("The mystery cell is at L" + mystery.getIndex() + " and will be at that location for the next 4 values.");
        }
    }

    private void announceWinner() {
        PieceColor winner = gameFacade.getPlayers().values().stream()
                .filter(model.Player::hasWon).map(model.Player::getColor).findFirst().orElse(null);
        if (winner != null) {
            System.out.println("[" + winner.name().toLowerCase() + "] player wins!!!");
        }
    }

    public int getLoggedEventCount() {
        return logGateway != null ? logGateway.getAllEvents().size() : 0;
    }
}