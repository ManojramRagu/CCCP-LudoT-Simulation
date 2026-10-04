package model;

import java.util.Random;

public enum MysteryCellEffect {
    TELEPORT_ALPHA,
    TELEPORT_BETA,
    TELEPORT_GAMMA,
    TELEPORT_BASE,
    TELEPORT_X,
    TELEPORT_APPROACH;

    public static MysteryCellEffect getRandomEffect(Random random) {
        MysteryCellEffect[] effects = values();
        return effects[random.nextInt(effects.length)];
    }
}
