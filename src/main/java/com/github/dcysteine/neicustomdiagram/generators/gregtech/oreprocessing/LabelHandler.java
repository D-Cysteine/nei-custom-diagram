package com.github.dcysteine.neicustomdiagram.generators.gregtech.oreprocessing;

import com.github.dcysteine.neicustomdiagram.api.Lang;
import com.github.dcysteine.neicustomdiagram.api.diagram.component.Component;
import com.github.dcysteine.neicustomdiagram.api.diagram.interactable.CustomInteractable;
import com.github.dcysteine.neicustomdiagram.api.diagram.layout.ComponentLabel;
import com.github.dcysteine.neicustomdiagram.api.diagram.tooltip.Tooltip;
import com.github.dcysteine.neicustomdiagram.api.draw.Point;
import com.github.dcysteine.neicustomdiagram.util.gregtech.GregTechOreDictUtil;
import gregtech.api.enums.ItemList;

import java.util.EnumMap;

class LabelHandler {
    enum ItemLabel {
        MACERATOR(ItemList.Machine_HV_Macerator, "maceratorlabel"),
        ORE_WASHER(ItemList.Machine_HV_OreWasher, "orewasherlabel"),
        CENTRIFUGE(ItemList.Machine_HV_Centrifuge, "centrifugelabel"),
        THERMAL_CENTRIFUGE(ItemList.Machine_HV_ThermalCentrifuge, "thermalcentrifugelabel"),
        SIFTER(ItemList.Machine_HV_Sifter, "sifterlabel"),
        ELECTROMAGNETIC_SEPARATOR(
                ItemList.Machine_HV_ElectromagneticSeparator, "electromagneticseparatorlabel"),

        FURNACE(ItemList.Machine_HV_E_Furnace, "furnacelabel"),
        ELECTRIC_BLAST_FURNACE(ItemList.Machine_Multi_BlastFurnace, "ebflabel"),
        CHEMICAL_BATH(ItemList.Machine_HV_ChemicalBath, "chemicalbathlabel"),
        CHEMICAL_REACTOR(ItemList.Machine_HV_ChemicalReactor, "chemicalreactorlabel"),
        AUTOCLAVE(ItemList.Machine_HV_Autoclave, "autoclavelabel");

        final ItemList item;
        final String tooltipKey;

        ItemLabel(ItemList item, String tooltipKey) {
            this.item = item;
            this.tooltipKey = tooltipKey;
        }
    }

    private final EnumMap<ItemLabel, Component> componentMap;

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
                        Lang.GREGTECH_ORE_PROCESSING.trans(itemLabel.tooltipKey),
                        Tooltip.INFO_FORMATTING);

        return CustomInteractable.builder(label)
                .setTooltip(tooltip)
                .build();
    }
}
