package com.github.dcysteine.neicustomdiagram.impl.gregtech.oreprocessing;

import com.github.dcysteine.neicustomdiagram.api.Logger;
import com.github.dcysteine.neicustomdiagram.api.diagram.Diagram;
import com.github.dcysteine.neicustomdiagram.api.diagram.component.Component;
import com.github.dcysteine.neicustomdiagram.api.diagram.component.DisplayComponent;
import com.github.dcysteine.neicustomdiagram.api.diagram.component.FluidComponent;
import com.github.dcysteine.neicustomdiagram.api.diagram.component.ItemComponent;
import com.github.dcysteine.neicustomdiagram.api.diagram.interactable.CustomInteractable;
import com.github.dcysteine.neicustomdiagram.api.diagram.interactable.Interactable;
import com.github.dcysteine.neicustomdiagram.api.diagram.matcher.ComponentDiagramMatcher;
import com.github.dcysteine.neicustomdiagram.api.diagram.tooltip.Tooltip;
import com.github.dcysteine.neicustomdiagram.api.draw.Point;
import com.github.dcysteine.neicustomdiagram.impl.common.ComponentTransformer;
import com.github.dcysteine.neicustomdiagram.impl.gregtech.common.GregTechOreDictUtils;
import com.github.dcysteine.neicustomdiagram.impl.gregtech.common.GregTechRecipeUtils;
import com.google.common.collect.Collections2;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Iterables;
import gregtech.api.enums.Materials;
import gregtech.api.enums.OrePrefixes;
import gregtech.api.util.GT_OreDictUnificator;
import gregtech.api.util.GT_Recipe;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.FurnaceRecipes;
import net.minecraftforge.fluids.FluidRegistry;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;

class DiagramBuilder {
    private static final FluidComponent WATER =
            FluidComponent.create(FluidRegistry.WATER);
    private static final ItemComponent STONE_DUST =
            ItemComponent.create(GT_OreDictUnificator.get(OrePrefixes.dust, Materials.Stone, 1));

    @SuppressWarnings("unchecked")
    private static final ImmutableSet<Component> FURNACE_RECIPE_INPUTS =
            ImmutableSet.copyOf(
                    Collections2.transform(
                            (Set<ItemStack>) FurnaceRecipes.smelting().getSmeltingList().keySet(),
                            ItemComponent::create));

    private final LayoutHandler layoutHandler;
    private final LabelHandler labelHandler;
    private final Component rawOre;

    private final List<Component> craftingComponents;
    private final List<Component> usageComponents;
    private final Diagram.Builder diagramBuilder;

    DiagramBuilder(
            LayoutHandler layoutHandler, LabelHandler labelHandler, List<Component> rawOres) {
        this.layoutHandler = layoutHandler;
        this.labelHandler = labelHandler;
        this.rawOre = rawOres.get(0);

        this.craftingComponents = new ArrayList<>(rawOres);
        this.usageComponents = new ArrayList<>(rawOres);
        this.diagramBuilder = Diagram.builder();
    }

    void buildDiagram(ComponentDiagramMatcher.Builder matcherBuilder) {
        diagramBuilder
                .addAllOptionalLayouts(layoutHandler.layouts())
                .insertIntoSlot(
                        LayoutHandler.SlotKeys.RAW_ORE,
                        DisplayComponent.builder(rawOre).build());

        Optional<Component> crushedOreOptional =
                handleRecipes(
                        GregTechRecipeUtils.findRecipeByInput(
                                GT_Recipe.GT_Recipe_Map.sMaceratorRecipes, rawOre),
                        rawOre, LayoutHandler.SlotGroupKeys.RAW_ORE_MACERATE);

        Optional<Component> purifiedOreOptional =
                crushedOreOptional.flatMap(
                        crushedOre ->
                                handleRecipes(
                                        GregTechRecipeUtils.findRecipeByInput(
                                                GT_Recipe.GT_Recipe_Map.sOreWasherRecipes,
                                                crushedOre, WATER),
                                        crushedOre,
                                        LayoutHandler.SlotGroupKeys.CRUSHED_ORE_WASH));

        Optional<Component> impureDustOptional =
                crushedOreOptional.flatMap(
                        crushedOre ->
                                handleRecipes(
                                        GregTechRecipeUtils.findRecipeByInput(
                                                GT_Recipe.GT_Recipe_Map.sMaceratorRecipes,
                                                crushedOre),
                                        crushedOre,
                                        LayoutHandler.SlotGroupKeys.CRUSHED_ORE_MACERATE));

        Optional<Component> crushedOreThermalCentrifugeOptional =
                crushedOreOptional.flatMap(
                        crushedOre ->
                                handleRecipes(
                                        GregTechRecipeUtils.findRecipeByInput(
                                                GT_Recipe.GT_Recipe_Map.sThermalCentrifugeRecipes,
                                                crushedOre),
                                        crushedOre,
                                        LayoutHandler.SlotGroupKeys
                                                .CRUSHED_ORE_THERMAL_CENTRIFUGE));

        Optional<Component> purifiedDustOptional =
                purifiedOreOptional.flatMap(
                        purifiedOre ->
                                handleRecipes(
                                        GregTechRecipeUtils.findRecipeByInput(
                                                GT_Recipe.GT_Recipe_Map.sMaceratorRecipes,
                                                purifiedOre),
                                        purifiedOre,
                                        LayoutHandler.SlotGroupKeys.PURIFIED_ORE_MACERATE));

        Optional<Component> purifiedOreThermalCentrifugeOptional =
                purifiedOreOptional.flatMap(
                        purifiedOre ->
                                handleRecipes(
                                        GregTechRecipeUtils.findRecipeByInput(
                                                GT_Recipe.GT_Recipe_Map.sThermalCentrifugeRecipes,
                                                purifiedOre),
                                        purifiedOre,
                                        LayoutHandler.SlotGroupKeys
                                                .PURIFIED_ORE_THERMAL_CENTRIFUGE));

        purifiedOreOptional.ifPresent(
                purifiedOre ->
                        handleRecipes(
                                GregTechRecipeUtils.findRecipeByInput(
                                        GT_Recipe.GT_Recipe_Map.sSifterRecipes, purifiedOre),
                                purifiedOre, LayoutHandler.SlotGroupKeys.PURIFIED_ORE_SIFT));

        impureDustOptional.ifPresent(
                impureDust ->
                        handleRecipes(
                                GregTechRecipeUtils.findRecipeByInput(
                                        GT_Recipe.GT_Recipe_Map.sCentrifugeRecipes, impureDust),
                                impureDust, LayoutHandler.SlotGroupKeys.IMPURE_DUST_CENTRIFUGE));

        purifiedDustOptional.ifPresent(
                purifiedDust ->
                        handleRecipes(
                                GregTechRecipeUtils.findRecipeByInput(
                                        GT_Recipe.GT_Recipe_Map.sCentrifugeRecipes, purifiedDust),
                                purifiedDust,
                                LayoutHandler.SlotGroupKeys.PURIFIED_DUST_CENTRIFUGE));

        crushedOreThermalCentrifugeOptional.ifPresent(
                centrifugedOre ->
                        handleRecipes(
                                GregTechRecipeUtils.findRecipeByInput(
                                        GT_Recipe.GT_Recipe_Map.sMaceratorRecipes, centrifugedOre),
                                centrifugedOre,
                                LayoutHandler.SlotGroupKeys
                                        .CRUSHED_ORE_THERMAL_CENTRIFUGE_MACERATE));

        purifiedOreThermalCentrifugeOptional.ifPresent(
                centrifugedOre ->
                        handleRecipes(
                                GregTechRecipeUtils.findRecipeByInput(
                                        GT_Recipe.GT_Recipe_Map.sMaceratorRecipes, centrifugedOre),
                                centrifugedOre,
                                LayoutHandler.SlotGroupKeys
                                        .PURIFIED_ORE_THERMAL_CENTRIFUGE_MACERATE));

        addAdditionalFurnaceRecipesInteractable(
                LabelHandler.ItemLabel.FURNACE,
                LayoutHandler.AdditionalRecipeLabelPositions.FURNACE,
                Optional.of(rawOre), crushedOreOptional, purifiedOreOptional,
                crushedOreThermalCentrifugeOptional, purifiedOreThermalCentrifugeOptional,
                impureDustOptional, purifiedDustOptional);

        addAdditionalRecipesInteractable(
                LabelHandler.ItemLabel.ELECTRIC_BLAST_FURNACE,
                LayoutHandler.AdditionalRecipeLabelPositions.ELECTRIC_BLAST_FURNACE,
                GT_Recipe.GT_Recipe_Map.sBlastRecipes,
                Optional.of(rawOre));

        addAdditionalRecipesInteractable(
                LabelHandler.ItemLabel.CHEMICAL_BATH,
                LayoutHandler.AdditionalRecipeLabelPositions.CHEMICAL_BATH,
                GT_Recipe.GT_Recipe_Map.sChemicalBathRecipes,
                crushedOreOptional);

        addAdditionalRecipesInteractable(
                LabelHandler.ItemLabel.CHEMICAL_REACTOR,
                LayoutHandler.AdditionalRecipeLabelPositions.CHEMICAL_REACTOR,
                GT_Recipe.GT_Recipe_Map.sChemicalRecipes,
                crushedOreOptional);

        addAdditionalRecipesInteractable(
                LabelHandler.ItemLabel.ELECTROMAGNETIC_SEPARATOR,
                LayoutHandler.AdditionalRecipeLabelPositions.ELECTROMAGNETIC_SEPARATOR,
                GT_Recipe.GT_Recipe_Map.sElectroMagneticSeparatorRecipes,
                purifiedDustOptional);

        matcherBuilder.addDiagram(diagramBuilder.build())
                .addAllComponents(Interactable.RecipeType.CRAFTING, craftingComponents)
                .addAllComponents(Interactable.RecipeType.USAGE, usageComponents);
    }

    /**
     * Checks if there is exactly one recipe. If so, inserts its outputs into the slot and returns
     * its first output.
     */
    private Optional<Component> handleRecipes(
            List<GT_Recipe> recipes, Component input, String key) {
        Set<List<DisplayComponent>> outputsSet =
                recipes.stream()
                        .map(GregTechRecipeUtils::buildComponentsFromItemOutputs)
                        .collect(Collectors.toSet());
        if (outputsSet.size() > 1) {
            Logger.GREGTECH_ORE_PROCESSING.warn(
                    "Found {} recipe outputs: [{}] [{}]", outputsSet.size(), input, key);

            return Optional.empty();
        } else if (outputsSet.isEmpty()) {
            return Optional.empty();
        }

        List<DisplayComponent> outputs = Iterables.getOnlyElement(outputsSet);
        ComponentTransformer.removeComponent(outputs, STONE_DUST);
        if (outputs.size() == 0) {
            Logger.GREGTECH_ORE_PROCESSING.warn(
                    "Found no recipe outputs: [{}] [{}]", input, key);

            return Optional.empty();
        }
        diagramBuilder.autoInsertIntoSlotGroup(key).insertEachSafe(outputs);

        usageComponents.addAll(GregTechOreDictUtils.getAssociatedComponents(input));
        for (DisplayComponent output : outputs) {
            craftingComponents.addAll(
                    GregTechOreDictUtils.getAssociatedComponents(output.component()));
        }

        return Optional.of(outputs.get(0).component());
    }

    @SafeVarargs
    private final void addAdditionalRecipesInteractable(
            LabelHandler.ItemLabel label,
            Point pos,
            GT_Recipe.GT_Recipe_Map recipes,
            Optional<Component>... components) {
        addAdditionalRecipesInteractable(
                label, pos,
                component -> !GregTechRecipeUtils.findRecipeByInput(recipes, component).isEmpty(),
                components);
    }

    @SafeVarargs
    private final void addAdditionalFurnaceRecipesInteractable(
            LabelHandler.ItemLabel label, Point pos, Optional<Component>... components) {
        addAdditionalRecipesInteractable(label, pos, FURNACE_RECIPE_INPUTS::contains, components);
    }

    @SafeVarargs
    private final void addAdditionalRecipesInteractable(
            LabelHandler.ItemLabel label,
            Point pos,
            Predicate<Component> hasRecipe,
            Optional<Component>... components) {
        List<Component> validComponents =
                Arrays.stream(components)
                        .filter(Optional::isPresent)
                        .map(Optional::get)
                        .filter(hasRecipe)
                        .distinct()
                        .collect(Collectors.toList());
        if (validComponents.isEmpty()) {
            return;
        }

        CustomInteractable baseLabel = labelHandler.buildLabel(label, pos);
        Tooltip.Builder tooltipBuilder =
                Tooltip.builder()
                        .addAllLines(baseLabel.tooltip().lines())
                        .addSpacing();
        validComponents.forEach(tooltipBuilder::addComponent);

        diagramBuilder.addInteractable(
                CustomInteractable.builder(baseLabel.drawable())
                        .setTooltip(tooltipBuilder.build())
                        .build());
    }
}