package com.github.dcysteine.neicustomdiagram.api.draw;

import codechicken.lib.gui.GuiDraw;
import codechicken.nei.guihook.GuiContainerManager;
import codechicken.nei.recipe.GuiRecipe;
import com.github.dcysteine.neicustomdiagram.mod.Reflection;
import com.github.dcysteine.neicustomdiagram.mod.config.ConfigOptions;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.ScaledResolution;
import org.lwjgl.opengl.GL11;

/** Handles things like getting the mouse position, and scrolling. */
public final class GuiManager {
    // Margins for glScissor, in pixels. These margins will be excluded from the scissor region.
    private static final int TOP_MARGIN = 31;
    private static final int BOTTOM_MARGIN = 5;
    private static final int SIDE_MARGIN = 4;

    /**
     * Increases the scrollable height, to make sure that everything is fully scrollable into view.
     */
    private static final int VERTICAL_PADDING = 2;

    /**
     * Specifies # of pixels of leeway (in all four directions) for scrollbar mouseover detection.
     */
    private static final int SCROLLBAR_MOUSEOVER_PADDING = 2;

    /** The scrollbar will fade away over this many ticks. */
    private static final int SCROLLBAR_FADE_TICKS = 24;

    // Colors from: https://www.canva.com/colors/color-palettes/mermaid-lagoon/
    private static final int SCROLLBAR_FOREGROUND_COLOR = 0x145DA0;
    private static final int SCROLLBAR_FOREGROUND_SELECTED_COLOR = 0x2E8BC0;
    private static final int SCROLLBAR_BACKGROUND_COLOR = 0xB1D4E0;
    private static final int SCROLLBAR_FOREGROUND_COLOR_OPACITY = 0xF0;
    private static final int SCROLLBAR_BACKGROUND_COLOR_OPACITY = 0x90;

    public enum ScrollDirection {
        UP(-1),
        DOWN(1);

        /** Either {@code +1} or {@code -1}. */
        public final int factor;

        ScrollDirection(int factor) {
            this.factor = factor;
        }
    }

    public enum MouseButton {
        LEFT, RIGHT;
    }

    /** Horizontal scrolling is not fully implemented; there isn't a way to change this value. */
    private int scrollX;
    private int scrollY;

    private boolean scrollbarSelected;
    private int scrollbarFade;

    public GuiManager() {
        this.scrollX = 0;
        this.scrollY = 0;

        this.scrollbarSelected = false;
        // Draw the scrollbar upon initialization of the diagram, if it is scrollable.
        this.scrollbarFade = SCROLLBAR_FADE_TICKS;
    }

    public void tick() {
        if (scrollbarFade > 0) {
            scrollbarFade--;
        }
    }

    /**
     * Checks for bad scroll state due to things like resizes or switching diagrams.
     *
     * <p>{@link #scroll(ScrollDirection)} does not check bounds, because we will check them here.
     */
    public void checkScrollState(Dimension diagramDimension) {
        int scrollableHeight = computeScrollableHeight(diagramDimension);
        if (scrollableHeight <= 0) {
            scrollY = 0;
            scrollbarSelected = false;
            scrollbarFade = 0;
            return;
        }

        if (mouseInScrollBounds()) {
            scrollbarFade = SCROLLBAR_FADE_TICKS;
        }
        if (scrollbarSelected) {
            scrollToMouse(diagramDimension);
        }

        if (scrollY > scrollableHeight) {
            scrollY = scrollableHeight;
        } else if (scrollY < 0) {
            scrollY = 0;
        }
    }

    public void beforeDraw(Dimension diagramDimension) {
        GL11.glPushMatrix();
        GL11.glTranslatef(-scrollX, -scrollY, 0);
        setScissor();
        GL11.glEnable(GL11.GL_SCISSOR_TEST);
        GL11.glColor4f(1, 1, 1, 1);
    }

    public void afterDraw(Dimension diagramDimension) {
        GL11.glDisable(GL11.GL_SCISSOR_TEST);
        GL11.glPopMatrix();
    }

    /** This needs to be called with absolute coordinate context, such as when drawing tooltips. */
    public void drawScrollbar(Dimension diagramDimension) {
        if (scrollbarFade <= 0) {
            return;
        }

        // TODO move all of this to yet another helper class. Maybe in new gui/ directory.
        //  Also move related methods in this class.
        //  Maybe when / if we add a horizontal scrollbar?
        // We'll keep the scrollbar at 100% opacity for half the fade duration, then have it fade.
        int fade = Math.min(2 * scrollbarFade, SCROLLBAR_FADE_TICKS);
        int fgOpacity = SCROLLBAR_FOREGROUND_COLOR_OPACITY * fade / SCROLLBAR_FADE_TICKS;
        int bgOpacity = SCROLLBAR_BACKGROUND_COLOR_OPACITY * fade / SCROLLBAR_FADE_TICKS;
        int fgColor =
                scrollbarSelected
                        ? SCROLLBAR_FOREGROUND_SELECTED_COLOR : SCROLLBAR_FOREGROUND_COLOR;
        fgColor |= fgOpacity << 24;
        int bgColor = SCROLLBAR_BACKGROUND_COLOR | bgOpacity << 24;

        Point viewportPos = getViewportPosition();
        Dimension viewportDim = getViewportDimension();
        int scrollbarX = viewportPos.x() + viewportDim.width() + 7;
        int scrollbarY = viewportPos.y();

        int scrollbarCursorHeight =
                viewportDim.height() * viewportDim.height() / diagramDimension.height();
        int scrollbarCursorY =
                scrollbarY + (scrollY * viewportDim.height() / diagramDimension.height());
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
            GuiDraw.drawRect(scrollbarX, scrollbarY, 2, scrollbarAboveHeight, bgColor);
        }
        GuiDraw.drawRect(scrollbarX - 2, scrollbarCursorY, 6, scrollbarCursorHeight, fgColor);
        if (scrollbarBelowHeight > 0) {
            GuiDraw.drawRect(scrollbarX, scrollbarCursorBottom, 2, scrollbarBelowHeight, bgColor);
        }

        GuiDraw.gui.incZLevel(-300);
        GL11.glEnable(GL11.GL_LIGHTING);
        GL11.glEnable(GL11.GL_DEPTH_TEST);
    }

    public boolean mouseInBounds() {
        Point mousePos = getAbsoluteMousePosition();
        Point viewportPos = getViewportPosition();
        int xDiff = mousePos.x() - viewportPos.x();
        int yDiff = mousePos.y() - viewportPos.y();

        Dimension viewportDim = getViewportDimension();
        return xDiff >= 0 && xDiff <= viewportDim.width()
                && yDiff >= 0 && yDiff <= viewportDim.height();
    }

    public boolean mouseInScrollBounds() {
        Point viewportPos = getViewportPosition();
        Dimension viewportDim = getViewportDimension();
        int scrollbarX = viewportPos.x() + viewportDim.width() + 5 - SCROLLBAR_MOUSEOVER_PADDING;
        int scrollbarY = viewportPos.y() - SCROLLBAR_MOUSEOVER_PADDING;
        int scrollbarWidth = 4 + 2 * SCROLLBAR_MOUSEOVER_PADDING;
        int scrollbarHeight = viewportDim.height() + 2 * SCROLLBAR_MOUSEOVER_PADDING;

        Point mousePos = getAbsoluteMousePosition();
        int xDiff = mousePos.x() - scrollbarX;
        int yDiff = mousePos.y() - scrollbarY;

        return xDiff >= 0 && xDiff <= scrollbarWidth && yDiff >= 0 && yDiff <= scrollbarHeight;
    }

    public Point getAbsoluteMousePosition() {
        java.awt.Point mouse = GuiDraw.getMousePosition();
        return Point.create(mouse.x, mouse.y);
    }

    public Point getRelativeMousePosition(int recipe) {
        GuiRecipe gui = getGui();
        java.awt.Point mouse = GuiDraw.getMousePosition();
        java.awt.Point offset = gui.getRecipePosition(recipe);

        int x = mouse.x + scrollX - (Reflection.GUI_LEFT.get(gui) + offset.x);
        int y = mouse.y + scrollY - (Reflection.GUI_TOP.get(gui) + offset.y);
        return Point.create(x, y);
    }

    public boolean isScrollable(Dimension diagramDimension) {
        return computeScrollableHeight(diagramDimension) > 0;
    }

    /** Does not check bounds! Bounds checks are done by {@link #checkScrollState(Dimension)}. */
    public void scroll(ScrollDirection direction) {
        int scrollAmount = direction.factor * ConfigOptions.SCROLL_SPEED.get();
        scrollY += scrollAmount;
        scrollbarFade = SCROLLBAR_FADE_TICKS;
    }

    /** Returns whether the click was handled. */
    public boolean mouseClickScrollbar(MouseButton button, Dimension diagramDimension) {
        if (!mouseInScrollBounds() && !scrollbarSelected) {
            return false;
        }

        switch (button) {
            case LEFT:
                scrollbarSelected = !scrollbarSelected;
                return true;

            case RIGHT:
                scrollToMouse(diagramDimension);
                return true;
        }

        return false;
    }

    /** Does not check bounds! Bounds checks are done by {@link #checkScrollState(Dimension)}. */
    public void scrollToMouse(Dimension diagramDimension) {
        Dimension viewportDim = getViewportDimension();
        int scrollbarCursorHeight =
                viewportDim.height() * viewportDim.height() / diagramDimension.height();

        int mouseOffset =
                getAbsoluteMousePosition().y()
                        - (getViewportPosition().y() + scrollbarCursorHeight / 2);
        int scrollbarHeight = viewportDim.height() - scrollbarCursorHeight;

        scrollY = mouseOffset * computeScrollableHeight(diagramDimension) / scrollbarHeight;
        scrollbarFade = SCROLLBAR_FADE_TICKS;
    }

    /**
     * Returns the top-left corner of the viewport.
     *
     * <p>Note that this is incorrect for {@code glScissor}!
     */
    public Point getViewportPosition() {
        GuiRecipe gui = getGui();
        return Point.create(
                Reflection.GUI_LEFT.get(gui) + SIDE_MARGIN,
                Reflection.GUI_TOP.get(gui) + TOP_MARGIN);
    }

    public Dimension getViewportDimension() {
        GuiRecipe gui = getGui();
        return Dimension.create(
                Reflection.X_SIZE.get(gui) - 2 * SIDE_MARGIN,
                Reflection.Y_SIZE.get(gui) - (TOP_MARGIN + BOTTOM_MARGIN));
    }

    private int computeScrollableHeight(Dimension diagramDimension) {
        // Need to add one to avoid clipping the bottom-most row.
        return diagramDimension.height() + VERTICAL_PADDING - getViewportDimension().height();
    }

    private void setScissor() {
        GuiRecipe gui = getGui();
        int left = Reflection.GUI_LEFT.get(gui) + SIDE_MARGIN;
        int bottom =
                gui.height - (Reflection.GUI_TOP.get(gui) + Reflection.Y_SIZE.get(gui))
                        + BOTTOM_MARGIN;

        Minecraft minecraft = Minecraft.getMinecraft();
        ScaledResolution res = new ScaledResolution(minecraft, minecraft.displayWidth, minecraft.displayHeight);
        int scaleFactor = res.getScaleFactor();

        Dimension viewportDim = getViewportDimension();
        // glScissor measures from the bottom-left corner rather than the top-left corner.
        // It also uses absolute screen coordinates, without taking into account the GUI scale
        // factor, so we must manually compute the scale.
        GL11.glScissor(
                left * scaleFactor, bottom * scaleFactor,
                viewportDim.width() * scaleFactor, viewportDim.height() * scaleFactor);
    }

    private GuiRecipe getGui() {
        return (GuiRecipe) GuiContainerManager.getManager().window;
    }
}
