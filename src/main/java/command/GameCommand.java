package command;

import model.PieceColor;

public interface GameCommand {
    void execute();
    boolean isExecutable();
    boolean hasCaptured();
    int getPreviousPosition();
    String getCapturedOpponentName();
    PieceColor getCapturedOpponentColor();
}