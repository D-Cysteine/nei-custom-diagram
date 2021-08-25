package com.github.dcysteine.neicustomdiagram.util.gregtech;

import com.github.dcysteine.neicustomdiagram.api.diagram.component.Component;
import com.github.dcysteine.neicustomdiagram.api.diagram.component.FluidComponent;
import com.github.dcysteine.neicustomdiagram.api.diagram.component.ItemComponent;
import gregtech.api.enums.ItemList;
import gregtech.api.util.GT_Utility;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidRegistry;

import java.util.Optional;

public final class GregTechFluidDictUtils {
    // Static class.
    private GregTechFluidDictUtils() {}

    public static Optional<ItemComponent> fluidToDisplayItem(Component component) {
        if (component.type() != Component.ComponentType.FLUID) {
            return Optional.empty();
        }

        ItemStack itemStack = GT_Utility.getFluidDisplayStack(((FluidComponent) component).fluid());
        return Optional.ofNullable(itemStack).map(ItemComponent::create);
    }

    public static Optional<FluidComponent> displayItemToFluid(Component component) {
        if (component.type() != Component.ComponentType.ITEM) {
            return Optional.empty();
        }

        ItemComponent itemComponent = (ItemComponent) component;
        if (itemComponent.item() != ItemList.Display_Fluid.getItem()) {
            return Optional.empty();
        }

        Fluid fluid = FluidRegistry.getFluid(itemComponent.damage());
        return Optional.ofNullable(fluid).map(FluidComponent::create);
    }
}
