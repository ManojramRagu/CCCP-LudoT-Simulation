package gateway;

import dto.GameEventDTO;
import java.util.List;

public interface GameLogGateway {
    void logEvent(GameEventDTO event);
    List<GameEventDTO> getAllEvents();
    void clear();
}
