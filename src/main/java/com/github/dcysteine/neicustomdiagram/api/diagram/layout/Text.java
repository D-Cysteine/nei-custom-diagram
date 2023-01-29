package com.github.dcysteine.neicustomdiagram.api.diagram.layout;

import codechicken.lib.gui.GuiDraw;
import com.github.dcysteine.neicustomdiagram.api.diagram.DiagramState;
import com.github.dcysteine.neicustomdiagram.api.draw.BoundedDrawable;
import com.github.dcysteine.neicustomdiagram.api.draw.Dimension;
import com.github.dcysteine.neicustomdiagram.api.draw.Draw;
import com.github.dcysteine.neicustomdiagram.api.draw.Point;
import com.google.auto.value.AutoValue;
import com.google.auto.value.extension.toprettystring.ToPrettyString;
import com.google.common.base.Preconditions;
import com.google.common.base.Splitter;

import java.util.ArrayList;
import java.util.List;

/**
 * Immutable class representing a text label.
 *
 * <p>Multi-line text is supported.
 */
@AutoValue
public abstract class Text implements BoundedDrawable {
    private static final Splitter SPLITTER = Splitter.on('\n');

    public abstract String text();

    /** The center of the text. */
    @Override
    public abstract Point position();

    /** See {@link Draw.Colour} for colour encoding information. */
    public abstract int colour();

    /** If true, the text will be rendered at half-scale. */
    public abstract boolean small();

    /** If true, the text will be rendered with a shadow. */
    public abstract boolean shadow();

    @Override
    public abstract Dimension dimension();

    @Override
    public void draw(DiagramState diagramState) {
        Point topLeft = position().translate(-dimension().width() / 2, -dimension().height() / 2);
        Draw.drawText(text(), topLeft, colour(), small(), shadow());
    }

    @ToPrettyString
    public abstract String toPrettyString();

    /**
     * Returns a new single-line text builder.
     *
     * <p>{@code dir} specifies which direction the text will extend from {@code pos}.
     *
     * <p>For example, if {@code dir = Direction.NW}, the lower-right corner of the text's bounding
     * box would be placed at {@code pos}. If instead {@code dir = Direction.E}, then the center of
     * the left side of the text's bounding box would be placed at {@code pos}.
     */
    public static Builder builder(String text, Point pos, Grid.Direction dir) {
        return new Builder(text, pos, dir);
    }

    public static MultiLineBuilder multiLineBuilder(Point pos, Grid.Direction dir) {
        Preconditions.checkArgument(
                dir.yFactor != 0, "Direction must have a vertical component: %s", dir);

        return new MultiLineBuilder(pos, dir);
    }

    public static final class Builder {
        private final String text;
        private final Point position;
        private final Grid.Direction direction;

        private int colour;
        private boolean small;
        private boolean shadow;

        private Builder(String text, Point pos, Grid.Direction dir) {
            this.text = text;
            this.position = pos;
            this.direction = dir;

            this.colour = Draw.Colour.BLACK;
            this.small = false;
            this.shadow = false;
        }

        /** See {@link Draw.Colour} for colour encoding information. */
        public Builder setColour(int colour) {
            this.colour = colour;
            return this;
        }

        public Builder setSmall(boolean small) {
            this.small = small;
            return this;
        }

        public Builder setShadow(boolean shadow) {
            this.shadow = shadow;
            return this;
        }

        public Text build() {
            int width = GuiDraw.getStringWidth(text);
            int height = Draw.TEXT_HEIGHT;
            if (small) {
                width /= 2;
                height /= 2;
            }
            Dimension dimension = Dimension.create(width, height);

            Point center =
                    position.translate(
                            direction.xFactor * width / 2,
                            direction.yFactor * height / 2);

            return new AutoValue_Text(text, center, colour, small, shadow, dimension);
        }
    }

    public static final class MultiLineBuilder {
        public static final int LINE_HEIGHT = Draw.TEXT_HEIGHT + 2;

        private final Point position;
        private final Grid.Direction direction;

        private int lineHeight;
        private int colour;
        private boolean small;
        private boolean shadow;
        private List<String> textLines;

        private MultiLineBuilder(Point position, Grid.Direction direction) {
            this.position = position;
            this.direction = direction;

            this.colour = Draw.Colour.BLACK;
            this.small = false;
            this.shadow = false;
            this.lineHeight = LINE_HEIGHT;
            this.textLines = new ArrayList<>();
        }

        /** See {@link Draw.Colour} for colour encoding information. */
        public MultiLineBuilder setColour(int colour) {
            this.colour = colour;
            return this;
        }

        public MultiLineBuilder setSmall(boolean small) {
            this.small = small;
            return this;
        }

        public MultiLineBuilder setShadow(boolean shadow) {
            this.shadow = shadow;
            return this;
        }

        public MultiLineBuilder setLineHeight(int lineHeight) {
            this.lineHeight = lineHeight;
            return this;
        }

        public MultiLineBuilder adjustLineHeight(int delta) {
            this.lineHeight += delta;
            return this;
        }

        /** This method will split the input on the newline character {@code '\n'}. */
        public MultiLineBuilder addLine(String line) {
            return this.addAllLines(SPLITTER.split(line));
        }

        public MultiLineBuilder addAllLines(Iterable<String> lines) {
            lines.forEach(textLines::add);
            return this;
        }

        public List<Text> build() {
            List<Text> list = new ArrayList<>(textLines.size());

            if (small) {
                lineHeight /= 2;
            }

            int y = position.y();
            // Move to top of text area.
            switch (direction.yFactor) {
                case -1:
                    y -= textLines.size() * lineHeight;
                    break;

                case 0:
                    y -= textLines.size() * lineHeight / 2;
                    break;

                case 1:
                    // We're already at the top.
                    break;
            }

            for (String line : textLines) {
                list.add(
                        builder(line, Point.create(position.x(), y), direction)
                                .setColour(colour)
                                .setSmall(small)
                                .setShadow(shadow)
                                .build());
                y += lineHeight;
            }

            return list;
        }
    }
}