package com.github.dcysteine.neicustomdiagram.api.diagram.tooltip;

import codechicken.lib.gui.GuiDraw;
import com.github.dcysteine.neicustomdiagram.api.diagram.component.Component;
import com.github.dcysteine.neicustomdiagram.api.diagram.component.DisplayComponent;
import com.github.dcysteine.neicustomdiagram.api.diagram.layout.Grid;
import com.github.dcysteine.neicustomdiagram.api.draw.Draw;
import com.github.dcysteine.neicustomdiagram.api.draw.Point;
import com.google.auto.value.AutoOneOf;

/**
 * An element in a tooltip line.
 *
 * <p>There are four types of elements:
 * <ul>
 *     <li><b>Spacing:</b> an empty horizontal space, of the specified width in pixels.
 *     <li><b>Text:</b> a plain text string, which can be formatted.
 *     <li><b>Component Icon:</b> the icon of a component. This is unaffected by formatting.
 *     <li><b>Component Description:</b> the description of a component. Use this instead of trying
 *         to include {@link Component#description()} as plain text. This is necessary because
 *         component descriptions aren't set properly until after diagram generation happens. This
 *         will be affected by formatting just like normal text.
 *     <li><b>Formatting:</b> applies text formatting. The formatting will persist until overridden
 *         by another formatting element, or until the line ends.
 * </ul>
 */
@AutoOneOf(TooltipElement.ElementType.class)
public abstract class TooltipElement {
    public enum ElementType {
        SPACING, TEXT, DISPLAY_COMPONENT_ICON, COMPONENT_DESCRIPTION, FORMATTING;
    }

    public static TooltipElement ofSpacing(int spacing) {
        return AutoOneOf_TooltipElement.spacing(spacing);
    }

    public static TooltipElement ofText(String text) {
        return AutoOneOf_TooltipElement.text(text);
    }

    public static TooltipElement ofDisplayComponentIcon(DisplayComponent displayComponent) {
        return AutoOneOf_TooltipElement.displayComponentIcon(displayComponent);
    }

    public static TooltipElement ofComponentIcon(Component component) {
        return AutoOneOf_TooltipElement.displayComponentIcon(
                DisplayComponent.builder(component).build());
    }

    public static TooltipElement ofComponentDescription(Component component) {
        return AutoOneOf_TooltipElement.componentDescription(component);
    }

    public static TooltipElement ofFormatting(TextFormatting formatting) {
        return AutoOneOf_TooltipElement.formatting(formatting);
    }

    public abstract ElementType type();
    public abstract int spacing();
    public abstract String text();
    public abstract DisplayComponent displayComponentIcon();
    public abstract Component componentDescription();
    public abstract TextFormatting formatting();

    public int width(TextFormatting formatting) {
        switch (type()) {
            case SPACING:
                return spacing();

            case TEXT:
                if (formatting.small()) {
                    return GuiDraw.getStringWidth(formatting.format(text())) / 2;
                } else {
                    return GuiDraw.getStringWidth(formatting.format(text()));
                }

            case DISPLAY_COMPONENT_ICON:
                return Grid.SLOT_WIDTH;

            case COMPONENT_DESCRIPTION:
                return GuiDraw.getStringWidth(componentDescription().description());

            case FORMATTING:
                return 0;
        }

        throw new IllegalStateException("Unhandled element type: " + this);
    }

    public int height(TextFormatting formatting) {
        switch (type()) {
            case SPACING:
            case FORMATTING:
                return 0;

            case TEXT:
            case COMPONENT_DESCRIPTION:
                if (formatting.small()) {
                    return Draw.TEXT_HEIGHT / 2;
                } else {
                    return Draw.TEXT_HEIGHT;
                }

            case DISPLAY_COMPONENT_ICON:
                return Grid.SLOT_WIDTH;
        }

        throw new IllegalStateException("Unhandled element type: " + this);
    }

    /**
     * Unlike most other draw methods, this method takes position coordinates as separate integers,
     * and will draw with that point as the top-left, rather than the center. These differences are
     * to facilitate our custom tooltip drawing code.
     */
    public void draw(int x, int y, TextFormatting formatting) {
        Point topLeft = Point.create(x, y);
        Point center = Point.create(x + width(formatting) / 2, y + height(formatting) / 2);

        switch (type()) {
            case TEXT:
                Draw.drawText(
                        formatting.format(text()), topLeft,
                        Draw.Colour.WHITE, formatting.small(), true);
                break;

            case DISPLAY_COMPONENT_ICON:
                Draw.drawSlot(center);
                displayComponentIcon().draw(center);
                break;

            case COMPONENT_DESCRIPTION:
                Draw.drawText(
                        formatting.format(componentDescription().description()), topLeft,
                        Draw.Colour.WHITE, formatting.small(), true);
                break;
        }
    }
}
