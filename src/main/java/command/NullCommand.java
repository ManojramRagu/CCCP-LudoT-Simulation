package command;

import model.PieceColor;

public class NullCommand implements GameCommand {
    @Override
    public void execute() { }
    @Override
    public boolean isExecutable() { return false; }
    @Override
    public boolean hasCaptured() { return false; }
    @Override
    public int getPreviousPosition() { return -1; }
    @Override
    public String getCapturedOpponentName() { return ""; }
    @Override
    public PieceColor getCapturedOpponentColor() { return null; }
}