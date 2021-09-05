package com.github.dcysteine.neicustomdiagram.generators.gregtech.oreprocessing;

import com.github.dcysteine.neicustomdiagram.api.Logger;
import com.github.dcysteine.neicustomdiagram.api.diagram.Diagram;
import com.github.dcysteine.neicustomdiagram.api.diagram.component.Component;
import com.github.dcysteine.neicustomdiagram.api.diagram.component.DisplayComponent;
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
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.google.common.collect.MultimapBuilder;
import com.google.common.collect.SetMultimap;
import gregtech.api.enums.Materials;
import gregtech.api.enums.OrePrefixes;
import gregtech.api.util.GT_OreDictUnificator;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

class DiagramBuilder {
    private static final ItemComponent STONE_DUST =
            ItemComponent.create(GT_OreDictUnificator.get(OrePrefixes.dust, Materials.Stone, 1));

    private final LayoutHandler layoutHandler;
    private final LabelHandler labelHandler;
    private final RecipeHandler recipeHandler;

    private final ItemComponent rawOre;
    private final Set<Component> craftingComponents;
    private final Set<Component> usageComponents;
    private final Diagram.Builder diagramBuilder;

    DiagramBuilder(
            LayoutHandler layoutHandler, LabelHandler labelHandler, RecipeHandler recipeHandler,
            List<ItemComponent> rawOres) {
        this.layoutHandler = layoutHandler;
        this.labelHandler = labelHandler;
        this.recipeHandler = recipeHandler;

        // Sometimes, non-GregTech-compatible ores get returned because they are in the Forge ore
        // dictionary. Filter them out to fix the diagrams.
        List<ItemComponent> filteredRawOres =
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

        Optional<ItemComponent> crushedOreOptional =
                handleRecipes(
                        RecipeHandler.RecipeMap.MACERATOR, rawOre,
                        LayoutHandler.SlotGroupKeys.RAW_ORE_MACERATE);

        Optional<ItemComponent> purifiedOreOptional =
                crushedOreOptional.flatMap(
                        crushedOre ->
                                handleRecipes(
                                        RecipeHandler.RecipeMap.ORE_WASHER, crushedOre,
                                        LayoutHandler.SlotGroupKeys.CRUSHED_ORE_WASH));

        Optional<ItemComponent> impureDustOptional =
                crushedOreOptional.flatMap(
                        crushedOre ->
                                handleRecipes(
                                        RecipeHandler.RecipeMap.MACERATOR, crushedOre,
                                        LayoutHandler.SlotGroupKeys.CRUSHED_ORE_MACERATE));

        Optional<ItemComponent> crushedOreThermalCentrifugeOptional =
                crushedOreOptional.flatMap(
                        crushedOre ->
                                handleRecipes(
                                        RecipeHandler.RecipeMap.THERMAL_CENTRIFUGE, crushedOre,
                                        LayoutHandler.SlotGroupKeys
                                                .CRUSHED_ORE_THERMAL_CENTRIFUGE));

        Optional<ItemComponent> purifiedDustOptional =
                purifiedOreOptional.flatMap(
                        purifiedOre ->
                                handleRecipes(
                                        RecipeHandler.RecipeMap.MACERATOR, purifiedOre,
                                        LayoutHandler.SlotGroupKeys.PURIFIED_ORE_MACERATE));

        Optional<ItemComponent> purifiedOreThermalCentrifugeOptional =
                purifiedOreOptional.flatMap(
                        purifiedOre ->
                                handleRecipes(
                                        RecipeHandler.RecipeMap.THERMAL_CENTRIFUGE, purifiedOre,
                                        LayoutHandler.SlotGroupKeys
                                                .PURIFIED_ORE_THERMAL_CENTRIFUGE));

        purifiedOreOptional.ifPresent(
                purifiedOre ->
                        handleRecipes(
                                RecipeHandler.RecipeMap.SIFTER, purifiedOre,
                                LayoutHandler.SlotGroupKeys.PURIFIED_ORE_SIFT));

        impureDustOptional.ifPresent(
                impureDust ->
                        handleRecipes(
                                RecipeHandler.RecipeMap.CENTRIFUGE, impureDust,
                                LayoutHandler.SlotGroupKeys.IMPURE_DUST_CENTRIFUGE));

        purifiedDustOptional.ifPresent(
                purifiedDust ->
                        handleRecipes(
                                RecipeHandler.RecipeMap.CENTRIFUGE, purifiedDust,
                                LayoutHandler.SlotGroupKeys.PURIFIED_DUST_CENTRIFUGE));

        purifiedDustOptional.ifPresent(
                purifiedDust ->
                        handleRecipes(
                                RecipeHandler.RecipeMap.ELECTROMAGNETIC_SEPARATOR, purifiedDust,
                                LayoutHandler.SlotGroupKeys
                                        .PURIFIED_DUST_ELECTROMAGNETIC_SEPARATE));

        crushedOreThermalCentrifugeOptional.ifPresent(
                centrifugedOre ->
                        handleRecipes(
                                RecipeHandler.RecipeMap.MACERATOR, centrifugedOre,
                                LayoutHandler.SlotGroupKeys
                                        .CRUSHED_ORE_THERMAL_CENTRIFUGE_MACERATE));

        purifiedOreThermalCentrifugeOptional.ifPresent(
                centrifugedOre ->
                        handleRecipes(
                                RecipeHandler.RecipeMap.MACERATOR, centrifugedOre,
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
                        RecipeHandler.RecipeMap.BLAST_FURNACE,
                        Optional.of(rawOre)));

        additionalRecipeOutputs.addAll(
                addAdditionalRecipesInteractable(
                        LabelHandler.ItemLabel.CHEMICAL_BATH,
                        LayoutHandler.AdditionalRecipeLabelPositions.CHEMICAL_BATH,
                        RecipeHandler.RecipeMap.CHEMICAL_BATH,
                        crushedOreOptional));

        additionalRecipeOutputs.addAll(
                addAdditionalRecipesInteractable(
                        LabelHandler.ItemLabel.CHEMICAL_REACTOR,
                        LayoutHandler.AdditionalRecipeLabelPositions.CHEMICAL_REACTOR,
                        RecipeHandler.RecipeMap.CHEMICAL_REACTOR,
                        crushedOreOptional, purifiedOreOptional));

        additionalRecipeOutputs.addAll(
                addAdditionalRecipesInteractable(
                        LabelHandler.ItemLabel.AUTOCLAVE,
                        LayoutHandler.AdditionalRecipeLabelPositions.AUTOCLAVE,
                        RecipeHandler.RecipeMap.AUTOCLAVE,
                        impureDustOptional, purifiedDustOptional));

        additionalRecipeOutputs.removeIf(STONE_DUST::equals);
        for (Component component : ImmutableList.copyOf(additionalRecipeOutputs)) {
            craftingComponents.addAll(GregTechOreDictUtil.getAssociatedComponents(component));
            craftingComponents.addAll(
                    ComponentTransformer.transformFromDisplay(
                            FluidDictUtil.getFluidContainers(component)));

            Optional<ItemComponent> displayItem = GregTechFluidDictUtil.getDisplayItem(component);
            if (displayItem.isPresent()) {
                craftingComponents.add(displayItem.get());

                // Replace fluids with their GregTech cell form (or, failing that, fluid display
                // item form), since it's more convenient when looking up recipes.
                ItemComponent replacementItem = displayItem.get();
                replacementItem =
                        GregTechFluidDictUtil.fillCell(replacementItem).orElse(replacementItem);
                additionalRecipeOutputs.remove(component);
                additionalRecipeOutputs.add(replacementItem);
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
    private Optional<ItemComponent> handleRecipes(
            RecipeHandler.RecipeMap recipeMap, ItemComponent input, String key) {
        Optional<ImmutableList<DisplayComponent>> outputsOptional =
                recipeHandler.getUniqueRecipeOutput(recipeMap, input);
        if (!outputsOptional.isPresent()) {
            return Optional.empty();
        }

        List<DisplayComponent> outputs = new ArrayList<>(outputsOptional.get());
        ComponentTransformer.removeComponent(outputs, STONE_DUST);
        if (outputs.size() == 0) {
            Logger.GREGTECH_ORE_PROCESSING.warn(
                    "Found no recipe outputs: [{}] [{}]", key, input);

            return Optional.empty();
        }
        diagramBuilder.autoInsertIntoSlotGroup(key).insertEachSafe(outputs);

        usageComponents.addAll(GregTechOreDictUtil.getAssociatedComponents(input));
        for (DisplayComponent output : outputs) {
            craftingComponents.addAll(
                    GregTechOreDictUtil.getAssociatedComponents(output.component()));
        }

        Component firstOutput = outputs.get(0).component();
        if (firstOutput.type() == Component.ComponentType.FLUID) {
            Logger.GREGTECH_ORE_PROCESSING.warn(
                    "Found unexpected fluid output: [{}] [{}] [{}]", key, input, outputs);
            return Optional.empty();
        }
        return Optional.of((ItemComponent) firstOutput);
    }

    @SafeVarargs
    private final Collection<Component> addAdditionalRecipesInteractable(
            LabelHandler.ItemLabel label,
            Point pos,
            RecipeHandler.RecipeMap recipeMap,
            Optional<ItemComponent>... components) {
        return addAdditionalRecipesInteractable(
                label, pos, component -> getRecipeOutputs(recipeMap, component), components);
    }

    private Collection<Component> getRecipeOutputs(
            RecipeHandler.RecipeMap recipeMap, ItemComponent component) {
        return recipeHandler.getRecipeOutputs(recipeMap, component).stream()
                .flatMap(List::stream)
                .map(DisplayComponent::component)
                .collect(Collectors.toSet());
    }

    @SafeVarargs
    private final Collection<Component> addAdditionalFurnaceRecipesInteractable(
            LabelHandler.ItemLabel label, Point pos, Optional<ItemComponent>... components) {
        return addAdditionalRecipesInteractable(
                label, pos, this::getFurnaceRecipeOutputs, components);
    }

    private Collection<Component> getFurnaceRecipeOutputs(ItemComponent component) {
        Optional<ItemComponent> output = recipeHandler.getFurnaceRecipeOutput(component);
        return output.isPresent() ? Lists.newArrayList(output.get()) : Lists.newArrayList();
    }

    /**
     * Returns a collection of outputs for the given additional recipes.
     */
    @SafeVarargs
    private final Collection<Component> addAdditionalRecipesInteractable(
            LabelHandler.ItemLabel label,
            Point pos,
            Function<ItemComponent, Collection<Component>> recipeOutputs,
            Optional<ItemComponent>... components) {
        SetMultimap<ItemComponent, Component> multimap =
                MultimapBuilder.linkedHashKeys().hashSetValues().build();
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