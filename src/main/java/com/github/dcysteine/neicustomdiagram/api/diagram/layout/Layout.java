package com.github.dcysteine.neicustomdiagram.api.diagram.layout;

import com.github.dcysteine.neicustomdiagram.api.diagram.interactable.Interactable;
import com.github.dcysteine.neicustomdiagram.api.draw.Drawable;
import com.github.dcysteine.neicustomdiagram.api.draw.Ticker;
import com.google.auto.value.AutoValue;
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
    public abstract ImmutableList<Lines> lines();
    public abstract ImmutableList<Drawable> labels();
    public abstract ImmutableList<Interactable> interactables();
    public abstract ImmutableMap<String, Slot> slots();
    public abstract ImmutableMap<String, SlotGroup> slotGroups();

    public Optional<Slot> slot(String key) {
        return Optional.ofNullable(slots().get(key));
    }

    public Optional<SlotGroup> slotGroup(String key) {
        return Optional.ofNullable(slotGroups().get(key));
    }

    public Iterable<Slot> allSlots() {
        List<Iterable<Slot>> allSlots = new ArrayList<>(slotGroups().size() + 1);

        allSlots.add(slots().values());
        slotGroups().values().forEach(slotGroup -> allSlots.add(slotGroup.slots()));

        return Iterables.concat(allSlots);
    }

    @Override
    public void draw(Ticker ticker) {
        lines().forEach(Lines::draw);
        slotGroups().values().forEach(slotGroup -> slotGroup.draw(ticker));
        slots().values().forEach(slot -> slot.draw(ticker));
        labels().forEach(label -> label.draw(ticker));
        // Interactables are drawn in the foreground, and will be handled by Diagram.draw()
    }

    public static Builder builder() {
        return new AutoValue_Layout.Builder();
    }

    public abstract Builder toBuilder();

    @AutoValue.Builder
    public abstract static class Builder {
        public abstract Builder setLines(Iterable<Lines> lines);
        public abstract ImmutableList.Builder<Lines> linesBuilder();
        public abstract Builder setLabels(Iterable<Drawable> labels);
        public abstract ImmutableList.Builder<Drawable> labelsBuilder();
        public abstract Builder setInteractables(Iterable<Interactable> interactables);
        public abstract ImmutableList.Builder<Interactable> interactablesBuilder();
        public abstract Builder setSlots(Map<String, Slot> slots);
        public abstract ImmutableMap.Builder<String, Slot> slotsBuilder();
        public abstract Builder setSlotGroups(Map<String, SlotGroup> slotGroups);
        public abstract ImmutableMap.Builder<String, SlotGroup> slotGroupsBuilder();

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

        public Builder addAllLabels(Iterable<Drawable> labels) {
            labelsBuilder().addAll(labels);
            return this;
        }

        public Builder addInteractable(Interactable interactable) {
            interactablesBuilder().add(interactable);
            return this;
        }

        public Builder addAllInteractables(Iterable<Interactable> interactables) {
            interactablesBuilder().addAll(interactables);
            return this;
        }

        public Builder putSlot(String key, Slot slot) {
            slotsBuilder().put(key, slot);
            return this;
        }

        public Builder putAllSlots(Map<String, Slot> slots) {
            slotsBuilder().putAll(slots);
            return this;
        }

        public Builder putSlotGroup(String key, SlotGroup slotGroup) {
            slotGroupsBuilder().put(key, slotGroup);
            return this;
        }

        public Builder putAllSlotGroups(Map<String, SlotGroup> slotGroups) {
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