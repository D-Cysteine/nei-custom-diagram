package com.github.dcysteine.neicustomdiagram.api.draw.scroll;

import codechicken.lib.gui.GuiDraw;
import com.github.dcysteine.neicustomdiagram.api.draw.Dimension;
import com.github.dcysteine.neicustomdiagram.api.draw.Point;

/** Handles drawing and controlling of a scrollbar. */
class Scrollbar {
    /**
     * Increases the scrollable area, to make sure that everything is fully scrollable into view.
     */
    private static final int PADDING = 2;

    /** The distance, in pixels, between the edge of the diagram and the scrollbar. */
    private static final int OFFSET = 8;

    /** The minimum length of the scrollbar cursor, in pixels. */
    private static final int MIN_CURSOR_LENGTH = 2;

    /** Half of the width of the scrollbar cursor, in pixels. */
    private static final int CURSOR_RADIUS = 3;

    /** Half of the width of the scrollbar itself, in pixels. */
    private static final int SCROLLBAR_RADIUS = 1;

    /** The # of pixels of leeway (in all four directions) for scrollbar mouseover detection. */
    private static final int MOUSEOVER_PADDING = 2;

    /** The scrollbar will fade away over this many ticks. */
    private static final int FADE_TICKS = 24;

    // Colours from: https://www.canva.com/colours/colour-palettes/mermaid-lagoon/
    private static final int FOREGROUND_COLOUR = 0x145DA0;
    private static final int FOREGROUND_SELECTED_COLOUR = 0x2E8BC0;
    private static final int BACKGROUND_COLOUR = 0xB1D4E0;
    private static final int FOREGROUND_COLOUR_OPACITY = 0xF0;
    private static final int BACKGROUND_COLOUR_OPACITY = 0x90;


    private final ScrollManager scrollManager;
    private final ScrollOrientation orientation;
    private final ScrollbarDimensions dimensions;
    private int scroll;
    private boolean selected;
    private int fade;

    public Scrollbar(ScrollManager scrollManager, ScrollOrientation orientation) {
        this.scrollManager = scrollManager;
        this.orientation = orientation;
        this.dimensions = new ScrollbarDimensions();
        this.scroll = 0;
        this.selected = false;
        // Draw the scrollbar upon initialization of the diagram, if it is scrollable.
        this.fade = FADE_TICKS;
    }

    /**
     * Returns the scroll offset, in pixels.
     * Will be a non-negative integer between 0 and the maximum scroll amount, inclusive.
     */
    public int getScroll() {
        return scroll;
    }

    /** Does not check bounds! Bounds checks are done by {@link #refreshState(Dimension)}. */
    public boolean scroll(ScrollDirection direction, int amount) {
        int scrollAmount = orientation.dotProduct(direction) * amount;
        if (dimensions.offscreenLength <= 0 || scrollAmount == 0) {
            return false;
        }

        scroll += scrollAmount;
        fade = FADE_TICKS;
        return true;
    }

    /** Returns whether the click was handled. */
    public boolean mouseClickScrollbar(MouseButton button) {
        if (!mouseInScrollBounds()) {
            if (selected) {
                selected = false;
                return true;
            } else {
                return false;
            }
        }

        switch (button) {
            case LEFT:
                selected = !selected;
                return true;

            case RIGHT:
                scrollToMouse();
                return true;
        }

        return false;
    }

    /** Does not check bounds! Bounds checks are done by {@link #refreshState(Dimension)}. */
    public void scrollToMouse() {
        int mousePos =
                orientation.dotProduct(scrollManager.getAbsoluteMousePosition());
        int mouseOffset =
                mousePos - orientation.dotProduct(dimensions.anchor) - dimensions.cursorLength / 2;

        scroll = mouseOffset * dimensions.offscreenLength / dimensions.scrollableLength;
        fade = FADE_TICKS;
    }

    public boolean mouseInScrollBounds() {
        int minX = dimensions.anchor.x() - MOUSEOVER_PADDING;
        int maxX = dimensions.end.x() + MOUSEOVER_PADDING;
        int minY = dimensions.anchor.y() - MOUSEOVER_PADDING;
        int maxY = dimensions.end.y() + MOUSEOVER_PADDING;

        Point mousePos = scrollManager.getAbsoluteMousePosition();
        return mousePos.x() >= minX && mousePos.x() <= maxX
                && mousePos.y() >= minY && mousePos.y() <= maxY;
    }

    public void tick() {
        if (mouseInScrollBounds()) {
            fade = FADE_TICKS;
        } else if (fade > 0) {
            fade--;
        }
    }

    /**
     * Checks for bad scroll state due to things like resizes or switching diagrams.
     *
     * <p>{@link #scroll(ScrollDirection, int)} does not check bounds,
     * because we will check them here.
     */
    public void refreshState(Dimension diagramDimension) {
        dimensions.compute(diagramDimension);
        if (dimensions.offscreenLength <= 0) {
            scroll = 0;
            selected = false;
            fade = 0;
            return;
        }

        if (selected) {
            scrollToMouse();
        }

        if (scroll > dimensions.offscreenLength) {
            scroll = dimensions.offscreenLength;
        } else if (scroll < 0) {
            scroll = 0;
        }
    }

    /** This needs to be called with absolute coordinate context, such as when drawing tooltips. */
    public void draw() {
        if (fade <= 0) {
            return;
        }

        // We'll keep the scrollbar at 100% opacity for half the fade duration, then have it fade.
        int fadeOpacity = Math.min(2 * fade, FADE_TICKS);
        int fgOpacity = FOREGROUND_COLOUR_OPACITY * fadeOpacity / FADE_TICKS;
        int bgOpacity = BACKGROUND_COLOUR_OPACITY * fadeOpacity / FADE_TICKS;
        int fgColour = selected ? FOREGROUND_SELECTED_COLOUR : FOREGROUND_COLOUR;
        fgColour |= fgOpacity << 24;
        int bgColour = BACKGROUND_COLOUR | bgOpacity << 24;

        int scrollbarAboveLength =
                scroll * dimensions.scrollableLength / dimensions.offscreenLength;
        // The top-left corner of the scrollbar above the cursor.
        Point scrollbarAboveStart =
                dimensions.anchor.translate(orientation.transposeScale(-SCROLLBAR_RADIUS));
        Dimension scrollbarAboveDimension =
                Dimension.create(scrollbarAboveLength, 2 * SCROLLBAR_RADIUS);
        int scrollbarAboveWidth = orientation.dotProduct(scrollbarAboveDimension);
        int scrollbarAboveHeight = orientation.transposeProduct(scrollbarAboveDimension);

        // The top-left corner of the cursor.
        Point cursorStart = dimensions.anchor.translate(orientation.scale(scrollbarAboveLength));
        cursorStart = cursorStart.translate(orientation.transposeScale(-CURSOR_RADIUS));
        Dimension cursorDimension = Dimension.create(dimensions.cursorLength, 2 * CURSOR_RADIUS);
        int cursorWidth = orientation.dotProduct(cursorDimension);
        int cursorHeight = orientation.transposeProduct(cursorDimension);

        int scrollbarBelowLength =
                dimensions.scrollbarLength - (scrollbarAboveLength + dimensions.cursorLength);
        // The top-left corner of the scrollbar below the cursor.
        Point scrollbarBelowStart =
                dimensions.end.translate(orientation.scale(-scrollbarBelowLength));
        scrollbarBelowStart =
                scrollbarBelowStart.translate(orientation.transposeScale(-SCROLLBAR_RADIUS));
        Dimension scrollbarBelowDimension =
                Dimension.create(scrollbarBelowLength, 2 * SCROLLBAR_RADIUS);
        int scrollbarBelowWidth = orientation.dotProduct(scrollbarBelowDimension);
        int scrollbarBelowHeight = orientation.transposeProduct(scrollbarBelowDimension);

        if (scrollbarAboveLength > 0) {
            GuiDraw.drawRect(
                    scrollbarAboveStart.x(), scrollbarAboveStart.y(),
                    scrollbarAboveWidth, scrollbarAboveHeight, bgColour);
        }

        GuiDraw.drawRect(
                cursorStart.x(), cursorStart.y(), cursorWidth, cursorHeight, fgColour);

        if (scrollbarBelowLength > 0) {
            GuiDraw.drawRect(
                    scrollbarBelowStart.x(), scrollbarBelowStart.y(),
                    scrollbarBelowWidth, scrollbarBelowHeight, bgColour);
        }
    }

    /**
     * Helper class which computes and stores various parameters for the scrollbar.
     *
     * <p>We'll re-compute these values each time the diagram is rendered, and then just store them,
     * to avoid needlessly re-computing them.
     */
    private class ScrollbarDimensions {
        private Point anchor = Point.create(0, 0);
        private Point end = Point.create(0, 0);

        /** The length of the complete scrollbar, in pixels. */
        private int scrollbarLength = 0;

        /** The length of the scrollbar cursor, in pixels. */
        private int cursorLength = 0;

        /** The amount of the scrollbar that is actually scrollable (excludes cursor length). */
        private int scrollableLength = 0;

        /** The length of the viewport, in pixels. */
        private int viewportLength = 0;

        /** The length of the diagram, in pixels. */
        private int diagramLength = 0;

        /** The amount of the diagram that is offscreen and must be scrolled, in pixels. */
        private int offscreenLength = 0;

        public void compute(Dimension diagramDimension) {
            Point viewportPos = scrollManager.getViewportPosition();
            Dimension viewportDim = scrollManager.getViewportDimension();

            viewportLength = orientation.dotProduct(viewportDim);
            scrollbarLength = viewportLength;

            // Transpose because a vertical scrollbar should be offset horizontally, and vice-versa.
            anchor = viewportPos.translate(
                    orientation.transposeScale(orientation.transposeProduct(viewportDim) + OFFSET));
            end = anchor.translate(orientation.scale(scrollbarLength));

            diagramLength = orientation.dotProduct(diagramDimension) + PADDING;
            offscreenLength = diagramLength - viewportLength;

            cursorLength =
                    Math.max((viewportLength * scrollbarLength) / diagramLength, MIN_CURSOR_LENGTH);
            scrollableLength = scrollbarLength - cursorLength;
        }
    }
}