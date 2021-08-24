package com.github.dcysteine.neicustomdiagram.impl.gregtech.common;

import com.github.bartimaeusnek.bartworks.system.material.Werkstoff;
import com.github.bartimaeusnek.bartworks.system.material.WerkstoffLoader;
import com.github.dcysteine.neicustomdiagram.api.diagram.component.Component;
import com.github.dcysteine.neicustomdiagram.api.diagram.component.ItemComponent;
import gregtech.api.enums.ItemList;
import gregtech.api.enums.Materials;
import gregtech.api.enums.OrePrefixes;
import gregtech.api.objects.ItemData;
import gregtech.api.util.GT_OreDictUnificator;
import net.minecraft.item.ItemStack;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public final class GregTechOreDictUtils {
    // Static class.
    private GregTechOreDictUtils() {}

    public static ItemComponent getComponent(ItemList item) {
        return ItemComponent.create(item.get(1));
    }

    public static Optional<ItemComponent> getComponent(OrePrefixes prefix, Materials material) {
        Optional<ItemStack> itemStackOptional =
                Optional.ofNullable(GT_OreDictUnificator.get(prefix, material, 1));
        return itemStackOptional.map(ItemComponent::create);
    }

    public static List<Component> getAllComponents(OrePrefixes prefix, Materials material) {
        List<ItemStack> itemStacks = GT_OreDictUnificator.getOres(prefix, material);
        return itemStacks.stream().map(ItemComponent::create).collect(Collectors.toList());
    }

    /** BartWorks method. */
    public static Optional<ItemComponent> getComponent(OrePrefixes prefix, Werkstoff werkstoff) {
        if (!werkstoff.hasGenerationFeature(prefix)) {
            return Optional.empty();
        }

        Optional<ItemStack> itemStackOptional =
                Optional.ofNullable(
                        WerkstoffLoader.getCorrespondingItemStackUnsafe(prefix, werkstoff, 1));
        return itemStackOptional.map(ItemComponent::create);
    }

    /**
     * Returns the unified version of {@code component}, or just returns {@code component} if it
     * couldn't be unified or isn't an item.
     */
    public static Component unify(Component component) {
        if (component.type() != Component.ComponentType.ITEM) {
            return component;
        }

        return ItemComponent.create(GT_OreDictUnificator.get_nocopy((ItemStack) component.stack()));
    }

    /** Returns a list of everything that unifies into {@code component}. */
    public static List<Component> reverseUnify(Component component) {
        Component unified = unify(component);
        List<Component> results = new ArrayList<>();

        if (unified.type() == Component.ComponentType.ITEM) {
            GT_OreDictUnificator.getNonUnifiedStacks((ItemStack) unified.stack()).forEach(
                    itemStack -> results.add(ItemComponent.create(itemStack)));
        } else {
            results.add(unified);
        }

        return results;
    }

    public static Optional<ItemData> getItemData(Component component) {
        if (component.type() != Component.ComponentType.ITEM) {
            return Optional.empty();
        }
        return Optional.ofNullable(
                GT_OreDictUnificator.getAssociation((ItemStack) component.stack()));
    }

    /**
     * Returns a list of GregTech associated items for {@code component} (including
     * {@code component} itself), or a list containing just {@code component} if it has no GregTech
     * associated items.
     *
     * <p>Use this method when matching diagrams by component.
     */
    public static List<Component> getAssociatedComponents(Component component) {
        Optional<ItemData> itemDataOptional = getItemData(component);
        List<Component> results = new ArrayList<>(reverseUnify(component));

        if (itemDataOptional.isPresent()) {
            ItemData itemData = itemDataOptional.get();
            itemData.mPrefix.mFamiliarPrefixes.forEach(
                    prefix ->
                            getComponent(prefix, itemData.mMaterial.mMaterial)
                                    .ifPresent(c -> results.addAll(reverseUnify(c))));
        }

        return results;
    }
}
