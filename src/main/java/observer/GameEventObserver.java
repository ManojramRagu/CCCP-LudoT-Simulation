package observer;

import dto.GameEventDTO;

public interface GameEventObserver {
    void onGameEvent(GameEventDTO event);
}
