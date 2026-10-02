package facade;

import command.GameCommand;
import dto.GameEventDTO;
import model.*;
import observer.GameEventObserver;
import strategy.AggressiveStrategy;
import strategy.BlockingStrategy;
import strategy.ChaoticStrategy;
import strategy.OpportunisticStrategy;

import java.util.*;

public class LudoTGameFacade {
    private final Board board;
    private final Map<PieceColor, Player> players;
    private final List<GameEventObserver> observers;
    private final Dice dice;
    private int turnCounter;

    public LudoTGameFacade() {
        this.board = Board.getInstance();
        this.players = new EnumMap<>(PieceColor.class);
        this.observers = new ArrayList<>();
        this.dice = new Dice();
        this.turnCounter = 0;
        initializePlayers();
    }

    private void initializePlayers() {
        players.put(PieceColor.RED, new Player("Red", PieceColor.RED, new AggressiveStrategy()));
        players.put(PieceColor.GREEN, new Player("Green", PieceColor.GREEN, new BlockingStrategy()));
        players.put(PieceColor.YELLOW, new Player("Yellow", PieceColor.YELLOW, new OpportunisticStrategy()));
        players.put(PieceColor.BLUE, new Player("Blue", PieceColor.BLUE, new ChaoticStrategy()));
    }

    public GameEventDTO playTurn(PieceColor color) {
        turnCounter++;
        Player player = players.get(color);
        int roll = dice.roll();

        List<BoardToken> movableTokens = extractTokens(player);
        BoardToken selectedToken = player.getStrategy().selectTokenToMove(player, movableTokens, board, roll);

        int startPos = -1;
        String pieceId = "None";
        boolean captured = false;
        String description;

        if (selectedToken != null) {
            startPos = selectedToken.getCurrentPosition();
            pieceId = formatTokenId(selectedToken);

            GameCommand moveCommand = command.CommandFactory.createMoveCommand(selectedToken, player, board, roll);
            if (moveCommand.isExecutable()) {
                moveCommand.execute();
                captured = moveCommand.hasCaptured();

                if (startPos == -1) {
                    description = String.format("[%s] player moves piece %s to the starting point.", color.name().toLowerCase(), pieceId);
                } else if (captured) {
                    description = String.format("[%s] piece %s lands on square L%d, captures opponent, and returns it to the base.", color.name().toLowerCase(), pieceId, selectedToken.getCurrentPosition());
                } else {
                    description = String.format("[%s] moves piece %s from location L%d to L%d by %d units.", color.name().toLowerCase(), pieceId, startPos, selectedToken.getCurrentPosition(), roll);
                }
            } else {
                description = String.format("[%s] player rolled %d. [%s] player has no valid moves.", color.name().toLowerCase(), roll, color.name().toLowerCase());
            }
        } else {
            description = String.format("[%s] player rolled %d. [%s] player has no valid moves.", color.name().toLowerCase(), roll, color.name().toLowerCase());
        }

        GameEventDTO event = new GameEventDTO(turnCounter, color, roll, pieceId, startPos,
                selectedToken != null ? selectedToken.getCurrentPosition() : -1, captured, description);
        notifyObservers(event);
        return event;
    }

    // RULE T-6 IMPLEMENTED: Shatter blockade on three consecutive 6s
    public void executePenaltyBreak(PieceColor color) {
        Player player = players.get(color);
        List<BoardToken> tokens = extractTokens(player);
        for (BoardToken token : tokens) {
            if (token instanceof Block block) {
                List<Piece> pieces = block.getComponentPieces();
                Piece survivor = pieces.getFirst();

                for (int i = 1; i < pieces.size(); i++) {
                    pieces.get(i).resetToBase();
                }

                Cell currentCell = board.getTrackCell(survivor.getCurrentPosition());
                currentCell.clearPieces();

                int newPos = survivor.getDirection() == MovementDirection.CLOCKWISE
                        ? (survivor.getCurrentPosition() + 6) % Board.TOTAL_TRACK_CELLS
                        : (survivor.getCurrentPosition() - 6 + Board.TOTAL_TRACK_CELLS) % Board.TOTAL_TRACK_CELLS;

                survivor.setCurrentPosition(newPos);
                board.getTrackCell(newPos).addPiece(survivor);

                System.out.println("RULE T-6 TRIGGERED: [" + color.name() + "] rolled three 6s. Blockade shattered.");
                break;
            }
        }
    }

    public boolean hasBlockade(PieceColor color) {
        return extractTokens(players.get(color)).stream().anyMatch(t -> t instanceof Block);
    }

    private List<BoardToken> extractTokens(Player player) {
        List<BoardToken> tokens = new ArrayList<>();
        Map<Integer, List<Piece>> positionMap = new HashMap<>();

        for (Piece p : player.getPieces()) {
            if (p.isCompleted()) continue;
            positionMap.computeIfAbsent(p.getCurrentPosition(), k -> new ArrayList<>()).add(p);
        }

        for (List<Piece> group : positionMap.values()) {
            if (group.size() > 1 && group.getFirst().getCurrentPosition() != -1) {
                tokens.add(new Block(group));
            } else {
                tokens.addAll(group);
            }
        }
        return tokens;
    }

    private String formatTokenId(BoardToken token) {
        if (token instanceof Piece p) return p.getId();
        if (token instanceof Block b) {
            StringBuilder sb = new StringBuilder("BLOCK(");
            for (Piece p : b.getComponentPieces()) sb.append(p.getId()).append(",");
            return sb.substring(0, sb.length() - 1) + ")";
        }
        return "Unknown";
    }

    public Board getBoard() { return board; }
    public Map<PieceColor, Player> getPlayers() { return Collections.unmodifiableMap(players); }
    public boolean isGameOver() { return players.values().stream().anyMatch(Player::hasWon); }
    public void addObserver(GameEventObserver observer) { if (!observers.contains(observer)) observers.add(observer); }
    public void removeObserver(GameEventObserver observer) { observers.remove(observer); }
    private void notifyObservers(GameEventDTO event) { for (GameEventObserver obs : observers) obs.onGameEvent(event); }
}