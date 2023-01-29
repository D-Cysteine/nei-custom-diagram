package com.github.dcysteine.neicustomdiagram.api.draw.scroll;

import codechicken.lib.gui.GuiDraw;
import com.github.dcysteine.neicustomdiagram.api.draw.Dimension;
import com.github.dcysteine.neicustomdiagram.api.draw.Point;
import org.lwjgl.opengl.GL11;

/** Handles drawing and controlling of a scrollbar. */
class Scrollbar {
    /**
     * Increases the scrollable area, to make sure that everything is fully scrollable into view.
     */
    private static final int PADDING = 2;

    /**
     * Specifies # of pixels of leeway (in all four directions) for scrollbar mouseover detection.
     */
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
    private int scroll;
    private boolean selected;
    private int fade;

    public Scrollbar(ScrollManager scrollManager, ScrollOrientation orientation) {
        this.scrollManager = scrollManager;
        this.orientation = orientation;
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

    /** Does not check bounds! Bounds checks are done by {@link #checkScrollState(Dimension)}. */
    public boolean scroll(
            Dimension diagramDimension, ScrollDirection direction, int amount) {
        int scrollAmount = orientation.scrollMultiplier(direction) * amount;
        if (!isScrollable(diagramDimension) || scrollAmount == 0) {
            return false;
        }

        scroll += scrollAmount;
        fade = FADE_TICKS;
        return true;
    }

    /** Returns whether the click was handled. */
    public boolean mouseClickScrollbar(Dimension diagramDimension, MouseButton button) {
        if (!mouseInScrollBounds() && !selected) {
            return false;
        }

        switch (button) {
            case LEFT:
                selected = !selected;
                return true;

            case RIGHT:
                scrollToMouse(diagramDimension);
                return true;
        }

        return false;
    }

    /** Does not check bounds! Bounds checks are done by {@link #checkScrollState(Dimension)}. */
    public void scrollToMouse(Dimension diagramDimension) {
        Dimension viewportDim = scrollManager.getViewportDimension();
        int paddedHeight = diagramDimension.height() + PADDING;
        int scrollbarCursorHeight = viewportDim.height() * viewportDim.height() / paddedHeight;

        int mouseOffset =
                scrollManager.getAbsoluteMousePosition().y()
                        - (scrollManager.getViewportPosition().y() + scrollbarCursorHeight / 2);
        int scrollbarHeight = viewportDim.height() - scrollbarCursorHeight;

        scroll = mouseOffset * computeScrollableLength(diagramDimension) / scrollbarHeight;
        fade = FADE_TICKS;
    }

    public boolean mouseInScrollBounds() {
        Point viewportPos = scrollManager.getViewportPosition();
        Dimension viewportDim = scrollManager.getViewportDimension();
        int scrollbarX = viewportPos.x() + viewportDim.width() + 5 - MOUSEOVER_PADDING;
        int scrollbarY = viewportPos.y() - MOUSEOVER_PADDING;
        int scrollbarWidth = 4 + 2 * MOUSEOVER_PADDING;
        int scrollbarHeight = viewportDim.height() + 2 * MOUSEOVER_PADDING;

        Point mousePos = scrollManager.getAbsoluteMousePosition();
        int xDiff = mousePos.x() - scrollbarX;
        int yDiff = mousePos.y() - scrollbarY;

        return xDiff >= 0 && xDiff <= scrollbarWidth && yDiff >= 0 && yDiff <= scrollbarHeight;
    }

    private int computeScrollableLength(Dimension diagramDimension) {
        switch (orientation) {
            case VERTICAL:
                return diagramDimension.height() - scrollManager.getViewportDimension().height()
                        + PADDING;

            case HORIZONTAL:
                return diagramDimension.width() - scrollManager.getViewportDimension().width()
                        + PADDING;

            default:
                throw new IllegalStateException("Unhandled orientation: " + orientation);
        }
    }

    public boolean isScrollable(Dimension diagramDimension) {
        return computeScrollableLength(diagramDimension) > 0;
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
     * <p>{@link #scroll(Dimension, ScrollDirection, int)} does not check bounds,
     * because we will check them here.
     */
    public void checkScrollState(Dimension diagramDimension) {
        int scrollableHeight = computeScrollableLength(diagramDimension);
        if (scrollableHeight <= 0) {
            scroll = 0;
            selected = false;
            fade = 0;
            return;
        }

        if (selected) {
            scrollToMouse(diagramDimension);
        }

        if (scroll > scrollableHeight) {
            scroll = scrollableHeight;
        } else if (scroll < 0) {
            scroll = 0;
        }
    }

    /** This needs to be called with absolute coordinate context, such as when drawing tooltips. */
    public void draw(Dimension diagramDimension) {
        if (fade <= 0) {
            return;
        }

        // We'll keep the scrollbar at 100% opacity for half the fade duration, then have it fade.
        int fadeOpacity = Math.min(2 * fade, FADE_TICKS);
        int fgOpacity = FOREGROUND_COLOUR_OPACITY * fadeOpacity / FADE_TICKS;
        int bgOpacity = BACKGROUND_COLOUR_OPACITY * fadeOpacity / FADE_TICKS;
        int fgColour =
                selected
                        ? FOREGROUND_SELECTED_COLOUR : FOREGROUND_COLOUR;
        fgColour |= fgOpacity << 24;
        int bgColour = BACKGROUND_COLOUR | bgOpacity << 24;

        Point viewportPos = scrollManager.getViewportPosition();
        Dimension viewportDim = scrollManager.getViewportDimension();
        int scrollbarX = viewportPos.x() + viewportDim.width() + 7;
        int scrollbarY = viewportPos.y();

        int paddedHeight = diagramDimension.height() + PADDING;
        int scrollbarCursorHeight = viewportDim.height() * viewportDim.height() / paddedHeight;
        int scrollbarCursorY = scrollbarY + (scroll * viewportDim.height() / paddedHeight);
        // Set a min height just in case integer division gives us 0 or something =S
        scrollbarCursorHeight += 2;
        scrollbarCursorY -= 1;

        int scrollbarCursorBottom = scrollbarCursorY + scrollbarCursorHeight;
        // Heights of exposed portions of scrollbar above and below the cursor.
        int scrollbarAboveHeight = scrollbarCursorY - scrollbarY;
        int scrollbarBelowHeight = scrollbarY + viewportDim.height() - scrollbarCursorBottom;

        GL11.glDisable(GL11.GL_DEPTH_TEST);
        GL11.glDisable(GL11.GL_LIGHTING);
        GuiDraw.gui.incZLevel(300);

        if (scrollbarAboveHeight > 0) {
            GuiDraw.drawRect(scrollbarX, scrollbarY, 2, scrollbarAboveHeight, bgColour);
        }
        GuiDraw.drawRect(scrollbarX - 2, scrollbarCursorY, 6, scrollbarCursorHeight, fgColour);
        if (scrollbarBelowHeight > 0) {
            GuiDraw.drawRect(scrollbarX, scrollbarCursorBottom, 2, scrollbarBelowHeight, bgColour);
        }

        GuiDraw.gui.incZLevel(-300);
        GL11.glEnable(GL11.GL_LIGHTING);
        GL11.glEnable(GL11.GL_DEPTH_TEST);
    }
}
