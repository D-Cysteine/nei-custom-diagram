package com.github.dcysteine.neicustomdiagram.api.diagram;

import codechicken.nei.NEIClientUtils;
import com.github.dcysteine.neicustomdiagram.mod.config.ConfigOptions;

/**
 * Class that keeps track of any mutable state for diagrams.
 *
 * <p>The base implementation just keeps track of the tick count, for cycling through groups of
 * components or animations. However, this class can be extended to include custom state.
 */
public class DiagramState {
    /**
     * Defines the length of a cycle, in ticks.
     *
     * <p>Cycles are used to time slow animations, such as cycling through components to show in a
     * slot.
     */
    public static final int TICKS_PER_CYCLE = 20;

    private int ticks;

    public DiagramState() {
        ticks = 0;
    }

    public void tick() {
        if (ConfigOptions.CTRL_FAST_FORWARD.get() && NEIClientUtils.controlKey()) {
            if (NEIClientUtils.shiftKey()) {
                // <Ctrl + Shift> fast-forwards backwards.
                ticks -= DiagramState.TICKS_PER_CYCLE;
            } else {
                ticks += DiagramState.TICKS_PER_CYCLE;
            }
        } else if (!NEIClientUtils.shiftKey()) {
            ticks++;
        }

        // Prevent going too far backwards, as well as handle the unlikely case of overflow.
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