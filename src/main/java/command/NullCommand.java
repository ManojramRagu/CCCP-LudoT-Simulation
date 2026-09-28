package command;

public class NullCommand implements GameCommand {
    @Override
    public void execute() {
    }

    @Override
    public boolean isExecutable() {
        return false;
    }

    @Override
    public boolean hasCaptured() {
        return false;
    }

    @Override
    public int getPreviousPosition() {
        return -1;
    }
}
