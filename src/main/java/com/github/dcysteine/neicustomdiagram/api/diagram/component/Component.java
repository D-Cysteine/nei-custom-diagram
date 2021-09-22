package com.github.dcysteine.neicustomdiagram.api.diagram.component;

import com.github.dcysteine.neicustomdiagram.api.diagram.interactable.Interactable;
import com.github.dcysteine.neicustomdiagram.api.draw.Point;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidStack;

import java.util.Optional;

/**
 * Interface to allow us to handle both items and fluids.
 *
 * <p>Each {@code Component} contains either an {@link Item} or a {@link Fluid}. This class is
 * intended to help with comparing different types of items and fluids, ignoring concrete details
 * such as stack size. NBT is however included in this class, as it is sometimes a concrete detail
 * (e.g. enchantments) and sometimes part of the definition of an item type.
 */
public interface Component extends Comparable<Component> {
    enum ComponentType {
        ITEM, FLUID;
    }

    ComponentType type();
    Optional<ImmutableNbtWrapper> nbtWrapper();

    default Optional<NBTTagCompound> nbt() {
        return nbtWrapper().map(ImmutableNbtWrapper::get);
    }

    Component withNbt(NBTTagCompound nbt);
    Component withoutNbt();

    /** Returns an {@link ItemStack} or {@link FluidStack}, depending on the component type. */
    Object stack();

    /**
     * Returns an {@link ItemStack} or {@link FluidStack} with the specified stack size, depending
     * on the component type.
     */
    Object stack(int stackSize);

    /**
     * Returns an {@link ItemStack} or {@link FluidStack} with the specified stack size and NBT,
     * depending on the component type.
     */
    default Object stack(Optional<Integer> stackSize) {
        if (stackSize.isPresent()) {
            return stack(stackSize.get());
        } else {
            return stack();
        }
    }

    /** Returns a localized description of the item or fluid, for printing as text. */
    String description();

    void interact(Interactable.RecipeType recipeType);
    void draw(Point pos);
}
