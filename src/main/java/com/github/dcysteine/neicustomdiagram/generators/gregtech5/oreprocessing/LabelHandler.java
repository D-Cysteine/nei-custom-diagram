package com.github.dcysteine.neicustomdiagram.generators.gregtech5.oreprocessing;

import com.github.dcysteine.neicustomdiagram.api.diagram.component.ItemComponent;
import com.github.dcysteine.neicustomdiagram.api.diagram.interactable.CustomInteractable;
import com.github.dcysteine.neicustomdiagram.api.diagram.layout.ComponentLabel;
import com.github.dcysteine.neicustomdiagram.api.diagram.tooltip.Tooltip;
import com.github.dcysteine.neicustomdiagram.api.draw.Point;
import com.github.dcysteine.neicustomdiagram.mod.Lang;
import com.github.dcysteine.neicustomdiagram.util.gregtech5.GregTechOreDictUtil;
import gregtech.api.enums.ItemList;

import java.util.EnumMap;

class LabelHandler {
    enum ItemLabel {
        MACERATOR(ItemList.Machine_HV_Macerator, "maceratorlabel"),
        ORE_WASHING_PLANT(ItemList.Machine_HV_OreWasher, "orewashingplantlabel"),
        CHEMICAL_BATH(ItemList.Machine_HV_ChemicalBath, "chemicalbathlabel"),
        CENTRIFUGE(ItemList.Machine_HV_Centrifuge, "centrifugelabel"),
        SIFTER(ItemList.Machine_HV_Sifter, "sifterlabel"),
        ELECTROMAGNETIC_SEPARATOR(
                ItemList.Machine_HV_ElectromagneticSeparator, "electromagneticseparatorlabel"),
        THERMAL_CENTRIFUGE(ItemList.Machine_HV_ThermalCentrifuge, "thermalcentrifugelabel"),

        FURNACE(ItemList.Machine_HV_E_Furnace, "furnacelabel"),
        ELECTRIC_BLAST_FURNACE(ItemList.Machine_Multi_BlastFurnace, "electricblastfurnacelabel"),
        CHEMICAL_REACTOR(ItemList.Machine_HV_ChemicalReactor, "chemicalreactorlabel"),
        AUTOCLAVE(ItemList.Machine_HV_Autoclave, "autoclavelabel");

        final ItemList item;
        final String tooltipKey;

        ItemLabel(ItemList item, String tooltipKey) {
            this.item = item;
            this.tooltipKey = tooltipKey;
        }
    }

    private final EnumMap<ItemLabel, ItemComponent> componentMap;

    LabelHandler() {
        componentMap = new EnumMap<>(ItemLabel.class);
    }

    /** This method must be called before any other methods are called. */
    void initialize() {
        for (ItemLabel itemLabel : ItemLabel.values()) {
            componentMap.put(itemLabel, GregTechOreDictUtil.getComponent(itemLabel.item));
        }
    }

    CustomInteractable buildLabel(ItemLabel itemLabel, Point pos) {
        ComponentLabel label = ComponentLabel.create(componentMap.get(itemLabel), pos);
        Tooltip tooltip =
                Tooltip.create(
                        Lang.GREGTECH_5_ORE_PROCESSING.trans(itemLabel.tooltipKey),
                        Tooltip.INFO_FORMATTING);

        return CustomInteractable.builder(label)
                .setTooltip(tooltip)
                .build();
    }
}
