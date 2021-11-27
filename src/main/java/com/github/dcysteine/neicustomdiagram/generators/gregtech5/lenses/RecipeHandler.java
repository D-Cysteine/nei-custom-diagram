package com.github.dcysteine.neicustomdiagram.generators.gregtech5.lenses;

import com.github.dcysteine.neicustomdiagram.api.diagram.component.Component;
import com.github.dcysteine.neicustomdiagram.api.diagram.component.DisplayComponent;
import com.github.dcysteine.neicustomdiagram.api.diagram.component.ItemComponent;
import com.github.dcysteine.neicustomdiagram.main.Logger;
import com.github.dcysteine.neicustomdiagram.util.OreDictUtil;
import com.github.dcysteine.neicustomdiagram.util.gregtech5.GregTechOreDictUtil;
import com.github.dcysteine.neicustomdiagram.util.gregtech5.GregTechRecipeUtil;
import com.google.auto.value.AutoValue;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.ImmutableSortedSet;
import com.google.common.collect.Iterables;
import com.google.common.collect.MultimapBuilder;
import com.google.common.collect.Sets;
import com.google.common.collect.SortedSetMultimap;
import gregtech.api.enums.OrePrefixes;
import gregtech.api.objects.ItemData;
import gregtech.api.util.GT_Recipe;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

class RecipeHandler {
    static final String LENS_COLOUR_ORE_NAME_PREFIX = "craftingLens";

    @AutoValue
    abstract static class Lens implements Comparable<Lens> {
        private static final Comparator<Lens> COMPARATOR =
                Comparator.comparing(Lens::colour).thenComparing(Lens::itemComponent);

        static Lens create(LensColour colour, ItemComponent itemComponent) {
            return new AutoValue_RecipeHandler_Lens(colour, itemComponent);
        }

        abstract LensColour colour();
        abstract ItemComponent itemComponent();

        @Override
        public int compareTo(Lens other) {
            return COMPARATOR.compare(this, other);
        }
    }

    @AutoValue
    abstract static class Recipe implements Comparable<Recipe> {
        private static final Comparator<Recipe> COMPARATOR = Comparator.comparing(Recipe::input);

        static Recipe create(ItemComponent input, DisplayComponent output) {
            return new AutoValue_RecipeHandler_Recipe(input, output);
        }

        abstract ItemComponent input();
        abstract DisplayComponent output();

        @Override
        public int compareTo(Recipe other) {
            return COMPARATOR.compare(this, other);
        }
    }

    /** Multimap of lens to precision laser engraver recipes for that lens. */
    private final SortedSetMultimap<Lens, Recipe> lensRecipes;

    /** Multimap of lens colour to lenses of that colour. */
    private final SortedSetMultimap<LensColour, Lens> lensColours;

    /** Multimap of lens colour to recipes shared by all lenses of that colour. */
    private final SortedSetMultimap<LensColour, Recipe> colourRecipes;

    RecipeHandler() {
        lensRecipes = MultimapBuilder.treeKeys().treeSetValues().build();
        lensColours = MultimapBuilder.enumKeys(LensColour.class).treeSetValues().build();
        colourRecipes = MultimapBuilder.enumKeys(LensColour.class).treeSetValues().build();
    }

    /** This method must be called before any other methods are called. */
    void initialize() {
        GT_Recipe.GT_Recipe_Map.sLaserEngraverRecipes.mRecipeList.forEach(this::handleRecipe);

        // Check that lenses of the same colour all have the same recipes.
        for (LensColour colour : lensColours.keySet()) {
            Set<Recipe> currentRecipes = null;
            for (Lens lens : lensColours.get(colour)) {
                Set<Recipe> currentLensRecipes = lensRecipes.get(lens);
                if (currentRecipes == null) {
                    currentRecipes = currentLensRecipes;
                }

                currentRecipes = Sets.intersection(currentRecipes, currentLensRecipes);
            }

            if (currentRecipes != null) {
                colourRecipes.putAll(colour, currentRecipes);
            }
        }
    }

    void handleRecipe(GT_Recipe recipe) {
        // We need to be able to mark lens-specific recipes with '*', so we can't show any recipe
        // input formatting. So use plain ItemComponent here.
        // TODO if we ever do need the recipe input formatting, we'll need to change something here.
        List<ItemComponent> inputs =
                GregTechRecipeUtil.buildComponentsFromItemInputs(recipe).stream()
                        .map(DisplayComponent::component)
                        .map(ItemComponent.class::cast)
                        .collect(Collectors.toList());
        List<DisplayComponent> outputs = GregTechRecipeUtil.buildComponentsFromItemOutputs(recipe);

        if (inputs.size() != 2 || outputs.size() != 1) {
            Logger.GREGTECH_5_LENSES.warn("Found a malformed recipe: [{}] [{}]", inputs, outputs);
            return;
        }


        ItemComponent lensItemComponent;
        ItemComponent input;
        if (isLens(inputs.get(1))) {
            lensItemComponent = inputs.get(1);
            input = inputs.get(0);
        } else if (isLens(inputs.get(0))) {
            lensItemComponent = inputs.get(0);
            input = inputs.get(1);
        } else {
            // Not a lens recipe.
            return;
        }
        DisplayComponent output = outputs.get(0);

        List<String> lensColourOreNames =
                OreDictUtil.getOreNames(lensItemComponent).stream()
                        .filter(oreName -> oreName.startsWith(LENS_COLOUR_ORE_NAME_PREFIX))
                        .collect(Collectors.toList());
        if (lensColourOreNames.size() > 1) {
            Logger.GREGTECH_5_LENSES.warn(
                    "Found a multi-coloured lens: [{}] [{}]",
                    lensItemComponent, lensColourOreNames);
            return;
        }

        LensColour colour =
                lensColourOreNames.isEmpty()
                        ? LensColour.UNIQUE
                        : LensColour.get(Iterables.getOnlyElement(lensColourOreNames));
        Lens lens = Lens.create(colour, lensItemComponent);

        lensRecipes.put(lens, Recipe.create(input, output));
        lensColours.put(colour, lens);
    }

    ImmutableSet<Lens> allLenses() {
        return ImmutableSet.copyOf(lensRecipes.keySet());
    }

    ImmutableSortedSet<Recipe> recipes(Lens lens) {
        return ImmutableSortedSet.copyOf(lensRecipes.get(lens));
    }

    ImmutableSortedSet<Lens> lenses(LensColour colour) {
        return ImmutableSortedSet.copyOf(lensColours.get(colour));
    }

    boolean isColourRecipe(LensColour colour, Recipe recipe) {
        return colourRecipes.get(colour).contains(recipe);
    }

    private static boolean isLens(Component component) {
        Optional<ItemData> itemDataOptional = GregTechOreDictUtil.getItemData(component);
        if (!itemDataOptional.isPresent()) {
            return false;
        }

        return itemDataOptional.get().mPrefix == OrePrefixes.lens;
    }
}