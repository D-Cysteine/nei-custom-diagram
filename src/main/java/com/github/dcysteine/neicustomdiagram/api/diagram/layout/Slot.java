package com.github.dcysteine.neicustomdiagram.api.diagram.layout;

import com.github.dcysteine.neicustomdiagram.api.diagram.DiagramState;
import com.github.dcysteine.neicustomdiagram.api.diagram.interactable.Interactable;
import com.github.dcysteine.neicustomdiagram.api.diagram.tooltip.Tooltip;
import com.github.dcysteine.neicustomdiagram.api.draw.Dimension;
import com.github.dcysteine.neicustomdiagram.api.draw.Draw;
import com.github.dcysteine.neicustomdiagram.api.draw.Point;
import com.google.auto.value.AutoValue;
import com.google.auto.value.extension.toprettystring.ToPrettyString;

import java.util.function.BiConsumer;
import java.util.function.Consumer;

/**
 * A slot is an area where a stack can be displayed.
 *
 * <p>Slots can have tooltip messages. This message will be rendered when mousing over the slot. If
 * the slot contains a stack that also has a tooltip message, then the stack's message will be
 * rendered after the slot's.
 */
@AutoValue
public abstract class Slot implements Interactable {
    /** The center of this slot. */
    @Override
    public abstract Point position();
    public abstract int slotWidth();

    public abstract Tooltip tooltip();
    public abstract BiConsumer<DiagramState, Point> drawFunction();

    @Override
    public Dimension dimension() {
        return Dimension.create(Draw.ICON_WIDTH);
    }

    @Override
    public void draw(DiagramState diagramState) {
        drawFunction().accept(diagramState, position());
    }

    @Override
    public void drawOverlay(DiagramState diagramState) {
        Draw.drawOverlay(position(), Draw.Colour.OVERLAY_WHITE);
    }

    @Override
    public void drawTooltip(DiagramState diagramState, Point mousePos) {
        tooltip().draw(mousePos);
    }

    @ToPrettyString
    public abstract String toPrettyString();

    public static Builder builder(Point pos) {
        return new AutoValue_Slot.Builder()
                .setPosition(pos)
                .setSlotWidth(Grid.SLOT_WIDTH)
                .setTooltip(Tooltip.EMPTY_TOOLTIP)
                .setDrawFunction(Draw::drawSlot);
    }

    public abstract Builder toBuilder();

    @AutoValue.Builder
    public abstract static class Builder {
        public abstract Builder setPosition(Point pos);
        public abstract Builder setSlotWidth(int slotWidth);
        public abstract Builder setTooltip(Tooltip tooltip);
        public abstract Builder setDrawFunction(BiConsumer<DiagramState, Point> fun);

        public Builder setDrawFunction(Consumer<Point> fun) {
            setDrawFunction((ticker, point) -> fun.accept(point));
            return this;
        }

        public abstract Slot build();
    }
}
