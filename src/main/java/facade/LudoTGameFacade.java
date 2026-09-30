package facade;

import command.CommandFactory;
import command.GameCommand;
import dto.GameEventDTO;
import model.*;
import observer.GameEventObserver;
import strategy.AggressiveStrategy;
import strategy.BalancedStrategy;
import strategy.PlayerStrategy;

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
        this.board = new Board();
        this.dice = new Dice();
        this.players = new EnumMap<>(PieceColor.class);
        this.strategies = new EnumMap<>(PieceColor.class);
        this.gameLog = new ArrayList<>();
        this.observers = new ArrayList<>();
        this.currentTurn = 0;
        initializeGame();
    }

    public void addObserver(GameEventObserver observer) {
        if (observer != null && !observers.contains(observer)) {
            observers.add(observer);
        }
    }

    public void removeObserver(GameEventObserver observer) {
        observers.remove(observer);
    }

    private void notifyObservers(GameEventDTO event) {
        for (GameEventObserver obs : observers) {
            obs.onGameEvent(event);
        }
    }

    private void initializeGame() {
        CoinToss coinToss = new CoinToss();
        for (PieceColor color : PieceColor.values()) {
            Player player = new Player(color.name() + " Player", color);
            players.put(color, player);

            if (color == PieceColor.RED) {
                strategies.put(color, new AggressiveStrategy());
            } else {
                strategies.put(color, new BalancedStrategy());
            }

            MovementDirection direction = coinToss.flip();
            for (Piece piece : player.getPieces()) {
                piece.setDirection(direction);
            }
        }
    }

    public GameEventDTO playTurn(PieceColor activeColor) {
        currentTurn++;
        Player player = players.get(activeColor);
        PlayerStrategy strategy = strategies.get(activeColor);

        int roll = dice.roll();
        List<Piece> movablePieces = findMovablePieces(player, roll);

        Piece chosenPiece = strategy.selectPieceToMove(player, movablePieces, board, roll);
        GameCommand command = CommandFactory.createCommand(chosenPiece, roll, board, player);
        command.execute();

        boolean captured = command.hasCaptured();
        int startPos = chosenPiece != null ? command.getPreviousPosition() : -1;
        int endPos = chosenPiece != null ? chosenPiece.getCurrentPosition() : -1;
        String pieceId = chosenPiece != null ? chosenPiece.getId() : "NONE";

        String desc = formatSection31Message(activeColor, roll, pieceId, chosenPiece, startPos, endPos, captured, player);
        GameEventDTO event = new GameEventDTO(currentTurn, activeColor, roll, pieceId, startPos, endPos, captured, desc);
        gameLog.add(event);
        notifyObservers(event);

        return event;
    }

    private String formatSection31Message(PieceColor color, int roll, String pieceId, Piece piece, int startPos, int endPos, boolean captured, Player player) {
        String colName = color.name().toLowerCase();
        StringBuilder sb = new StringBuilder();
        sb.append(colName).append(" player rolled ").append(roll).append(".");

        if (piece == null || pieceId.equals("NONE")) {
            sb.append(" ").append(colName).append(" player has no valid moves.");
        } else if (startPos == -1 && !piece.isInBase()) {
            sb.append(" ").append(colName).append(" player moves piece ").append(pieceId)
              .append(" to the starting point. ").append(colName).append(" player now has ")
              .append(player.getActivePiecesOnBoardCount()).append("/4 pieces on the board and ")
              .append(player.getPiecesInBaseCount()).append("/4 pieces on the base.");
        } else {
            sb.append(" ").append(colName).append(" moves piece ").append(pieceId)
              .append(" from location L").append(startPos).append(" to L").append(endPos)
              .append(" by ").append(roll).append(" units in ")
              .append(piece.getDirection().name().toLowerCase().replace('_', '-')).append(" direction.");
        }

        if (captured) {
            sb.append(" ").append(colName).append(" piece ").append(pieceId)
              .append(" lands on square L").append(endPos).append(", captures opponent piece, and returns it to the base.");
        }

        return sb.toString();
    }

    private List<Piece> findMovablePieces(Player player, int roll) {
        List<Piece> movable = new ArrayList<>();
        for (Piece piece : player.getPieces()) {
            if (piece.isCompleted()) {
                continue;
            }
            if (piece.isInBase() && roll == 6) {
                movable.add(piece);
            } else if (!piece.isInBase()) {
                movable.add(piece);
            }
        }
        return movable;
    }

    public Board getBoard() {
        return board;
    }

    public Map<PieceColor, Player> getPlayers() {
        return Collections.unmodifiableMap(players);
    }

    public List<GameEventDTO> getGameLog() {
        return Collections.unmodifiableList(gameLog);
    }

    public boolean isGameOver() {
        return players.values().stream().anyMatch(Player::hasWon);
    }
}
