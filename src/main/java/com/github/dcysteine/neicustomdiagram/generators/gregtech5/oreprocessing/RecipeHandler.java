package com.github.dcysteine.neicustomdiagram.generators.gregtech5.oreprocessing;

import com.github.dcysteine.neicustomdiagram.api.diagram.component.DisplayComponent;
import com.github.dcysteine.neicustomdiagram.api.diagram.component.ItemComponent;
import com.github.dcysteine.neicustomdiagram.main.Logger;
import com.github.dcysteine.neicustomdiagram.util.gregtech5.GregTechOreDictUtil;
import com.github.dcysteine.neicustomdiagram.util.gregtech5.GregTechRecipeUtil;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Iterables;
import com.google.common.collect.MultimapBuilder;
import com.google.common.collect.Multimaps;
import com.google.common.collect.SetMultimap;
import gregtech.api.enums.Materials;
import gregtech.api.util.GT_OreDictUnificator;
import gregtech.api.util.GT_Recipe;
import gregtech.api.util.GT_Utility;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.FurnaceRecipes;

import java.util.Arrays;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

/** Class that caches GregTech recipe data and stores it in a map, for fast lookup later. */
class RecipeHandler {
    enum RecipeMap {
        MACERATOR(GT_Recipe.GT_Recipe_Map.sMaceratorRecipes),
        ORE_WASHING_PLANT(GT_Recipe.GT_Recipe_Map.sOreWasherRecipes),
        THERMAL_CENTRIFUGE(GT_Recipe.GT_Recipe_Map.sThermalCentrifugeRecipes),
        SIFTER(GT_Recipe.GT_Recipe_Map.sSifterRecipes),
        CENTRIFUGE(GT_Recipe.GT_Recipe_Map.sCentrifugeRecipes),

        BLAST_FURNACE(GT_Recipe.GT_Recipe_Map.sBlastRecipes),
        CHEMICAL_BATH(GT_Recipe.GT_Recipe_Map.sChemicalBathRecipes),
        CHEMICAL_REACTOR(GT_Recipe.GT_Recipe_Map.sChemicalRecipes),
        ELECTROMAGNETIC_SEPARATOR(GT_Recipe.GT_Recipe_Map.sElectroMagneticSeparatorRecipes),
        AUTOCLAVE(GT_Recipe.GT_Recipe_Map.sAutoclaveRecipes);

        final GT_Recipe.GT_Recipe_Map recipeMap;

        RecipeMap(GT_Recipe.GT_Recipe_Map recipeMap) {
            this.recipeMap = recipeMap;
        }
    }

    /** Enum containing fluids that we will look up crushed ore recipes for in the chemical bath. */
    enum ChemicalBathFluid {
        MERCURY(
                DisplayComponent.builder(GT_Utility.getFluidDisplayStack(Materials.Mercury.mFluid))
                        .setStackSize(1000)
                        .build()),

        SODIUM_PERSULFATE(
                DisplayComponent.builder(
                                GT_Utility.getFluidDisplayStack(Materials.SodiumPersulfate.mFluid))
                        .setStackSize(100)
                        .build());

        final DisplayComponent fluid;

        ChemicalBathFluid(DisplayComponent fluid) {
            this.fluid = fluid;
        }

        /**
         * Note that the keys are GregTech fluid display items, not fluids. This is for convenience,
         * because {@link GregTechRecipeUtil} returns GregTech fluid display items (when possible).
         */
        static final ImmutableMap<DisplayComponent, ChemicalBathFluid> VALUES_MAP =
                ImmutableMap.copyOf(
                        Arrays.stream(values())
                                .collect(Collectors.toMap(c -> c.fluid, Function.identity())));
    }

    /**
     * Map of recipe type to multimap. The multimaps are maps of input {@link ItemComponent}s to
     * lists of {@code DisplayComponent}s representing the outputs for each recipe for that input.
     *
     * <p>We usually don't look up recipe outputs by fluid, so fluid inputs will be skipped when
     * building this data. We have separate data structures for the few fluid lookups that we do.
     */
    private final EnumMap<RecipeMap,
            SetMultimap<ItemComponent, ImmutableList<DisplayComponent>>> recipeData;

    /**
     * Map of fluid to multimap. The multimaps are maps of input {@link ItemComponent}s to lists of
     * {@code DisplayComponent}s representing the outputs for each chemical bath recipe for that
     * fluid and input fluid and item.
     */
    private final EnumMap<ChemicalBathFluid,
            SetMultimap<ItemComponent, ImmutableList<DisplayComponent>>> chemicalBathFluidData;

    /** Map of smelting input to smelting output. */
    private final Map<ItemComponent, ItemComponent> furnaceData;

    RecipeHandler() {
        this.recipeData = new EnumMap<>(RecipeMap.class);
        this.chemicalBathFluidData = new EnumMap<>(ChemicalBathFluid.class);
        this.furnaceData = new HashMap<>();
    }

    /** This method must be called before any other methods are called. */
    @SuppressWarnings("unchecked")
    void initialize() {
        Arrays.stream(ChemicalBathFluid.values()).forEach(
                chemicalBathFluid -> chemicalBathFluidData.put(
                        chemicalBathFluid, MultimapBuilder.hashKeys().hashSetValues().build()));

        for (RecipeMap recipeMap : RecipeMap.values()) {
            SetMultimap<ItemComponent, ImmutableList<DisplayComponent>> multimap =
                    MultimapBuilder.hashKeys().hashSetValues().build();
            recipeData.put(recipeMap, multimap);

            for (GT_Recipe recipe : recipeMap.recipeMap.mRecipeList) {
                ImmutableList<DisplayComponent> outputs =
                        ImmutableList.copyOf(GregTechRecipeUtil.buildComponentsFromOutputs(recipe));

                Optional<ChemicalBathFluid> chemicalBathFluidOptional = Optional.empty();
                if (recipeMap == RecipeMap.CHEMICAL_BATH) {
                    List<DisplayComponent> fluidInputs =
                            GregTechRecipeUtil.buildComponents(recipe.mFluidInputs);

                    if (fluidInputs.size() != 1) {
                        Logger.GREGTECH_5_ORE_PROCESSING.warn(
                                "Found chemical bath recipe with {} fluids:\n[{}]\n ->\n[{}]",
                                fluidInputs.size(),
                                GregTechRecipeUtil.buildComponentsFromInputs(recipe),
                                GregTechRecipeUtil.buildComponentsFromOutputs(recipe));
                    } else {
                        chemicalBathFluidOptional =
                                Optional.ofNullable(
                                        ChemicalBathFluid.VALUES_MAP.get(
                                                Iterables.getOnlyElement(fluidInputs)));
                    }
                }

                for (ItemStack itemStack : recipe.mInputs) {
                    if (itemStack == null) {
                        continue;
                    }

                    ItemComponent itemComponent =
                            ItemComponent.create(GT_OreDictUnificator.get_nocopy(itemStack));
                    multimap.put(itemComponent, outputs);

                    chemicalBathFluidOptional.ifPresent(
                            chemicalBathFluid -> chemicalBathFluidData.get(chemicalBathFluid)
                                    .put(itemComponent, outputs));
                }
            }
        }

        ((Map<ItemStack, ItemStack>) FurnaceRecipes.smelting().getSmeltingList()).entrySet()
                .forEach(
                        entry -> furnaceData.put(
                                ItemComponent.create(entry.getKey()),
                                ItemComponent.create(entry.getValue())));
    }

    /** The returned set is immutable! */
    Set<ImmutableList<DisplayComponent>> getRecipeOutputs(
            RecipeMap recipeMap, ItemComponent input) {
        return Multimaps.unmodifiableSetMultimap(recipeData.get(recipeMap))
                .get((ItemComponent) GregTechOreDictUtil.unify(input));
    }

    /**
     * Returns the unique recipe output for recipes including {@code input}, or empty optional if
     * there were zero such recipes, or if there were multiple recipes with differing outputs.
     *
     * <p>Also will log each case where multiple differing recipe outputs were found.
     */
    Optional<ImmutableList<DisplayComponent>> getUniqueRecipeOutput(
            RecipeMap recipeMap, ItemComponent input) {
        Set<ImmutableList<DisplayComponent>> outputs = recipeData.get(recipeMap)
                .get((ItemComponent) GregTechOreDictUtil.unify(input));

        if (outputs.size() > 1) {
            Logger.GREGTECH_5_ORE_PROCESSING.warn(
                    "Found {} recipes: [{}] [{}]", outputs.size(), recipeMap, input);

            return Optional.empty();
        } else if (outputs.isEmpty()) {
            return Optional.empty();
        }

        return Optional.of(Iterables.getOnlyElement(outputs));
    }

    /**
     * Returns the unique recipe output for chemical bath recipes including
     * {@code chemicalBathFluid} and {@code input}, or empty optional if there were zero such
     * recipes, or if there were multiple recipes with differing outputs.
     *
     * <p>Also will log each case where multiple differing recipe outputs were found.
     */
    Optional<ImmutableList<DisplayComponent>> getUniqueChemicalBathOutput(
            ChemicalBathFluid chemicalBathFluid, ItemComponent input) {
        Set<ImmutableList<DisplayComponent>> outputs =
                chemicalBathFluidData.get(chemicalBathFluid).get(input);
        if (outputs.size() > 1) {
            Logger.GREGTECH_5_ORE_PROCESSING.warn(
                    "Found {} chemical bath recipes: [{}] [{}]",
                    outputs.size(), chemicalBathFluid, input);

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
