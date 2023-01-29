package com.github.dcysteine.neicustomdiagram.api.draw.scroll;

import codechicken.lib.gui.GuiDraw;
import codechicken.nei.guihook.GuiContainerManager;
import codechicken.nei.recipe.GuiRecipe;
import com.github.dcysteine.neicustomdiagram.api.draw.Dimension;
import com.github.dcysteine.neicustomdiagram.api.draw.Point;
import com.github.dcysteine.neicustomdiagram.main.Reflection;
import com.github.dcysteine.neicustomdiagram.main.config.ConfigOptions;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.ScaledResolution;
import org.lwjgl.opengl.GL11;

import java.util.Optional;

/** Handles scrolling support, as well as finding the mouse position. */
public final class ScrollManager {
    // Margins for glScissor, in pixels. These margins will be excluded from the scissor region.
    static final int TOP_MARGIN = 31;
    static final int BOTTOM_MARGIN = 5;
    static final int SIDE_MARGIN = 4;

    private final Scrollbar verticalScrollbar;
    private final Scrollbar horizontalScrollbar;

    public ScrollManager() {
        verticalScrollbar = new Scrollbar(this, ScrollOrientation.VERTICAL);
        horizontalScrollbar = new Scrollbar(this, ScrollOrientation.HORIZONTAL);
    }

    public boolean keyboardScroll(Dimension diagramDimension, ScrollDirection direction) {
        // This technically allows both scrollbars to handle the same event,
        // but in practice, this cannot happen.
        boolean handled = verticalScrollbar.scroll(
                direction, ConfigOptions.KEYBOARD_SCROLL_SPEED.get());
        handled |= horizontalScrollbar.scroll(direction, ConfigOptions.KEYBOARD_SCROLL_SPEED.get());
        return handled;
    }

    public boolean mouseScroll(ScrollDirection direction) {
        // Horizontal scrolling is more rarely done, so we will scroll horizontally only if the
        // mouse is directly over the horizontal scrollbar. Otherwise, we will default to vertical.
        if (horizontalScrollbar.mouseInScrollBounds()) {
            ScrollDirection horizontalDirection;
            switch (direction) {
                case UP:
                    horizontalDirection = ScrollDirection.LEFT;
                    break;

                case DOWN:
                    horizontalDirection = ScrollDirection.RIGHT;
                    break;

                default:
                    // We don't currently support direct horizontal scrolling with the mouse,
                    // but just in case...
                    horizontalDirection = direction;
                    break;
            }
            return horizontalScrollbar.scroll(
                    horizontalDirection, ConfigOptions.MOUSE_SCROLL_SPEED.get());
        } else {
            return verticalScrollbar.scroll(
                    direction, ConfigOptions.MOUSE_SCROLL_SPEED.get());
        }
    }

    /** Returns whether the click was handled. */
    public boolean mouseClickScrollbar(MouseButton button) {
        // We intentionally allow both scrollbars to handle the same event.
        // This allows for de-selecting one scrollbar, and selecting another, with a single click.
        boolean handled = verticalScrollbar.mouseClickScrollbar(button);
        handled |= horizontalScrollbar.mouseClickScrollbar(button);
        return handled;
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

    public Point getAbsoluteMousePosition() {
        java.awt.Point mouse = GuiDraw.getMousePosition();
        return Point.create(mouse.x, mouse.y);
    }

    public Point getRelativeMousePosition(int recipe) {
        Optional<GuiRecipe<?>> guiOptional = getGui();
        if (!guiOptional.isPresent()) {
            // The GUI got closed already, or something.
            return Point.create(0, 0);
        }
        GuiRecipe<?> gui = guiOptional.get();

        java.awt.Point mouse = GuiDraw.getMousePosition();
        java.awt.Point offset = gui.getRecipePosition(recipe);

        int x = mouse.x + horizontalScrollbar.getScroll()
                - (Reflection.GUI_LEFT.get(gui) + offset.x);
        int y = mouse.y + verticalScrollbar.getScroll() - (Reflection.GUI_TOP.get(gui) + offset.y);
        return Point.create(x, y);
    }

    /**
     * Returns the top-left corner of the viewport.
     *
     * <p>Note that this is incorrect for {@code glScissor}!
     */
    Point getViewportPosition() {
        Optional<GuiRecipe<?>> guiOptional = getGui();
        if (!guiOptional.isPresent()) {
            // The GUI got closed already, or something.
            return Point.create(0, 0);
        }
        GuiRecipe<?> gui = guiOptional.get();

        return Point.create(
                Reflection.GUI_LEFT.get(gui) + SIDE_MARGIN,
                Reflection.GUI_TOP.get(gui) + TOP_MARGIN);
    }

    Dimension getViewportDimension() {
        Optional<GuiRecipe<?>> guiOptional = getGui();
        if (!guiOptional.isPresent()) {
            // The GUI got closed already, or something.
            return Dimension.create(0, 0);
        }
        GuiRecipe<?> gui = guiOptional.get();

        return Dimension.create(
                Reflection.X_SIZE.get(gui) - 2 * SIDE_MARGIN,
                Reflection.Y_SIZE.get(gui) - (TOP_MARGIN + BOTTOM_MARGIN));
    }

    private void setScissor() {
        Optional<GuiRecipe<?>> guiOptional = getGui();
        if (!guiOptional.isPresent()) {
            // The GUI got closed already, or something.
            return;
        }
        GuiRecipe<?> gui = guiOptional.get();

        int left = Reflection.GUI_LEFT.get(gui) + SIDE_MARGIN;
        int bottom =
                gui.height - (Reflection.GUI_TOP.get(gui) + Reflection.Y_SIZE.get(gui))
                        + BOTTOM_MARGIN;

        Minecraft minecraft = Minecraft.getMinecraft();
        ScaledResolution res =
                new ScaledResolution(minecraft, minecraft.displayWidth, minecraft.displayHeight);
        int scaleFactor = res.getScaleFactor();

        Dimension viewportDim = getViewportDimension();
        // glScissor measures from the bottom-left corner rather than the top-left corner.
        // It also uses absolute screen coordinates, without taking into account the GUI scale
        // factor, so we must manually compute the scale.
        GL11.glScissor(
                left * scaleFactor, bottom * scaleFactor,
                viewportDim.width() * scaleFactor, viewportDim.height() * scaleFactor);
    }

    /** Returns empty {@link Optional} in cases such as the GUI being instantly closed. */
    private Optional<GuiRecipe<?>> getGui() {
        GuiContainerManager manager = GuiContainerManager.getManager();
        if (manager == null || !(manager.window instanceof GuiRecipe)) {
            return Optional.empty();
        }

        return Optional.of((GuiRecipe<?>) manager.window);
    }

    public void tick() {
        verticalScrollbar.tick();
        horizontalScrollbar.tick();
    }

    /** Checks for bad scroll state due to things like resizes or switching diagrams. */
    public void refreshState(Dimension diagramDimension) {
        horizontalScrollbar.refreshState(diagramDimension);
        verticalScrollbar.refreshState(diagramDimension);
    }

    public void beforeDraw() {
        GL11.glPushMatrix();
        GL11.glTranslatef(-horizontalScrollbar.getScroll(), -verticalScrollbar.getScroll(), 0);
        setScissor();
        GL11.glEnable(GL11.GL_SCISSOR_TEST);
        GL11.glColor4f(1, 1, 1, 1);
    }

    public void afterDraw() {
        GL11.glDisable(GL11.GL_SCISSOR_TEST);
        GL11.glPopMatrix();
    }

    /** This needs to be called with absolute coordinate context, such as when drawing tooltips. */
    public void drawScrollbars() {
        GL11.glDisable(GL11.GL_DEPTH_TEST);
        GL11.glDisable(GL11.GL_LIGHTING);
        GuiDraw.gui.incZLevel(300);

        horizontalScrollbar.draw();
        verticalScrollbar.draw();

        GuiDraw.gui.incZLevel(-300);
        GL11.glEnable(GL11.GL_LIGHTING);
        GL11.glEnable(GL11.GL_DEPTH_TEST);
    }
}
