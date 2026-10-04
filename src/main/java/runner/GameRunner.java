package runner;

import dto.GameEventDTO;
import facade.LudoTGameFacade;
import gateway.GameLogGateway;
import model.Board;
import model.Cell;
import model.CellType;
import model.PieceColor;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class GameRunner {
    public static final int MAX_TURNS = 2000;

    private final LudoTGameFacade gameFacade;
    private final GameLogGateway logGateway;
    private final PieceColor startingColor;

    private int roundCount = 0;
    private int mysteryCellTimer = 0;
    private int previousMysteryCellIndex = -1;

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
        int playersPlayedInRound = 0;
        java.util.Map<PieceColor, Integer> consecutiveThreesMap = new java.util.HashMap<>();

        while (!gameFacade.isGameOver() && totalTurnsExecuted < MAX_TURNS) {
            PieceColor activeColor = turnOrder[turnIndex];

            // RULE 4 & T-2 IMPLEMENTED: Bonus Rolls & Turn Execution Loop
            int pendingBonusRolls = 0;
            int consecutiveSixes = 0;

            do {
                GameEventDTO event = gameFacade.playTurn(activeColor);
                totalTurnsExecuted++;

                int r = event.diceRoll();
                if (r == 6) {
                    consecutiveSixes++;
                    consecutiveThreesMap.put(activeColor, 0);
                    if (consecutiveSixes == 3) {
                        System.out.println("Rule 4 Triggered: Third consecutive 6 rolled. Turn ignored.");
                        if (gameFacade.hasBlockade(activeColor)) {
                            gameFacade.executePenaltyBreak(activeColor); // Rule T-6 Execution
                        }
                        pendingBonusRolls = 0;
                        break; // End the turn
                    } else {
                        pendingBonusRolls++; // Rule 4 Bonus Roll
                    }
                } else {
                    consecutiveSixes = 0;
                    if (r == 3) {
                        int count = consecutiveThreesMap.getOrDefault(activeColor, 0) + 1;
                        consecutiveThreesMap.put(activeColor, count);
                        if (count == 3) {
                            consecutiveThreesMap.put(activeColor, 0);
                            for (model.Piece p : gameFacade.getPlayers().get(activeColor).getPieces()) {
                                if (p.isRestricted()) {
                                    p.resetToBase();
                                    System.out.println(String.format("[%s] piece %s is movement-restricted and has rolled three consecutively. Teleporting piece %s to base.", activeColor.name().toLowerCase(), p.getId(), p.getId()));
                                }
                            }
                        }
                    } else {
                        consecutiveThreesMap.put(activeColor, 0);
                    }
                }
                
                if (event.capturedOpponent()) {
                    pendingBonusRolls++; // Rule T-2 Capture Bonus Roll
                }
                
                if (pendingBonusRolls > 0) {
                    pendingBonusRolls--;
                } else {
                    break;
                }
            } while (!gameFacade.isGameOver() && totalTurnsExecuted < MAX_TURNS);

            // A single round is completed when all 4 players have executed their turns
            playersPlayedInRound++;
            if (playersPlayedInRound == 4) {
                handleRoundEnd();
                playersPlayedInRound = 0;
            }

            turnIndex = (turnIndex + 1) % turnOrder.length;
        }

        if (gameFacade.isGameOver()) {
            announceWinner();
        }
    }

    private void handleRoundEnd() {
        roundCount++;
        Board board = gameFacade.getBoard();

        if (board.getActiveMysteryCell() != null) {
            mysteryCellTimer--;
            if (mysteryCellTimer <= 0) {
                board.removeMysteryCell();
            }
        }

        if (roundCount >= 2 && board.getActiveMysteryCell() == null) {
            List<Cell> emptyStandardCells = new ArrayList<>();
            for (int i = 0; i < Board.TOTAL_TRACK_CELLS; i++) {
                if (i == previousMysteryCellIndex) continue;
                Cell c = board.getTrackCell(i);
                if (c.getType() == CellType.STANDARD && c.getOccupyingPieces().isEmpty()) {
                    emptyStandardCells.add(c);
                }
            }

            if (!emptyStandardCells.isEmpty()) {
                Cell target = emptyStandardCells.get(new Random().nextInt(emptyStandardCells.size()));
                board.spawnMysteryCell(target.getIndex());
                mysteryCellTimer = 4;
                previousMysteryCellIndex = target.getIndex();
                System.out.println("A mystery cell has spawned in location L" + target.getIndex() + " and will be at this location for the next four rounds.");
            }
        }
        
        for (PieceColor color : PieceColor.values()) {
            for (model.Piece p : gameFacade.getPlayers().get(color).getPieces()) {
                p.decrementStatusEffects();
            }
        }
        
        printRoundSummary();
    }

    private void printRoundSummary() {
        System.out.println("\n--- END OF ROUND " + roundCount + " SUMMARY ---");
        for (PieceColor color : PieceColor.values()) {
            model.Player player = gameFacade.getPlayers().get(color);
            long onBoard = player.getPieces().stream().filter(p -> !p.isInBase() && !p.isCompleted()).count();
            long inBase = player.getPieces().stream().filter(model.Piece::isInBase).count();

            // Replaced concatenation with exact String.format requested in the audit
            System.out.println(String.format("[%s] player now has %d/4 on pieces on the board and %d/4 pieces on the base.", color.name().toLowerCase(), onBoard, inBase));
            System.out.println("============================");
            System.out.println("Location of pieces " + color.name().toLowerCase());
            System.out.println("============================");
            for (model.Piece p : player.getPieces()) {
                String loc = "Base";
                if (!p.isInBase()) {
                    if (p.isCompleted()) {
                        loc = "Home";
                    } else if (p.getState() == model.PieceState.HOME_STRAIGHT) {
                        loc = color.name().toLowerCase() + "homepath" + p.getCurrentPosition();
                    } else {
                        loc = "L" + p.getCurrentPosition();
                    }
                }
                System.out.println("Piece " + p.getId() + " − > " + loc);
            }
        }

        model.Cell mystery = gameFacade.getBoard().getActiveMysteryCell();
        if (mystery != null) {
            // Replaced concatenation with exact String.format requested in the audit
            System.out.println(String.format("The mystery cell is at L%d and will be at that location for the next %d values.", mystery.getIndex(), mysteryCellTimer));
        }
        System.out.println();
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