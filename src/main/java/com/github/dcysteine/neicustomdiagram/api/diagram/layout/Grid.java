package com.github.dcysteine.neicustomdiagram.api.diagram.layout;

import com.github.dcysteine.neicustomdiagram.api.draw.Point;
import com.google.auto.value.AutoValue;

/**
 * Helper class for aligning slots and lines to a grid, and computing slot and margin offsets.
 *
 * <p>NEI gives us {@code 166px} width to work with. Slots take up {@code 18px} each, and we want to
 * have a margin between slots to give us room to draw lines. So we can construct a 7-column grid of
 * slots with a {@code 6px} margin between slots, for a total of {@code 7 * 18 + 6 * 6 = 160}.
 *
 * <p>For height, it looks like there's just about exactly enough height to fit 14 rows of slots
 * when NEI is maximized. At the default Minecraft window height, NEI can only fit 6 rows of slots.
 *
 * <p>For better resolution, we'll halve the width of each grid square. So the even-numbered grid
 * indices correspond to slot centers in the {@code 14 x 7} slot grid described above, and the
 * odd-numbered grid indices correspond to the midpoint between those slots. These midpoints can be
 * used as margin points, or to construct offset slots.
 */
@AutoValue
public abstract class Grid {
    /** The default {@code Grid} instance, which has no offset. */
    public static final Grid GRID = create(0, 0);

    public static final int TOTAL_WIDTH = 166;
    public static final int TOTAL_HEIGHT = 332;
    public static final int STACK_WIDTH = 16;
    public static final int SLOT_WIDTH = 18;
    public static final int BIG_SLOT_WIDTH = 26;
    public static final int MARGIN_WIDTH = 6;
    public static final int SIDE_MARGIN_WIDTH = 2;

    public static final int GRID_WIDTH = 7;
    public static final int GRID_HEIGHT = 14;

    public enum Direction {
        NW(-1, -1), N(0, -1), NE(1, -1),
        W(-1, 0), C(0, 0), E(1, 0),
        SW(-1, 1), S(0, 1), SE(1, 1);

        public final int xFactor, yFactor;
        Direction(int xFactor, int yFactor) {
            this.xFactor = xFactor;
            this.yFactor = yFactor;
        }
    }

    public static Grid create(int offsetX, int offsetY) {
        return create(Point.create(offsetX, offsetY));
    }

    public static Grid create(Point offset) {
        return new AutoValue_Grid(offset);
    }

    /** The grid's offset, which is also the top-left point of the constructed grid. */
    public abstract Point offset();

    /**
     * Returns a {@link Point} corresponding to the center of the grid square {@code (x, y)}.
     *
     * <p>Grid coordinates are 0-indexed.
     */
    public Point grid(int x, int y) {
        int gridX = SIDE_MARGIN_WIDTH + (SLOT_WIDTH + x * (SLOT_WIDTH + MARGIN_WIDTH)) / 2;
        int gridY = SIDE_MARGIN_WIDTH + (SLOT_WIDTH + y * (SLOT_WIDTH + MARGIN_WIDTH)) / 2;
        return offset().translate(gridX, gridY);
    }

    /**
     * Returns a {@link Point} corresponding to the margin of the grid square {@code (x, y)} in the
     * specified direction.
     *
     * <p>Grid coordinates are 0-indexed.
     */
    public Point margin(int x, int y, Direction dir) {
        return margin(grid(x, y), dir);
    }

    /**
     * Returns a {@link Point} corresponding to the edge of the grid square {@code (x, y)} in the
     * specified direction.
     *
     * <p>Grid coordinates are 0-indexed.
     */
    public Point edge(int x, int y, Direction dir) {
        return edge(grid(x, y), dir);
    }

    /**
     * Returns a {@link Point} corresponding to the edge of a big slot placed on the grid square
     * {@code (x, y)} in the specified direction.
     *
     * <p>Grid coordinates are 0-indexed.
     */
    public Point bigEdge(int x, int y, Direction dir) {
        return bigEdge(grid(x, y), dir);
    }

    /**
     * Returns a {@link Point} corresponding to the margin of the slot at position {@code pos} in
     * the specified direction.
     */
    public static Point margin(Point pos, Direction dir) {
        int offsetX = dir.xFactor * (SLOT_WIDTH + MARGIN_WIDTH) / 2;
        int offsetY = dir.yFactor * (SLOT_WIDTH + MARGIN_WIDTH) / 2;
        return pos.translate(offsetX, offsetY);
    }

    /**
     * Returns a {@link Point} corresponding to the edge of the slot at position {@code pos} in the
     * specified direction.
     */
    public static Point edge(Point pos, Direction dir) {
        int offsetX = dir.xFactor * SLOT_WIDTH / 2;
        int offsetY = dir.yFactor * SLOT_WIDTH / 2;
        return pos.translate(offsetX, offsetY);
    }

    /**
     * Returns a {@link Point} corresponding to the edge of the big slot at position {@code pos} in
     * the specified direction.
     */
    public static Point bigEdge(Point pos, Direction dir) {
        int offsetX = dir.xFactor * BIG_SLOT_WIDTH / 2;
        int offsetY = dir.yFactor * BIG_SLOT_WIDTH / 2;
        return pos.translate(offsetX, offsetY);
    }
}