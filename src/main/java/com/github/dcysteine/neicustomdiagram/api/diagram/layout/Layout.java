package com.github.dcysteine.neicustomdiagram.api.diagram.layout;

import com.github.dcysteine.neicustomdiagram.api.diagram.Diagram;
import com.github.dcysteine.neicustomdiagram.api.diagram.DiagramState;
import com.github.dcysteine.neicustomdiagram.api.diagram.interactable.Interactable;
import com.github.dcysteine.neicustomdiagram.api.draw.Dimension;
import com.github.dcysteine.neicustomdiagram.api.draw.Drawable;
import com.google.auto.value.AutoValue;
import com.google.auto.value.extension.toprettystring.ToPrettyString;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Iterables;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Immutable class representing a diagram layout.
 *
 * <p>Slots and slot groups in the layout are associated with a string key, so that they can easily
 * be referenced when placing stacks.
 */
@AutoValue
public abstract class Layout implements Drawable {
    /** Interface to act as a common ancestor of {@link SlotKey} and {@link SlotGroupKey}. */
    public interface Key {
        String key();
    }

    /** Class that wraps string; used to add type-checking of slot keys and slot group keys. */
    @AutoValue
    public abstract static class SlotKey implements Key {
        public static SlotKey create(String key) {
            return new AutoValue_Layout_SlotKey(key);
        }

        @Override
        public abstract String key();
    }

    /** Class that wraps string; used to add type-checking of slot keys and slot group keys. */
    @AutoValue
    public abstract static class SlotGroupKey implements Key {
        public static SlotGroupKey create(String key) {
            return new AutoValue_Layout_SlotGroupKey(key);
        }

        @Override
        public abstract String key();
    }

    public abstract ImmutableList<Lines> lines();
    public abstract ImmutableList<Drawable> labels();
    public abstract ImmutableList<Interactable> interactables();
    public abstract ImmutableMap<SlotKey, Slot> slots();
    public abstract ImmutableMap<SlotGroupKey, SlotGroup> slotGroups();

    public Optional<Slot> slot(SlotKey key) {
        return Optional.ofNullable(slots().get(key));
    }

    public Optional<SlotGroup> slotGroup(SlotGroupKey key) {
        return Optional.ofNullable(slotGroups().get(key));
    }

    public Iterable<Slot> allSlots() {
        List<Iterable<Slot>> allSlots = new ArrayList<>(slotGroups().size() + 1);

        allSlots.add(slots().values());
        slotGroups().values().forEach(slotGroup -> allSlots.add(slotGroup.slots()));

        return Iterables.concat(allSlots);
    }

    @Override
    public Dimension maxDimension() {
        return Drawable.computeMaxDimension(drawables());
    }

    @Override
    public void draw(DiagramState diagramState) {
        drawables().forEach(drawable -> drawable.draw(diagramState));
    }

    /**
     * Interactables are not included here, because they will be handled by {@link Diagram}.
     *
     * <p>This means that interactables <b>will not be included</b> in the calculations for
     * {@link #maxX()} and {@link #maxY()}!
     */
    private Iterable<Drawable> drawables() {
        return Iterables.concat(lines(), slotGroups().values(), slots().values(), labels());
    }

    @ToPrettyString
    public abstract String toPrettyString();

    public static Builder builder() {
        return new AutoValue_Layout.Builder();
    }

    public abstract Builder toBuilder();

    @AutoValue.Builder
    public abstract static class Builder {
        public abstract Builder setLines(Iterable<Lines> lines);
        public abstract ImmutableList.Builder<Lines> linesBuilder();
        public abstract Builder setLabels(Iterable<? extends Drawable> labels);
        public abstract ImmutableList.Builder<Drawable> labelsBuilder();
        public abstract Builder setInteractables(Iterable<? extends Interactable> interactables);
        public abstract ImmutableList.Builder<Interactable> interactablesBuilder();
        public abstract Builder setSlots(Map<SlotKey, Slot> slots);
        public abstract ImmutableMap.Builder<SlotKey, Slot> slotsBuilder();
        public abstract Builder setSlotGroups(Map<SlotGroupKey, SlotGroup> slotGroups);
        public abstract ImmutableMap.Builder<SlotGroupKey, SlotGroup> slotGroupsBuilder();

        public Builder addLines(Lines lines) {
            linesBuilder().add(lines);
            return this;
        }

        public Builder addAllLines(Iterable<Lines> lines) {
            linesBuilder().addAll(lines);
            return this;
        }

        public Builder addLabel(Drawable label) {
            labelsBuilder().add(label);
            return this;
        }

        public Builder addAllLabels(Iterable<? extends Drawable> labels) {
            labelsBuilder().addAll(labels);
            return this;
        }

        public Builder addInteractable(Interactable interactable) {
            interactablesBuilder().add(interactable);
            return this;
        }

        public Builder addAllInteractables(Iterable<? extends Interactable> interactables) {
            interactablesBuilder().addAll(interactables);
            return this;
        }

        public Builder putSlot(SlotKey key, Slot slot) {
            slotsBuilder().put(key, slot);
            return this;
        }

        public Builder putAllSlots(Map<SlotKey, Slot> slots) {
            slotsBuilder().putAll(slots);
            return this;
        }

        public Builder putSlotGroup(SlotGroupKey key, SlotGroup slotGroup) {
            slotGroupsBuilder().put(key, slotGroup);
            return this;
        }

        public Builder putAllSlotGroups(Map<SlotGroupKey, SlotGroup> slotGroups) {
            slotGroupsBuilder().putAll(slotGroups);
            return this;
        }

        public Builder addSubLayout(Layout layout) {
            addAllLines(layout.lines());
            addAllLabels(layout.labels());
            addAllInteractables(layout.interactables());
            putAllSlots(layout.slots());
            putAllSlotGroups(layout.slotGroups());
            return this;
        }

        public abstract Layout build();
    }
}