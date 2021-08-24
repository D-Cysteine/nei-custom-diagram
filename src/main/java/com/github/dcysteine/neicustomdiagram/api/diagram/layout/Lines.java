package com.github.dcysteine.neicustomdiagram.api.diagram.layout;

import com.github.dcysteine.neicustomdiagram.api.draw.Draw;
import com.github.dcysteine.neicustomdiagram.api.draw.Point;
import com.google.auto.value.AutoValue;
import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;

/**
 * Immutable class holding line segments and arrows.
 *
 * <p>See {@link Draw.Color} for color encoding information.
 */
@AutoValue
public abstract class Lines {
    /**
     * Immutable class representing a line segment or arrow drawn between two points.
     *
     * <p>Order of the points matters only if this segment is being drawn as an arrow.
     */
    @AutoValue
    public abstract static class Segment {
        public static Segment create(Point a, Point b) {
            Preconditions.checkArgument(
                    a.isOrthogonal(b), "Points aren't orthogonal: [%s] [%s]", a, b);

            return new AutoValue_Lines_Segment(a, b);
        }

        public abstract Point a();
        public abstract Point b();

        /** See {@link Draw.Color} for color encoding information. */
        public void drawSegment(int color) {
            Draw.drawLine(a(), b(), color);
        }

        /** See {@link Draw.Color} for color encoding information. */
        public void drawArrow(int color) {
            drawSegment(color);
            Draw.drawArrowhead(a(), b(), color);
        }
    }

    /** See {@link Draw.Color} for color encoding information. */
    public abstract int color();

    public abstract ImmutableList<Segment> segments();
    public abstract ImmutableList<Segment> arrows();

    public void draw() {
        segments().forEach(segment -> segment.drawSegment(color()));
        arrows().forEach(segment -> segment.drawArrow(color()));
    }

    public static Builder builder(Point pos) {
        return new Builder(pos);
    }

    /**
     * Fluent builder for {@code Lines}.
     *
     * <p>This builder is initialized with a position, and keeps track of its current position.
     * This allows segments to be specified with just a single point: the segment will be drawn
     * between the builder's position and the new point, and then the builder will set its position
     * to the new point, kind of like a real pen.
     */
    public static final class Builder {
        private int color;
        private Point currentPosition;

        private final ImmutableList.Builder<Segment> segmentsBuilder;
        private final ImmutableList.Builder<Segment> arrowsBuilder;

        public Builder(Point pos) {
            color = Draw.Color.BLACK;
            currentPosition = pos;

            segmentsBuilder = ImmutableList.builder();
            arrowsBuilder = ImmutableList.builder();
        }

        /** See {@link Draw.Color} for color encoding information. */
        public Builder setColor(int color) {
            this.color = color;
            return this;
        }

        /** Sets the builder's current position without adding a segment. */
        public Builder move(Point pos) {
            currentPosition = pos;
            return this;
        }

        public Builder addSegment(Point pos) {
            segmentsBuilder.add(Segment.create(currentPosition, pos));
            currentPosition = pos;
            return this;
        }

        public Builder addSegment(Segment segment) {
            segmentsBuilder.add(segment);
            return this;
        }

        public Builder addArrow(Point pos) {
            arrowsBuilder.add(Segment.create(currentPosition, pos));
            currentPosition = pos;
            return this;
        }

        public Builder addReverseArrow(Point pos) {
            arrowsBuilder.add(Segment.create(pos, currentPosition));
            currentPosition = pos;
            return this;
        }

        public Builder addDoubleArrow(Point pos) {
            arrowsBuilder.add(Segment.create(currentPosition, pos));
            arrowsBuilder.add(Segment.create(pos, currentPosition));
            currentPosition = pos;
            return this;
        }

        public Builder addArrow(Segment segment) {
            arrowsBuilder.add(segment);
            return this;
        }

        public Lines build() {
            return new AutoValue_Lines(color, segmentsBuilder.build(), arrowsBuilder.build());
        }
    }
}
