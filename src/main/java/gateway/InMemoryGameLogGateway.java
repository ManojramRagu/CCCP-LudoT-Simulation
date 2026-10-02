package gateway;

import dto.GameEventDTO;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class InMemoryGameLogGateway implements GameLogGateway {
    private final List<GameEventDTO> events = new ArrayList<>();

    @Override
    public void logEvent(GameEventDTO event) {
        if (event != null) {
            events.add(event);
            if (event.eventDescription() != null && !event.eventDescription().isEmpty()) {
                System.out.println(event.eventDescription());
            }
        }
    }

    @Override
    public void onGameEvent(GameEventDTO event) {
        logEvent(event);
    }

    @Override
    public List<GameEventDTO> getAllEvents() {
        return Collections.unmodifiableList(events);
    }

    @Override
    public void clear() {
        events.clear();
    }
}
