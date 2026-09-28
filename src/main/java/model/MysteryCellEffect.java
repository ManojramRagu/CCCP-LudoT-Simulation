package model;

import java.util.Random;

public enum MysteryCellEffect {
    TELEPORT_FORWARD,
    SPEED_DOUBLE,
    MOVEMENT_RESTRICTION,
    BONUS_ROLL;

    public static MysteryCellEffect getRandomEffect(Random random) {
        MysteryCellEffect[] effects = values();
        return effects[random.nextInt(effects.length)];
    }
}
