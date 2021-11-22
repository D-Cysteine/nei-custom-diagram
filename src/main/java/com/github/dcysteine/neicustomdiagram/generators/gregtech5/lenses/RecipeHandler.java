package com.github.dcysteine.neicustomdiagram.generators.gregtech5.lenses;

import com.github.dcysteine.neicustomdiagram.api.diagram.component.ItemComponent;
import com.github.dcysteine.neicustomdiagram.mod.Logger;
import com.google.auto.value.AutoValue;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.ImmutableSortedSet;
import com.google.common.collect.Iterables;
import com.google.common.collect.MultimapBuilder;
import com.google.common.collect.Sets;
import com.google.common.collect.SortedSetMultimap;
import gregtech.api.enums.OrePrefixes;
import gregtech.api.objects.ItemData;
import gregtech.api.util.GT_OreDictUnificator;
import gregtech.api.util.GT_Recipe;
import net.minecraft.item.ItemStack;
import net.minecraftforge.oredict.OreDictionary;

import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

class RecipeHandler {
    static final String LENS_COLOR_ORE_NAME_PREFIX = "craftingLens";

    @AutoValue
    abstract static class Lens implements Comparable<Lens> {
        private static final Comparator<Lens> COMPARATOR =
                Comparator.comparing(Lens::color).thenComparing(Lens::itemComponent);

        static Lens create(LensColor color, ItemComponent itemComponent) {
            return new AutoValue_RecipeHandler_Lens(color, itemComponent);
        }

        abstract LensColor color();
        abstract ItemComponent itemComponent();

        @Override
        public int compareTo(Lens other) {
            return COMPARATOR.compare(this, other);
        }
    }

    @AutoValue
    abstract static class Recipe implements Comparable<Recipe> {
        private static final Comparator<Recipe> COMPARATOR = Comparator.comparing(Recipe::input);

        static Recipe create(ItemStack input, ItemStack output) {
            return new AutoValue_RecipeHandler_Recipe(
                    ItemComponent.create(input), ItemComponent.create(output));
        }

        abstract ItemComponent input();
        abstract ItemComponent output();

        @Override
        public int compareTo(Recipe other) {
            return COMPARATOR.compare(this, other);
        }
    }

    /** Multimap of lens to precision laser engraver recipes for that lens. */
    private final SortedSetMultimap<Lens, Recipe> lensRecipes;

    /** Multimap of lens color to lenses of that color. */
    private final SortedSetMultimap<LensColor, Lens> lensColors;

    /** Multimap of lens color to recipes shared by all lenses of that color. */
    private final SortedSetMultimap<LensColor, Recipe> colorRecipes;

    RecipeHandler() {
        lensRecipes = MultimapBuilder.treeKeys().treeSetValues().build();
        lensColors = MultimapBuilder.enumKeys(LensColor.class).treeSetValues().build();
        colorRecipes = MultimapBuilder.enumKeys(LensColor.class).treeSetValues().build();
    }

    /** This method must be called before any other methods are called. */
    void initialize() {
        GT_Recipe.GT_Recipe_Map.sLaserEngraverRecipes.mRecipeList.forEach(this::handleRecipe);

        // Check that lenses of the same color all have the same recipes.
        for (LensColor color : lensColors.keySet()) {
            Set<Recipe> currentRecipes = null;
            for (Lens lens : lensColors.get(color)) {
                Set<Recipe> currentLensRecipes = lensRecipes.get(lens);
                if (currentRecipes == null) {
                    currentRecipes = currentLensRecipes;
                }

                currentRecipes = Sets.intersection(currentRecipes, currentLensRecipes);
            }

            if (currentRecipes != null) {
                colorRecipes.putAll(color, currentRecipes);
            }
        }
    }

    void handleRecipe(GT_Recipe recipe) {
        if (recipe.mInputs.length < 2 || recipe.mOutputs.length < 1
                || recipe.mInputs[0] == null || recipe.mInputs[1] == null
                || recipe.mOutputs[0] == null) {
            // TODO GT_Recipe does not implement toString(), so this is going to give some not very
            //  useful output. Well, let's worry about it if we ever actually run into this error.
            Logger.GREGTECH_5_LENSES.warn("Found a malformed recipe: [{}]", recipe);
            return;
        }


        ItemStack lensItemStack;
        ItemStack input;
        if (isLens(recipe.mInputs[1])) {
            lensItemStack = recipe.mInputs[1];
            input = recipe.mInputs[0];
        } else if (isLens(recipe.mInputs[0])) {
            lensItemStack = recipe.mInputs[0];
            input = recipe.mInputs[1];
        } else {
            // Not a lens recipe.
            return;
        }
        ItemStack output = recipe.mOutputs[0];

        List<String> lensColorOreNames =
                Arrays.stream(OreDictionary.getOreIDs(lensItemStack))
                        .mapToObj(OreDictionary::getOreName)
                        .filter(oreName -> oreName.startsWith(LENS_COLOR_ORE_NAME_PREFIX))
                        .collect(Collectors.toList());
        if (lensColorOreNames.size() > 1) {
            Logger.GREGTECH_5_LENSES.warn(
                    "Found a multi-colored lens: [{}] [{}]", lensItemStack, lensColorOreNames);
            return;
        }

        LensColor color =
                lensColorOreNames.isEmpty()
                        ? LensColor.UNIQUE
                        : LensColor.get(Iterables.getOnlyElement(lensColorOreNames));
        Lens lens = Lens.create(color, ItemComponent.create(lensItemStack));
        lensRecipes.put(lens, Recipe.create(input, output));
        lensColors.put(color, lens);
    }

    ImmutableSet<Lens> allLenses() {
        return ImmutableSet.copyOf(lensRecipes.keySet());
    }

    ImmutableSortedSet<Recipe> recipes(Lens lens) {
        return ImmutableSortedSet.copyOf(lensRecipes.get(lens));
    }

    ImmutableSortedSet<Lens> lenses(LensColor color) {
        return ImmutableSortedSet.copyOf(lensColors.get(color));
    }

    boolean isColorRecipe(LensColor color, Recipe recipe) {
        return colorRecipes.get(color).contains(recipe);
    }

    private static boolean isLens(ItemStack itemStack) {
        ItemData itemData = GT_OreDictUnificator.getAssociation(itemStack);
        if (itemData == null) {
            return false;
        }

        return itemData.mPrefix == OrePrefixes.lens;
    }
}