package com.github.dcysteine.neicustomdiagram.generators.gregtech5.materialtools;

import com.detrav.items.DetravMetaGeneratedTool01;
import com.github.dcysteine.neicustomdiagram.api.Formatter;
import com.github.dcysteine.neicustomdiagram.api.diagram.component.DisplayComponent;
import com.github.dcysteine.neicustomdiagram.api.diagram.component.ItemComponent;
import com.github.dcysteine.neicustomdiagram.api.diagram.tooltip.Tooltip;
import com.github.dcysteine.neicustomdiagram.mod.Lang;
import com.github.dcysteine.neicustomdiagram.mod.Registry;
import com.github.dcysteine.neicustomdiagram.util.gregtech5.GregTechFormatting;
import com.google.auto.value.AutoValue;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSortedSet;
import com.google.common.collect.ListMultimap;
import com.google.common.collect.MultimapBuilder;
import com.google.common.collect.SortedSetMultimap;
import gregtech.api.enums.Materials;
import gregtech.api.items.GT_MetaGenerated_Tool;
import gregtech.api.util.GT_Recipe;
import gregtech.common.items.GT_MetaGenerated_Tool_01;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.CraftingManager;
import net.minecraft.item.crafting.IRecipe;

import javax.annotation.Nullable;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Class that finds GregTech tools by looking up recipes, and provides access to them by primary
 * material.
 */
class RecipeHandler {
    /**
     * This is basically a struct class that holds an {@link ItemComponent} without NBT, as well as
     * additional data that would normally be stored in NBT. We use this as a key in our multimaps
     * to allow us to group together tools with different NBT but that are the same base tool.
     */
    @AutoValue
    abstract static class BaseTool implements Comparable<BaseTool> {
        private static final Comparator<BaseTool> COMPARATOR =
                Comparator.<BaseTool, Integer>comparing(b -> b.primaryMaterial().mMetaItemSubID)
                        .thenComparing(b -> b.primaryMaterial().mName)
                        .thenComparing(BaseTool::itemComponent);

        private static BaseTool create(ItemStack itemStack) {
            return new AutoValue_RecipeHandler_BaseTool(
                    ItemComponent.create(itemStack),
                    GT_MetaGenerated_Tool.getPrimaryMaterial(itemStack));
        }

        abstract ItemComponent itemComponent();
        abstract Materials primaryMaterial();

        @Override
        public int compareTo(BaseTool other) {
            return COMPARATOR.compare(this, other);
        }
    }

    /** Comparator that takes EU capacity into account. */
    private static final Comparator<DisplayComponent> EU_CAPACITY_COMPARATOR =
            Comparator.<DisplayComponent, Long>comparing(
                            d -> getEuCapacity((ItemComponent) d.component()).orElse(-1L))
                    .thenComparing(Comparator.naturalOrder());

    private static final ImmutableList<Integer> TURBINE_TOOL_IDS =
            ImmutableList.of(
                    (int) GT_MetaGenerated_Tool_01.TURBINE_SMALL,
                    (int) GT_MetaGenerated_Tool_01.TURBINE,
                    (int) GT_MetaGenerated_Tool_01.TURBINE_LARGE,
                    (int) GT_MetaGenerated_Tool_01.TURBINE_HUGE);

    private static final int ELECTRIC_SCANNER_ID_START = 100;

    /**
     * Multimap of base tool (without NBT) to sorted set of tool item components (with NBT).
     *
     * <p>This is an intermediary data structure which we use to group tools together ignoring
     * extraneous NBT such as electrical stats. We use a sorted set to hold the values, to ensure
     * that item components are iterated through in order taking into account extraneous NBT.
     */
    private final SortedSetMultimap<BaseTool, ItemComponent> toolsMultimap;

    /**
     * Multimap of base tool (without NBT) to sorted set of Detrav scanner components (with NBT).
     *
     * <p>This is an intermediary data structure which we use to group tools together ignoring
     * extraneous NBT such as electrical stats. We use a sorted set to hold the values, to ensure
     * that item components are iterated through in order taking into account extraneous NBT.
     */
    private final SortedSetMultimap<BaseTool, ItemComponent> scannersMultimap;

    /**
     * Multimap of material to list of lists of tools with that primary material.
     *
     * <p>We group together tools with the same base item but different NBT (which will be
     * electrical stats). This is why values will be lists of lists.
     */
    private final ListMultimap<Materials, ImmutableList<DisplayComponent>> materialToolsMultimap;

    /**
     * Multimap of material to list of lists of turbines with that primary material.
     *
     * <p>We group together tools with the same base item but different NBT (which will be
     * electrical stats). This is why values will be lists of lists. Though in practice, turbines
     * don't have electric stats, so each inner list will have size 1.
     */
    private final ListMultimap<Materials, ImmutableList<DisplayComponent>> materialTurbinesMultimap;

    /**
     * Multimap of material to list of lists of Detrav scanners with that primary material.
     *
     * <p>We group together tools with the same base item but different NBT (which will be
     * electrical stats). This is why values will be lists of lists. Though in practice, scanners
     * don't have electric stats, so each inner list will have size 1.
     */
    private final ListMultimap<Materials, ImmutableList<DisplayComponent>> materialScannersMultimap;

    /**
     * Multimap of material to list of lists of Detrav electric scanners with that primary material.
     *
     * <p>We group together tools with the same base item but different NBT (which will be
     * electrical stats). This is why values will be lists of lists. Though in practice, scanners
     * don't have varying electric stats, so each inner list will have size 1.
     */
    private final ListMultimap<Materials, ImmutableList<DisplayComponent>>
            materialElectricScannersMultimap;

    RecipeHandler() {
        this.toolsMultimap = MultimapBuilder.hashKeys().treeSetValues().build();
        this.scannersMultimap = MultimapBuilder.hashKeys().treeSetValues().build();
        this.materialToolsMultimap = MultimapBuilder.hashKeys().arrayListValues().build();
        this.materialTurbinesMultimap = MultimapBuilder.hashKeys().arrayListValues().build();
        this.materialScannersMultimap = MultimapBuilder.hashKeys().arrayListValues().build();
        this.materialElectricScannersMultimap =
                MultimapBuilder.hashKeys().arrayListValues().build();
    }

    /** This method must be called before any other methods are called. */
    @SuppressWarnings("unchecked")
    void initialize() {
        // First pass: find all tools with recipes, and group them by base NBT item stack.
        ((List<IRecipe>) CraftingManager.getInstance().getRecipeList())
                .forEach(recipe -> addTool(recipe.getRecipeOutput()));
        GT_Recipe.GT_Recipe_Map.sAssemblerRecipes.mRecipeList
                .forEach(recipe -> addTool(recipe.getOutput(0)));

        // Second pass: iterate through and construct DisplayComponents for found tools.
        // We iterate on SortedSet copies so that the resulting lists of tools are ordered.
        for (BaseTool baseTool: ImmutableSortedSet.copyOf(toolsMultimap.keySet())) {
            ImmutableList<DisplayComponent> displayComponents =
                    ImmutableList.copyOf(
                            toolsMultimap.get(baseTool).stream()
                                    .map(RecipeHandler::buildDisplayComponent)
                                    .sorted(EU_CAPACITY_COMPARATOR)
                                    .collect(Collectors.toList()));

            if (TURBINE_TOOL_IDS.contains(baseTool.itemComponent().damage())) {
                materialTurbinesMultimap.put(baseTool.primaryMaterial(), displayComponents);
            } else {
                materialToolsMultimap.put(baseTool.primaryMaterial(), displayComponents);
            }
        }

        for (BaseTool baseTool : ImmutableSortedSet.copyOf(scannersMultimap.keySet())) {
            ImmutableList<DisplayComponent> displayComponents =
                    ImmutableList.copyOf(
                            scannersMultimap.get(baseTool).stream()
                                    .map(RecipeHandler::buildDisplayComponent)
                                    .sorted(EU_CAPACITY_COMPARATOR)
                                    .collect(Collectors.toList()));

            if (baseTool.itemComponent().damage() >= ELECTRIC_SCANNER_ID_START) {
                materialElectricScannersMultimap.put(baseTool.primaryMaterial(), displayComponents);
            } else {
                materialScannersMultimap.put(baseTool.primaryMaterial(), displayComponents);
            }
        }
    }

    /**
     * Returns a list of lists of tools with the specified primary material.
     *
     * <p>We group together tools with the same base item but different NBT (which will be
     * electrical stats). This is why values will be lists of lists.
     */
    ImmutableList<ImmutableList<DisplayComponent>> getTools(Materials material) {
        return ImmutableList.copyOf(materialToolsMultimap.get(material));
    }

    /**
     * Returns a list of lists of turbines with the specified primary material.
     *
     * <p>We group together tools with the same base item but different NBT (which will be
     * electrical stats). This is why values will be lists of lists. Though in practice, turbines
     * don't have electric stats, so each inner list will have size 1.
     */
    ImmutableList<ImmutableList<DisplayComponent>> getTurbines(Materials material) {
        return ImmutableList.copyOf(materialTurbinesMultimap.get(material));
    }

    /**
     * Returns a list of lists of Detrav scanners with the specified primary material.
     *
     * <p>We group together tools with the same base item but different NBT (which will be
     * electrical stats). This is why values will be lists of lists. Though in practice, scanners
     * don't have electric stats, so each inner list will have size 1.
     */
    ImmutableList<ImmutableList<DisplayComponent>> getScanners(Materials material) {
        return ImmutableList.copyOf(materialScannersMultimap.get(material));
    }

    /**
     * Returns a list of lists of Detrav electric scanners with the specified primary material.
     *
     * <p>We group together tools with the same base item but different NBT (which will be
     * electrical stats). This is why values will be lists of lists. Though in practice, scanners
     * don't have varying electric stats, so each inner list will have size 1.
     */
    ImmutableList<ImmutableList<DisplayComponent>> getElectricScanners(Materials material) {
        return ImmutableList.copyOf(materialElectricScannersMultimap.get(material));
    }

    private void addTool(@Nullable ItemStack itemStack) {
        if (itemStack == null) {
            return;
        }

        if (itemStack.getItem() == GT_MetaGenerated_Tool_01.INSTANCE) {
            toolsMultimap.put(BaseTool.create(itemStack), ItemComponent.createWithNbt(itemStack));
        }

        if (Registry.ModDependency.DETRAV_SCANNER.isLoaded()) {
            if (itemStack.getItem() == DetravMetaGeneratedTool01.INSTANCE) {
                scannersMultimap.put(
                        BaseTool.create(itemStack), ItemComponent.createWithNbt(itemStack));
            }
        }
    }

    /** Returns the EU capacity of the given item, if available. */
    private static Optional<Long> getEuCapacity(ItemComponent itemComponent) {
        Long[] electricStats =
                ((GT_MetaGenerated_Tool) itemComponent.item())
                        .getElectricStats(itemComponent.stack());
        if (electricStats == null) {
            return Optional.empty();
        } else {
            // The first entry in electricStats is the max energy capacity.
            return Optional.of(electricStats[0]);
        }
    }

    private static DisplayComponent buildDisplayComponent(ItemComponent itemComponent) {
        DisplayComponent.Builder builder = DisplayComponent.builder(itemComponent);

        ItemStack itemStack = itemComponent.stack();
        Materials primaryMaterial = GT_MetaGenerated_Tool.getPrimaryMaterial(itemStack);
        Materials secondaryMaterial = GT_MetaGenerated_Tool.getSecondaryMaterial(itemStack);
        builder.setAdditionalTooltip(
                Tooltip.builder()
                        .setFormatting(Tooltip.INFO_FORMATTING)
                        .addTextLine(
                                Lang.GREGTECH_5_MATERIAL_TOOLS.transf(
                                        "primarymateriallabel",
                                        GregTechFormatting.getMaterialDescription(primaryMaterial)))
                        .addTextLine(
                                Lang.GREGTECH_5_MATERIAL_TOOLS.transf(
                                        "secondarymateriallabel",
                                        GregTechFormatting.getMaterialDescription(
                                                secondaryMaterial)))
                        .build());

        getEuCapacity(itemComponent).ifPresent(
                euCapacity -> builder.setAdditionalInfo(
                        Formatter.smartFormatInteger(euCapacity)));

        return builder.build();
    }
}
