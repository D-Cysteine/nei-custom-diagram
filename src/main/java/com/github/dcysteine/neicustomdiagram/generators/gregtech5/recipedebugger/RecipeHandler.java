package com.github.dcysteine.neicustomdiagram.generators.gregtech5.recipedebugger;

import codechicken.nei.NEIServerUtils;
import com.github.dcysteine.neicustomdiagram.api.diagram.component.Component;
import com.github.dcysteine.neicustomdiagram.api.diagram.component.DisplayComponent;
import com.github.dcysteine.neicustomdiagram.api.diagram.component.FluidComponent;
import com.github.dcysteine.neicustomdiagram.api.diagram.component.ItemComponent;
import com.github.dcysteine.neicustomdiagram.api.diagram.tooltip.Tooltip;
import com.github.dcysteine.neicustomdiagram.main.Lang;
import com.github.dcysteine.neicustomdiagram.main.Logger;
import com.github.dcysteine.neicustomdiagram.util.gregtech5.GregTechOreDictUtil;
import com.github.dcysteine.neicustomdiagram.util.gregtech5.GregTechRecipeUtil;
import com.google.auto.value.AutoValue;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Sets;
import gregtech.api.enums.ItemList;
import gregtech.api.enums.OrePrefixes;
import gregtech.api.util.GT_ModHandler;
import gregtech.api.util.GT_OreDictUnificator;
import gregtech.api.util.GT_Recipe;
import gregtech.api.util.GT_Utility;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.CraftingManager;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.item.crafting.ShapedRecipes;
import net.minecraft.item.crafting.ShapelessRecipes;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.oredict.ShapedOreRecipe;
import net.minecraftforge.oredict.ShapelessOreRecipe;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

class RecipeHandler {
    static final Item PROGRAMMED_CIRCUIT = ItemList.Circuit_Integrated.getItem();
    static final ImmutableSet<ItemComponent> PROGRAMMED_CIRCUITS =
            ImmutableSet.copyOf(
                    IntStream.range(0, 25)
                            .mapToObj(i -> ItemComponent.create(GT_Utility.getIntegratedCircuit(i)))
                            .collect(Collectors.toList()));
    static final ImmutableSet<ItemComponent> SCHEMATICS =
            ImmutableSet.<ItemComponent>builder()
                    .add(GregTechOreDictUtil.getComponent(ItemList.Schematic_1by1))
                    .add(GregTechOreDictUtil.getComponent(ItemList.Schematic_2by2))
                    .add(GregTechOreDictUtil.getComponent(ItemList.Schematic_3by3))
                    .add(GregTechOreDictUtil.getComponent(ItemList.Schematic_Dust))
                    .build();

    static final ImmutableSet<OrePrefixes> SMALL_VARIANT_ORE_PREFIXES =
            ImmutableSet.of(OrePrefixes.dustTiny, OrePrefixes.dustSmall, OrePrefixes.nugget);
    static final ImmutableSet<OrePrefixes> CABLE_ORE_PREFIXES = ImmutableSet.of(
            OrePrefixes.cableGt01,
            OrePrefixes.cableGt02,
            OrePrefixes.cableGt04,
            OrePrefixes.cableGt08,
            OrePrefixes.cableGt12,
            OrePrefixes.cableGt16);
    static final ImmutableSet<RecipeMap> RECIPE_MAPS_TO_IGNORE_FOR_SMALL_VARIANT = ImmutableSet.of(
            // These recipemaps are meant to have tiny / small dusts or nuggets.
            RecipeMap.PACKAGER,
            RecipeMap.UNPACKAGER,
            RecipeMap.MACERATOR,
            RecipeMap.LATHE,
            RecipeMap.FLUID_EXTRACTOR,
            RecipeMap.IMPLOSION_COMPRESSOR,
            RecipeMap.ALLOY_SMELTER);

    enum RecipeMap {
        /**
         * Crafting table recipes.
         *
         * <p>This is the only enum entry which will have null {@code recipeMap}, since it doesn't
         * correspond to a GT5 recipe map. Currently, we only use this value to be able to check for
         * crafting table recipes containing a bad {@link ItemStack} which has a null {@link Item}.
         */
        CRAFTING_TABLE(null, ItemList.Schematic_Crafting, "craftingtablelabel"),

        ORE_WASHING_PLANT(
                GT_Recipe.GT_Recipe_Map.sOreWasherRecipes,
                ItemList.Machine_HV_OreWasher, "orewashingplantlabel"),
        THERMAL_CENTRIFUGE(
                GT_Recipe.GT_Recipe_Map.sThermalCentrifugeRecipes,
                ItemList.Machine_HV_ThermalCentrifuge, "thermalcentrifugelabel"),
        COMPRESSOR(
                GT_Recipe.GT_Recipe_Map.sCompressorRecipes,
                ItemList.Machine_HV_Compressor, "compressorlabel"),
        EXTRACTOR(
                GT_Recipe.GT_Recipe_Map.sExtractorRecipes,
                ItemList.Machine_HV_Extractor, "extractorlabel"),
        // Recycler, Furnace, Microwave, Scanner, Rock Breaker, By-product, Replicator

        /*
         * Assembly line recipes don't conflict usually since they are position-dependent.
         * So it's disabled.
        ASSEMBLY_LINE(
                GT_Recipe.GT_Recipe_Map.sAssemblylineVisualRecipes,
                ItemList.Machine_Multi_Assemblyline, "assemblylinelabel"),
         */

        // Plasma Arc Furnace, Arc Furnace
        PRINTER(
                GT_Recipe.GT_Recipe_Map.sPrinterRecipes,
                ItemList.Machine_HV_Printer, "printerlabel"),
        SIFTER(GT_Recipe.GT_Recipe_Map.sSifterRecipes, ItemList.Machine_HV_Sifter, "sifterlabel"),
        FORMING_PRESS(
                GT_Recipe.GT_Recipe_Map.sPressRecipes,
                ItemList.Machine_HV_Press, "formingpresslabel"),
        PRECISION_LASER_ENGRAVER(
                GT_Recipe.GT_Recipe_Map.sLaserEngraverRecipes,
                ItemList.Machine_HV_LaserEngraver, "precisionlaserengraverlabel"),
        MIXER(GT_Recipe.GT_Recipe_Map.sMixerRecipes, ItemList.Machine_HV_Mixer, "mixerlabel"),
        AUTOCLAVE(
                GT_Recipe.GT_Recipe_Map.sAutoclaveRecipes,
                ItemList.Machine_HV_Autoclave, "autoclavelabel"),
        ELECTROMAGNETIC_SEPARATOR(
                GT_Recipe.GT_Recipe_Map.sElectroMagneticSeparatorRecipes,
                ItemList.Machine_HV_ElectromagneticSeparator, "electromagneticseparatorlabel"),
        POLARIZER(
                GT_Recipe.GT_Recipe_Map.sPolarizerRecipes,
                ItemList.Machine_HV_Polarizer, "polarizerlabel"),
        MACERATOR(
                GT_Recipe.GT_Recipe_Map.sMaceratorRecipes,
                ItemList.Machine_HV_Macerator, "maceratorlabel"),
        CHEMICAL_BATH(
                GT_Recipe.GT_Recipe_Map.sChemicalBathRecipes,
                ItemList.Machine_HV_ChemicalBath, "chemicalbathlabel"),
        FLUID_CANNER(
                GT_Recipe.GT_Recipe_Map.sFluidCannerRecipes,
                ItemList.Machine_HV_FluidCanner, "fluidcannerlabel"),
        BREWERY(
                GT_Recipe.GT_Recipe_Map.sBrewingRecipes,
                ItemList.Machine_HV_Brewery, "brewerylabel"),
        FLUID_HEATER(
                GT_Recipe.GT_Recipe_Map.sFluidHeaterRecipes,
                ItemList.Machine_HV_FluidHeater, "fluidheaterlabel"),
        DISTILLERY(
                GT_Recipe.GT_Recipe_Map.sDistilleryRecipes,
                ItemList.Machine_HV_Distillery, "distillerylabel"),
        FERMENTER(
                GT_Recipe.GT_Recipe_Map.sFermentingRecipes,
                ItemList.Machine_HV_Fermenter, "fermenterlabel"),
        // Fluid Solidifier
        // Be warned: this thing has way too many recipes (~46k), and they all have similar
        // components! Expect extreme slow-down if you want to add it.
        FLUID_EXTRACTOR(
                GT_Recipe.GT_Recipe_Map.sFluidExtractionRecipes,
                ItemList.Machine_HV_FluidExtractor, "fluidextractorlabel"),
        PACKAGER(
                GT_Recipe.GT_Recipe_Map.sBoxinatorRecipes,
                ItemList.Machine_HV_Boxinator, "packagerlabel"),
        UNPACKAGER(
                GT_Recipe.GT_Recipe_Map.sUnboxinatorRecipes,
                ItemList.Machine_HV_Unboxinator, "unpackagerlabel"),
        FUSION_REACTOR(
                GT_Recipe.GT_Recipe_Map.sFusionRecipes,
                ItemList.FusionComputer_LuV, "fusionreactorlabel"),
        COMPLEX_FUSION_REACTOR(
                GT_Recipe.GT_Recipe_Map.sComplexFusionRecipes,
                ItemList.FusionComputer_UV, "complexfusionreactorlabel"),
        CENTRIFUGE(
                GT_Recipe.GT_Recipe_Map.sCentrifugeRecipes,
                ItemList.Machine_HV_Centrifuge, "centrifugelabel"),
        ELECTROLYZER(
                GT_Recipe.GT_Recipe_Map.sElectrolyzerRecipes,
                ItemList.Machine_HV_Electrolyzer, "electrolyzerlabel"),
        ELECTRIC_BLAST_FURNACE(
                GT_Recipe.GT_Recipe_Map.sBlastRecipes,
                ItemList.Machine_Multi_BlastFurnace, "electricblastfurnacelabel"),
        PLASMA_FORGE(
                GT_Recipe.GT_Recipe_Map.sPlasmaForgeRecipes,
                ItemList.Machine_Multi_PlasmaForge, "plasmaforgelabel"),
        // Commenting this one out for now, for backward compatibility. Uncomment later.
        /*
        TRANSCENDENT_PLASMA_MIXER(
                GT_Recipe.GT_Recipe_Map.sTranscendentPlasmaMixerRecipes,
                ItemList.Machine_Multi_TranscendentPlasmaMixer, "transcendentplasmamixerlabel"),
         */
        // Fake Space Project
        BRICKED_BLAST_FURNACE(
                GT_Recipe.GT_Recipe_Map.sPrimitiveBlastRecipes,
                ItemList.Machine_Bricked_BlastFurnace, "brickedblastfurnacelabel"),
        IMPLOSION_COMPRESSOR(
                GT_Recipe.GT_Recipe_Map.sImplosionRecipes,
                ItemList.Machine_Multi_ImplosionCompressor, "implosioncompressorlabel"),
        VACUUM_FREEZER(
                GT_Recipe.GT_Recipe_Map.sVacuumRecipes,
                ItemList.Machine_Multi_VacuumFreezer, "vacuumfreezerlabel"),
        CHEMICAL_REACTOR(
                GT_Recipe.GT_Recipe_Map.sChemicalRecipes,
                ItemList.Machine_HV_ChemicalReactor, "chemicalreactorlabel"),
        LARGE_CHEMICAL_REACTOR(
                GT_Recipe.GT_Recipe_Map.sMultiblockChemicalRecipes,
                ItemList.Machine_Multi_LargeChemicalReactor, "largechemicalreactorlabel"),
        DISTILLATION_TOWER(
                GT_Recipe.GT_Recipe_Map.sDistillationRecipes,
                ItemList.Distillation_Tower, "distillationtowerlabel"),
        OIL_CRACKER(
                GT_Recipe.GT_Recipe_Map.sCrackingRecipes,
                ItemList.OilCracker, "oilcrackerlabel"),
        PYROLYSE_OVEN(
                GT_Recipe.GT_Recipe_Map.sPyrolyseRecipes,
                ItemList.PyrolyseOven, "pyrolyseovenlabel"),
        WIREMILL(
                GT_Recipe.GT_Recipe_Map.sWiremillRecipes,
                ItemList.Machine_HV_Wiremill, "wiremilllabel"),
        BENDING_MACHINE(
                GT_Recipe.GT_Recipe_Map.sBenderRecipes,
                ItemList.Machine_HV_Bender, "bendingmachinelabel"),
        ALLOY_SMELTER(
                GT_Recipe.GT_Recipe_Map.sAlloySmelterRecipes,
                ItemList.Machine_HV_AlloySmelter, "alloysmelterlabel"),
        ASSEMBLING_MACHINE(
                GT_Recipe.GT_Recipe_Map.sAssemblerRecipes,
                ItemList.Machine_HV_Assembler, "assemblingmachinelabel"),
        CIRCUIT_ASSEMBLING_MACHINE(
                GT_Recipe.GT_Recipe_Map.sCircuitAssemblerRecipes,
                ItemList.Machine_HV_CircuitAssembler, "circuitassemblingmachinelabel"),
        CANNING_MACHINE(
                GT_Recipe.GT_Recipe_Map.sCannerRecipes,
                ItemList.Machine_HV_Canner, "canningmachinelabel"),
        // CNC Machine (what even is this?)
        LATHE(GT_Recipe.GT_Recipe_Map.sLatheRecipes, ItemList.Machine_HV_Lathe, "lathelabel"),
        CUTTING_MACHINE(
                GT_Recipe.GT_Recipe_Map.sCutterRecipes,
                ItemList.Machine_HV_Cutter, "cuttingmachinelabel"),
        SLICING_MACHINE(
                GT_Recipe.GT_Recipe_Map.sSlicerRecipes,
                ItemList.Machine_HV_Slicer, "slicingmachinelabel"),
        EXTRUDER(
                GT_Recipe.GT_Recipe_Map.sExtruderRecipes,
                ItemList.Machine_HV_Extruder, "extruderlabel"),
        FORGE_HAMMER(
                GT_Recipe.GT_Recipe_Map.sHammerRecipes,
                ItemList.Machine_HV_Hammer, "forgehammerlabel"),
        // Amplifabricator, Mass Fabrication, fuels
        // Multiblock Electrolyzer, Multiblock Centrifuge, Multiblock Mixer
        NANO_FORGE(
                GT_Recipe.GT_Recipe_Map.sNanoForge,
                ItemList.NanoForge, "nanoforgelabel"),
        PCB_FACTORY(
                GT_Recipe.GT_Recipe_Map.sPCBFactory,
                ItemList.PCBFactory, "pcbfactory"),
        ;

        /** This field will be null for the {@link RecipeMap#CRAFTING_TABLE} enum only. */
        @Nullable
        final GT_Recipe.GT_Recipe_Map recipeMap;
        final ItemList item;
        final String tooltipKey;

        RecipeMap(@Nullable GT_Recipe.GT_Recipe_Map recipeMap, ItemList item, String tooltipKey) {
            this.recipeMap = recipeMap;
            this.item = item;
            this.tooltipKey = tooltipKey;
        }
    }

    @AutoValue
    abstract static class Recipe {
        static Recipe create(RecipeMap recipeMap, GT_Recipe recipe) {
            Map<Component, Integer> inputs = new HashMap<>();
            for (ItemStack itemStack : recipe.mInputs) {
                if (itemStack == null) {
                    continue;
                }
                ItemStack unified = GT_OreDictUnificator.get_nocopy(itemStack);
                inputs.merge(
                        ItemComponent.createWithNbt(unified), itemStack.stackSize, Integer::sum);
            }
            for (FluidStack fluidStack : recipe.mFluidInputs) {
                if (fluidStack == null) {
                    continue;
                }
                inputs.merge(
                        FluidComponent.createWithNbt(fluidStack), fluidStack.amount, Integer::sum);
            }

            Map<Component, Integer> outputs = new HashMap<>();
            for (ItemStack itemStack : recipe.mOutputs) {
                if (itemStack == null) {
                    continue;
                }
                ItemStack unified = GT_OreDictUnificator.get_nocopy(itemStack);
                outputs.merge(
                        ItemComponent.createWithNbt(unified), itemStack.stackSize, Integer::sum);
            }
            for (FluidStack fluidStack : recipe.mFluidOutputs) {
                if (fluidStack == null) {
                    continue;
                }
                outputs.merge(
                        FluidComponent.createWithNbt(fluidStack), fluidStack.amount, Integer::sum);
            }

            return new AutoValue_RecipeHandler_Recipe(
                    recipeMap, ImmutableMap.copyOf(inputs), ImmutableMap.copyOf(outputs),
                    ImmutableList.copyOf(GregTechRecipeUtil.buildComponentsFromInputs(recipe)),
                    ImmutableList.copyOf(GregTechRecipeUtil.buildComponentsFromOutputs(recipe)));
        }

        /**
         * Checks if {@code recipe} contains any bad {@link ItemStack} instances.
         * If so, returns an optional of a {@link Recipe}; otherwise, returns an empty optional.
         */
        @SuppressWarnings("unchecked")
        static Optional<Recipe> createIfBadItemStack(IRecipe recipe) {
            boolean badRecipe = false;

            List<DisplayComponent> inputList = new ArrayList<>();
            List<DisplayComponent> outputList = new ArrayList<>();

            Map<Component, Integer> inputs = new HashMap<>();
            Map<Component, Integer> outputs = new HashMap<>();

            ItemStack output = recipe.getRecipeOutput();
            if (output == null) {
                Logger.GREGTECH_5_RECIPE_DEBUGGER.warn(
                        "Found a crafting table recipe with null output:\n{}", recipe);
            } else if (output.getItem() == null) {
                badRecipe = true;
                Logger.GREGTECH_5_RECIPE_DEBUGGER.warn(
                        "Found a crafting table recipe with bad output:\n{}", recipe);
            } else {
                outputList.add(DisplayComponent.builderWithNbt(output).build());
                outputs.put(ItemComponent.createWithNbt(output), output.stackSize);
            }

            // We'll try to get the inputs,
            // but can only do so for recipe types that we know how to handle.
            List<ItemStack> inputItemStacks = new ArrayList<>();
            if (recipe instanceof ShapedRecipes) {
                inputItemStacks = Arrays.asList(((ShapedRecipes) recipe).recipeItems);
            } else if (recipe instanceof ShapedOreRecipe) {
                inputItemStacks =
                        Arrays.stream(((ShapedOreRecipe) recipe).getInput())
                                .filter(Objects::nonNull)
                                .map(NEIServerUtils::extractRecipeItems)
                                .flatMap(Arrays::stream)
                                .collect(Collectors.toCollection(ArrayList::new));
            } else if (recipe instanceof ShapelessRecipes) {
                inputItemStacks = (List<ItemStack>) ((ShapelessRecipes) recipe).recipeItems;
            } else if (recipe instanceof ShapelessOreRecipe) {
                inputItemStacks =
                        ((ShapelessOreRecipe) recipe).getInput().stream()
                                .filter(Objects::nonNull)
                                .map(NEIServerUtils::extractRecipeItems)
                                .flatMap(Arrays::stream)
                                .collect(Collectors.toCollection(ArrayList::new));
            }

            for (ItemStack input : inputItemStacks) {
                if (input == null) {
                    continue;
                } else if (input.getItem() == null) {
                    badRecipe = true;
                    Logger.GREGTECH_5_RECIPE_DEBUGGER.warn(
                            "Found a crafting table recipe with bad input:\n{}", recipe);
                    continue;
                }

                // The diagram layout code currently doesn't support permutations,
                // so for now, we'll handle wildcard damage by just reverting to damage 0.
                boolean isWildcard = false;

                ItemComponent itemComponent = ItemComponent.createWithNbt(input);
                if (itemComponent.hasWildcardDamage()) {
                    isWildcard = true;

                    ItemStack nonWildcardInput = input.copy();
                    nonWildcardInput.setItemDamage(0);
                    itemComponent = ItemComponent.createWithNbt(nonWildcardInput);
                }
                inputs.put(itemComponent, input.stackSize);

                DisplayComponent.Builder displayComponentBuilder =
                        DisplayComponent.builder(itemComponent);
                if (isWildcard) {
                    displayComponentBuilder
                            .setAdditionalInfo("*")
                            .setAdditionalTooltip(
                                    Tooltip.create(
                                            Lang.GREGTECH_5_RECIPE_DEBUGGER.trans("wildcardlabel"),
                                            Tooltip.INFO_FORMATTING));
                }
                inputList.add(displayComponentBuilder.build());
            }

            if (badRecipe) {
                return Optional.of(
                        new AutoValue_RecipeHandler_Recipe(
                                RecipeMap.CRAFTING_TABLE,
                                ImmutableMap.copyOf(inputs), ImmutableMap.copyOf(outputs),
                                ImmutableList.copyOf(inputList), ImmutableList.copyOf(outputList)));
            } else {
                return Optional.empty();
            }
        }

        abstract RecipeMap recipeMap();

        /** Map of component to stack size. */
        abstract ImmutableMap<Component, Integer> inputs();

        /** Map of component to stack size. */
        abstract ImmutableMap<Component, Integer> outputs();

        abstract ImmutableList<DisplayComponent> displayInputs();
        abstract ImmutableList<DisplayComponent> displayOutputs();
    }

    final Map<RecipeMap, RecipePartitioner> allRecipes;
    final List<Recipe> consumeCircuitRecipes;
    final List<Recipe> unnecessaryCircuitRecipes;
    final Set<Recipe> collidingRecipes;
    final List<Recipe> voidingRecipes;
    final List<Recipe> unequalCellRecipes;
    final List<Recipe> smallVariantRecipes;
    final List<Recipe> badCraftingTableRecipes;

    RecipeHandler() {
        this.allRecipes = new HashMap<>();
        this.consumeCircuitRecipes = new ArrayList<>();
        this.unnecessaryCircuitRecipes = new ArrayList<>();
        this.collidingRecipes = new LinkedHashSet<>();
        this.voidingRecipes = new ArrayList<>();
        this.unequalCellRecipes = new ArrayList<>();
        this.smallVariantRecipes = new ArrayList<>();
        this.badCraftingTableRecipes = new ArrayList<>();
    }

    /** This method must be called before any other methods are called. */
    @SuppressWarnings("unchecked")
    void initialize() {
        // First pass: build recipe data.
        for (RecipeMap recipeMap : RecipeMap.values()) {
            if (recipeMap == RecipeMap.CRAFTING_TABLE) {
                continue;
            }

            Logger.GREGTECH_5_RECIPE_DEBUGGER.info(
                    "Checking recipes, pass 1: {}", recipeMap.name());

            ImmutableList.Builder<Recipe> recipeListBuilder = ImmutableList.builder();
                    recipeMap.recipeMap.mRecipeList.stream()
                            .map(recipe -> Recipe.create(recipeMap, recipe))
                            .filter(recipe -> RecipeHandler.filterRecipes(recipeMap, recipe))
                            .forEach(recipeListBuilder::add);

            RecipePartitioner recipePartitioner = new RecipePartitioner(recipeListBuilder.build());
            recipePartitioner.initialize();
            allRecipes.put(recipeMap, recipePartitioner);
        }

        // Second pass: check recipes for overlap, etc.
        for (RecipeMap recipeMap : RecipeMap.values()) {
            if (recipeMap == RecipeMap.CRAFTING_TABLE) {
                continue;
            }

            Logger.GREGTECH_5_RECIPE_DEBUGGER.info(
                    "Checking recipes, pass 2: {} [{}]",
                    recipeMap.name(), allRecipes.get(recipeMap).size());

            RecipePartitioner recipePartitioner = allRecipes.get(recipeMap);
            for (Recipe recipe : recipePartitioner.allRecipes()) {
                Iterable<Recipe> matchingRecipes =
                        recipePartitioner.lookup(recipe.inputs().keySet());

                if (consumesCircuit(recipe)) {
                    consumeCircuitRecipes.add(recipe);
                }

                if (unnecessaryCircuit(recipe, matchingRecipes)) {
                    unnecessaryCircuitRecipes.add(recipe);
                }

                collidingRecipes.addAll(findCollidingRecipes(recipe, matchingRecipes));

                if (voidingRecipe(recipe)) {
                    voidingRecipes.add(recipe);
                }

                if (unequalCellRecipe(recipe)) {
                    unequalCellRecipes.add(recipe);
                }

                if (smallVariantRecipe(recipe)) {
                    smallVariantRecipes.add(recipe);
                }
            }
        }

        // Third pass: check for crafting table recipes with bad item stacks.
        Logger.GREGTECH_5_RECIPE_DEBUGGER.info("Checking crafting table recipes");
        ((List<IRecipe>) CraftingManager.getInstance().getRecipeList()).stream()
                .map(Recipe::createIfBadItemStack)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .forEach(badCraftingTableRecipes::add);
    }

    static Set<Component> filterCircuits(Set<Component> components) {
        return Sets.difference(components, PROGRAMMED_CIRCUITS);
    }

    /**
     * There are a few bad recipes, which cause trouble for the recipe checks and are not actually
     * valid. Filter them out here.
     *
     * <p>Return {@code false} to filter out a recipe.
     */
    private static boolean filterRecipes(RecipeMap recipeMap, Recipe recipe) {
        if (recipeMap == RecipeMap.CUTTING_MACHINE) {
            // There are invalid cutting machine recipes which contain only fluids, and no items.
            return recipe.inputs().keySet().stream()
                    .anyMatch(component -> component.type() == Component.ComponentType.ITEM);
        }

        return true;
    }

    /** Returns whether {@code a} is a subset of {@code b}, ignoring stack sizes. */
    private static boolean isSubset(Set<Component> a, Set<Component> b) {
        return b.containsAll(a);
    }

    /**
     * Returns whether {@code a} is a subset of {@code b}, checking stack sizes.
     */
    private static boolean isSubsetComparingStackSizes(
            Map<Component, Integer> a, Map<Component, Integer> b) {
        for (Map.Entry<Component, Integer> entry : a.entrySet()) {
            if (b.getOrDefault(entry.getKey(), 0) < entry.getValue()) {
                return false;
            }
        }

        return true;
    }

    private static boolean consumesCircuit(Recipe recipe) {
        for (Map.Entry<Component, Integer> entry : recipe.inputs().entrySet()) {
            Component component = entry.getKey();
            if (component.type() != Component.ComponentType.ITEM) {
                continue;
            }

            if ((PROGRAMMED_CIRCUITS.contains(component) || SCHEMATICS.contains(component))
                    && entry.getValue() > 0) {
                return true;
            }
        }

        return false;
    }

    private static boolean unnecessaryCircuit(Recipe recipe, Iterable<Recipe> recipes) {
        Set<Component> inputs = recipe.inputs().keySet();
        if (inputs.stream().noneMatch(PROGRAMMED_CIRCUITS::contains)) {
            return false;
        }

        Set<Component> filteredInputs = filterCircuits(inputs);
        for (Recipe otherRecipe : recipes) {
            if (recipe == otherRecipe) {
                continue;
            }

            if (isSubset(filteredInputs, otherRecipe.inputs().keySet())) {
                return false;
            }
        }

        return true;
    }

    // TODO this won't find cases where we have multiple identical recipes
    //  (maybe differing in recipe time or voltage or something). Do we care?
    private static Set<Recipe> findCollidingRecipes(Recipe recipe, Iterable<Recipe> recipes) {
        Set<Recipe> collidingRecipes = Sets.newLinkedHashSet();
        collidingRecipes.add(recipe);

        for (Recipe otherRecipe : recipes) {
            if (recipe == otherRecipe) {
                continue;
            }

            if (isSubset(recipe.inputs().keySet(), otherRecipe.inputs().keySet())) {
                collidingRecipes.add(otherRecipe);
            }
        }

        if (collidingRecipes.size() > 1) {
            return collidingRecipes;
        } else {
            return Sets.newHashSet();
        }
    }

    private static boolean voidingRecipe(Recipe recipe) {
        return isSubsetComparingStackSizes(recipe.outputs(), recipe.inputs());
    }

    private static int countCells(Map<Component, Integer> componentMap) {
        int cells = 0;
        for (Map.Entry<Component, Integer> entry : componentMap.entrySet()) {
            Component component = entry.getKey();
            if (component.type() != Component.ComponentType.ITEM) {
                continue;
            }

            ItemStack itemStack = ((ItemComponent) component).stack();
            try {
                if (GT_ModHandler.getCapsuleCellContainerCount(itemStack) > 0) {
                    cells += entry.getValue();
                }
            } catch (NullPointerException suppressed) {
                // EnderStorage throws NullPointerException when we try to get fluid contents.
                // Probably because the game has not yet started, so EnderStorageManager is
                // unavailable.
            }
        }

        return cells;
    }

    private static boolean unequalCellRecipe(Recipe recipe) {
        // Prevent spamming the unequal cell recipes view with macerator recipes.
        if (recipe.recipeMap() == RecipeMap.MACERATOR) {
            return false;
        }

        return countCells(recipe.inputs()) != countCells(recipe.outputs());
    }

    private static boolean smallVariantRecipe(Recipe recipe) {
        if (RECIPE_MAPS_TO_IGNORE_FOR_SMALL_VARIANT.contains(recipe.recipeMap())) {
            return false;
        }

        Set<OrePrefixes> orePrefixes = getOrePrefixes(recipe.outputs().keySet());
        if (recipe.recipeMap() == RecipeMap.ASSEMBLING_MACHINE
                && Sets.intersection(orePrefixes, CABLE_ORE_PREFIXES).size() > 0) {
            // Allow using small dusts for cable insulation.
            return false;
        } else {
            orePrefixes.addAll(getOrePrefixes(recipe.inputs().keySet()));
            return Sets.intersection(orePrefixes, SMALL_VARIANT_ORE_PREFIXES).size() > 0;
        }
    }

    private static Set<OrePrefixes> getOrePrefixes(Set<Component> componentSet) {
        return componentSet.stream()
                .map(GregTechOreDictUtil::getItemData)
                .filter(Optional::isPresent)
                .map(itemData -> itemData.get().mPrefix)
                .collect(Collectors.toCollection(HashSet::new));
    }
}