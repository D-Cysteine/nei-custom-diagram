package com.github.dcysteine.neicustomdiagram.generators.gregtech5.recipedebugger;

import com.github.dcysteine.neicustomdiagram.api.diagram.Diagram;
import com.github.dcysteine.neicustomdiagram.api.diagram.DiagramGroupInfo;
import com.github.dcysteine.neicustomdiagram.api.diagram.component.DisplayComponent;
import com.github.dcysteine.neicustomdiagram.api.diagram.component.ItemComponent;
import com.github.dcysteine.neicustomdiagram.api.diagram.interactable.CustomInteractable;
import com.github.dcysteine.neicustomdiagram.api.diagram.layout.ComponentLabel;
import com.github.dcysteine.neicustomdiagram.api.diagram.tooltip.Tooltip;
import com.github.dcysteine.neicustomdiagram.api.draw.Draw;
import com.github.dcysteine.neicustomdiagram.mod.Lang;
import com.github.dcysteine.neicustomdiagram.util.ComponentTransformer;
import com.google.common.collect.ListMultimap;
import com.google.common.collect.Lists;
import com.google.common.collect.MultimapBuilder;
import gregtech.api.util.GT_Utility;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

class DiagramHandler {
    private final DiagramGroupInfo info;
    private final LayoutFactory layoutFactory;
    private final RecipeHandler recipeHandler;

    private final ListMultimap<GregTechRecipeDebugger.View, Diagram> diagramListMultimap;
    private Diagram menuDiagram;

    DiagramHandler(
            DiagramGroupInfo info, LayoutFactory layoutFactory, RecipeHandler recipeHandler) {
        this.info = info;
        this.layoutFactory = layoutFactory;
        this.recipeHandler = recipeHandler;

        this.diagramListMultimap =
                MultimapBuilder
                        .enumKeys(GregTechRecipeDebugger.View.class)
                        .arrayListValues()
                        .build();
    }

    /** This method must be called before any other methods are called. */
    void initialize() {
        diagramListMultimap.put(
                GregTechRecipeDebugger.View.PROGRAMMED_CIRCUITS, buildProgrammedCircuitsDiagram());
        diagramListMultimap.putAll(
                GregTechRecipeDebugger.View.CONSUME_CIRCUIT_RECIPES,
                buildRecipeDiagrams(recipeHandler.consumeCircuitRecipes));
        diagramListMultimap.putAll(
                GregTechRecipeDebugger.View.UNNECESSARY_CIRCUIT_RECIPES,
                buildRecipeDiagrams(recipeHandler.unnecessaryCircuitRecipes));
        diagramListMultimap.putAll(
                GregTechRecipeDebugger.View.COLLIDING_RECIPES,
                buildRecipeDiagrams(recipeHandler.collidingRecipes));
        diagramListMultimap.putAll(
                GregTechRecipeDebugger.View.VOIDING_RECIPES,
                buildRecipeDiagrams(recipeHandler.voidingRecipes));
        diagramListMultimap.putAll(
                GregTechRecipeDebugger.View.UNEQUAL_CELL_RECIPES,
                buildRecipeDiagrams(recipeHandler.unequalCellRecipes));

        // This must be last, as it reads counts from diagramListMultimap.
        menuDiagram = buildMenuDiagram();
    }

    List<Diagram> getMenuDiagram() {
        return Lists.newArrayList(menuDiagram);
    }

    List<Diagram> getDiagrams(GregTechRecipeDebugger.View view) {
        return diagramListMultimap.get(view);
    }

    private CustomInteractable buildViewButton(GregTechRecipeDebugger.View view) {
        return CustomInteractable.builder(
                        ComponentLabel.create(
                                view.icon, LayoutFactory.VIEW_BUTTON_POSITIONS.get(view)))
                .setTooltip(
                        Tooltip.builder()
                                .setFormatting(Tooltip.SPECIAL_FORMATTING)
                                .addTextLine(Lang.GREGTECH_5_RECIPE_DEBUGGER.trans(view.tooltipKey))
                                .addSpacing()
                                .setFormatting(Tooltip.INFO_FORMATTING)
                                .addTextLine(
                                        Lang.GREGTECH_5_RECIPE_DEBUGGER.transf(
                                                "diagramcount",
                                                diagramListMultimap.get(view).size()))
                                .build())
                .setInteract(view.behaviorId(info))
                .setDrawBackground(Draw::drawRaisedSlot)
                .setDrawOverlay(pos -> Draw.drawOverlay(pos, Draw.Color.OVERLAY_BLUE))
                .build();
    }

    private Diagram buildMenuDiagram() {
        int recipeCount =
                recipeHandler.allRecipes.values().stream().mapToInt(RecipePartitioner::size).sum();
        Diagram.Builder builder =
                Diagram.builder().addLayout(layoutFactory.buildMenuLayout(recipeCount));

        Arrays.stream(GregTechRecipeDebugger.View.values())
                .forEach(view -> builder.addInteractable(buildViewButton(view)));

        return builder.build();
    }

    private Diagram buildProgrammedCircuitsDiagram() {
        Diagram.Builder builder =
                Diagram.builder().addLayout(layoutFactory.buildProgrammedCircuitsLayout());

        List<DisplayComponent> programmedCircuits =
                IntStream.range(0, 25)
                        .mapToObj(i -> ItemComponent.create(GT_Utility.getIntegratedCircuit(i)))
                        .map(ComponentTransformer::transformToDisplay)
                        .collect(Collectors.toList());
        builder.autoInsertIntoSlotGroup(LayoutFactory.SlotGroupKeys.PROGRAMMED_CIRCUITS)
                .insertEachSafe(programmedCircuits);

        return builder.build();
    }

    private List<Diagram> buildRecipeDiagrams(List<RecipeHandler.Recipe> recipes) {
        List<Diagram> diagrams = new ArrayList<>();

        List<List<RecipeHandler.Recipe>> partitionedRecipes =
                Lists.partition(recipes, LayoutFactory.SlotGroupKeys.RECIPES_PER_PAGE);
        for (List<RecipeHandler.Recipe> partition : partitionedRecipes) {
            Diagram.Builder builder = Diagram.builder().addInteractable(layoutFactory.menuButton());

            for (int i = 0; i < partition.size(); i++) {
                RecipeHandler.Recipe recipe = partition.get(i);

                builder.addLayout(layoutFactory.buildRecipeLayout(i, recipe.recipeMap()));
                builder.autoInsertIntoSlotGroup(LayoutFactory.SlotGroupKeys.RECIPE_INPUTS.get(i))
                        .insertEachSafe(recipe.displayInputs());
                builder.autoInsertIntoSlotGroup(LayoutFactory.SlotGroupKeys.RECIPE_OUTPUTS.get(i))
                        .insertEachSafe(recipe.displayOutputs());
            }

            diagrams.add(builder.build());
        }

        return diagrams;
    }
}
