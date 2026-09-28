package command;

public interface GameCommand {
    void execute();
    boolean isExecutable();
    boolean hasCaptured();
    int getPreviousPosition();
}
