package com.github.dcysteine.neicustomdiagram.api.diagram.tooltip;

import com.github.dcysteine.neicustomdiagram.api.diagram.component.Component;
import com.github.dcysteine.neicustomdiagram.api.diagram.component.DisplayComponent;
import com.github.dcysteine.neicustomdiagram.api.draw.Draw;
import com.github.dcysteine.neicustomdiagram.api.draw.Point;
import com.google.auto.value.AutoValue;
import com.google.auto.value.extension.toprettystring.ToPrettyString;
import com.google.common.base.Splitter;
import com.google.common.collect.ImmutableList;
import net.minecraft.util.EnumChatFormatting;

import java.util.Arrays;

/** Class holding a tooltip, with options for using a default colour. */
@AutoValue
public abstract class Tooltip {
    private static final Splitter SPLITTER = Splitter.on('\n');

    public static final Tooltip EMPTY_TOOLTIP = builder().build();
    public static final TextFormatting DEFAULT_FORMATTING =
            TextFormatting.create(false, EnumChatFormatting.RESET);
    public static final TextFormatting SLOT_FORMATTING =
            TextFormatting.create(false, EnumChatFormatting.AQUA);
    public static final TextFormatting INFO_FORMATTING =
            TextFormatting.create(false, EnumChatFormatting.YELLOW);
    public static final TextFormatting URGENT_FORMATTING =
            TextFormatting.create(false, EnumChatFormatting.RED);
    public static final TextFormatting TRIVIAL_FORMATTING =
            TextFormatting.create(false, EnumChatFormatting.GRAY);
    public static final TextFormatting SPECIAL_FORMATTING =
            TextFormatting.create(false, EnumChatFormatting.LIGHT_PURPLE);

    /** The default vertical space between tooltip lines. */
    public static final int LINE_SPACING = 2;

    /** The default horizontal space between tooltip lines. */
    public static final int ELEMENT_SPACING = 4;

    public abstract ImmutableList<TooltipLine> lines();

    public int width() {
        return lines().stream()
                .mapToInt(TooltipLine::width)
                .max()
                .orElse(0);
    }

    public int height() {
        return Tooltip.LINE_SPACING * (lines().size() - 1)
                + lines().stream()
                        .mapToInt(TooltipLine::height)
                        .sum();
    }

    /**
     * Unlike the other draw methods, tooltips are drawn with absolute mouse coordinates.
     *
     * <p>This is due to how the handle tooltip method gets called.
     */
    public void draw(Point mousePos) {
        Draw.drawTooltip(this, mousePos);
    }

    @ToPrettyString
    public abstract String toPrettyString();

    /** This method will split the input on the newline character {@code '\n'}. */
    public static Tooltip create(String line) {
        return builder().addTextLine(line).build();
    }

    /** This method will split the input on the newline character {@code '\n'}. */
    public static Tooltip create(String line, TextFormatting formatting) {
        return builder().setFormatting(formatting).addTextLine(line).build();
    }

    public static Tooltip concat(Iterable<Tooltip> tooltips) {
        Tooltip.Builder builder = builder();
        boolean first = true;

        for (Tooltip tooltip : tooltips) {
            if (tooltip.lines().isEmpty()) {
                continue;
            }

            if (!first) {
                builder.addSpacing();
            } else {
                first = false;
            }
            builder.addAllLines(tooltip.lines());
        }

        return builder.build();
    }

    public static Tooltip concat(Tooltip... tooltips) {
        return concat(Arrays.asList(tooltips));
    }

    public static Builder builder() {
        return new Builder();
    }

    public static final class Builder {
        private final ImmutableList.Builder<TooltipLine> linesBuilder;
        private TextFormatting currentFormatting;

        public Builder() {
            linesBuilder = ImmutableList.builder();
            currentFormatting = DEFAULT_FORMATTING;
        }

        /** This method ignores this builder's current formatting. */
        public Builder addLine(TooltipLine line) {
            linesBuilder.add(line);
            return this;
        }

        /** This method ignores this builder's current formatting. */
        public Builder addAllLines(Iterable<TooltipLine> lines) {
            linesBuilder.addAll(lines);
            return this;
        }

        /** Adds some vertical spacing. */
        public Builder addSpacing() {
            return addLine(TooltipLine.builder().setAdditionalSpacing(LINE_SPACING).build());
        }

        /** Adds some vertical spacing. */
        public Builder addSpacing(int spacing) {
            return addLine(TooltipLine.builder().setAdditionalSpacing(spacing).build());
        }

        /** This method will split the input on the newline character {@code '\n'}. */
        public Builder addTextLine(String textLine) {
            return addAllTextLines(SPLITTER.split(textLine));
        }

        public Builder addAllTextLines(Iterable<String> textLines) {
            for (String textLine : textLines) {
                addLine(
                        TooltipLine.builder()
                                .addFormatting(currentFormatting)
                                .addText(textLine)
                                .build());
            }

            return this;
        }

        public Builder addDisplayComponent(DisplayComponent displayComponent) {
            return addLine(
                    TooltipLine.builder()
                            .addFormatting(currentFormatting)
                            .addDisplayComponentIcon(displayComponent)
                            .addComponentDescription(displayComponent.component())
                            .build());
        }

        public Builder addComponent(Component component) {
            return addLine(
                    TooltipLine.builder()
                            .addFormatting(currentFormatting)
                            .addComponentIcon(component)
                            .addComponentDescription(component)
                            .build());
        }

        public Builder addComponentDescription(Component component) {
            return addLine(
                    TooltipLine.builder()
                            .addFormatting(currentFormatting)
                            .addComponentDescription(component)
                            .build());
        }

        public Builder addAllDisplayComponents(
                Iterable<? extends DisplayComponent> displayComponents) {
            displayComponents.forEach(this::addDisplayComponent);
            return this;
        }

        public Builder addAllComponents(Iterable<? extends Component> components) {
            components.forEach(this::addComponent);
            return this;
        }

        /** Starts formatting new text lines with the specified formatting. */
        public Builder setFormatting(TextFormatting formatting) {
            currentFormatting = formatting;
            return this;
        }

        public Tooltip build() {
            return new AutoValue_Tooltip(linesBuilder.build());
        }
    }
}
