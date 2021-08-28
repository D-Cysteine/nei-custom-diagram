package com.github.dcysteine.neicustomdiagram.generators.gregtech.oreprocessing;

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
import com.github.dcysteine.neicustomdiagram.util.ComponentTransformer;
import com.github.dcysteine.neicustomdiagram.util.FluidDictUtil;
import com.github.dcysteine.neicustomdiagram.util.gregtech.GregTechFluidDictUtil;
import com.github.dcysteine.neicustomdiagram.util.gregtech.GregTechOreDictUtil;
import com.github.dcysteine.neicustomdiagram.util.gregtech.GregTechRecipeUtil;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.MultimapBuilder;
import com.google.common.collect.SetMultimap;
import gregtech.api.enums.Materials;
import gregtech.api.enums.OrePrefixes;
import gregtech.api.util.GT_OreDictUnificator;
import gregtech.api.util.GT_Recipe;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.FurnaceRecipes;
import net.minecraftforge.fluids.FluidRegistry;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

class DiagramBuilder {
    private static final FluidComponent WATER =
            FluidComponent.create(FluidRegistry.WATER);
    private static final ItemComponent STONE_DUST =
            ItemComponent.create(GT_OreDictUnificator.get(OrePrefixes.dust, Materials.Stone, 1));

    private final LayoutHandler layoutHandler;
    private final LabelHandler labelHandler;
    private final Component rawOre;

    private final Set<Component> craftingComponents;
    private final Set<Component> usageComponents;
    private final Diagram.Builder diagramBuilder;

    DiagramBuilder(
            LayoutHandler layoutHandler, LabelHandler labelHandler, List<Component> rawOres) {
        this.layoutHandler = layoutHandler;
        this.labelHandler = labelHandler;

        // Sometimes, non-GregTech-compatible ores get returned because they are in the Forge ore
        // dictionary. Filter them out to fix the diagrams.
        List<Component> filteredRawOres =
                rawOres.stream()
                        .filter(rawOre -> GregTechOreDictUtil.getItemData(rawOre).isPresent())
                        .collect(Collectors.toList());
        this.rawOre = filteredRawOres.get(0);

        this.craftingComponents = new HashSet<>(filteredRawOres);
        this.usageComponents = new HashSet<>(filteredRawOres);
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
                        GregTechRecipeUtil.findRecipeByInput(
                                GT_Recipe.GT_Recipe_Map.sMaceratorRecipes, rawOre),
                        rawOre, LayoutHandler.SlotGroupKeys.RAW_ORE_MACERATE);

        Optional<Component> purifiedOreOptional =
                crushedOreOptional.flatMap(
                        crushedOre ->
                                handleRecipes(
                                        GregTechRecipeUtil.findRecipeByInput(
                                                GT_Recipe.GT_Recipe_Map.sOreWasherRecipes,
                                                crushedOre, WATER),
                                        crushedOre,
                                        LayoutHandler.SlotGroupKeys.CRUSHED_ORE_WASH));

        Optional<Component> impureDustOptional =
                crushedOreOptional.flatMap(
                        crushedOre ->
                                handleRecipes(
                                        GregTechRecipeUtil.findRecipeByInput(
                                                GT_Recipe.GT_Recipe_Map.sMaceratorRecipes,
                                                crushedOre),
                                        crushedOre,
                                        LayoutHandler.SlotGroupKeys.CRUSHED_ORE_MACERATE));

        Optional<Component> crushedOreThermalCentrifugeOptional =
                crushedOreOptional.flatMap(
                        crushedOre ->
                                handleRecipes(
                                        GregTechRecipeUtil.findRecipeByInput(
                                                GT_Recipe.GT_Recipe_Map.sThermalCentrifugeRecipes,
                                                crushedOre),
                                        crushedOre,
                                        LayoutHandler.SlotGroupKeys
                                                .CRUSHED_ORE_THERMAL_CENTRIFUGE));

        Optional<Component> purifiedDustOptional =
                purifiedOreOptional.flatMap(
                        purifiedOre ->
                                handleRecipes(
                                        GregTechRecipeUtil.findRecipeByInput(
                                                GT_Recipe.GT_Recipe_Map.sMaceratorRecipes,
                                                purifiedOre),
                                        purifiedOre,
                                        LayoutHandler.SlotGroupKeys.PURIFIED_ORE_MACERATE));

        Optional<Component> purifiedOreThermalCentrifugeOptional =
                purifiedOreOptional.flatMap(
                        purifiedOre ->
                                handleRecipes(
                                        GregTechRecipeUtil.findRecipeByInput(
                                                GT_Recipe.GT_Recipe_Map.sThermalCentrifugeRecipes,
                                                purifiedOre),
                                        purifiedOre,
                                        LayoutHandler.SlotGroupKeys
                                                .PURIFIED_ORE_THERMAL_CENTRIFUGE));

        purifiedOreOptional.ifPresent(
                purifiedOre ->
                        handleRecipes(
                                GregTechRecipeUtil.findRecipeByInput(
                                        GT_Recipe.GT_Recipe_Map.sSifterRecipes, purifiedOre),
                                purifiedOre, LayoutHandler.SlotGroupKeys.PURIFIED_ORE_SIFT));

        impureDustOptional.ifPresent(
                impureDust ->
                        handleRecipes(
                                GregTechRecipeUtil.findRecipeByInput(
                                        GT_Recipe.GT_Recipe_Map.sCentrifugeRecipes, impureDust),
                                impureDust, LayoutHandler.SlotGroupKeys.IMPURE_DUST_CENTRIFUGE));

        purifiedDustOptional.ifPresent(
                purifiedDust ->
                        handleRecipes(
                                GregTechRecipeUtil.findRecipeByInput(
                                        GT_Recipe.GT_Recipe_Map.sCentrifugeRecipes, purifiedDust),
                                purifiedDust,
                                LayoutHandler.SlotGroupKeys.PURIFIED_DUST_CENTRIFUGE));

        crushedOreThermalCentrifugeOptional.ifPresent(
                centrifugedOre ->
                        handleRecipes(
                                GregTechRecipeUtil.findRecipeByInput(
                                        GT_Recipe.GT_Recipe_Map.sMaceratorRecipes, centrifugedOre),
                                centrifugedOre,
                                LayoutHandler.SlotGroupKeys
                                        .CRUSHED_ORE_THERMAL_CENTRIFUGE_MACERATE));

        purifiedOreThermalCentrifugeOptional.ifPresent(
                centrifugedOre ->
                        handleRecipes(
                                GregTechRecipeUtil.findRecipeByInput(
                                        GT_Recipe.GT_Recipe_Map.sMaceratorRecipes, centrifugedOre),
                                centrifugedOre,
                                LayoutHandler.SlotGroupKeys
                                        .PURIFIED_ORE_THERMAL_CENTRIFUGE_MACERATE));

        HashSet<Component> additionalRecipeOutputs = new HashSet<>();
        additionalRecipeOutputs.addAll(
                addAdditionalFurnaceRecipesInteractable(
                        LabelHandler.ItemLabel.FURNACE,
                        LayoutHandler.AdditionalRecipeLabelPositions.FURNACE,
                        Optional.of(rawOre), crushedOreOptional, purifiedOreOptional,
                        crushedOreThermalCentrifugeOptional, purifiedOreThermalCentrifugeOptional,
                        impureDustOptional, purifiedDustOptional));

        additionalRecipeOutputs.addAll(
                addAdditionalRecipesInteractable(
                        LabelHandler.ItemLabel.ELECTRIC_BLAST_FURNACE,
                        LayoutHandler.AdditionalRecipeLabelPositions.ELECTRIC_BLAST_FURNACE,
                        GT_Recipe.GT_Recipe_Map.sBlastRecipes,
                        Optional.of(rawOre)));

        additionalRecipeOutputs.addAll(
                addAdditionalRecipesInteractable(
                        LabelHandler.ItemLabel.CHEMICAL_BATH,
                        LayoutHandler.AdditionalRecipeLabelPositions.CHEMICAL_BATH,
                        GT_Recipe.GT_Recipe_Map.sChemicalBathRecipes,
                        crushedOreOptional));

        additionalRecipeOutputs.addAll(
                addAdditionalRecipesInteractable(
                        LabelHandler.ItemLabel.CHEMICAL_REACTOR,
                        LayoutHandler.AdditionalRecipeLabelPositions.CHEMICAL_REACTOR,
                        GT_Recipe.GT_Recipe_Map.sChemicalRecipes,
                        crushedOreOptional, purifiedOreOptional));

        additionalRecipeOutputs.addAll(
                addAdditionalRecipesInteractable(
                        LabelHandler.ItemLabel.ELECTROMAGNETIC_SEPARATOR,
                        LayoutHandler.AdditionalRecipeLabelPositions.ELECTROMAGNETIC_SEPARATOR,
                        GT_Recipe.GT_Recipe_Map.sElectroMagneticSeparatorRecipes,
                        purifiedDustOptional));

        additionalRecipeOutputs.removeIf(STONE_DUST::equals);
        for (Component component : ImmutableList.copyOf(additionalRecipeOutputs)) {
            craftingComponents.addAll(GregTechOreDictUtil.getAssociatedComponents(component));
            craftingComponents.addAll(
                    ComponentTransformer.transformFromDisplay(
                            FluidDictUtil.getFluidContainers(component)));

            Optional<ItemComponent> displayItem =
                    GregTechFluidDictUtil.fluidToDisplayItem(component);
            if (displayItem.isPresent()) {
                craftingComponents.add(displayItem.get());
                // Replace fluids with their GregTech display item form, since it's more convenient
                // when looking up recipes.
                additionalRecipeOutputs.remove(component);
                additionalRecipeOutputs.add(displayItem.get());
            }
        }
        diagramBuilder.autoInsertIntoSlotGroup(
                        LayoutHandler.SlotGroupKeys.ADDITIONAL_RECIPE_OUTPUTS)
                .insertEachSafe(
                        ComponentTransformer.transformToDisplay(additionalRecipeOutputs));

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
                        .map(GregTechRecipeUtil::buildComponentsFromItemOutputs)
                        .collect(Collectors.toSet());
        if (outputsSet.size() > 1) {
            Logger.GREGTECH_ORE_PROCESSING.warn(
                    "Found {} recipes: [{}] [{}]", outputsSet.size(), input, key);

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

        usageComponents.addAll(GregTechOreDictUtil.getAssociatedComponents(input));
        for (DisplayComponent output : outputs) {
            craftingComponents.addAll(
                    GregTechOreDictUtil.getAssociatedComponents(output.component()));
        }

        return Optional.of(outputs.get(0).component());
    }

    @SafeVarargs
    private final Collection<Component> addAdditionalRecipesInteractable(
            LabelHandler.ItemLabel label,
            Point pos,
            GT_Recipe.GT_Recipe_Map recipes,
            Optional<Component>... components) {
        return addAdditionalRecipesInteractable(
                label, pos, component -> getRecipeOutputs(recipes, component), components);
    }

    private static Collection<Component> getRecipeOutputs(
            GT_Recipe.GT_Recipe_Map recipes, Component component) {
        return GregTechRecipeUtil.findRecipeByInput(recipes, component).stream()
                .flatMap(recipe -> GregTechRecipeUtil.buildComponentsFromOutputs(recipe).stream())
                .map(DisplayComponent::component)
                .collect(Collectors.toList());
    }

    @SafeVarargs
    private final Collection<Component> addAdditionalFurnaceRecipesInteractable(
            LabelHandler.ItemLabel label, Point pos, Optional<Component>... components) {
        return addAdditionalRecipesInteractable(
                label, pos, DiagramBuilder::getFurnaceRecipeOutputs, components);
    }

    private static Collection<Component> getFurnaceRecipeOutputs(Component component) {
        ItemStack output =
                (ItemStack) FurnaceRecipes.smelting().getSmeltingList().get(component.stack());
        return output == null
                ? Lists.newArrayList() : Lists.newArrayList(ItemComponent.create(output));
    }

    /**
     * Returns a collection of outputs for the given additional recipes.
     */
    @SafeVarargs
    private final Collection<Component> addAdditionalRecipesInteractable(
            LabelHandler.ItemLabel label,
            Point pos,
            Function<Component, Collection<Component>> recipeOutputs,
            Optional<Component>... components) {
        SetMultimap<Component, Component> multimap =
                MultimapBuilder.hashKeys().hashSetValues().build();
        Arrays.stream(components)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .forEach(component -> multimap.putAll(component, recipeOutputs.apply(component)));
        if (multimap.keySet().isEmpty()) {
            return Lists.newArrayList();
        }

        CustomInteractable baseLabel = labelHandler.buildLabel(label, pos);
        Tooltip.Builder tooltipBuilder =
                Tooltip.builder()
                        .addAllLines(baseLabel.tooltip().lines())
                        .addSpacing();
        multimap.keySet().forEach(tooltipBuilder::addComponent);

        diagramBuilder.addInteractable(
                CustomInteractable.builder(baseLabel.drawable())
                        .setTooltip(tooltipBuilder.build())
                        .build());
        return multimap.values();
    }
}