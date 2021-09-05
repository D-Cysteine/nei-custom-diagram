package com.github.dcysteine.neicustomdiagram.generators.gregtech.oreprocessing;

import com.github.dcysteine.neicustomdiagram.api.Logger;
import com.github.dcysteine.neicustomdiagram.api.diagram.component.Component;
import com.github.dcysteine.neicustomdiagram.api.diagram.component.DisplayComponent;
import com.github.dcysteine.neicustomdiagram.api.diagram.component.ItemComponent;
import com.github.dcysteine.neicustomdiagram.util.gregtech.GregTechRecipeUtil;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;
import com.google.common.collect.MultimapBuilder;
import com.google.common.collect.Multimaps;
import com.google.common.collect.SetMultimap;
import gregtech.api.util.GT_OreDictUnificator;
import gregtech.api.util.GT_Recipe;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.FurnaceRecipes;

import java.util.EnumMap;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

/** Class that caches GregTech recipe data and stores it in a map, for fast lookup later. */
class RecipeHandler {
    enum RecipeMap {
        MACERATOR(GT_Recipe.GT_Recipe_Map.sMaceratorRecipes),
        ORE_WASHER(GT_Recipe.GT_Recipe_Map.sOreWasherRecipes),
        THERMAL_CENTRIFUGE(GT_Recipe.GT_Recipe_Map.sThermalCentrifugeRecipes),
        SIFTER(GT_Recipe.GT_Recipe_Map.sSifterRecipes),
        CENTRIFUGE(GT_Recipe.GT_Recipe_Map.sCentrifugeRecipes),

        BLAST_FURNACE(GT_Recipe.GT_Recipe_Map.sBlastRecipes),
        CHEMICAL_BATH(GT_Recipe.GT_Recipe_Map.sChemicalBathRecipes),
        CHEMICAL_REACTOR(GT_Recipe.GT_Recipe_Map.sChemicalRecipes),
        ELECTROMAGNETIC_SEPARATOR(GT_Recipe.GT_Recipe_Map.sElectroMagneticSeparatorRecipes),
        AUTOCLAVE(GT_Recipe.GT_Recipe_Map.sAutoclaveRecipes);

        private final GT_Recipe.GT_Recipe_Map recipeMap;

        RecipeMap(GT_Recipe.GT_Recipe_Map recipeMap) {
            this.recipeMap = recipeMap;
        }
    }

    /**
     * Map of recipe type to multimap. The multimaps are maps of input {@link Component}s to
     * lists of {@code DisplayComponent}s representing the outputs for each recipe for that input.
     *
     * <p>We don't currently look up recipe outputs by fluid, so fluid inputs will be skipped when
     * building this data.
     */
    private final EnumMap<RecipeMap,
            SetMultimap<ItemComponent, ImmutableList<DisplayComponent>>> recipeData;

    /** Map of smelting input to smelting output. */
    private final Map<ItemComponent, ItemComponent> furnaceData;

    RecipeHandler() {
        this.recipeData = new EnumMap<>(RecipeMap.class);
        this.furnaceData = new HashMap<>();
    }

    /** This method must be called before any other methods are called. */
    @SuppressWarnings("unchecked")
    void initialize() {
        for (RecipeMap recipeMap : RecipeMap.values()) {
            SetMultimap<ItemComponent, ImmutableList<DisplayComponent>> multimap =
                    MultimapBuilder.hashKeys().hashSetValues().build();
            recipeData.put(recipeMap, multimap);

            for (GT_Recipe recipe : recipeMap.recipeMap.mRecipeList) {
                ImmutableList<DisplayComponent> outputs =
                        ImmutableList.copyOf(GregTechRecipeUtil.buildComponentsFromOutputs(recipe));

                for (ItemStack itemStack : recipe.mInputs) {
                    if (itemStack == null) {
                        continue;
                    }

                    ItemComponent itemComponent =
                            ItemComponent.create(GT_OreDictUnificator.get_nocopy(itemStack));
                    multimap.put(itemComponent, outputs);
                }
            }
        }

        ((Map<ItemStack, ItemStack>) FurnaceRecipes.smelting().getSmeltingList()).entrySet()
                .forEach(
                        entry ->
                                furnaceData.put(
                                        ItemComponent.create(entry.getKey()),
                                        ItemComponent.create(entry.getValue())));
    }

    /** The returned set is immutable! */
    Set<ImmutableList<DisplayComponent>> getRecipeOutputs(
            RecipeMap recipeMap, ItemComponent input) {
        return Multimaps.unmodifiableSetMultimap(recipeData.get(recipeMap)).get(input);
    }

    /**
     * Returns the unique recipe output for recipes including {@code input}, or empty optional if
     * there were zero such recipes, or if there were multiple recipes with differing outputs.
     *
     * <p>Also will log each case where multiple differing recipe outputs were found.
     */
    Optional<ImmutableList<DisplayComponent>> getUniqueRecipeOutput(
            RecipeMap recipeMap, ItemComponent input) {
        Set<ImmutableList<DisplayComponent>> outputs = getRecipeOutputs(recipeMap, input);
        if (outputs.size() > 1) {
            Logger.GREGTECH_ORE_PROCESSING.warn(
                    "Found {} recipes: [{}] [{}]", outputs.size(), recipeMap, input);

            return Optional.empty();
        } else if (outputs.isEmpty()) {
            return Optional.empty();
        }

        return Optional.of(Iterables.getOnlyElement(outputs));
    }

    Optional<ItemComponent> getFurnaceRecipeOutput(ItemComponent input) {
        return Optional.ofNullable(furnaceData.get(input));
    }
}
