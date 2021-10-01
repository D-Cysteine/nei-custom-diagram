package com.github.dcysteine.neicustomdiagram.api.diagram.interactable;

import codechicken.nei.NEIClientUtils;
import com.github.dcysteine.neicustomdiagram.api.diagram.DiagramState;
import com.github.dcysteine.neicustomdiagram.api.diagram.component.Component;
import com.github.dcysteine.neicustomdiagram.api.diagram.component.DisplayComponent;
import com.github.dcysteine.neicustomdiagram.api.diagram.layout.Slot;
import com.github.dcysteine.neicustomdiagram.api.diagram.tooltip.Tooltip;
import com.github.dcysteine.neicustomdiagram.api.draw.Dimension;
import com.github.dcysteine.neicustomdiagram.api.draw.Draw;
import com.github.dcysteine.neicustomdiagram.api.draw.Point;
import com.github.dcysteine.neicustomdiagram.mod.Lang;
import com.github.dcysteine.neicustomdiagram.mod.config.ConfigOptions;
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

    /** Returns a tooltip containing information about the group's component cycle. */
    public Tooltip cycleTooltip(DiagramState diagramState) {
        if (components.size() <= 1) {
            return Tooltip.EMPTY_TOOLTIP;
        }

        int maxComponents = ConfigOptions.TOOLTIP_MAX_CYCLE_COUNT.get();
        if (!NEIClientUtils.shiftKey() || maxComponents <= 0) {
            String transKey = maxComponents > 0 ? "cycleindexwithshift" : "cycleindex";
            return Tooltip.builder()
                    .setFormatting(Tooltip.INFO_FORMATTING)
                    .addTextLine(
                            Lang.API.transf(transKey,
                                    currentIndex(diagramState) + 1, components.size()))
                    .build();
        }

        int numComponents = Math.min(components.size(), maxComponents);
        List<DisplayComponent> tooltipComponents = components.subList(0, numComponents);
        Tooltip.Builder builder =
                Tooltip.builder()
                        .setFormatting(Tooltip.INFO_FORMATTING)
                        .addTextLine(
                                Lang.API.transf("cycleindex",
                                        currentIndex(diagramState) + 1, components.size()))
                        .addSpacing()
                        .addTextLine(Lang.API.trans("cyclecomponents"))
                        .addAllDisplayComponents(tooltipComponents);

        if (numComponents < components.size()) {
            builder.addTextLine(
                    Lang.API.transf("excesscyclecomponents",
                            components.size() - numComponents));
        }

        return builder.build();
    }

    @Override
    public Point position() {
        return position;
    }

    @Override
    public Dimension dimension() {
        return Dimension.create(Draw.ICON_WIDTH);
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
                        component.descriptionTooltip(), slotTooltip, component.additionalTooltip(),
                        itemStackTooltip, cycleTooltip(diagramState))
                .draw(mousePos);
    }
}
