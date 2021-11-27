package com.github.dcysteine.neicustomdiagram.generators.gregtech5.circuits;

import com.github.dcysteine.neicustomdiagram.api.diagram.Diagram;
import com.github.dcysteine.neicustomdiagram.api.diagram.component.Component;
import com.github.dcysteine.neicustomdiagram.api.diagram.component.DisplayComponent;
import com.github.dcysteine.neicustomdiagram.api.diagram.component.ItemComponent;
import com.github.dcysteine.neicustomdiagram.api.diagram.interactable.CustomInteractable;
import com.github.dcysteine.neicustomdiagram.api.diagram.interactable.Interactable;
import com.github.dcysteine.neicustomdiagram.api.diagram.layout.Grid;
import com.github.dcysteine.neicustomdiagram.api.diagram.matcher.ComponentDiagramMatcher;
import com.github.dcysteine.neicustomdiagram.api.diagram.tooltip.Tooltip;
import com.github.dcysteine.neicustomdiagram.api.draw.Draw;
import com.github.dcysteine.neicustomdiagram.api.draw.Point;
import com.github.dcysteine.neicustomdiagram.main.Lang;
import com.github.dcysteine.neicustomdiagram.util.ComponentTransformer;
import com.github.dcysteine.neicustomdiagram.util.gregtech5.GregTechOreDictUtil;
import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSortedSet;
import com.google.common.collect.Sets;

import java.util.Optional;
import java.util.Set;
import java.util.function.Consumer;

class DiagramFactory {
    private final CircuitLineHandler circuitLineHandler;
    private final LabelHandler labelHandler;
    private final LayoutHandler layoutHandler;
    private final RecipeHandler recipeHandler;

    DiagramFactory(
            CircuitLineHandler circuitLineHandler, LabelHandler labelHandler,
            LayoutHandler layoutHandler, RecipeHandler recipeHandler) {
        this.circuitLineHandler = circuitLineHandler;
        this.labelHandler = labelHandler;
        this.layoutHandler = layoutHandler;
        this.recipeHandler = recipeHandler;
    }

    Diagram buildOverviewDiagram() {
        Diagram.Builder diagramBuilder = Diagram.builder()
                .addLayout(layoutHandler.overviewLayout());

        Diagram.Builder.SlotGroupManualSubBuilder circuitLinesSlotSubBuilder =
                diagramBuilder.manualInsertIntoSlotGroup(LayoutHandler.SlotGroupKeys.CIRCUIT_LINES);
        ImmutableList<CircuitLine> circuitLines = circuitLineHandler.circuitLines();
        for (int i = 0; i < circuitLines.size(); i++) {
            CircuitLine circuitLine = circuitLines.get(i);

            circuitLinesSlotSubBuilder.insertIntoSlot(
                    i, 0, ComponentTransformer.transformToDisplay(circuitLine.boards()));

            for (int j = 0; j < circuitLine.circuits().size(); j++) {
                circuitLinesSlotSubBuilder.insertIntoSlot(
                        i, circuitLine.startTier() + j + 1,
                        buildCircuitDisplayComponent(circuitLine, j));
            }
        }

        Diagram.Builder.SlotGroupManualSubBuilder individualCircuitsSlotSubBuilder =
                diagramBuilder.manualInsertIntoSlotGroup(
                        LayoutHandler.SlotGroupKeys.INDIVIDUAL_CIRCUITS);
        circuitLines = circuitLineHandler.individualCircuits();
        for (int i = 0; i < circuitLines.size(); i++) {
            CircuitLine circuitLine = circuitLines.get(i);
            Preconditions.checkState(
                    circuitLine.circuits().size() == 1,
                    "Expected individual circuit: %s", circuitLine);

            individualCircuitsSlotSubBuilder.insertIntoSlot(
                    i, 0, ComponentTransformer.transformToDisplay(circuitLine.boards()));
            individualCircuitsSlotSubBuilder.insertIntoSlot(
                    i, 1, buildCircuitDisplayComponent(circuitLine, 0));
        }

        Diagram.Builder.SlotGroupManualSubBuilder circuitPartsSlotSubBuilder =
                diagramBuilder.manualInsertIntoSlotGroup(LayoutHandler.SlotGroupKeys.CIRCUIT_PARTS);
        ImmutableList<ImmutableList<ItemComponent>> circuitParts =
                circuitLineHandler.circuitParts();
        for (int i = 0; i < circuitParts.size(); i++) {
            ImmutableList<ItemComponent> circuitPartsSubList = circuitParts.get(i);
            for (int j = 0; j < circuitPartsSubList.size(); j++) {
                circuitPartsSlotSubBuilder.insertIntoSlot(
                        i, j, ComponentTransformer.transformToDisplay(circuitPartsSubList.get(j)));
            }
        }

        return diagramBuilder.build();
    }

    void buildDiagrams(ItemComponent circuit, ComponentDiagramMatcher.Builder matcherBuilder) {
        ImmutableList<CircuitRecipe> recipes =
                recipeHandler.getCircuitAssemblingMachineRecipes(circuit);

        if (recipes.isEmpty()) {
            // There's no circuit assembling machine recipes, but we should still build a single
            // diagram with no recipe displayed.
            buildDiagram(circuit, 0, 0, Optional.empty(), matcherBuilder);
        } else {
            int recipeCount = recipes.size();
            for (int i = 0; i < recipeCount; i++) {
                buildDiagram(circuit, i, recipeCount, Optional.of(recipes.get(i)), matcherBuilder);
            }
        }
    }

    void buildDiagram(
            ItemComponent circuit,
            int recipeIndex, int recipeCount, Optional<CircuitRecipe> recipeOptional,
            ComponentDiagramMatcher.Builder matcherBuilder) {
        Diagram.Builder diagramBuilder = Diagram.builder()
                .addAllLayouts(layoutHandler.requiredLayouts())
                .addAllOptionalLayouts(layoutHandler.optionalLayouts());
        Set<Component> craftingComponents = Sets.newHashSet(circuit);
        Set<Component> usageComponents = Sets.newHashSet(circuit);

        if (recipeHandler.hasCraftingTableRecipes(circuit)) {
            diagramBuilder.addInteractable(
                    labelHandler.buildLabel(
                            LabelHandler.ItemLabel.CRAFTING_TABLE,
                            LayoutHandler.AdditionalRecipeLabelPositions.CRAFTING_TABLE));
        }
        if (recipeHandler.hasAssemblingMachineRecipes(circuit)) {
            diagramBuilder.addInteractable(
                    labelHandler.buildLabel(
                            LabelHandler.ItemLabel.ASSEMBLING_MACHINE,
                            LayoutHandler.AdditionalRecipeLabelPositions.ASSEMBLING_MACHINE));
        }
        if (recipeHandler.hasAssemblingLineRecipes(circuit)) {
            diagramBuilder.addInteractable(
                    labelHandler.buildLabel(
                            LabelHandler.ItemLabel.ASSEMBLING_LINE,
                            LayoutHandler.AdditionalRecipeLabelPositions.ASSEMBLING_LINE));
        }

        CircuitLineHandler.CircuitLineCircuits circuitLineCircuits =
                circuitLineHandler.circuitLineCircuits(circuit);
        Diagram.Builder.SlotGroupManualSubBuilder circuitLineCircuitsSlotSubBuilder =
                diagramBuilder.manualInsertIntoSlotGroup(
                        LayoutHandler.SlotGroupKeys.CIRCUIT_LINE_CIRCUITS);
        circuitLineCircuits.previousCircuit().ifPresent(
                previousCircuit -> circuitLineCircuitsSlotSubBuilder.insertIntoSlot(
                        0, 0, previousCircuit));
        circuitLineCircuits.currentCircuit().ifPresent(
                currentCircuit -> circuitLineCircuitsSlotSubBuilder.insertIntoSlot(
                        1, 0, currentCircuit));
        circuitLineCircuits.nextCircuit().ifPresent(
                nextCircuit -> circuitLineCircuitsSlotSubBuilder.insertIntoSlot(
                        2, 0, nextCircuit));

        diagramBuilder.autoInsertIntoSlotGroup(LayoutHandler.SlotGroupKeys.TIER_CIRCUITS)
                .insertEachSafe(circuitLineHandler.tierCircuits(circuit));

        if (recipeOptional.isPresent()) {
            CircuitRecipe recipe = recipeOptional.get();

            CustomInteractable baseLabel =
                    labelHandler.buildLabel(
                            LabelHandler.ItemLabel.CIRCUIT_ASSEMBLING_MACHINE,
                            LayoutHandler.AdditionalRecipeLabelPositions
                                    .CIRCUIT_ASSEMBLING_MACHINE);
            Tooltip.Builder tooltipBuilder =
                    Tooltip.builder()
                            .addAllLines(baseLabel.tooltip().lines())
                            .addSpacing()
                            .addTextLine(
                                    Lang.GREGTECH_5_CIRCUITS.transf(
                                            "recipeindexlabel", recipeIndex + 1, recipeCount));
            Consumer<Point> drawForeground = position -> {};
            if (recipe.missingCombinations()) {
                tooltipBuilder
                        .addSpacing()
                        .setFormatting(Tooltip.URGENT_FORMATTING)
                        .addTextLine(Lang.GREGTECH_5_CIRCUITS.trans("missingcombinationslabel"));
                drawForeground =
                        position ->
                                Draw.drawTextOverIcon(
                                        "*", position, Grid.Direction.NW,
                                        Draw.Colour.RED, true, true);
            }
            diagramBuilder.addInteractable(
                    CustomInteractable.builder(baseLabel.drawable())
                            .setTooltip(tooltipBuilder.build())
                            .setDrawForeground(drawForeground)
                            .build());

            if (recipe.requiresCleanroom()) {
                diagramBuilder.addInteractable(
                        labelHandler.buildLabel(
                                LabelHandler.ItemLabel.CLEAN_ROOM,
                                LayoutHandler.AdditionalRecipeLabelPositions.CLEAN_ROOM));
            }
            if (recipe.requiresLowGravity()) {
                diagramBuilder.addInteractable(
                        labelHandler.buildLabel(
                                LabelHandler.ItemLabel.LOW_GRAVITY,
                                LayoutHandler.AdditionalRecipeLabelPositions.LOW_GRAVITY));
            }

            ImmutableList<ImmutableSortedSet<DisplayComponent>> itemInputs = recipe.itemInputs();
            for (int i = 0; i < itemInputs.size(); i++) {
                ImmutableSortedSet<DisplayComponent> subItemInputs = itemInputs.get(i);
                diagramBuilder.autoInsertIntoSlotGroup(
                                LayoutHandler.SLOT_GROUP_RECIPE_ITEM_INPUTS.get(i))
                        .insertEachSafe(subItemInputs);
                subItemInputs.stream()
                        .map(DisplayComponent::component)
                        .flatMap(c -> GregTechOreDictUtil.getAssociatedComponents(c).stream())
                        .forEach(usageComponents::add);
            }

            ImmutableSortedSet<DisplayComponent> fluidInputs = recipe.fluidInputs();
            diagramBuilder.autoInsertIntoSlotGroup(LayoutHandler.SlotGroupKeys.RECIPE_FLUID_INPUTS)
                    .insertEachSafe(fluidInputs);
            fluidInputs.stream()
                    .map(DisplayComponent::component).forEach(usageComponents::add);

            diagramBuilder.insertIntoSlot(LayoutHandler.SlotKeys.RECIPE_OUTPUT, recipe.output());
        }

        matcherBuilder.addDiagram(diagramBuilder.build())
                .addAllComponents(Interactable.RecipeType.CRAFTING, craftingComponents)
                .addAllComponents(Interactable.RecipeType.USAGE, usageComponents);
    }

    static DisplayComponent buildCircuitDisplayComponent(
            CircuitLine circuitLine, int index) {
        return GregTechCircuits.buildCircuitDisplayComponent(
                circuitLine.circuits().get(index), circuitLine.startTier() + index);
    }
}
