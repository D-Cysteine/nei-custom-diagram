package com.github.dcysteine.neicustomdiagram.util;

import com.github.dcysteine.neicustomdiagram.api.Formatter;
import com.github.dcysteine.neicustomdiagram.api.diagram.component.Component;
import com.github.dcysteine.neicustomdiagram.api.diagram.component.DisplayComponent;
import com.github.dcysteine.neicustomdiagram.api.diagram.component.FluidComponent;
import com.github.dcysteine.neicustomdiagram.api.diagram.component.ItemComponent;
import com.github.dcysteine.neicustomdiagram.api.diagram.tooltip.Tooltip;
import com.github.dcysteine.neicustomdiagram.mod.Lang;
import com.google.common.base.Suppliers;
import com.google.common.collect.ImmutableList;
import net.minecraft.block.Block;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidContainerRegistry;
import net.minecraftforge.fluids.FluidStack;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Supplier;

public final class FluidDictUtil {
    // TODO if we need more memoization, move this to a Memoized class or something
    /**
     * Memoize the fluid container data, because
     * {@link FluidContainerRegistry#getRegisteredFluidContainerData()} makes a copy every time.
     */
    public static final Supplier<ImmutableList<FluidContainerRegistry.FluidContainerData>>
            FORGE_FLUID_CONTAINER_DATA_SUPPLIER =
            () -> Suppliers.memoize(
                            () -> ImmutableList.copyOf(
                                    FluidContainerRegistry.getRegisteredFluidContainerData()))
                    .get();

    // Static class.
    private FluidDictUtil() {}

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

                FluidStack containedFluid =
                        FluidContainerRegistry.getFluidForFilledItem((ItemStack) component.stack());
                if (containedFluid != null) {
                    return Optional.of(FluidComponent.create(containedFluid));
                }

                return Optional.empty();

            case FLUID:
                return Optional.of((FluidComponent) component);
        }

        return Optional.empty();
    }

    /**
     * Returns a {@link DisplayComponent} for the given fluid container data, complete with
     * capacity, contained fluid, and empty container tooltips.
     */
    public static DisplayComponent displayFluidContainer(
            FluidContainerRegistry.FluidContainerData data) {
        int capacity = data.fluid.amount;
        return DisplayComponent.builder(data.filledContainer)
                .clearStackSize()
                .setAdditionalInfo(Formatter.smartFormatInteger(capacity))
                .setAdditionalTooltip(
                        Tooltip.builder()
                                .setFormatting(Tooltip.INFO_FORMATTING)
                                .addTextLine(
                                        Lang.UTIL.transf("capacity", capacity))
                                .addSpacing()
                                .addTextLine(Lang.UTIL.transf("fluidcontainercontents"))
                                .addDisplayComponent(DisplayComponent.builder(data.fluid).build())
                                .addSpacing()
                                .addTextLine(Lang.UTIL.transf("emptyfluidcontainer"))
                                .addComponent(ItemComponent.create(data.emptyContainer))
                                .build())
                .build();
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
                    : FORGE_FLUID_CONTAINER_DATA_SUPPLIER.get()) {
                if (fluid == data.fluid.getFluid()) {
                    results.add(displayFluidContainer(data));
                }
            }
        }

        return results;
    }

    /**
     * Returns the empty fluid container for {@code component}.
     *
     * <p>Has three modes:
     * <ul>
     *     <li>If {@code component} is a filled fluid container, then returns the empty fluid
     *     container.
     *
     *     <li>If {@code component} is an empty fluid container, then returns {@code component}.
     *
     *     <li>Otherwise, returns an empty optional.
     * </ul>
     */
    public static Optional<ItemComponent> getEmptyContainer(Component component) {
        if (component.type() != Component.ComponentType.ITEM) {
            return Optional.empty();
        }

        ItemComponent itemComponent = (ItemComponent) component;
        ItemStack itemStack = itemComponent.stack();
        if (FluidContainerRegistry.isEmptyContainer(itemStack)) {
            return Optional.of(itemComponent);
        } else {
            ItemStack emptyContainer = FluidContainerRegistry.drainFluidContainer(itemStack);
            return Optional.ofNullable(emptyContainer).map(ItemComponent::create);
        }
    }
}
