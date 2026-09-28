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

    public void registerObserver(GameEventObserver observer) {
        if (observer != null && !observers.contains(observer)) {
            observers.add(observer);
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
        
        int startPos = chosenPiece != null ? chosenPiece.getCurrentPosition() : -1;
        boolean wasInBase = chosenPiece != null && chosenPiece.isInBase();

        command.execute();

        boolean captured = command.hasCaptured();
        int endPos = chosenPiece != null ? chosenPiece.getCurrentPosition() : -1;
        String pieceId = chosenPiece != null ? chosenPiece.getId() : "NONE";

        String desc = formatEventDescription(player, chosenPiece, roll, wasInBase, startPos, endPos, captured);
        GameEventDTO event = new GameEventDTO(currentTurn, activeColor, roll, pieceId, startPos, endPos, captured, desc);
        
        gameLog.add(event);
        notifyObservers(event);

        return event;
    }

    private String formatEventDescription(Player player, Piece piece, int roll, boolean wasInBase, int startPos, int endPos, boolean captured) {
        if (piece == null) {
            return player.getColor() + " player rolled " + roll + " but has no valid moves.";
        }
        if (wasInBase && !piece.isInBase()) {
            return player.getColor() + " player moves piece " + piece.getId() + " to the starting point. " +
                   player.getColor() + " player now has " + player.getActivePiecesOnBoardCount() + "/4 pieces on the board and " +
                   player.getPiecesInBaseCount() + "/4 pieces on the base.";
        }
        if (captured) {
            return player.getColor() + " piece " + piece.getId() + " lands on square L" + endPos + ", captures an opponent piece, and returns it to base. " +
                   player.getColor() + " player now has " + player.getActivePiecesOnBoardCount() + "/4 pieces on board and " +
                   player.getPiecesInBaseCount() + "/4 pieces in base.";
        }
        String dir = piece.getDirection() == MovementDirection.CLOCKWISE ? "clockwise" : "counter-clockwise";
        return player.getColor() + " moves piece " + piece.getId() + " from location L" + startPos + " to L" + endPos +
               " by " + roll + " units in " + dir + " direction.";
    }

    private void notifyObservers(GameEventDTO event) {
        for (GameEventObserver observer : observers) {
            observer.onGameEvent(event);
        }
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
