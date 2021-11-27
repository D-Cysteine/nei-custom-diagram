package com.github.dcysteine.neicustomdiagram.api.diagram;

import codechicken.nei.NEIClientUtils;
import com.github.dcysteine.neicustomdiagram.api.draw.GuiManager;
import com.github.dcysteine.neicustomdiagram.main.config.ConfigOptions;

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

    /** Due to backwards scrolling, {@code ticks} may be negative! */
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
    }

    /** "Scrolls" the tick counter by one cycle at a time. */
    public void scroll(GuiManager.ScrollDirection direction) {
        ticks += direction.factor * TICKS_PER_CYCLE;
    }

    /** Due to backwards scrolling, {@code ticks()} may be negative! */
    public int ticks() {
        return ticks;
    }

    /** Due to backwards scrolling, {@code cycle()} may be negative! */
    public int cycle() {
        return ticks / TICKS_PER_CYCLE;
    }

    public int cycleIndex(int maxIndex) {
        // Because cycle() can be negative, we must not use the modulus (%) operator here.
        //
        // In Java, modulus of a negative number and a positive number may return a negative value,
        // which would cause ArrayIndexOutOfBoundsException to be thrown downstream.
        //
        // Math.floorMod() will always return a non-negative value.
        return Math.floorMod(cycle(), maxIndex);
    }
}