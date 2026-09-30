package facade;

import dto.GameEventDTO;
import model.Board;
import model.PieceColor;
import observer.GameEventObserver;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;

class FacadeAndObserverTest {

    private LudoTGameFacade facade;

    @BeforeEach
    void setUp() {
        Board.resetInstance();
        facade = new LudoTGameFacade();
    }

    @Test
    @DisplayName("Facade initializes board and players cleanly")
    void testFacadeInitialization() {
        assertNotNull(facade.getBoard());
        assertEquals(4, facade.getPlayers().size());
        assertFalse(facade.isGameOver());
    }

    @Test
    @DisplayName("Facade observer registration and turn notification work via Observer Pattern")
    void testObserverRegistrationAndNotification() {
        AtomicInteger eventCount = new AtomicInteger(0);
        GameEventObserver testObserver = event -> eventCount.incrementAndGet();

        facade.addObserver(testObserver);
        facade.playTurn(PieceColor.RED);

        // Verifies the observer successfully caught the DTO broadcast
        assertEquals(1, eventCount.get());
    }

    @Test
    @DisplayName("GameEventDTO record accessors provide immutable event data")
    void testGameEventDTOAccessors() {
        GameEventDTO dto = new GameEventDTO(1, PieceColor.RED, 6, "R1", -1, 26, false, "Red moved R1 to X");

        assertEquals(1, dto.turnNumber());
        assertEquals(PieceColor.RED, dto.playerColor());
        assertEquals(6, dto.diceRoll());

        // Corrected to match our updated DTO variable names
        assertEquals("R1", dto.tokenId());
        assertEquals(-1, dto.startPosition());
        assertEquals(26, dto.endPosition());
        assertFalse(dto.capturedOpponent());
        assertEquals("Red moved R1 to X", dto.eventDescription());
    }

    @Test
    @DisplayName("Facade playTurn generates non-null GameEventDTO")
    void testPlayTurnEventGeneration() {
        GameEventDTO event = facade.playTurn(PieceColor.YELLOW);
        assertNotNull(event);
        assertEquals(PieceColor.YELLOW, event.playerColor());
    }

    @Test
    @DisplayName("Facade players map is unmodifiable to preserve encapsulation")
    void testUnmodifiablePlayersMap() {
        assertThrows(UnsupportedOperationException.class, () -> {
            facade.getPlayers().clear();
        });
    }

    @Test
    @DisplayName("Facade tracks cumulative turns across successive playTurn calls")
    void testCumulativeTurnTracking() {
        GameEventDTO e1 = facade.playTurn(PieceColor.RED);
        GameEventDTO e2 = facade.playTurn(PieceColor.GREEN);

        assertEquals(1, e1.turnNumber());
        assertEquals(2, e2.turnNumber());
    }

    @Test
    @DisplayName("Facade supports multiple independent observers")
    void testMultipleObservers() {
        AtomicInteger count1 = new AtomicInteger(0);
        AtomicInteger count2 = new AtomicInteger(0);

        facade.addObserver(e -> count1.incrementAndGet());
        facade.addObserver(e -> count2.incrementAndGet());

        facade.playTurn(PieceColor.RED);

        assertEquals(1, count1.get());
        assertEquals(1, count2.get());
    }
}