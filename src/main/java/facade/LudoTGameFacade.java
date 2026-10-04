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

        System.out.println(String.format("[%s] player rolled %d.", color.name().toLowerCase(), roll));
        
        List<BoardToken> allTokens = extractTokens(player);
        List<BoardToken> movableTokens = new ArrayList<>();
        for (BoardToken token : allTokens) {
            if (command.CommandFactory.createMoveCommand(token, player, board, roll).isExecutable()) {
                movableTokens.add(token);
            }
        }
        
        BoardToken selectedToken = player.getStrategy().selectTokenToMove(player, movableTokens, board, roll);

        int startPos = -1;
        String pieceId = "None";
        boolean captured = false;
        String description = null;

        if (selectedToken != null) {
            startPos = selectedToken.getCurrentPosition();
            pieceId = formatTokenId(selectedToken);
            boolean wasInHomeStraight = selectedToken.getComponentPieces().getFirst().getState() == PieceState.HOME_STRAIGHT;

            command.GameCommand genericCommand = command.CommandFactory.createMoveCommand(selectedToken, player, board, roll);
            if (genericCommand.isExecutable()) {
                genericCommand.execute();
                captured = genericCommand.hasCaptured();

                boolean landedOnMystery = selectedToken.getCurrentPosition() >= 0 
                        && selectedToken.getComponentPieces().getFirst().getState() == PieceState.STANDARD_TRACK
                        && board.getTrackCell(selectedToken.getCurrentPosition()).getType() == CellType.MYSTERY;

                if (captured) {
                    description = String.format("[%s] piece %s lands on square L%d, captures [%s] piece %s, and returns it to the base.", 
                            color.name().toLowerCase(), pieceId, selectedToken.getCurrentPosition(), 
                            genericCommand.getCapturedOpponentColor().name().toLowerCase(), genericCommand.getCapturedOpponentName());
                } else if (startPos == -1) {
                    description = String.format("[%s] player moves piece %s to the starting point.", color.name().toLowerCase(), pieceId);
                } else if (landedOnMystery) {
                    description = null;
                } else if (startPos != -1 && startPos == selectedToken.getCurrentPosition()) {
                    description = null;
                } else {
                    String dirStr = selectedToken.getDirection() == MovementDirection.CLOCKWISE ? "clockwise" : "counter-clockwise";
                    String startLocStr = wasInHomeStraight ? color.name().toLowerCase() + "homepath" + startPos : "L" + startPos;
                    boolean isNowInHomeStraight = selectedToken.getComponentPieces().getFirst().getState() == PieceState.HOME_STRAIGHT;
                    boolean isNowCompleted = selectedToken.getComponentPieces().getFirst().getState() == PieceState.COMPLETED;
                    String endLocStr;
                    if (isNowCompleted) {
                        endLocStr = "Home";
                    } else if (isNowInHomeStraight) {
                        endLocStr = color.name().toLowerCase() + "homepath" + selectedToken.getCurrentPosition();
                    } else {
                        endLocStr = "L" + selectedToken.getCurrentPosition();
                    }
                    
                    int actualSteps = roll;
                    if (selectedToken.getTokenSize() > 1) {
                        actualSteps = roll / selectedToken.getTokenSize();
                    }
                    Piece firstP = selectedToken.getComponentPieces().getFirst();
                    if (firstP.isEnergized()) actualSteps *= 2;
                    if (firstP.isSick()) actualSteps /= 2;

                    description = String.format("[%s] moves piece %s from location %s to %s by %d units in %s direction.", 
                            color.name().toLowerCase(), pieceId, startLocStr, endLocStr, actualSteps, dirStr);
                }
                
                if (startPos == -1 || captured) {
                    long onBoard = player.getPieces().stream().filter(p -> !p.isInBase() && !p.isCompleted()).count();
                    long inBase = player.getPieces().stream().filter(Piece::isInBase).count();
                    System.out.println(String.format("[%s] player now has %d/4 on pieces on the board and %d/4 pieces on the base.", color.name().toLowerCase(), onBoard, inBase));
                }
                
                if (landedOnMystery) {
                    boolean teleportCaptured = handleTeleportation(selectedToken, color);
                    if (teleportCaptured) captured = true;
                }
            } else {
                description = String.format("[%s] player has no valid moves.", color.name().toLowerCase());
            }
        } else {
            description = String.format("[%s] player has no valid moves.", color.name().toLowerCase());
        }

        GameEventDTO event = new GameEventDTO(turnCounter, color, roll, pieceId, startPos,
                selectedToken != null ? selectedToken.getCurrentPosition() : -1, captured, description != null ? description : "");
        notifyObservers(event);
        return event;
    }

    private boolean handleTeleportation(BoardToken token, PieceColor color) {
        MysteryCellEffect effect = MysteryCellEffect.getRandomEffect(new Random());
        Piece firstPiece = token.getComponentPieces().getFirst();
        String pieceId = formatTokenId(token);
        String locName = effect.name().replace("TELEPORT_", "");
        locName = locName.substring(0, 1).toUpperCase() + locName.substring(1).toLowerCase();
        
        System.out.println(String.format("[%s] player lands on a mystery cell and is teleported to %s.", color.name().toLowerCase(), locName));

        int currentPos = token.getCurrentPosition();
        Cell currentCell = board.getTrackCell(currentPos);
        for (Piece p : token.getComponentPieces()) {
            currentCell.removePiece(p);
            p.setArrivedViaTeleport(true);
            p.setState(PieceState.STANDARD_TRACK);
        }

        int targetPos = currentPos;
        switch (effect) {
            case TELEPORT_ALPHA:
                targetPos = Board.ALPHA_CELL_INDEX;
                System.out.println(String.format("[%s] piece %s teleported to Alpha.", color.name().toLowerCase(), pieceId));
                if (new Random().nextBoolean()) {
                    for (Piece p : token.getComponentPieces()) p.setEnergizedRounds(4);
                    System.out.println(String.format("[%s] piece %s feels energized, and movement speed doubles.", color.name().toLowerCase(), pieceId));
                } else {
                    for (Piece p : token.getComponentPieces()) p.setSickRounds(4);
                    System.out.println(String.format("[%s] piece %s feels sick, and movement speed halves.", color.name().toLowerCase(), pieceId));
                }
                break;
            case TELEPORT_BETA:
                targetPos = Board.BETA_CELL_INDEX;
                System.out.println(String.format("[%s] piece %s teleported to Beta.", color.name().toLowerCase(), pieceId));
                for (Piece p : token.getComponentPieces()) p.setRestrictedRounds(4);
                System.out.println(String.format("[%s] piece %s attends briefing and cannot move for four rounds.", color.name().toLowerCase(), pieceId));
                break;
            case TELEPORT_GAMMA:
                targetPos = Board.GAMMA_CELL_INDEX;
                System.out.println(String.format("[%s] piece %s teleported to Gamma.", color.name().toLowerCase(), pieceId));
                if (firstPiece.getDirection() == MovementDirection.CLOCKWISE) {
                    for (Piece p : token.getComponentPieces()) p.setDirection(MovementDirection.COUNTER_CLOCKWISE);
                    System.out.println(String.format("The [%s] piece %s, which was moving clockwise, has changed to moving counterclockwise.", color.name().toLowerCase(), pieceId));
                } else {
                    targetPos = Board.BETA_CELL_INDEX;
                    for (Piece p : token.getComponentPieces()) p.setRestrictedRounds(4);
                    System.out.println(String.format("The [%s] piece %s is moving in a counterclockwise direction. Teleporting to Beta from Gamma.", color.name().toLowerCase(), pieceId));
                    System.out.println(String.format("[%s] piece %s attends briefing and cannot move for four rounds.", color.name().toLowerCase(), pieceId));
                }
                break;
            case TELEPORT_APPROACH:
                targetPos = Board.getApproachIndex(color);
                System.out.println(String.format("[%s] piece %s teleported to Approach.", color.name().toLowerCase(), pieceId));
                break;
            case TELEPORT_X:
                targetPos = Board.getStartingIndex(color);
                System.out.println(String.format("[%s] piece %s teleported to X.", color.name().toLowerCase(), pieceId));
                break;
            case TELEPORT_BASE:
                for (Piece p : token.getComponentPieces()) {
                    p.resetToBase();
                }
                System.out.println(String.format("[%s] piece %s teleported to Base.", color.name().toLowerCase(), pieceId));
                return false;
        }

        token.setCurrentPosition(targetPos);
        Cell targetCell = board.getTrackCell(targetPos);
        for (Piece p : token.getComponentPieces()) {
            targetCell.addPiece(p);
        }
        
        if (targetCell.hasOpponentPiece(color)) {
            List<Piece> occupants = new java.util.ArrayList<>(targetCell.getOccupyingPieces());
            long opponentCount = occupants.stream().filter(occ -> occ.getColor() != color).count();
            if (opponentCount > 0 && opponentCount <= token.getTokenSize()) {
                String opponentName = occupants.stream().filter(occ -> occ.getColor() != color).findFirst().get().getId();
                PieceColor oppColor = occupants.stream().filter(occ -> occ.getColor() != color).findFirst().get().getColor();
                for (Piece occupant : occupants) {
                    if (occupant.getColor() != color) {
                        occupant.resetToBase();
                        token.recordCapture(1);
                        targetCell.removePiece(occupant);
                    }
                }
                System.out.println(String.format("[%s] piece %s lands on square L%d, captures [%s] piece %s, and returns it to the base.", color.name().toLowerCase(), pieceId, targetPos, oppColor.name().toLowerCase(), opponentName));
                long onBoard = players.get(color).getPieces().stream().filter(p -> !p.isInBase() && !p.isCompleted()).count();
                long inBase = players.get(color).getPieces().stream().filter(Piece::isInBase).count();
                System.out.println(String.format("[%s] player now has %d/4 on pieces on the board and %d/4 pieces on the base.", color.name().toLowerCase(), onBoard, inBase));
                return true;
            }
        }
        return false;
    }

    public void executePenaltyBreak(PieceColor color) {
        Player player = players.get(color);
        List<BoardToken> tokens = extractTokens(player);
        for (BoardToken token : tokens) {
            if (token instanceof Block block) {
                List<Piece> pieces = block.getComponentPieces();
                Piece survivor = pieces.getFirst();
                Cell currentCell = board.getTrackCell(survivor.getCurrentPosition());

                for (int i = 1; i < pieces.size(); i++) {
                    Piece p = pieces.get(i);
                    currentCell.removePiece(p);
                    int newPos = block.getDirection() == MovementDirection.CLOCKWISE
                            ? (p.getCurrentPosition() + 6) % Board.TOTAL_TRACK_CELLS
                            : (p.getCurrentPosition() - 6 + Board.TOTAL_TRACK_CELLS) % Board.TOTAL_TRACK_CELLS;
                    p.setCurrentPosition(newPos);
                    board.getTrackCell(newPos).addPiece(p);
                }

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
        Map<String, List<Piece>> positionMap = new HashMap<>();

        for (Piece p : player.getPieces()) {
            if (p.isCompleted()) continue;
            String key = p.getState().name() + "_" + p.getCurrentPosition();
            positionMap.computeIfAbsent(key, k -> new ArrayList<>()).add(p);
        }

        for (List<Piece> group : positionMap.values()) {
            if (group.size() > 1 && group.getFirst().getCurrentPosition() != -1) {
                tokens.add(new Block(group));
                tokens.addAll(group);
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