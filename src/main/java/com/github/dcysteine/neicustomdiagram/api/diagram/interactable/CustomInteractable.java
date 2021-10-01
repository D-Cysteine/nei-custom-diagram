package com.github.dcysteine.neicustomdiagram.api.diagram.interactable;

import codechicken.nei.recipe.GuiCraftingRecipe;
import codechicken.nei.recipe.GuiUsageRecipe;
import com.github.dcysteine.neicustomdiagram.api.diagram.DiagramState;
import com.github.dcysteine.neicustomdiagram.api.diagram.component.FluidComponent;
import com.github.dcysteine.neicustomdiagram.api.diagram.component.ItemComponent;
import com.github.dcysteine.neicustomdiagram.api.diagram.tooltip.Tooltip;
import com.github.dcysteine.neicustomdiagram.api.draw.BoundedDrawable;
import com.github.dcysteine.neicustomdiagram.api.draw.Dimension;
import com.github.dcysteine.neicustomdiagram.api.draw.Point;

import java.util.function.Consumer;

/** This class is a flexible way to create arbitrary interactables, but requires a lot of setup. */
public class CustomInteractable implements Interactable {
    protected final BoundedDrawable drawable;
    protected final Tooltip tooltip;
    protected final Consumer<RecipeType> interact;

    /**
     * This function will be called before {@code label.draw()}, with {@code position} as a
     * parameter.
     *
     * <p>You can use this to do something like draw a slot under the label.
     */
    protected final Consumer<Point> drawBackground;

    /**
     * This function will be called when mousing over this interactable, with {@code position} as a
     * parameter.
     */
    protected final Consumer<Point> drawOverlay;

    protected CustomInteractable(
            BoundedDrawable drawable, Tooltip tooltip, Consumer<RecipeType> interact,
            Consumer<Point> drawBackground, Consumer<Point> drawOverlay) {
        this.drawable = drawable;
        this.tooltip = tooltip;
        this.interact = interact;
        this.drawBackground = drawBackground;
        this.drawOverlay = drawOverlay;
    }

    public BoundedDrawable drawable() {
        return drawable;
    }

    public Tooltip tooltip() {
        return tooltip;
    }

    @Override
    public Point position() {
        return drawable.position();
    }

    @Override
    public Dimension dimension() {
        return drawable.dimension();
    }

    @Override
    public void interact(DiagramState diagramState, RecipeType recipeType) {
        interact.accept(recipeType);
    }

    @Override
    public void draw(DiagramState diagramState) {
        drawBackground.accept(position());
        drawable.draw(diagramState);
    }

    @Override
    public void drawOverlay(DiagramState diagramState) {
        drawOverlay.accept(position());
    }

    @Override
    public void drawTooltip(DiagramState diagramState, Point mousePos) {
        tooltip.draw(mousePos);
    }

    /** Helper method for building an interaction lambda with a custom ID. */
    protected static Consumer<RecipeType> buildInteractionLambda(String id) {
        return recipeType -> {
            switch (recipeType) {
                case CRAFTING:
                    GuiUsageRecipe.openRecipeGui(id);
                    break;

                case USAGE:
                    GuiCraftingRecipe.openRecipeGui(id);
                    break;
            }
        };
    }

    public static Builder builder(BoundedDrawable drawable) {
        return new Builder(drawable);
    }

    public static final class Builder {
        private final BoundedDrawable drawable;
        private Tooltip tooltip;
        private Consumer<RecipeType> interact;
        private Consumer<Point> drawBackground;
        private Consumer<Point> drawOverlay;

        private Builder(BoundedDrawable drawable) {
            this.drawable = drawable;
            this.tooltip = Tooltip.EMPTY_TOOLTIP;

            // Initialize all method handlers to do nothing by default.
            this.interact = recipeType -> {};
            this.drawBackground = position -> {};
            this.drawOverlay = position -> {};
        }

        public Builder setTooltip(Tooltip tooltip) {
            this.tooltip = tooltip;
            return this;
        }

        /** Sets this interactable to invoke NEI with the given ID. */
        public Builder setInteract(String id) {
            this.interact = buildInteractionLambda(id);
            return this;
        }

        /**
         * You probably want to call this with one of these method references:
         * <ul>
         *     <li>{@link ItemComponent#interact(RecipeType)}
         *     <li>{@link FluidComponent#interact(RecipeType)}
         * </ul>
         */
        public Builder setInteract(Consumer<RecipeType> interact) {
            this.interact = interact;
            return this;
        }

        /**
         * This function will be called before {@code label.draw()}, with {@code position} as a
         * parameter.
         *
         * <p>You can use this to do something like draw a slot under the label.
         */
        public Builder setDrawBackground(Consumer<Point> drawBackground) {
            this.drawBackground = drawBackground;
            return this;
        }

        /**
         * This function will be called when mousing over this interactable, with {@code position}
         * as a parameter.
         */
        public Builder setDrawOverlay(Consumer<Point> drawOverlay) {
            this.drawOverlay = drawOverlay;
            return this;
        }

        public CustomInteractable build() {
            return new CustomInteractable(drawable, tooltip, interact, drawBackground, drawOverlay);
        }
    }
}
