package gateway;

import dto.GameEventDTO;
import observer.GameEventObserver;
import java.util.List;

public interface GameLogGateway extends GameEventObserver {
    void logEvent(GameEventDTO event);
    List<GameEventDTO> getAllEvents();
    void clear();
}
