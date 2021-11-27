package com.github.dcysteine.neicustomdiagram.generators.gregtech5.lenses;

import com.github.dcysteine.neicustomdiagram.api.diagram.Diagram;
import com.github.dcysteine.neicustomdiagram.api.diagram.component.Component;
import com.github.dcysteine.neicustomdiagram.api.diagram.component.DisplayComponent;
import com.github.dcysteine.neicustomdiagram.api.diagram.interactable.Interactable;
import com.github.dcysteine.neicustomdiagram.api.diagram.matcher.ComponentDiagramMatcher;
import com.github.dcysteine.neicustomdiagram.api.diagram.tooltip.Tooltip;
import com.github.dcysteine.neicustomdiagram.main.Lang;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

import java.util.Comparator;
import java.util.List;
import java.util.Set;

class DiagramFactory {
    private final LayoutHandler layoutHandler;
    private final RecipeHandler recipeHandler;

    DiagramFactory(LayoutHandler layoutHandler, RecipeHandler recipeHandler) {
        this.layoutHandler = layoutHandler;
        this.recipeHandler = recipeHandler;
    }

    void buildDiagrams(RecipeHandler.Lens lens, ComponentDiagramMatcher.Builder matcherBuilder) {
        List<RecipeHandler.Recipe> recipes =
                Lists.newArrayList(recipeHandler.recipes(lens));

        // Sort so that lens-specific recipes are shown first.
        Comparator<RecipeHandler.Recipe> recipeComparator =
                Comparator.<RecipeHandler.Recipe, Boolean>comparing(
                                recipe -> recipeHandler.isColourRecipe(lens.colour(), recipe))
                        .thenComparing(Comparator.naturalOrder());
        recipes.sort(recipeComparator);

        List<List<RecipeHandler.Recipe>> partitionedRecipes =
                Lists.partition(recipes, LayoutHandler.MAX_RECIPES_PER_PAGE);
        partitionedRecipes.forEach(partition -> buildDiagram(lens, matcherBuilder, partition));
    }

    void buildDiagram(
            RecipeHandler.Lens lens, ComponentDiagramMatcher.Builder matcherBuilder,
            List<RecipeHandler.Recipe> recipes) {
        Diagram.Builder diagramBuilder = Diagram.builder()
                .addAllLayouts(layoutHandler.requiredLayouts())
                .addAllOptionalLayouts(layoutHandler.optionalLayouts())
                .addInteractable(lens.colour().buildLabel());
        Set<Component> craftingComponents = Sets.newHashSet(lens.itemComponent());
        Set<Component> usageComponents = Sets.newHashSet(lens.itemComponent());

        Diagram.Builder.SlotGroupAutoSubBuilder slotGroupSubBuilder =
                diagramBuilder.autoInsertIntoSlotGroup(
                        LayoutHandler.SlotGroupKeys.SAME_COLOUR_LENSES);
        for (RecipeHandler.Lens sameColourLens : recipeHandler.lenses(lens.colour())) {
            DisplayComponent.Builder displayComponentBuilder =
                    DisplayComponent.builder(sameColourLens.itemComponent());

            if (sameColourLens.equals(lens)) {
                displayComponentBuilder
                        .setAdditionalInfo("*")
                        .setAdditionalTooltip(
                                Tooltip.create(
                                        Lang.GREGTECH_5_LENSES.trans("currentlenslabel"),
                                        Tooltip.INFO_FORMATTING));
            }

            slotGroupSubBuilder.insertIntoNextSlot(displayComponentBuilder.build());
        }

        for (int i = 0; i < recipes.size(); i++) {
            RecipeHandler.Recipe recipe = recipes.get(i);

            DisplayComponent.Builder inputBuilder = DisplayComponent.builder(recipe.input());
            if (recipeHandler.isColourRecipe(lens.colour(), recipe)) {
                inputBuilder.setAdditionalTooltip(
                        Tooltip.create(
                                Lang.GREGTECH_5_LENSES.transf(
                                        "colourrecipelabel", lens.colour().translateColour()),
                                Tooltip.INFO_FORMATTING));
            } else {
                inputBuilder
                        .setAdditionalInfo("*")
                        .setAdditionalTooltip(
                                Tooltip.create(
                                        Lang.GREGTECH_5_LENSES.trans("specificrecipelabel"),
                                        Tooltip.INFO_FORMATTING));
            }

            diagramBuilder.insertIntoSlot(
                    LayoutHandler.SLOT_RECIPE_INPUTS.get(i), inputBuilder.build());
            diagramBuilder.insertIntoSlot(
                    LayoutHandler.SLOT_RECIPE_OUTPUTS.get(i), recipe.output());
            craftingComponents.add(recipe.output().component());
            usageComponents.add(recipe.input());
        }

        matcherBuilder.addDiagram(diagramBuilder.build())
                .addAllComponents(Interactable.RecipeType.CRAFTING, craftingComponents)
                .addAllComponents(Interactable.RecipeType.USAGE, usageComponents);
    }
}
