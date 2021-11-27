package com.github.dcysteine.neicustomdiagram.api.diagram;

import codechicken.nei.NEIClientConfig;
import codechicken.nei.NEIClientUtils;
import codechicken.nei.PositionedStack;
import codechicken.nei.api.IOverlayHandler;
import codechicken.nei.api.IRecipeOverlayRenderer;
import codechicken.nei.recipe.GuiRecipe;
import codechicken.nei.recipe.ICraftingHandler;
import codechicken.nei.recipe.IUsageHandler;
import codechicken.nei.recipe.RecipeItemInputHandler;
import com.github.dcysteine.neicustomdiagram.api.diagram.component.Component;
import com.github.dcysteine.neicustomdiagram.api.diagram.component.DisplayComponent;
import com.github.dcysteine.neicustomdiagram.api.diagram.component.FluidComponent;
import com.github.dcysteine.neicustomdiagram.api.diagram.component.ItemComponent;
import com.github.dcysteine.neicustomdiagram.api.diagram.interactable.Interactable;
import com.github.dcysteine.neicustomdiagram.api.diagram.interactable.InteractiveComponentGroup;
import com.github.dcysteine.neicustomdiagram.api.diagram.matcher.DiagramMatcher;
import com.github.dcysteine.neicustomdiagram.api.draw.Dimension;
import com.github.dcysteine.neicustomdiagram.api.draw.GuiManager;
import com.github.dcysteine.neicustomdiagram.api.draw.Point;
import com.github.dcysteine.neicustomdiagram.main.config.ConfigOptions;
import com.google.common.collect.ImmutableList;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.inventory.Container;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fluids.FluidStack;

import javax.annotation.OverridingMethodsMustInvokeSuper;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.function.Supplier;
import java.util.stream.Collectors;

public class DiagramGroup implements ICraftingHandler, IUsageHandler {
    protected final DiagramGroupInfo info;
    protected final DiagramMatcher matcher;
    protected final Supplier<DiagramState> diagramStateSupplier;

    protected final GuiManager guiManager;
    protected final DiagramState diagramState;
    protected final ImmutableList<Diagram> diagrams;

    public DiagramGroup(
            DiagramGroupInfo info, DiagramMatcher matcher,
            Supplier<DiagramState> diagramStateSupplier) {
        this.info = info;
        this.matcher = matcher;
        this.diagramStateSupplier = diagramStateSupplier;

        this.guiManager = new GuiManager();
        this.diagramState = diagramStateSupplier.get();
        this.diagrams = ImmutableList.of();
    }

    public DiagramGroup(DiagramGroupInfo info, DiagramMatcher matcher) {
        this(info, matcher, DiagramState::new);
    }

    public DiagramGroup(DiagramGroup parent, Iterable<? extends Diagram> diagrams) {
        this.info = parent.info;
        this.matcher = parent.matcher;
        this.diagramStateSupplier = parent.diagramStateSupplier;

        this.guiManager = new GuiManager();
        this.diagramState = this.diagramStateSupplier.get();
        this.diagrams = ImmutableList.copyOf(diagrams);
    }

    public DiagramGroupInfo info() {
        return info;
    }

    /** Subclasses will need to override this to use their own constructor. */
    public DiagramGroup newInstance(Iterable<? extends Diagram> diagrams) {
        return new DiagramGroup(this, diagrams);
    }

    @Override
    public String getHandlerId() {
        return info.groupId();
    }

    @Override
    public String getRecipeName() {
        return info.groupName();
    }

    @Override
    public int recipiesPerPage() {
        return info.diagramsPerPage();
    }

    @Override
    public int numRecipes() {
        return diagrams.size();
    }

    public DiagramGroup loadDiagrams(
            String id, Interactable.RecipeType recipeType, Object... stacks) {
        Collection<Diagram> matchingDiagrams = matchDiagrams(id, recipeType, stacks);

        if (!ConfigOptions.SHOW_EMPTY_DIAGRAMS.get()) {
            matchingDiagrams =
                    matchingDiagrams.stream()
                            .filter(diagram -> !info.emptyDiagramPredicate().test(diagram))
                            .collect(Collectors.toList());
        }

        return newInstance(matchingDiagrams);
    }

    /**
     * Helper method responsible for finding all matching diagrams.
     *
     * <p>Subclasses should generally override / extend this method, leaving the general logic in
     * {@link #loadDiagrams(String, Interactable.RecipeType, Object...)} un-overridden.
     */
    protected Collection<Diagram> matchDiagrams(
            String id, Interactable.RecipeType recipeType, Object... stacks) {
        if (id.equals(info.groupId())) {
            return matcher.all();
        }

        if (!ConfigOptions.getDiagramGroupVisibility(info).isShown()) {
            return ImmutableList.of();
        }

        switch (id) {
            case "item":
                ItemStack itemStack = (ItemStack) stacks[0];
                ItemComponent itemComponent =
                        info.ignoreNbt()
                                ? ItemComponent.create(itemStack)
                                : ItemComponent.createWithNbt(itemStack);

                return matcher.match(recipeType, itemComponent);

            case "liquid":
            case "fluid":
                FluidStack fluidStack = (FluidStack) stacks[0];
                FluidComponent fluidComponent =
                        info.ignoreNbt()
                                ? FluidComponent.create(fluidStack)
                                : FluidComponent.createWithNbt(fluidStack);

                return matcher.match(recipeType, fluidComponent);
        }

        return ImmutableList.of();
    }

    @Override
    public final ICraftingHandler getRecipeHandler(String outputId, Object... results) {
        return loadDiagrams(outputId, Interactable.RecipeType.CRAFTING, results);
    }

    @Override
    public final IUsageHandler getUsageHandler(String inputId, Object... ingredients) {
        return loadDiagrams(inputId, Interactable.RecipeType.USAGE, ingredients);
    }

    @OverridingMethodsMustInvokeSuper
    @Override
    public void onUpdate() {
        guiManager.tick();
        diagramState.tick();
    }

    @Override
    public void drawBackground(int recipe) {
        Diagram diagram = diagrams.get(recipe);
        Dimension diagramDimension = diagram.dimension(diagramState);
        guiManager.checkScrollState(diagramDimension);
        guiManager.beforeDraw(diagramDimension);

        diagram.drawBackground(diagramState);

        guiManager.afterDraw(diagramDimension);
    }

    @Override
    public void drawForeground(int recipe) {
        Diagram diagram = diagrams.get(recipe);
        Dimension diagramDimension = diagram.dimension(diagramState);
        guiManager.beforeDraw(diagramDimension);

        diagram.drawForeground(diagramState);
        Optional<Interactable> interactable = findHoveredInteractable(recipe);
        interactable.ifPresent(i -> i.drawOverlay(diagramState));

        guiManager.afterDraw(diagramDimension);
    }

    public void drawTooltip(GuiRecipe gui, int recipe) {
        Diagram diagram = diagrams.get(recipe);
        Dimension diagramDimension = diagram.dimension(diagramState);
        guiManager.drawScrollbar(diagramDimension);

        Optional<Interactable> interactable = findHoveredInteractable(recipe);
        interactable.ifPresent(
                i -> i.drawTooltip(diagramState, guiManager.getAbsoluteMousePosition()));
    }

    protected Optional<Interactable> findHoveredInteractable(int recipe) {
        if (!mouseInBounds()) {
            return Optional.empty();
        }

        Point mousePos = guiManager.getRelativeMousePosition(recipe);
        for (Interactable interactable : diagrams.get(recipe).interactables(diagramState)) {
            if (interactable.checkBoundingBox(mousePos)) {
                return Optional.of(interactable);
            }
        }

        return Optional.empty();
    }

    public boolean mouseInBounds() {
        return guiManager.mouseInBounds();
    }

    public boolean interact(int recipe, Interactable.RecipeType recipeType) {
        Optional<Interactable> interactable = findHoveredInteractable(recipe);
        if (interactable.isPresent()) {
            interactable.get().interact(diagramState, recipeType);
            return true;
        } else {
            return false;
        }
    }

    /**
     * Note that for item components, the code here seems to be getting overridden (more precisely,
     * intercepted and handled first) by the code in {@link RecipeItemInputHandler}.
     */
    @Override
    public boolean keyTyped(GuiRecipe gui, char keyChar, int keyCode, int recipe) {
        if (keyCode == NEIClientConfig.getKeyBinding("gui.recipe")) {
            return interact(recipe, Interactable.RecipeType.CRAFTING);

        } else if (keyCode == NEIClientConfig.getKeyBinding("gui.usage")) {
            return interact(recipe, Interactable.RecipeType.USAGE);
        }

        return false;
    }

    /**
     * Note that for item components, the code here seems to be getting overridden (more precisely,
     * intercepted and handled first) by the code in {@link RecipeItemInputHandler}.
     */
    @Override
    public boolean mouseClicked(GuiRecipe gui, int button, int recipe) {
        boolean handled =
            guiManager.mouseClickScrollbar(
                    button == 0 ? GuiManager.MouseButton.LEFT : GuiManager.MouseButton.RIGHT,
                    diagrams.get(recipe).dimension(diagramState));
        if (handled) {
            return true;
        }

        switch (button) {
            case 0:
                return interact(recipe, Interactable.RecipeType.CRAFTING);

            case 1:
                return interact(recipe, Interactable.RecipeType.USAGE);
        }

        return false;
    }

    @Override
    public boolean mouseScrolled(GuiRecipe gui, int scroll, int recipe) {
        if (!mouseInBounds() && !guiManager.mouseInScrollBounds()) {
            return false;
        }
        GuiManager.ScrollDirection direction =
                scroll > 0 ? GuiManager.ScrollDirection.UP : GuiManager.ScrollDirection.DOWN;

        if (NEIClientUtils.shiftKey()) {
            diagramState.scroll(direction);
            return true;
        }

        Diagram diagram = diagrams.get(recipe);
        Dimension diagramDimension = diagram.dimension(diagramState);
        if (guiManager.isScrollable(diagramDimension)) {
            guiManager.scroll(direction);
            return true;
        } else {
            return ConfigOptions.DISABLE_PAGE_SCROLL.get();
        }
    }

    public Optional<ItemStack> getStackUnderMouse(int recipe) {
        Optional<Interactable> interactableOptional = findHoveredInteractable(recipe);
        if (!interactableOptional.isPresent()) {
            return Optional.empty();
        }

        Interactable interactable = interactableOptional.get();
        if (!(interactable instanceof InteractiveComponentGroup)) {
            return Optional.empty();
        }

        DisplayComponent component =
                ((InteractiveComponentGroup) interactable).currentComponent(diagramState);
        if (component.type() == Component.ComponentType.ITEM) {
            return Optional.of((ItemStack) component.stack());
        } else {
            return Optional.empty();
        }
    }

    @Override
    public boolean hasOverlay(GuiContainer gui, Container container, int recipe) {
        return false;
    }

    @Override
    public IRecipeOverlayRenderer getOverlayRenderer(GuiContainer gui, int recipe) {
        return null;
    }

    @Override
    public IOverlayHandler getOverlayHandler(GuiContainer gui, int recipe) {
        return null;
    }

    /** We have our own custom tooltip drawing code. */
    @Override
    public List<String> handleTooltip(GuiRecipe gui, List<String> currenttip, int recipe) {
        // Call our custom tooltip logic. It must be called here rather than in drawForeground(),
        // because calling it in drawForeground() will cause it to be drawn under NEI side panels.
        drawTooltip(gui, recipe);

        return currenttip;
    }

    /** We have our own custom tooltip drawing code. */
    @Override
    public List<String> handleItemTooltip(
            GuiRecipe gui, ItemStack stack, List<String> currenttip, int recipe) {
        return currenttip;
    }

    /** We don't use {@link PositionedStack} because it only supports items, and not fluids. */
    @Override
    public List<PositionedStack> getIngredientStacks(int recipe) {
        return ImmutableList.of();
    }

    /** We don't use {@link PositionedStack} because it only supports items, and not fluids. */
    @Override
    public List<PositionedStack> getOtherStacks(int recipetype) {
        return ImmutableList.of();
    }

    /** We don't use {@link PositionedStack} because it only supports items, and not fluids. */
    @Override
    public PositionedStack getResultStack(int recipe) {
        return null;
    }
}
