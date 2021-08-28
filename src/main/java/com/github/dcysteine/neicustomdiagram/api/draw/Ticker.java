package com.github.dcysteine.neicustomdiagram.api.draw;

import codechicken.nei.NEIClientUtils;
import com.github.dcysteine.neicustomdiagram.api.Config;

/** Class that keeps track of the tick count, for drawing animations. */
public final class Ticker {
    /**
     * Defines the length of a cycle, in ticks.
     *
     * <p>Cycles are used to time slow animations, such as cycling through components to show in a
     * slot.
     */
    public static final int TICKS_PER_CYCLE = 20;

    private int ticks;

    public Ticker() {
        ticks = 0;
    }

    public void tick() {
        if (Config.Options.CTRL_FAST_FORWARD.get() && NEIClientUtils.controlKey()) {
            ticks += Ticker.TICKS_PER_CYCLE;
        } else {
            ticks++;
        }

        // Just in case we somehow overflow.
        //
        // Note that Java's implementation of modulus returns a negative result for negative
        // numbers, so having ticks < 0 would probably cause IndexOutOfBoundsException for callers
        // of cycleIndex().
        if (ticks < 0) {
            ticks = 0;
        }
    }

    public int ticks() {
        return ticks;
    }

    public int cycle() {
        return ticks / TICKS_PER_CYCLE;
    }

    public int cycleIndex(int maxIndex) {
        return cycle() % maxIndex;
    }
}
