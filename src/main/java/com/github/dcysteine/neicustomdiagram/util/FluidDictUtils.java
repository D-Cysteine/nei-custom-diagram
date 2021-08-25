package com.github.dcysteine.neicustomdiagram.util;

import com.github.dcysteine.neicustomdiagram.api.Formatter;
import com.github.dcysteine.neicustomdiagram.api.Lang;
import com.github.dcysteine.neicustomdiagram.api.diagram.component.Component;
import com.github.dcysteine.neicustomdiagram.api.diagram.component.DisplayComponent;
import com.github.dcysteine.neicustomdiagram.api.diagram.component.FluidComponent;
import com.github.dcysteine.neicustomdiagram.api.diagram.component.ItemComponent;
import com.github.dcysteine.neicustomdiagram.api.diagram.tooltip.Tooltip;
import net.minecraft.block.Block;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidContainerRegistry;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public final class FluidDictUtils {
    // Static class.
    private FluidDictUtils() {}

    public static Optional<ItemComponent> fluidToItem(Component component) {
        if (component.type() != Component.ComponentType.FLUID) {
            return Optional.empty();
        }

        return ItemComponent.create(((FluidComponent) component).fluid().getBlock(), 0);
    }

    public static Optional<FluidComponent> itemToFluid(Component component) {
        if (component.type() != Component.ComponentType.ITEM) {
            return Optional.empty();
        }

        Block block = Block.getBlockFromItem(((ItemComponent) component).item());
        if (block == null) {
            return Optional.empty();
        }

        return FluidComponent.create(block);
    }

    /**
     * Returns the fluid contained by {@code component}.
     *
     * <p>Has three modes:
     * <ul>
     *     <li>If {@code component} is a fluid, then returns {@code component}.
     *
     *     <li>If {@code component} is an item that is actually a fluid block, then returns the
     *     fluid corresponding to that fluid block.
     *
     *     <li>If {@code component} is an item that contains fluid, then returns the fluid contained
     *     by {@code component}.
     *
     *     <li>Otherwise, returns an empty optional.
     * </ul>
     */
    public static Optional<FluidComponent> getFluidContents(Component component) {
        switch (component.type()) {
            case ITEM:
                Optional<FluidComponent> fluidOptional = itemToFluid(component);
                if (fluidOptional.isPresent()) {
                    return fluidOptional;
                }

                for (FluidContainerRegistry.FluidContainerData data
                        : FluidContainerRegistry.getRegisteredFluidContainerData()) {
                    if (component.equals(ItemComponent.create(data.filledContainer))) {
                        return Optional.of(FluidComponent.create(data.fluid));
                    }
                }
                return Optional.empty();

            case FLUID:
                return Optional.of((FluidComponent) component);
        }

        return Optional.empty();
    }

    /**
     * Returns all fluid containers that contain the same fluid as {@code component}.
     *
     * <p>{@link DisplayComponent} is returned so that fluid container capacities can be shown.
     *
     * <p>Has three modes:
     * <ul>
     *     <li>If {@code component} is a fluid, then returns a list of all fluid containers
     *     containing that fluid, as well as the fluid itself (i.e. {@code component}). The fluid
     *     will be the first element of the list.
     *
     *     <li>If {@code component} is an item that is a fluid container, then returns a list of all
     *     fluid containers containing the same fluid as {@component} (this includes
     *     {@code component}), as well as the fluid itself. The fluid will be the first element of
     *     the list.
     *
     *     <li>Otherwise, returns an empty list.
     * </ul>
     */
    public static List<DisplayComponent> getFluidContainers(Component component) {
        Optional<FluidComponent> fluidOptional = getFluidContents(component);
        List<DisplayComponent> results = new ArrayList<>();

        if (fluidOptional.isPresent()) {
            results.add(DisplayComponent.builder(fluidOptional.get()).build());
            Fluid fluid = fluidOptional.get().fluid();

            for (FluidContainerRegistry.FluidContainerData data
                    : FluidContainerRegistry.getRegisteredFluidContainerData()) {
                if (fluid == data.fluid.getFluid()) {
                    results.add(
                            DisplayComponent.builder(data.filledContainer)
                                    .setStackSize(Optional.empty())
                                    .setAdditionalInfo(Formatter.formatInt(data.fluid.amount))
                                    .setAdditionalTooltip(
                                            Tooltip.create(
                                                    Lang.UTIL.transf("capacity", data.fluid.amount),
                                                    Tooltip.INFO_FORMATTING))
                                    .build());
                }
            }
        }

        return results;
    }
}
