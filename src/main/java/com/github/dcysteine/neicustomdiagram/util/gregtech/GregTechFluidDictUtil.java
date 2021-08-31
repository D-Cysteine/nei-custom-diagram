package com.github.dcysteine.neicustomdiagram.util.gregtech;

import com.github.dcysteine.neicustomdiagram.api.diagram.component.Component;
import com.github.dcysteine.neicustomdiagram.api.diagram.component.FluidComponent;
import com.github.dcysteine.neicustomdiagram.api.diagram.component.ItemComponent;
import com.github.dcysteine.neicustomdiagram.util.FluidDictUtil;
import gregtech.api.enums.ItemList;
import gregtech.api.util.GT_Utility;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidRegistry;
import net.minecraftforge.fluids.FluidStack;

import java.util.Optional;

public final class GregTechFluidDictUtil {
    // Static class.
    private GregTechFluidDictUtil() {}

    public static Optional<ItemComponent> fluidToDisplayItem(FluidComponent component) {
        ItemStack itemStack = GT_Utility.getFluidDisplayStack(component.fluid());
        return Optional.ofNullable(itemStack).map(ItemComponent::create);
    }

    public static Optional<FluidComponent> displayItemToFluid(ItemComponent component) {
        if (component.item() != ItemList.Display_Fluid.getItem()) {
            return Optional.empty();
        }

        Fluid fluid = FluidRegistry.getFluid(component.damage());
        return Optional.ofNullable(fluid).map(FluidComponent::create);
    }

    /** Attempts to get the GregTech fluid display item for {@code component}. */
    public static Optional<ItemComponent> getDisplayItem(Component component) {
        switch (component.type()) {
            case ITEM:
                ItemComponent itemComponent = (ItemComponent) component;
                if (itemComponent.item() == ItemList.Display_Fluid.getItem()) {
                    return Optional.of(itemComponent);
                } else {
                    return FluidDictUtil.getFluidContents(component)
                            .flatMap(GregTechFluidDictUtil::fluidToDisplayItem);
                }

            case FLUID:
                return GregTechFluidDictUtil.fluidToDisplayItem((FluidComponent) component);
        }

        return Optional.empty();
    }

    /**
     * Attempts to fill a cell with the fluid contents of {@code component}.
     *
     * <p>Many of GregTech's utility methods don't work on fluid stacks or GregTech fluid display
     * items, so converting these to a filled cell can make it easier to work with these components.
     */
    public static Optional<ItemComponent> fillCell(Component component) {
        switch (component.type()) {
            case ITEM:
                ItemComponent itemComponent = (ItemComponent) component;
                Optional<FluidComponent> fluidOptional;
                if (itemComponent.item() == ItemList.Display_Fluid.getItem()) {
                    fluidOptional = displayItemToFluid(itemComponent);
                } else {
                    fluidOptional = FluidDictUtil.getFluidContents(component);
                }

                return fluidOptional.map(
                        fluidComponent ->
                                GT_Utility.fillFluidContainer(
                                        fluidComponent.stack(),
                                        ItemList.Cell_Empty.get(1),
                                        false, false))
                        .map(ItemComponent::create);

            case FLUID:
                return Optional.ofNullable(
                                GT_Utility.fillFluidContainer(
                                        (FluidStack) component.stack(),
                                        ItemList.Cell_Empty.get(1),
                                        false, false))
                        .map(ItemComponent::create);
        }

        return Optional.empty();
    }

    /**
     * Attempts to convert {@code component} into a GregTech filled cell or fluid display item (in
     * that order), and returns {@code component} if unsuccessful.
     */
    public static Component getCellOrDisplayItem(Component component) {
        return fillCell(component)
                .map(Component.class::cast)
                .orElse(
                        getDisplayItem(component)
                                .map(Component.class::cast)
                                .orElse(component));
    }
}
