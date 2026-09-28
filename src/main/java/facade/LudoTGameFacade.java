package facade;

import command.CommandFactory;
import command.GameCommand;
import dto.GameEventDTO;
import model.*;
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
    private int currentTurn;

    public LudoTGameFacade() {
        this.board = new Board();
        this.dice = new Dice();
        this.players = new EnumMap<>(PieceColor.class);
        this.strategies = new EnumMap<>(PieceColor.class);
        this.gameLog = new ArrayList<>();
        this.currentTurn = 0;
        initializeGame();
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

        String desc = activeColor + " rolled " + roll + " and moved piece " + pieceId;
        GameEventDTO event = new GameEventDTO(currentTurn, activeColor, roll, pieceId, startPos, endPos, captured, desc);
        gameLog.add(event);

        return event;
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
