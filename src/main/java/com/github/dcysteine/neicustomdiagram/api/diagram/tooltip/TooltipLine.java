package com.github.dcysteine.neicustomdiagram.api.diagram.tooltip;

import com.github.dcysteine.neicustomdiagram.api.diagram.component.Component;
import com.google.auto.value.AutoValue;
import com.google.auto.value.extension.toprettystring.ToPrettyString;
import com.google.common.collect.ImmutableList;
import net.minecraft.util.EnumChatFormatting;

@AutoValue
public abstract class TooltipLine {
    public abstract ImmutableList<TooltipElement> elements();
    public abstract int additionalSpacing();

    public int width() {
        int width = 0;
        TextFormatting currFormatting = Tooltip.DEFAULT_FORMATTING;
        boolean first = true;

        for (TooltipElement element : elements()) {
            if (element.type() == TooltipElement.ElementType.FORMATTING) {
                currFormatting = element.formatting();
                continue;
            } else if (!first) {
                width += Tooltip.ELEMENT_SPACING;
            } else {
                first = false;
            }

            width += element.width(currFormatting);
        }

        return width;
    }

    public int height() {
        int maxHeight = 0;
        TextFormatting currFormatting = Tooltip.DEFAULT_FORMATTING;

        for (TooltipElement element : elements()) {
            if (element.type() == TooltipElement.ElementType.FORMATTING) {
                currFormatting = element.formatting();
                continue;
            }

            maxHeight = Math.max(maxHeight, element.height(currFormatting));
        }

        return maxHeight + additionalSpacing();
    }

    @ToPrettyString
    public abstract String toPrettyString();

    /**
     * Unlike most other draw methods, this method takes position coordinates as separate integers,
     * and will draw with that point as the top-left, rather than the center. These differences are
     * to facilitate our custom tooltip drawing code.
     */
    public void draw(int x, int y) {
        int currX = x;
        TextFormatting currFormatting = Tooltip.DEFAULT_FORMATTING;

        for (TooltipElement element : elements()) {
            if (element.type() == TooltipElement.ElementType.FORMATTING) {
                currFormatting = element.formatting();
                continue;
            }

            int offsetY = y + (height() - element.height(currFormatting)) / 2;
            element.draw(currX, offsetY, currFormatting);
            currX += Tooltip.ELEMENT_SPACING + element.width(currFormatting);
        }
    }

    public static Builder builder() {
        return new Builder();
    }

    public static final class Builder {
        private final ImmutableList.Builder<TooltipElement> elementsBuilder;
        private int additionalSpacing;

        public Builder() {
            this.elementsBuilder = ImmutableList.builder();
            this.additionalSpacing = 0;
        }

        public Builder addElement(TooltipElement element) {
            elementsBuilder.add(element);
            return this;
        }

        public Builder addAllElements(Iterable<TooltipElement> elements) {
            elementsBuilder.addAll(elements);
            return this;
        }

        public Builder addSpacing(int spacing) {
            return addElement(TooltipElement.ofSpacing(spacing));
        }

        public Builder addText(String text) {
            return addElement(TooltipElement.ofText(text));
        }

        public Builder addComponentIcon(Component component) {
            return addElement(TooltipElement.ofComponentIcon(component));
        }

        public Builder addComponentDescription(Component component) {
            return addElement(TooltipElement.ofComponentDescription(component));
        }

        public Builder addFormatting(TextFormatting formatting) {
            return addElement(TooltipElement.ofFormatting(formatting));
        }

        public Builder addFormatting(EnumChatFormatting formatting) {
            return addFormatting(TextFormatting.create(formatting));
        }

        public Builder addFormattingSmall(EnumChatFormatting formatting) {
            return addFormatting(TextFormatting.create(true, formatting));
        }

        public Builder setAdditionalSpacing(int additionalSpacing) {
            this.additionalSpacing = additionalSpacing;
            return this;
        }

        public TooltipLine build() {
            return new AutoValue_TooltipLine(elementsBuilder.build(), additionalSpacing);
        }
    }
}
