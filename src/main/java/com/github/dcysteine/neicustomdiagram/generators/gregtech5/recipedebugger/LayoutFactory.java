package com.github.dcysteine.neicustomdiagram.generators.gregtech5.recipedebugger;

import com.github.dcysteine.neicustomdiagram.api.diagram.DiagramGroupInfo;
import com.github.dcysteine.neicustomdiagram.api.diagram.interactable.AllDiagramsButton;
import com.github.dcysteine.neicustomdiagram.api.diagram.interactable.CustomInteractable;
import com.github.dcysteine.neicustomdiagram.api.diagram.layout.Grid;
import com.github.dcysteine.neicustomdiagram.api.diagram.layout.Layout;
import com.github.dcysteine.neicustomdiagram.api.diagram.layout.Lines;
import com.github.dcysteine.neicustomdiagram.api.diagram.layout.SlotGroup;
import com.github.dcysteine.neicustomdiagram.api.diagram.layout.Text;
import com.github.dcysteine.neicustomdiagram.api.draw.Point;
import com.github.dcysteine.neicustomdiagram.mod.Lang;
import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;

import java.util.stream.Collectors;
import java.util.stream.IntStream;

class LayoutFactory {
    /** These start on the second row, to leave room for the total recipes label. */
    static final ImmutableMap<GregTechRecipeDebugger.View, Point> VIEW_BUTTON_POSITIONS =
            ImmutableMap.<GregTechRecipeDebugger.View, Point>builder()
                    .put(GregTechRecipeDebugger.View.PROGRAMMED_CIRCUITS, Grid.GRID.grid(0, 2))
                    .put(GregTechRecipeDebugger.View.CONSUME_CIRCUIT_RECIPES, Grid.GRID.grid(2, 2))
                    .put(GregTechRecipeDebugger.View.UNNECESSARY_CIRCUIT_RECIPES, Grid.GRID.grid(4, 2))
                    .put(GregTechRecipeDebugger.View.COLLIDING_RECIPES, Grid.GRID.grid(6, 2))
                    .put(GregTechRecipeDebugger.View.VOIDING_RECIPES, Grid.GRID.grid(8, 2))
                    .put(GregTechRecipeDebugger.View.UNEQUAL_CELL_RECIPES, Grid.GRID.grid(10, 2))
                    .build();

    static final class SlotGroupKeys {
        static final int RECIPES_PER_PAGE = 3;
        static final ImmutableList<Layout.SlotGroupKey> RECIPE_INPUTS =
                ImmutableList.copyOf(
                        IntStream.range(0, RECIPES_PER_PAGE)
                                .mapToObj(i -> Layout.SlotGroupKey.create("recipe-inputs-" + i))
                                .collect(Collectors.toList()));
        static final ImmutableList<Layout.SlotGroupKey> RECIPE_OUTPUTS =
                ImmutableList.copyOf(
                        IntStream.range(0, RECIPES_PER_PAGE)
                                .mapToObj(i -> Layout.SlotGroupKey.create("recipe-outputs-" + i))
                                .collect(Collectors.toList()));

        static final Layout.SlotGroupKey PROGRAMMED_CIRCUITS =
                Layout.SlotGroupKey.create("programmed-circuits");
    }

    private final DiagramGroupInfo info;
    private final LabelHandler labelHandler;

    private CustomInteractable menuButton;

    LayoutFactory(DiagramGroupInfo info, LabelHandler labelHandler) {
        this.info = info;
        this.labelHandler = labelHandler;
    }

    /** This method must be called before any other methods are called. */
    void initialize() {
        menuButton =
                new AllDiagramsButton(
                        info, Grid.GRID.grid(0, 0),
                        Lang.GREGTECH_5_RECIPE_DEBUGGER.trans("menubutton"));
    }

    CustomInteractable menuButton() {
        return menuButton;
    }

    Layout buildMenuLayout(int totalRecipes) {
        return Layout.builder()
                .addLabel(
                        Text.builder(
                                        Lang.GREGTECH_5_RECIPE_DEBUGGER.transf(
                                                "totalrecipecount", totalRecipes),
                                        Grid.GRID.grid(6, 0), Grid.Direction.C)
                                .build())
                .build();
    }

    Layout buildProgrammedCircuitsLayout() {
        return Layout.builder()
                .addInteractable(menuButton)
                .putSlotGroup(
                        SlotGroupKeys.PROGRAMMED_CIRCUITS,
                        SlotGroup.builder(5, 5, Grid.GRID.grid(6, 2), Grid.Direction.S)
                                .build())
                .build();
    }

    Layout buildRecipeLayout(int i, RecipeHandler.RecipeMap recipeMap) {
        Preconditions.checkArgument(
                i >= 0 && i < SlotGroupKeys.RECIPES_PER_PAGE,
                "i not in range [0, %d): %d", SlotGroupKeys.RECIPES_PER_PAGE, i);

        int y = 5 + i * 9;
        return Layout.builder()
                .addLines(
                        Lines.builder(Grid.GRID.grid(5, y))
                                .addArrow(Grid.edge(Grid.GRID.grid(9, y), Grid.Direction.W))
                                .build())
                .addInteractable(labelHandler.buildLabel(recipeMap, Grid.GRID.grid(7, y)))
                .putSlotGroup(
                        SlotGroupKeys.RECIPE_INPUTS.get(i),
                        SlotGroup.builder(4, 5, Grid.GRID.grid(5, y), Grid.Direction.W)
                                .build())
                .putSlotGroup(
                        SlotGroupKeys.RECIPE_OUTPUTS.get(i),
                        SlotGroup.builder(3, 5, Grid.GRID.grid(9, y), Grid.Direction.E)
                                .build())
                .build();
    }
}
