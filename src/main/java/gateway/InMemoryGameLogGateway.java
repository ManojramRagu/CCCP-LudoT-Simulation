package gateway;

import dto.GameEventDTO;
import observer.GameEventObserver;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class InMemoryGameLogGateway implements GameLogGateway, GameEventObserver {
    private final List<GameEventDTO> events = new ArrayList<>();

    @Override
    public void logEvent(GameEventDTO event) {
        if (event != null) {
            events.add(event);
            System.out.println(event.eventDescription());
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
