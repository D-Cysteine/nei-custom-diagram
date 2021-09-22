package com.github.dcysteine.neicustomdiagram.api.diagram.interactable;

import com.github.dcysteine.neicustomdiagram.api.diagram.DiagramState;
import com.github.dcysteine.neicustomdiagram.api.diagram.component.Component;
import com.github.dcysteine.neicustomdiagram.api.diagram.component.DisplayComponent;
import com.github.dcysteine.neicustomdiagram.api.diagram.layout.Slot;
import com.github.dcysteine.neicustomdiagram.api.diagram.tooltip.Tooltip;
import com.github.dcysteine.neicustomdiagram.api.draw.Draw;
import com.github.dcysteine.neicustomdiagram.api.draw.Point;
import com.github.dcysteine.neicustomdiagram.mod.Lang;
import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import net.minecraft.client.Minecraft;
import net.minecraft.item.ItemStack;

import java.util.List;

/**
 * An interactive component group.
 *
 * <p>If the component group contains multiple components, they will be cycled through.
 */
public class InteractiveComponentGroup implements Interactable {
    protected final Point position;

    /** Guaranteed to be non-empty. */
    protected final ImmutableList<DisplayComponent> components;

    /** Tooltip which will be drawn before annotated component tooltips. Meant for slot tooltips. */
    protected final Tooltip slotTooltip;

    /**
     * This class is really intended to be used with slots. If you're calling this constructor,
     * consider using {@link CustomInteractable} instead.
     */
    public InteractiveComponentGroup(
            Point pos, Iterable<DisplayComponent> components, Tooltip slotTooltip) {
        this.position = pos;
        this.components = ImmutableList.copyOf(components);
        this.slotTooltip = slotTooltip;

        Preconditions.checkState(
                !this.components.isEmpty(), "Must contain at least one component!");
    }

    public InteractiveComponentGroup(Slot slot, DisplayComponent... components) {
        this(slot.position(), ImmutableList.copyOf(components), slot.tooltip());
    }

    public InteractiveComponentGroup(Slot slot, Iterable<DisplayComponent> components) {
        this(slot.position(), components, slot.tooltip());
    }

    public int currentIndex(DiagramState diagramState) {
        return diagramState.cycleIndex(components.size());
    }

    public DisplayComponent currentComponent(DiagramState diagramState) {
        return components.get(currentIndex(diagramState));
    }

    /**
     * Returns a tooltip containing the current index and total number of components in this group.
     */
    public Tooltip indexTooltip(DiagramState diagramState) {
        if (components.size() <= 1) {
            return Tooltip.EMPTY_TOOLTIP;
        }

        return Tooltip.builder()
                .setFormatting(Tooltip.INFO_FORMATTING.toBuilder().setSmall(true).build())
                .addTextLine(
                        Lang.API.transf("componentindex",
                                currentIndex(diagramState) + 1, components.size()))
                .build();
    }

    @Override
    public Point position() {
        return position;
    }

    @Override
    public int width() {
        return Draw.ICON_WIDTH;
    }

    @Override
    public int height() {
        return Draw.ICON_WIDTH;
    }

    @Override
    public void interact(DiagramState diagramState, RecipeType recipeType) {
        currentComponent(diagramState).interact(recipeType);
    }

    @Override
    public void draw(DiagramState diagramState) {
        currentComponent(diagramState).draw(position);
    }

    @Override
    public void drawOverlay(DiagramState diagramState) {
        Draw.drawOverlay(position, Draw.Color.OVERLAY_WHITE);
    }

    @Override
    public void drawTooltip(DiagramState diagramState, Point mousePos) {
        DisplayComponent component = currentComponent(diagramState);

        Tooltip itemStackTooltip = Tooltip.EMPTY_TOOLTIP;
        if (component.type() == Component.ComponentType.ITEM) {
            ItemStack stack = (ItemStack) component.stack();

            @SuppressWarnings("unchecked")
            List<String> lines =
                    stack.getTooltip(
                            Minecraft.getMinecraft().thePlayer,
                            Minecraft.getMinecraft().gameSettings.advancedItemTooltips);

            itemStackTooltip =
                    Tooltip.builder()
                            .setFormatting(Tooltip.TRIVIAL_FORMATTING)
                            .addAllTextLines(lines.subList(1, lines.size()))
                            .build();
        }

        Tooltip.concat(
                        component.descriptionTooltip(), indexTooltip(diagramState),
                        slotTooltip, component.additionalTooltip(), itemStackTooltip)
                .draw(mousePos);
    }
}
