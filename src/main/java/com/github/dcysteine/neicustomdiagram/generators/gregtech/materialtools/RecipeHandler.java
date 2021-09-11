package com.github.dcysteine.neicustomdiagram.generators.gregtech.materialtools;

import com.detrav.items.DetravMetaGeneratedTool01;
import com.github.dcysteine.neicustomdiagram.api.Registry;
import com.github.dcysteine.neicustomdiagram.api.diagram.component.ItemComponent;
import com.google.common.collect.MultimapBuilder;
import com.google.common.collect.Multimaps;
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
import java.util.SortedSet;

/**
 * Class that finds GregTech tools by looking up recipes, and provides access to them by primary
 * material.
 */
class RecipeHandler {
    /** Comparator so that we can sort tools into a nice ordering. */
    private static final Comparator<ItemComponent> TOOL_COMPARATOR =
            Comparator.<ItemComponent, Integer>comparing(ItemComponent::damage)
                    .thenComparing(component -> component.nbt().map(Object::toString).orElse(null));

    /** Map of material to sorted set of tools with that primary material. */
    private final SortedSetMultimap<Materials, ItemComponent> toolsMultimap;

    /** Map of material to sorted set of Detrav scanners with that primary material. */
    private final SortedSetMultimap<Materials, ItemComponent> scannersMultimap;

    RecipeHandler() {
        this.toolsMultimap = MultimapBuilder.hashKeys().treeSetValues(TOOL_COMPARATOR).build();
        this.scannersMultimap = MultimapBuilder.hashKeys().treeSetValues(TOOL_COMPARATOR).build();
    }

    /** This method must be called before any other methods are called. */
    @SuppressWarnings("unchecked")
    void initialize() {
        ((List<IRecipe>) CraftingManager.getInstance().getRecipeList())
                .forEach(recipe -> addTool(recipe.getRecipeOutput()));

        GT_Recipe.GT_Recipe_Map.sAssemblerRecipes.mRecipeList
                .forEach(recipe -> addTool(recipe.getOutput(0)));
    }

    /** The returned set is immutable! */
    SortedSet<ItemComponent> getTools(Materials material) {
        return Multimaps.unmodifiableSortedSetMultimap(toolsMultimap).get(material);
    }

    /** The returned set is immutable! */
    SortedSet<ItemComponent> getScanners(Materials material) {
        return Multimaps.unmodifiableSortedSetMultimap(scannersMultimap).get(material);
    }

    private void addTool(@Nullable ItemStack itemStack) {
        if (itemStack == null) {
            return;
        }

        if (itemStack.getItem() == GT_MetaGenerated_Tool_01.INSTANCE) {
            toolsMultimap.put(
                    GT_MetaGenerated_Tool.getPrimaryMaterial(itemStack),
                    ItemComponent.createWithNbt(itemStack));
        }

        if (Registry.ModIds.isModLoaded(Registry.ModIds.DETRAV_SCANNER)) {
            if (itemStack.getItem() == DetravMetaGeneratedTool01.INSTANCE) {
                scannersMultimap.put(
                        GT_MetaGenerated_Tool.getPrimaryMaterial(itemStack),
                        ItemComponent.createWithNbt(itemStack));
            }
        }
    }
}
