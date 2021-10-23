package com.github.dcysteine.neicustomdiagram.generators.gregtech5.circuits;

import com.github.dcysteine.neicustomdiagram.api.diagram.component.ItemComponent;
import com.google.auto.value.AutoValue;
import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@AutoValue
abstract class CircuitLine {
    /**
     * The highest tier of circuit that will appear in any circuit line.
     *
     * <p>This is {@code 0}-indexed, so the number of tiers that we'll support is actually equal to
     * {@code MAX_TIER + 1}.
     *
     * <p>If you adjust this, don't forget to tweak the overview diagram layout in
     * {@link LayoutHandler}! The {@code y}-coordinate of slots below the circuit lines slots will
     * probably need to be adjusted.
     */
    static final int MAX_TIER = 10;

    /** The circuit boards associated with this circuit line. */
    abstract ImmutableList<ItemComponent> boards();

    /** The index of the tier of the first circuit in this circuit line. */
    abstract int startTier();

    /** Circuits in this circuit line, in order of tier, ascending. */
    abstract ImmutableList<ItemComponent> circuits();

    static Builder builder() {
        return new Builder();
    }

    static final class Builder {
        private final List<ItemComponent> boards;
        private int startTier;
        private final List<ItemComponent> circuits;

        private Builder() {
            this.boards = new ArrayList<>();
            this.startTier = 0;
            this.circuits = new ArrayList<>();
        }

        Builder addBoard(ItemComponent board) {
            boards.add(board);
            return this;
        }

        Builder addBoards(ItemComponent... boards) {
            this.boards.addAll(Arrays.asList(boards));
            return this;
        }

        Builder setStartTier(int tier) {
            startTier = tier;
            return this;
        }

        Builder addCircuit(ItemComponent circuit) {
            circuits.add(circuit);
            return this;
        }

        Builder addCircuits(ItemComponent... circuits) {
            this.circuits.addAll(Arrays.asList(circuits));
            return this;
        }

        CircuitLine build() {
            Preconditions.checkArgument(
                    startTier + circuits.size() <= MAX_TIER + 1,
                    "Too many circuits: (%d + %d) > (%d + 1)",
                    startTier, circuits.size(), MAX_TIER);

            return new AutoValue_CircuitLine(
                    ImmutableList.copyOf(boards), startTier, ImmutableList.copyOf(circuits));
        }
    }
}
