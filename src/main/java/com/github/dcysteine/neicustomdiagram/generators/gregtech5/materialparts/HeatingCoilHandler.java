package com.github.dcysteine.neicustomdiagram.generators.gregtech5.materialparts;

import com.github.dcysteine.neicustomdiagram.api.diagram.component.Component;
import com.github.dcysteine.neicustomdiagram.api.diagram.component.ItemComponent;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSortedMap;
import gregtech.api.enums.ItemList;
import gregtech.common.blocks.GT_Block_Casings5;
import net.minecraft.item.ItemStack;

class HeatingCoilHandler {
    /** List of all heating coil items in GregTech. Unfortunately, must be manually updated. */
    private static final ImmutableList<ItemList> HEATING_COILS =
            ImmutableList.of(
                    ItemList.Casing_Coil_Cupronickel,
                    ItemList.Casing_Coil_Kanthal,
                    ItemList.Casing_Coil_Nichrome,
                    ItemList.Casing_Coil_TungstenSteel,
                    ItemList.Casing_Coil_HSSG,
                    ItemList.Casing_Coil_Naquadah,
                    ItemList.Casing_Coil_NaquadahAlloy,
                    ItemList.Casing_Coil_ElectrumFlux,
                    ItemList.Casing_Coil_AwakenedDraconium,
                    ItemList.Casing_Coil_HSSS,
                    ItemList.Casing_Coil_Trinium,
                    ItemList.Casing_Coil_Infinity,
                    ItemList.Casing_Coil_Hypogen,
                    ItemList.Casing_Coil_Eternal);

    /** Sorted map of blast furnace recipe heat to heating coil item. */
    private ImmutableSortedMap<Long, Component> heatingCoilMap;

    /** This method must be called before any other methods are called. */
    void initialize() {
        ImmutableSortedMap.Builder<Long, Component> builder = ImmutableSortedMap.naturalOrder();
        for (ItemList item : HEATING_COILS) {
            ItemStack itemStack = item.get(1);
            long heat =
                    GT_Block_Casings5.getCoilHeatFromDamage(itemStack.getItemDamage()).getHeat();

            builder.put(heat, ItemComponent.create(itemStack));
        }
        heatingCoilMap = builder.build();
    }

    /**
     * Returns a sorted map of heating coils that support the specified heat level.
     *
     * <p>The returned map is a map of heating coil base heat capacity to heating coil.
     */
    ImmutableSortedMap<Long, Component> getHeatingCoils(long heat) {
        return heatingCoilMap.tailMap(heat, true);
    }
}
