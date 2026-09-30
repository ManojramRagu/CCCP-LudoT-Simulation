package facade;

import command.CommandFactory;
import command.GameCommand;
import dto.GameEventDTO;
import model.*;
import observer.GameEventObserver;
import strategy.*;

import java.util.*;

public class LudoTGameFacade {
    private final Board board;
    private final Dice dice;
    private final Map<PieceColor, Player> players;
    private final Map<PieceColor, PlayerStrategy> strategies;
    private final List<GameEventDTO> gameLog;
    private final List<GameEventObserver> observers;
    private int currentTurn;

    public LudoTGameFacade() {
        this.board = Board.getInstance();
        this.dice = new Dice();
        this.players = new EnumMap<>(PieceColor.class);
        this.strategies = new EnumMap<>(PieceColor.class);
        this.gameLog = new ArrayList<>();
        this.observers = new ArrayList<>();
        this.currentTurn = 0;
        initializeGame();
    }

    public void addObserver(GameEventObserver observer) {
        if (observer != null && !observers.contains(observer)) observers.add(observer);
    }

    private void notifyObservers(GameEventDTO event) {
        for (GameEventObserver obs : observers) obs.onGameEvent(event);
    }

    private void initializeGame() {
        for (PieceColor color : PieceColor.values()) {
            PlayerStrategy strategy = switch (color) {
                case RED -> new AggressiveStrategy();
                case GREEN -> new BlockingStrategy();
                case YELLOW -> new OpportunisticStrategy();
                case BLUE -> new ChaoticStrategy();
            };
            players.put(color, new Player(color.name() + " Player", color, strategy));
            strategies.put(color, strategy);
        }
    }

    public GameEventDTO playTurn(PieceColor activeColor) {
        currentTurn++;
        Player player = players.get(activeColor);
        PlayerStrategy strategy = strategies.get(activeColor);

        int roll = dice.roll();
        List<BoardToken> movableTokens = findMovableTokens(player, roll);

        BoardToken chosenToken = strategy.selectTokenToMove(player, movableTokens, board, roll);
        GameCommand command = CommandFactory.createMoveCommand(chosenToken, player, board, roll);
        command.execute();

        boolean captured = command.hasCaptured();
        int startPos = chosenToken != null ? command.getPreviousPosition() : -1;
        int endPos = chosenToken != null ? chosenToken.getCurrentPosition() : -1;
        String tokenId = buildTokenId(chosenToken);

        String desc = formatSection31Message(activeColor, roll, tokenId, chosenToken, startPos, endPos, captured, player);
        GameEventDTO event = new GameEventDTO(currentTurn, activeColor, roll, tokenId, startPos, endPos, captured, desc);
        gameLog.add(event);
        notifyObservers(event);

        return event;
    }

    private List<BoardToken> findMovableTokens(Player player, int roll) {
        List<BoardToken> movable = new ArrayList<>();
        Map<Integer, List<Piece>> trackPositions = new HashMap<>();

        for (Piece piece : player.getPieces()) {
            if (piece.isCompleted()) continue;

            if (piece.isInBase()) {
                if (roll == 6) movable.add(piece);
            } else {
                trackPositions.computeIfAbsent(piece.getCurrentPosition(), k -> new ArrayList<>()).add(piece);
            }
        }

        for (List<Piece> grouped : trackPositions.values()) {
            if (grouped.size() >= 2) {
                movable.add(new Block(grouped));
                movable.addAll(grouped);
            } else {
                movable.add(grouped.get(0));
            }
        }
        return movable;
    }

    private String buildTokenId(BoardToken token) {
        if (token == null) return "NONE";
        if (token.getTokenSize() == 1) return token.getComponentPieces().get(0).getId();
        StringBuilder sb = new StringBuilder("BLOCK[");
        for (Piece p : token.getComponentPieces()) sb.append(p.getId()).append(",");
        return sb.substring(0, sb.length() - 1) + "]";
    }

    private String formatSection31Message(PieceColor color, int roll, String tokenId, BoardToken token, int startPos, int endPos, boolean captured, Player player) {
        String colName = color.name().toLowerCase();
        StringBuilder sb = new StringBuilder();
        sb.append("[").append(colName).append("] player rolled ").append(roll).append(".");

        if (token == null || tokenId.equals("NONE") || startPos == endPos) {
            sb.append(" [").append(colName).append("] player has no valid moves.");
        } else if (startPos == -1) {
            sb.append(" [").append(colName).append("] player moves piece ").append(tokenId)
                    .append(" to the starting point. [").append(colName).append("] player now has ")
                    .append(player.getPieces().stream().filter(p -> !p.isInBase() && !p.isCompleted()).count())
                    .append("/4 pieces on the board and ")
                    .append(player.getPieces().stream().filter(Piece::isInBase).count()).append("/4 pieces on the base.");
        } else {
            sb.append(" [").append(colName).append("] moves piece ").append(tokenId)
                    .append(" from location L").append(startPos).append(" to L").append(endPos)
                    .append(" by ").append(roll).append(" units in ")
                    .append(token.getDirection().name().toLowerCase().replace('_', '-')).append(" direction.");
        }

        if (captured) {
            sb.append(" [").append(colName).append("] piece ").append(tokenId)
                    .append(" lands on square L").append(endPos).append(", captures opponent piece, and returns it to the base.");
        }
        return sb.toString();
    }

    public Board getBoard() { return board; }
    public Map<PieceColor, Player> getPlayers() { return Collections.unmodifiableMap(players); }
    public boolean isGameOver() { return players.values().stream().anyMatch(Player::hasWon); }
}