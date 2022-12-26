package com.github.dcysteine.neicustomdiagram.generators.gregtech5.circuits;

import com.github.dcysteine.neicustomdiagram.api.diagram.component.Component;
import com.github.dcysteine.neicustomdiagram.api.diagram.component.DisplayComponent;
import com.github.dcysteine.neicustomdiagram.api.diagram.component.ItemComponent;
import com.github.dcysteine.neicustomdiagram.main.Logger;
import com.github.dcysteine.neicustomdiagram.util.gregtech5.GregTechOreDictUtil;
import com.github.dcysteine.neicustomdiagram.util.gregtech5.GregTechRecipeUtil;
import com.google.auto.value.AutoValue;
import com.google.common.collect.HashBasedTable;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSortedSet;
import com.google.common.collect.Iterables;
import com.google.common.collect.Table;
import gregtech.api.util.GT_Recipe;
import net.minecraft.item.ItemStack;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
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
                                        .map(
                                                c -> DisplayComponent.builder(c)
                                                        .setStackSize(itemStack.stackSize)
                                                        .build())
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

        private int outputStackSize() {
            return output().stackSize().get();
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
        // This was changed to long because we were getting overflows earlier.
        // But it might no longer be needed.
        // I tried loading up the pack (2.2.8) without this logic on 2022-12-25 and it didn't error.
        long expectedNumberOfRecipes = fluidInputs.size();
        try {
            expectedNumberOfRecipes *=
                    itemInputs.stream().mapToLong(Set::size).reduce(1, Math::multiplyExact);
            expectedNumberOfRecipes /=
                    recipes.stream()
                            .mapToLong(Recipe::itemInputsPermutationMultiplier)
                            .reduce(1, Math::multiplyExact);
        } catch (ArithmeticException e) {
            Logger.GREGTECH_5_CIRCUITS.error(
                    "Arithmetic exception when calculating number of recipes for circuit: [{}] [{}]",
                            output.toPrettyString(), e);
            // Assume a valid number of recipes when calculation fails
            expectedNumberOfRecipes = recipes.size();
        }

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
        // TODO for now, we assume that all recipes with the same number of ingredients and the same
        //  output stack size are permutations of the same recipe.
        //  If this stops being true, then we'll need to add handling for that.

        // Table of recipe input size, recipe output stack size to set of recipes.
        Table<Integer, Integer, Set<Recipe>> recipeTable = HashBasedTable.create();

        for (GT_Recipe rawRecipe : rawRecipes) {
            Recipe recipe = Recipe.create(rawRecipe);
            int row = recipe.itemInputsSize();
            int column = recipe.outputStackSize();

            Set<Recipe> recipeSet = recipeTable.get(row, column);
            if (recipeSet == null) {
                recipeSet = new HashSet<>();
                recipeTable.put(row, column, recipeSet);
            }

            recipeSet.add(recipe);
        }

        return recipeTable.rowMap().entrySet().stream()
                // We use reverse sort order here because circuit recipes with fewer ingredients
                // tend to be more advanced, so we want to show those later.
                .sorted(Map.Entry.<Integer, Map<Integer, Set<Recipe>>>comparingByKey().reversed())
                .flatMap(
                        entry -> entry.getValue().entrySet().stream()
                                // For recipes with the same number of input ingredients, we will
                                // sort them by output stack size, ascending.
                                .sorted(Map.Entry.comparingByKey())
                                .map(innerEntry -> create(entry.getKey(), innerEntry.getValue())))
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