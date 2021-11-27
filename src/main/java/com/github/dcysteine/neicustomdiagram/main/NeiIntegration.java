package com.github.dcysteine.neicustomdiagram.main;

import codechicken.nei.event.NEIRegisterHandlerInfosEvent;
import codechicken.nei.guihook.GuiContainerManager;
import codechicken.nei.guihook.IContainerObjectHandler;
import codechicken.nei.recipe.GuiRecipe;
import codechicken.nei.recipe.IRecipeHandler;
import com.github.dcysteine.neicustomdiagram.api.diagram.DiagramGroup;
import com.github.dcysteine.neicustomdiagram.api.diagram.DiagramGroupInfo;
import com.github.dcysteine.neicustomdiagram.main.config.ConfigOptions;
import com.github.dcysteine.neicustomdiagram.main.config.DiagramGroupVisibility;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.item.ItemStack;

import java.util.List;
import java.util.Optional;

/** Singleton class that handles any NEI integration that needs to be done. */
public enum NeiIntegration {
    // Singleton class; enforced by being an enum.
    INSTANCE;

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

    private List<DiagramGroupInfo> infoList;

    /** This method is only intended to be called during mod initialization. */
    public void initialize(List<DiagramGroupInfo> infoList) {
        this.infoList = infoList;

        GuiContainerManager.addObjectHandler(new ObjectHandler());
    }

    @SubscribeEvent
    @SuppressWarnings("unused")
    public void registerHandlers(NEIRegisterHandlerInfosEvent event) {
        Logger.MOD.info("Registering handlers for diagram groups...");

        for (DiagramGroupInfo info : infoList) {
            if (ConfigOptions.getDiagramGroupVisibility(info) == DiagramGroupVisibility.DISABLED) {
                continue;
            }

            event.registerHandlerInfo(
                    info.groupId(), NeiCustomDiagram.MOD_NAME, NeiCustomDiagram.MOD_ID,
                    info::buildHandlerInfo);
            Logger.MOD.info("Registered handler for diagram group [{}]!", info.groupId());
        }

        Logger.MOD.info("Registration complete!");
    }
}