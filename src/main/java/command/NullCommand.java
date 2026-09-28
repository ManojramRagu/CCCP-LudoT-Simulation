package command;

public class NullCommand implements GameCommand {
    @Override
    public void execute() {
    }

    @Override
    public boolean isExecutable() {
        return false;
    }
}
