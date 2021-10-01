package com.github.dcysteine.neicustomdiagram.mod;

import codechicken.nei.guihook.GuiContainerManager;
import codechicken.nei.guihook.IContainerObjectHandler;
import codechicken.nei.recipe.GuiRecipe;
import codechicken.nei.recipe.IRecipeHandler;
import com.github.dcysteine.neicustomdiagram.api.diagram.DiagramGroup;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.item.ItemStack;

import java.util.Optional;

/** Class that handles any NEI integration that needs to be done. */
public final class NeiIntegration {
    // Static class.
    private NeiIntegration() {}

    /** This method is only intended to be called during mod initialization. */
    public static void initialize() {
        GuiContainerManager.addObjectHandler(new ObjectHandler());
    }

    private static class ObjectHandler implements IContainerObjectHandler {
        private static Optional<DiagramGroup> getDiagramGroup(GuiContainer guiContainer) {
            if (!(guiContainer instanceof GuiRecipe)) {
                return Optional.empty();
            }
            GuiRecipe gui = (GuiRecipe) guiContainer;

            IRecipeHandler handler = gui.getHandler();
            if (!(handler instanceof DiagramGroup)) {
                return Optional.empty();
            }
            return Optional.of((DiagramGroup) handler);
        }

        @Override
        public void guiTick(GuiContainer guiContainer) {}

        @Override
        public void refresh(GuiContainer guiContainer) {}

        @Override
        public void load(GuiContainer guiContainer) {}

        @Override
        public ItemStack getStackUnderMouse(GuiContainer guiContainer, int mousex, int mousey) {
            Optional<DiagramGroup> diagramGroupOptional = getDiagramGroup(guiContainer);
            if (!diagramGroupOptional.isPresent()) {
                return null;
            }

            DiagramGroup diagramGroup = diagramGroupOptional.get();
            for (int i : ((GuiRecipe) guiContainer).getRecipeIndices()) {
                Optional<ItemStack> itemStackOptional = diagramGroup.getStackUnderMouse(i);
                if (itemStackOptional.isPresent()) {
                    return itemStackOptional.get();
                }
            }
            return null;
        }

        @Override
        public boolean objectUnderMouse(GuiContainer guiContainer, int mousex, int mousey) {
            return false;
        }

        @Override
        public boolean shouldShowTooltip(GuiContainer guiContainer) {
            return getDiagramGroup(guiContainer)
                    .map(diagramGroup -> !diagramGroup.mouseInBounds())
                    .orElse(true);
        }
    }
}
