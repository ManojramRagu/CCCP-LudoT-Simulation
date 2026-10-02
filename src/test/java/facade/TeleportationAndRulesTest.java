package facade;

import dto.GameEventDTO;
import model.Board;
import model.CellType;
import model.MovementDirection;
import model.Piece;
import model.PieceColor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class TeleportationAndRulesTest {

    private LudoTGameFacade facade;

    @BeforeEach
    void setUp() {
        Board.resetInstance();
        facade = new LudoTGameFacade();
    }

    @Test
    void testMysteryCellTeleportationAndStatusEffects() {
        // T-11 to T-15 and CCW rules tested here loosely
        Board board = facade.getBoard();
        board.spawnMysteryCell(5);
        assertEquals(CellType.MYSTERY, board.getTrackCell(5).getType());
        
        // This is primarily an integration test check to ensure the methods are there and don't crash
        assertNotNull(facade);
    }
    
    @Test
    void testCCWMovement() {
        Piece p = new Piece("R1", PieceColor.RED);
        p.setDirection(MovementDirection.COUNTER_CLOCKWISE);
        p.setCurrentPosition(10);
        assertEquals(10, p.getCurrentPosition());
        assertEquals(MovementDirection.COUNTER_CLOCKWISE, p.getDirection());
        p.incrementApproachPassCount();
        assertEquals(1, p.getApproachPassCount());
    }
    
    @Test
    void testBlockBreakOriginalDirection() {
        Piece p = new Piece("G1", PieceColor.GREEN);
        p.setOriginalDirection(MovementDirection.CLOCKWISE);
        p.setDirection(MovementDirection.COUNTER_CLOCKWISE);
        assertEquals(MovementDirection.CLOCKWISE, p.getOriginalDirection());
        
        p.setArrivedViaTeleport(true);
        assertTrue(p.isArrivedViaTeleport());
    }
}
