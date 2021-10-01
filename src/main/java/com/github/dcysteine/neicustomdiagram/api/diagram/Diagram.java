package com.github.dcysteine.neicustomdiagram.api.diagram;

import com.github.dcysteine.neicustomdiagram.api.diagram.component.DisplayComponent;
import com.github.dcysteine.neicustomdiagram.api.diagram.interactable.Interactable;
import com.github.dcysteine.neicustomdiagram.api.diagram.interactable.InteractiveComponentGroup;
import com.github.dcysteine.neicustomdiagram.api.diagram.layout.Layout;
import com.github.dcysteine.neicustomdiagram.api.diagram.layout.Slot;
import com.github.dcysteine.neicustomdiagram.api.diagram.layout.SlotGroup;
import com.github.dcysteine.neicustomdiagram.api.draw.Dimension;
import com.github.dcysteine.neicustomdiagram.api.draw.Drawable;
import com.google.common.collect.HashMultiset;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMultiset;
import com.google.common.collect.Iterables;
import com.google.common.collect.Multiset;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Optional;
import java.util.stream.StreamSupport;

public class Diagram {
    protected final Layout layout;

    /**
     * Multiset of slot and slot group keys, tracking insertions into slots.
     *
     * <p>This data can be used by diagram generators to filter out empty diagrams.
     *
     * <p>{@link Diagram.Builder} will populate this, but other methods of constructing
     * {@code Diagram} might not, causing this to be empty.
     */
    protected final ImmutableMultiset<Layout.Key> slotInsertions;

    protected final ImmutableList<? extends Interactable> interactables;

    public Diagram(
            Layout layout, Multiset<Layout.Key> slotInsertions,
            ImmutableList<? extends Interactable> interactables) {
        this.layout = layout;
        this.slotInsertions = ImmutableMultiset.copyOf(slotInsertions);
        this.interactables = interactables;
    }

    public Diagram(Layout layout, ImmutableList<? extends Interactable> interactables) {
        this(layout, ImmutableMultiset.of(), interactables);
    }

    /**
     * Returns a multiset of slot and slot group keys, which tracks insertions into slots.
     *
     * <p>This data can be used by diagram generators to filter out empty diagrams.
     *
     * <p>{@link Diagram.Builder} will populate this, but other methods of constructing
     * {@code Diagram} might not, causing this to be empty.
     */
    public ImmutableMultiset<Layout.Key> slotInsertions() {
        return slotInsertions;
    }

    public Iterable<Interactable> interactables(DiagramState diagramState) {
        // Slots go at the end so that they get last priority.
        return Iterables.concat(interactables, layout.allSlots());
    }

    public Dimension dimension(DiagramState diagramState) {
        return Dimension.max(layout.maxDimension(), Drawable.computeMaxDimension(interactables));
    }

    public void drawBackground(DiagramState diagramState) {
        layout.draw(diagramState);
    }

    public void drawForeground(DiagramState diagramState) {
        interactables.forEach(interactable -> interactable.draw(diagramState));
    }

    public static Builder builder() {
        return new Builder();
    }

    public static final class Builder {
        /** Map of layout to whether it will be shown. */
        private final Map<Layout, Boolean> layouts;

        /* Multiset of slot and slot group keys, tracking insertions into slots. */
        private final Multiset<Layout.Key> slotInsertions;

        /** Map of slot group key to slot group auto sub-builder. */
        private final Map<Layout.SlotGroupKey, SlotGroupAutoSubBuilder> slotGroupAutoSubBuilders;

        private final ImmutableList.Builder<Interactable> interactablesBuilder;

        private Builder() {
            this.layouts = new HashMap<>();
            this.slotInsertions = HashMultiset.create();
            this.slotGroupAutoSubBuilders = new HashMap<>();
            this.interactablesBuilder = ImmutableList.builder();
        }

        /**
         * Returns the layout containing the slot with the specified key.
         *
         * <p>We need to return the layout, rather than the slot directly, so that we can mark the
         * layout to be shown when we insert into the slot.
         *
         * @throws IllegalArgumentException if no slot with the specified key could be found in any
         *     layout in this builder.
         */
        private Layout findLayoutContainingSlot(Layout.SlotKey key) {
            for (Layout layout : layouts.keySet()) {
                Optional<Slot> slot = layout.slot(key);
                if (slot.isPresent()) {
                    return layout;
                }
            }

            throw new IllegalArgumentException("Could not find slot with key: " + key);
        }

        /**
         * Returns the layout containing the slot group with the specified key.
         *
         * <p>We need to return the layout, rather than the slot group directly, so that we can mark
         * the layout to be shown when we insert into the slot group.
         *
         * @throws IllegalArgumentException if no slot group with the specified key could be found
         *     in any layout in this builder.
         */
        private Layout findLayoutContainingSlotGroup(Layout.SlotGroupKey key) {
            for (Layout layout : layouts.keySet()) {
                Optional<SlotGroup> slotGroup = layout.slotGroup(key);
                if (slotGroup.isPresent()) {
                    return layout;
                }
            }

            throw new IllegalArgumentException("Could not find slot group with key: " + key);
        }

        /**
         * Adds a layout that will always be shown.
         *
         * <p>It is the caller's responsibility to avoid adding layouts that have overlapping slot
         * or slot group keys.
         */
        public Builder addLayout(Layout layout) {
            layouts.put(layout, true);
            return this;
        }

        /**
         * Adds multiple layouts that will always be shown.
         *
         * <p>It is the caller's responsibility to avoid adding layouts that have overlapping slot
         * or slot group keys.
         */
        public Builder addAllLayouts(Iterable<Layout> layouts) {
            layouts.forEach(this::addLayout);
            return this;
        }

        /**
         * Adds a layout that will be shown only if it contains slotted components.
         *
         * <p>It is the caller's responsibility to avoid adding layouts that have overlapping slot
         * or slot group keys.
         */
        public Builder addOptionalLayout(Layout layout) {
            layouts.put(layout, false);
            return this;
        }

        /**
         * Adds multiple layouts that will be shown only if they contain slotted components.
         *
         * <p>It is the caller's responsibility to avoid adding layouts that have overlapping slot
         * or slot group keys.
         */
        public Builder addAllOptionalLayouts(Iterable<Layout> layouts) {
            layouts.forEach(this::addOptionalLayout);
            return this;
        }

        /**
         * It is the caller's responsibility to avoid inserting into the same slot multiple times.
         *
         * @throws IllegalArgumentException if no slot with the specified key could be found in any
         *     layout in this builder.
         */
        public Builder insertIntoSlot(Layout.SlotKey key, DisplayComponent... components) {
            if (components.length == 0) {
                return this;
            }

            Layout layout = findLayoutContainingSlot(key);
            interactablesBuilder.add(
                    new InteractiveComponentGroup(layout.slot(key).get(), components));
            layouts.put(layout, true);
            slotInsertions.add(key);
            return this;
        }

        /**
         * It is the caller's responsibility to avoid inserting into the same slot multiple times.
         *
         * @throws IllegalArgumentException if no slot with the specified key could be found in any
         *     layout in this builder.
         */
        public Builder insertIntoSlot(Layout.SlotKey key, Iterable<DisplayComponent> components) {
            if (Iterables.isEmpty(components)) {
                return this;
            }

            Layout layout = findLayoutContainingSlot(key);
            interactablesBuilder.add(
                    new InteractiveComponentGroup(layout.slot(key).get(), components));
            layouts.put(layout, true);
            slotInsertions.add(key);
            return this;
        }

        /**
         * Returns a sub-builder which will automatically insert into the next available slot in
         * the specified slot group.
         *
         * @throws IllegalArgumentException if no slot group with the specified key could be found
         *     in any layout in this builder.
         */
        public SlotGroupAutoSubBuilder autoInsertIntoSlotGroup(Layout.SlotGroupKey key) {
            if (slotGroupAutoSubBuilders.containsKey(key)) {
                return slotGroupAutoSubBuilders.get(key);
            }

            Layout layout = findLayoutContainingSlotGroup(key);
            SlotGroupAutoSubBuilder builder =
                    new SlotGroupAutoSubBuilder(layout, key, layout.slotGroup(key).get());
            slotGroupAutoSubBuilders.put(key, builder);
            return builder;
        }

        /**
         * Returns a sub-builder which will allow insertion into specific slots in the specified
         * slot group.
         *
         * <p>It is the caller's responsibility to avoid inserting into the same slot multiple
         * times.
         *
         * @throws IllegalArgumentException if no slot group with the specified key could be found
         *     in any layout in this builder.
         */
        public SlotGroupManualSubBuilder manualInsertIntoSlotGroup(Layout.SlotGroupKey key) {
            Layout layout = findLayoutContainingSlotGroup(key);
            return new SlotGroupManualSubBuilder(layout, key, layout.slotGroup(key).get());
        }

        public Builder addInteractable(Interactable interactable) {
            interactablesBuilder.add(interactable);
            return this;
        }

        public Diagram build() {
            Layout.Builder layoutBuilder = Layout.builder();
            layouts.entrySet().stream()
                    .filter(Map.Entry::getValue)
                    .map(Map.Entry::getKey)
                    .forEach(layoutBuilder::addSubLayout);

            Layout layout = layoutBuilder.build();
            interactablesBuilder.addAll(layout.interactables());

            return new Diagram(layout, slotInsertions, interactablesBuilder.build());
        }

        /** Sub-builder that helps with inserting into the next available slot in a slot group. */
        public final class SlotGroupAutoSubBuilder {
            private final Layout layout;
            private final Layout.SlotGroupKey slotGroupKey;
            private final Iterator<Slot> slotIterator;

            private SlotGroupAutoSubBuilder(
                    Layout layout, Layout.SlotGroupKey slotGroupKey, SlotGroup slotGroup) {
                this.layout = layout;
                this.slotGroupKey = slotGroupKey;
                this.slotIterator = slotGroup.slots().iterator();
            }

            /**
             * @throws java.util.NoSuchElementException if this slot group is full.
             */
            public SlotGroupAutoSubBuilder insertIntoNextSlot(DisplayComponent... components) {
                if (components.length == 0) {
                    return this;
                }

                insertIntoSlot(slotIterator.next(), components);
                return this;
            }

            /**
             * @throws java.util.NoSuchElementException if this slot group is full.
             */
            public SlotGroupAutoSubBuilder insertIntoNextSlot(
                    Iterable<DisplayComponent> components) {
                if (Iterables.isEmpty(components)) {
                    return this;
                }

                insertIntoSlot(slotIterator.next(), components);
                return this;
            }

            /**
             * Inserts components one at a time into this slot group's slots.
             *
             * @throws java.util.NoSuchElementException if this slot group is too small.
             */
            public SlotGroupAutoSubBuilder insertEach(Iterable<DisplayComponent> components) {
                components.forEach(this::insertIntoNextSlot);
                return this;
            }

            /**
             * Inserts components one at a time into this slot group's slots, without overfilling.
             *
             * <p>If there are more components than there are remaining slots, will put all excess
             * components together into the very last slot.
             *
             * @throws java.util.NoSuchElementException if this slot group is already full.
             */
            public SlotGroupAutoSubBuilder insertEachSafe(Iterable<DisplayComponent> components) {
                Iterator<DisplayComponent> iterator = components.iterator();

                while (slotIterator.hasNext() && iterator.hasNext()) {
                    Slot slot = slotIterator.next();

                    if (slotIterator.hasNext()) {
                        insertIntoSlot(slot, iterator.next());
                    } else {
                        // This is the last slot, so put all remaining components into it.
                        insertIntoSlot(slot, () -> iterator);
                    }
                }

                return this;
            }

            /**
             * Inserts components one group at a time into this slot group's slots.
             *
             * @throws java.util.NoSuchElementException if this slot group is too small.
             */
            public SlotGroupAutoSubBuilder insertEachGroup(
                    Iterable<? extends Iterable<DisplayComponent>> components) {
                components.forEach(this::insertIntoNextSlot);
                return this;
            }

            /**
             * Inserts components one group at a time into this slot group's slots, without
             * overfilling.
             *
             * <p>If there are more component groups than there are remaining slots, will flatten
             * all excess components groups into a single group, and insert it into the very last
             * slot.
             *
             * @throws java.util.NoSuchElementException if this slot group is already full.
             */
            public <T extends Iterable<DisplayComponent>> SlotGroupAutoSubBuilder
                    insertEachGroupSafe(Iterable<T> components) {
                Iterator<T> iterator =
                        StreamSupport.stream(components.spliterator(), false)
                                .filter(iter -> !Iterables.isEmpty(iter))
                                .iterator();

                while (slotIterator.hasNext() && iterator.hasNext()) {
                    Slot slot = slotIterator.next();

                    if (slotIterator.hasNext()) {
                        insertIntoSlot(slot, iterator.next());
                    } else {
                        // This is the last slot, so put all remaining components into it.
                        insertIntoSlot(slot, Iterables.concat((Iterable<T>) () -> iterator));
                    }
                }

                return this;
            }

            private void insertIntoSlot(Slot slot, DisplayComponent... components) {
                interactablesBuilder.add(new InteractiveComponentGroup(slot, components));
                layouts.put(layout, true);
                slotInsertions.add(slotGroupKey);
            }

            private void insertIntoSlot(Slot slot, Iterable<DisplayComponent> components) {
                interactablesBuilder.add(new InteractiveComponentGroup(slot, components));
                layouts.put(layout, true);
                slotInsertions.add(slotGroupKey);
            }
        }

        /** Sub-builder that helps with inserting into specific slots in a slot group. */
        public final class SlotGroupManualSubBuilder {
            private final Layout layout;
            private final Layout.SlotGroupKey slotGroupKey;
            private final SlotGroup slotGroup;

            private SlotGroupManualSubBuilder(
                    Layout layout, Layout.SlotGroupKey slotGroupKey, SlotGroup slotGroup) {
                this.layout = layout;
                this.slotGroupKey = slotGroupKey;
                this.slotGroup = slotGroup;
            }

            /**
             * It is the caller's responsibility to avoid inserting into the same slot multiple
             * times.
             */
            public SlotGroupManualSubBuilder insertIntoSlot(
                    int x, int y, DisplayComponent... components) {
                interactablesBuilder.add(
                        new InteractiveComponentGroup(slotGroup.slot(x, y), components));
                layouts.put(layout, true);
                slotInsertions.add(slotGroupKey);
                return this;
            }

            /**
             * It is the caller's responsibility to avoid inserting into the same slot multiple
             * times.
             */
            public SlotGroupManualSubBuilder insertIntoSlot(
                    int x, int y, Iterable<DisplayComponent> components) {
                interactablesBuilder.add(
                        new InteractiveComponentGroup(slotGroup.slot(x, y), components));
                layouts.put(layout, true);
                slotInsertions.add(slotGroupKey);
                return this;
            }
        }
    }
}
