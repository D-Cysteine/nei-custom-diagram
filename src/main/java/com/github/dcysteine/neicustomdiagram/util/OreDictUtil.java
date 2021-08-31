package com.github.dcysteine.neicustomdiagram.util;

import codechicken.nei.ItemList;
import com.github.dcysteine.neicustomdiagram.api.diagram.component.Component;
import com.github.dcysteine.neicustomdiagram.api.diagram.component.ItemComponent;
import com.google.common.collect.Lists;
import net.minecraft.item.ItemStack;
import net.minecraftforge.oredict.OreDictionary;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public final class OreDictUtil {
    // Static class.
    private OreDictUtil() {}

    public static List<String> getOreNames(Component component) {
        if (component.type() != Component.ComponentType.ITEM) {
            return Lists.newArrayList();
        }

        return Arrays.stream(OreDictionary.getOreIDs((ItemStack) component.stack()))
                .mapToObj(OreDictionary::getOreName)
                .collect(Collectors.toList());
    }

    public static List<ItemComponent> getComponents(String oreName) {
        // Note: OreDictionary.getOres() returns OreDictionary.UnmodifiableArrayList, whose stream()
        // method is not implemented correctly and returns an empty stream! Don't use it!
        List<ItemComponent> components = new ArrayList<>();
        for (ItemStack itemStack : OreDictionary.getOres(oreName, false)) {
            components.add(ItemComponent.create(itemStack));
        }
        return components;
    }

    /**
     * If {@component} is an {@link ItemComponent} with {@link ItemComponent#hasWildcardDamage()}
     * equal to {@code true}, then returns a list of all valid item damage permutations of that
     * item; otherwise, returns a list containing just {@code component}.
     */
    public static List<Component> getPermutations(Component component) {
        if (component.type() != Component.ComponentType.ITEM) {
            return Lists.newArrayList(component);
        }

        ItemComponent itemComponent = (ItemComponent) component;
        if (itemComponent.hasWildcardDamage()) {
            return ItemList.itemMap.get(itemComponent.item()).stream()
                    .map(ItemComponent::create)
                    .collect(Collectors.toList());
        } else {
            return Lists.newArrayList(component);
        }
    }
}
