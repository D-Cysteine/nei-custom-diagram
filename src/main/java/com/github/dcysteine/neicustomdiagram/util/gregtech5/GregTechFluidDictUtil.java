package com.github.dcysteine.neicustomdiagram.util.gregtech5;

import com.github.dcysteine.neicustomdiagram.api.diagram.component.Component;
import com.github.dcysteine.neicustomdiagram.api.diagram.component.FluidComponent;
import com.github.dcysteine.neicustomdiagram.api.diagram.component.ItemComponent;
import com.github.dcysteine.neicustomdiagram.util.FluidDictUtil;
import com.google.common.collect.Lists;
import gregtech.api.enums.ItemList;
import gregtech.api.util.GT_Utility;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidRegistry;

import java.util.ArrayList;
import java.util.List;
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

    /**
     * Just like {@link FluidDictUtil#getFluidContents(Component)}, except this method also checks
     * for GregTech fluid display items.
     */
    public static Optional<FluidComponent> getFluidContents(Component component) {
        if (component.type() == Component.ComponentType.ITEM
                && ((ItemComponent) component).item() == ItemList.Display_Fluid.getItem()) {
            return displayItemToFluid((ItemComponent) component);
        }

        return FluidDictUtil.getFluidContents(component);
    }

    /** Attempts to get the GregTech fluid display item for {@code component}. */
    public static Optional<ItemComponent> getDisplayItem(Component component) {
        return getFluidContents(component).flatMap(GregTechFluidDictUtil::fluidToDisplayItem);
    }

    /**
     * Attempts to fill a cell with the fluid contents of {@code component}.
     *
     * <p>Many of GregTech's utility methods don't work on fluid stacks or GregTech fluid display
     * items, so converting these to a filled cell can make it easier to work with these components.
     */
    public static Optional<ItemComponent> fillCell(Component component) {
        return getFluidContents(component)
                .map(
                        fluidComponent -> GT_Utility.fillFluidContainer(
                                fluidComponent.stack(),
                                ItemList.Cell_Empty.get(1),
                                false, false))
                .map(ItemComponent::create);
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

    /**
     * Returns a list of some fluid-related components associated with {@code component};
     * {@code component} should itself be either a fluid or a fluid-related component.
     *
     * <p>If {@code component} is not fluid-related, returns an empty list. If {@code component} is
     * fluid-related, then it will always be present in the returned list. Use this method when
     * matching diagrams by component.
     *
     * <p>For fluid-related components, the returned list includes, in order (if they exist):
     * <ol>
     *     <li>The fluid.
     *     <li>The block form of the fluid.
     *     <li>The GregTech fluid display item.
     *     <li>A GregTech cell filled with the fluid.
     *     <li>{@code component}, if it is not equal to one of the above.
     * </ol>
     */
    public static List<Component> getAssociatedComponents(Component component) {
        Optional<FluidComponent> fluidOptional = getFluidContents(component);
        if (!fluidOptional.isPresent()) {
            return Lists.newArrayList();
        }

        FluidComponent fluid = fluidOptional.get();
        List<Component> list = new ArrayList<>();
        list.add(fluid);
        FluidDictUtil.itemToFluid(fluid).ifPresent(list::add);
        fluidToDisplayItem(fluid).ifPresent(list::add);
        fillCell(fluid).ifPresent(list::add);

        if (!list.contains(component)) {
            list.add(component);
        }
        return list;
    }
}
