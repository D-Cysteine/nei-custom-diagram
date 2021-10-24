package com.github.dcysteine.neicustomdiagram.generators.gregtech5.circuits;

import com.github.dcysteine.neicustomdiagram.api.diagram.component.Component;
import com.github.dcysteine.neicustomdiagram.api.diagram.component.DisplayComponent;
import com.github.dcysteine.neicustomdiagram.api.diagram.component.ItemComponent;
import com.github.dcysteine.neicustomdiagram.mod.Logger;
import com.github.dcysteine.neicustomdiagram.util.ComponentTransformer;
import com.github.dcysteine.neicustomdiagram.util.gregtech5.GregTechOreDictUtil;
import com.github.dcysteine.neicustomdiagram.util.gregtech5.GregTechRecipeUtil;
import com.google.auto.value.AutoValue;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSortedSet;
import com.google.common.collect.Iterables;
import com.google.common.collect.ListMultimap;
import com.google.common.collect.MultimapBuilder;
import gregtech.api.util.GT_Recipe;
import net.minecraft.item.ItemStack;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * This class holds all of the valid input permutations for a given circuit's recipes in the circuit
 * assembling machine.
 */
@AutoValue
abstract class CircuitRecipe {
    static final int MAX_ITEM_INPUTS = 6;

    /** Helper class that holds display components describing a {@link GT_Recipe}. */
    @AutoValue
    abstract static class Recipe {
        private static Recipe create(GT_Recipe recipe) {
            ImmutableList.Builder<ImmutableList<DisplayComponent>> itemInputsBuilder =
                    ImmutableList.builder();
            for (ItemStack itemStack : recipe.mInputs) {
                if (itemStack == null) {
                    continue;
                }

                ItemComponent itemComponent = ItemComponent.create(itemStack);
                List<Component> reverseUnifiedItems =
                        GregTechOreDictUtil.reverseUnify(itemComponent);

                itemInputsBuilder.add(
                        ImmutableList.copyOf(
                                reverseUnifiedItems.stream()
                                        .map(ComponentTransformer::transformToDisplay)
                                        .collect(Collectors.toList())));
            }

            DisplayComponent fluidInput =
                    Iterables.getOnlyElement(
                            GregTechRecipeUtil.buildComponentsFromFluidInputs(recipe));
            DisplayComponent output =
                    Iterables.getOnlyElement(GregTechRecipeUtil.buildComponentsFromOutputs(recipe));

            return new AutoValue_CircuitRecipe_Recipe(
                    GregTechRecipeUtil.requiresCleanroom(recipe),
                    GregTechRecipeUtil.requiresLowGravity(recipe),
                    itemInputsBuilder.build(), fluidInput, output);
        }

        abstract boolean requiresCleanroom();
        abstract boolean requiresLowGravity();

        /** List of lists because we reverse unify each individual item. */
        abstract ImmutableList<ImmutableList<DisplayComponent>> itemInputs();
        abstract DisplayComponent fluidInput();
        abstract DisplayComponent output();

        private int itemInputsSize() {
            return itemInputs().size();
        }
        private int itemInputsPermutationMultiplier() {
            return itemInputs().stream().mapToInt(List::size).reduce(1, Math::multiplyExact);
        }
    }

    static CircuitRecipe create(int itemInputsSize, Collection<Recipe> recipes) {
        List<ImmutableSortedSet.Builder<DisplayComponent>> itemInputsBuilderList =
                new ArrayList<>();
        IntStream.range(0, itemInputsSize).forEach(
                i -> itemInputsBuilderList.add(ImmutableSortedSet.naturalOrder()));

        // TODO for now, we assume that all recipes with the same number of ingredients are
        //  permutations of the same recipe.
        //  If this stops being true, then we'll need to add handling for that.
        Recipe firstRecipe = Iterables.getFirst(recipes, null);
        boolean requiresCleanroom = firstRecipe.requiresCleanroom();
        boolean requiresLowGravity = firstRecipe.requiresLowGravity();
        DisplayComponent output = firstRecipe.output();

        ImmutableSortedSet.Builder<DisplayComponent> fluidInputsBuilder =
                ImmutableSortedSet.naturalOrder();
        for (Recipe recipe : recipes) {
            if (recipe.itemInputsSize() != itemInputsSize) {
                Logger.GREGTECH_5_CIRCUITS.warn(
                        "Expected recipe to have {} item inputs: [{}]", itemInputsSize, recipe);
                continue;
            }
            if (!output.equals(recipe.output())) {
                Logger.GREGTECH_5_CIRCUITS.warn(
                        "Expected recipe to have output [{}]: [{}]", output, recipe);
                continue;
            }
            if (requiresCleanroom != recipe.requiresCleanroom()) {
                Logger.GREGTECH_5_CIRCUITS.warn(
                        "Expected recipe to have cleanroom requirement [{}]: [{}]",
                        requiresCleanroom, recipe);
                continue;
            }
            if (requiresLowGravity != recipe.requiresLowGravity()) {
                Logger.GREGTECH_5_CIRCUITS.warn(
                        "Expected recipe to have low gravity requirement [{}]: [{}]",
                        requiresLowGravity, recipe);
                continue;
            }

            for (int i = 0; i < itemInputsSize; i++) {
                itemInputsBuilderList.get(i).addAll(recipe.itemInputs().get(i));
            }
            fluidInputsBuilder.add(recipe.fluidInput());
        }

        ImmutableList<ImmutableSortedSet<DisplayComponent>> itemInputs =
                ImmutableList.copyOf(
                        itemInputsBuilderList.stream()
                                .map(ImmutableSortedSet.Builder::build)
                                .collect(Collectors.toList()));
        ImmutableSortedSet<DisplayComponent> fluidInputs = fluidInputsBuilder.build();

        boolean missingCombinations = false;
        int expectedNumberOfRecipes = fluidInputs.size();
        expectedNumberOfRecipes *=
                itemInputs.stream().mapToInt(Set::size).reduce(1, Math::multiplyExact);
        expectedNumberOfRecipes /=
                recipes.stream()
                        .mapToInt(Recipe::itemInputsPermutationMultiplier)
                        .reduce(1, Math::multiplyExact);
        if (expectedNumberOfRecipes != recipes.size()) {
            Logger.GREGTECH_5_CIRCUITS.warn(
                    "Expected {} recipes but got {} for circuit: [{}]",
                    expectedNumberOfRecipes, recipes.size(), output);

            missingCombinations = expectedNumberOfRecipes > recipes.size();
        }

        return new AutoValue_CircuitRecipe(
                missingCombinations, requiresCleanroom, requiresLowGravity,
                itemInputs, fluidInputs, output);
    }

    static List<CircuitRecipe> buildCircuitRecipes(Iterable<GT_Recipe> rawRecipes) {
        ListMultimap<Integer, Recipe> recipeMultimap =
                MultimapBuilder.hashKeys().arrayListValues().build();

        for (GT_Recipe rawRecipe : rawRecipes) {
            Recipe recipe = Recipe.create(rawRecipe);
            recipeMultimap.put(recipe.itemInputsSize(), recipe);
        }

        return recipeMultimap.asMap().entrySet().stream()
                // We use reverse sort order here because circuit recipes with fewer ingredients
                // tend to be more advanced, so we want to show those later.
                .sorted(Map.Entry.<Integer, Collection<Recipe>>comparingByKey().reversed())
                .map(entry -> create(entry.getKey(), entry.getValue()))
                .collect(Collectors.toList());
    }

    abstract boolean missingCombinations();
    abstract boolean requiresCleanroom();
    abstract boolean requiresLowGravity();

    /** A list of the various accepted item inputs for each input slot. */
    abstract ImmutableList<ImmutableSortedSet<DisplayComponent>> itemInputs();
    abstract ImmutableSortedSet<DisplayComponent> fluidInputs();
    abstract DisplayComponent output();
}