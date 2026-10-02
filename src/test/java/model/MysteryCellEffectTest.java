package model;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import java.util.Random;

class MysteryCellEffectTest {
    @Test
    void testGetRandomEffect() {
        Random rand = new Random();
        MysteryCellEffect effect = MysteryCellEffect.getRandomEffect(rand);
        assertNotNull(effect);
        assertTrue(java.util.Arrays.asList(MysteryCellEffect.values()).contains(effect));
    }
}
