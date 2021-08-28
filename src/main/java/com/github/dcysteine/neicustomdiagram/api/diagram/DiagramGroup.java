package com.github.dcysteine.neicustomdiagram.api.diagram;

import codechicken.lib.gui.GuiDraw;
import codechicken.nei.NEIClientConfig;
import codechicken.nei.NEIClientUtils;
import codechicken.nei.PositionedStack;
import codechicken.nei.api.IOverlayHandler;
import codechicken.nei.api.IRecipeOverlayRenderer;
import codechicken.nei.guihook.GuiContainerManager;
import codechicken.nei.recipe.GuiRecipe;
import codechicken.nei.recipe.ICraftingHandler;
import codechicken.nei.recipe.IUsageHandler;
import com.github.dcysteine.neicustomdiagram.api.Config;
import com.github.dcysteine.neicustomdiagram.api.Reflection;
import com.github.dcysteine.neicustomdiagram.api.diagram.component.FluidComponent;
import com.github.dcysteine.neicustomdiagram.api.diagram.component.ItemComponent;
import com.github.dcysteine.neicustomdiagram.api.diagram.interactable.Interactable;
import com.github.dcysteine.neicustomdiagram.api.diagram.matcher.DiagramMatcher;
import com.github.dcysteine.neicustomdiagram.api.draw.Point;
import com.github.dcysteine.neicustomdiagram.api.draw.Ticker;
import com.google.common.collect.ImmutableList;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.inventory.Container;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fluids.FluidStack;
import org.lwjgl.opengl.GL11;

import javax.annotation.OverridingMethodsMustInvokeSuper;
import java.util.List;
import java.util.Optional;

public class DiagramGroup implements ICraftingHandler, IUsageHandler {
    protected final DiagramGroupInfo info;
    protected final DiagramMatcher matcher;

    /**
     * Diagrams to display in NEI.
     */
    protected final ImmutableList<Diagram> diagrams;
    protected final Ticker ticker;

    public DiagramGroup(DiagramGroupInfo info, DiagramMatcher matcher) {
        this.info = info;
        this.matcher = matcher;

        this.diagrams = ImmutableList.of();
        this.ticker = new Ticker();
    }

    public DiagramGroup(DiagramGroup parent, Iterable<Diagram> diagrams) {
        this.info = parent.info;
        this.matcher = parent.matcher;

        this.diagrams = ImmutableList.copyOf(diagrams);
        this.ticker = new Ticker();
    }

    public DiagramGroupInfo info() {
        return info;
    }

    /** Subclasses will need to override this to use their own constructor. */
    public DiagramGroup newInstance(Iterable<Diagram> diagrams) {
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

    public DiagramGroup loadDiagrams(String id, Interactable.RecipeType recipeType, Object... stacks) {
        if (id.equals(info.groupId())) {
            return newInstance(matcher.all());
        }

        switch (id) {
            case "item":
                ItemStack itemStack = (ItemStack) stacks[0];
                return newInstance(matcher.match(recipeType, ItemComponent.create(itemStack)));

            case "liquid":
            case "fluid":
                FluidStack fluidStack = (FluidStack) stacks[0];
                return newInstance(matcher.match(recipeType, FluidComponent.create(fluidStack)));
        }

        return newInstance(ImmutableList.of());
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
        if (!NEIClientUtils.shiftKey()) {
            ticker.tick();
        }
    }

    @Override
    public void drawBackground(int recipe) {
        GL11.glColor4f(1, 1, 1, 1);
        diagrams.get(recipe).drawBackground(ticker);
    }

    @Override
    public void drawForeground(int recipe) {
        GL11.glColor4f(1, 1, 1, 1);
        diagrams.get(recipe).drawForeground(ticker);

        GuiContainer window = GuiContainerManager.getManager().window;
        if (window instanceof GuiRecipe) {
            Point mousePos = computeMousePosition((GuiRecipe) window, recipe);
            Optional<Interactable> interactable = findHoveredInteractable(mousePos, recipe);
            interactable.ifPresent(i -> i.drawOverlay(ticker));
        }
    }

    public void drawTooltip(GuiRecipe gui, int recipe) {
        Point mousePos = computeMousePosition(gui, recipe);
        Optional<Interactable> interactable = findHoveredInteractable(mousePos, recipe);
        interactable.ifPresent(i -> i.drawTooltip(ticker, absoluteMousePosition()));
    }

    protected Point absoluteMousePosition() {
        java.awt.Point mouse = GuiDraw.getMousePosition();
        return Point.create(mouse.x, mouse.y);
    }

    protected Point computeMousePosition(GuiRecipe gui, int recipe) {
        java.awt.Point mouse = GuiDraw.getMousePosition();
        java.awt.Point offset = gui.getRecipePosition(recipe);

        int x = mouse.x - (Reflection.GUI_LEFT.getInt(gui) + offset.x);
        int y = mouse.y - (Reflection.GUI_TOP.getInt(gui) + offset.y);
        return Point.create(x, y);
    }

    protected Optional<Interactable> findHoveredInteractable(Point mousePos, int recipe) {
        for (Interactable interactable : diagrams.get(recipe).interactables()) {
            if (interactable.checkBoundingBox(mousePos)) {
                return Optional.of(interactable);
            }
        }

        return Optional.empty();
    }

    public boolean interact(GuiRecipe gui, int recipe, Interactable.RecipeType recipeType) {
        Point mousePos = computeMousePosition(gui, recipe);
        Optional<Interactable> interactable = findHoveredInteractable(mousePos, recipe);

        if (interactable.isPresent()) {
            interactable.get().interact(ticker, recipeType);
            return true;
        } else {
            return false;
        }
    }

    @Override
    public boolean keyTyped(GuiRecipe gui, char keyChar, int keyCode, int recipe) {
        if (keyCode == NEIClientConfig.getKeyBinding("gui.recipe")) {
            return interact(gui, recipe, Interactable.RecipeType.CRAFTING);

        } else if (keyCode == NEIClientConfig.getKeyBinding("gui.usage")) {
            return interact(gui, recipe, Interactable.RecipeType.USAGE);

        } else if (keyCode == NEIClientConfig.getKeyBinding("gui.bookmark")) {
            return interact(gui, recipe, Interactable.RecipeType.BOOKMARK);
        }

        return false;
    }

    @Override
    public boolean mouseClicked(GuiRecipe gui, int button, int recipe) {
        switch (button) {
            case 0:
                return interact(gui, recipe, Interactable.RecipeType.CRAFTING);

            case 1:
                return interact(gui, recipe, Interactable.RecipeType.USAGE);
        }

        return false;
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
