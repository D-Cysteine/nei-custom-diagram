package com.github.dcysteine.neicustomdiagram.api.diagram.layout;

import com.github.dcysteine.neicustomdiagram.api.diagram.tooltip.Tooltip;
import com.github.dcysteine.neicustomdiagram.api.draw.Draw;
import com.github.dcysteine.neicustomdiagram.api.draw.Drawable;
import com.github.dcysteine.neicustomdiagram.api.draw.Point;
import com.github.dcysteine.neicustomdiagram.api.draw.Ticker;
import com.google.auto.value.AutoValue;
import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;

import java.util.function.BiConsumer;
import java.util.function.Consumer;

/**
 * A slot group is a rectangular group of slots, which can be used to display related stacks.
 *
 * <p>Regardless of which direction was used when positioning the slot group, when accessing the
 * slots, {@code (0, 0)} will always be the top-left slot.
 *
 * <p>Slots will be iterated over left-to-right, then top-to-bottom.
 */
@AutoValue
public abstract class SlotGroup implements Drawable {
    /** The slot group's width, in # of slots. */
    public abstract int width();

    /** The slot group's height, in # of slots. */
    public abstract int height();

    /**
     * The list of slots, which will always have size {@code width * height}.
     *
     * <p>Slots will be iterated over left-to-right, then top-to-bottom.
     */
    public abstract ImmutableList<Slot> slots();

    public Slot slot(int x, int y) {
        return slots().get(x + y * height());
    }

    public void draw(Ticker ticker) {
        slots().forEach(slot -> slot.draw(ticker));
    }

    /**
     * Returns a builder, with {@code width x height} slots pre-constructed with no tooltip message
     * and default appearance and width.
     *
     * <p>{@code dir} specifies which direction the slot group will extend from {@code pos}.
     *
     * <p>For example, if {@code dir = Direction.NW}, the center of the bottom-right slot would be
     * placed at {@code pos}. If instead {@code dir = Direction.E}, then the center of the leftmost
     * column of slots would be placed at {@code pos}. If this slot group has an even height, say
     * {@code 8}, then {@code pos} would be the center of the margin between the fourth and fifth
     * slots in the leftmost column.
     */
    public static Builder builder(int width, int height, Point pos, Grid.Direction dir) {
        Preconditions.checkArgument(width > 0, "Width too small: %d", width);
        Preconditions.checkArgument(height > 0, "Height too small: %d", height);

        return new Builder(width, height, pos, dir);
    }

    public static final class Builder {
        private final int width;
        private final int height;
        private final Point position;
        private final Grid.Direction direction;

        private int slotWidth;
        private Tooltip defaultTooltip;
        private BiConsumer<Ticker, Point> defaultDrawFunction;
        private final Slot[][] slots;

        private Builder(int width, int height, Point position, Grid.Direction direction) {
            this.width = width;
            this.height = height;
            this.position = position;
            this.direction = direction;

            this.slotWidth = Grid.SLOT_WIDTH;
            this.defaultTooltip = Tooltip.EMPTY_TOOLTIP;
            this.defaultDrawFunction = (ticker, point) -> Draw.drawSlot(point);
            this.slots = new Slot[width][height];
        }

        public Builder setSlotWidth(int slotWidth) {
            Preconditions.checkArgument(slotWidth > 0, "Slot width too small: %d", slotWidth);
            this.slotWidth = slotWidth;
            return this;
        }

        /**
         * Sets the default tooltip, which will be used for any slots that are not explicitly set.
         */
        public Builder setDefaultTooltip(Tooltip tooltip) {
            this.defaultTooltip = tooltip;
            return this;
        }

        /**
         * Sets the default draw function, which will be used for any slots that are not explicitly
         * set.
         */
        public Builder setDefaultDrawFunction(BiConsumer<Ticker, Point> defaultDrawFunction) {
            this.defaultDrawFunction = defaultDrawFunction;
            return this;
        }

        /**
         * Sets the default draw function, which will be used for any slots that are not explicitly
         * set.
         */
        public Builder setDefaultDrawFunction(Consumer<Point> defaultDrawFunction) {
            this.defaultDrawFunction = (ticker, point) -> defaultDrawFunction.accept(point);
            return this;
        }

        /**
         * Sets a slot with a custom tooltip and/or draw function.
         * The slot's position will be overridden during slot group construction.
         */
        public Builder setSlot(int x, int y, Slot slot) {
            slots[x][y] = slot;
            return this;
        }

        public SlotGroup build() {
            ImmutableList.Builder<Slot> slotsBuilder = ImmutableList.builder();

            int offsetX = (direction.xFactor - 1) * (width - 1) * slotWidth / 2;
            int offsetY = (direction.yFactor - 1) * (width - 1) * slotWidth / 2;
            Point topLeft = position.translate(offsetX, offsetY);

            // Iterate left-to-right, then top-to-bottom.
            // This iteration order determines the order in which the list of slots will be filled.
            for (int j = 0; j < height; j++) {
                for (int i = 0; i < width; i++) {
                    Point currPos = topLeft.translate(i * slotWidth, j * slotWidth);
                    Slot.Builder slotBuilder;
                    if (slots[i][j] != null) {
                        slotBuilder = slots[i][j].toBuilder().setPosition(currPos);
                    } else {
                        slotBuilder =
                                Slot.builder(currPos)
                                        .setSlotWidth(slotWidth)
                                        .setTooltip(defaultTooltip)
                                        .setDrawFunction(defaultDrawFunction);
                    }

                    slotsBuilder.add(slotBuilder.build());
                }
            }

            return new AutoValue_SlotGroup(width, height, slotsBuilder.build());
        }
    }
}
