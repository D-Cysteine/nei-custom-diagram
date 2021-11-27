package com.github.dcysteine.neicustomdiagram.generators.gregtech5.circuits;

import com.github.dcysteine.neicustomdiagram.api.diagram.component.ItemComponent;
import com.github.dcysteine.neicustomdiagram.api.diagram.interactable.CustomInteractable;
import com.github.dcysteine.neicustomdiagram.api.diagram.layout.ComponentLabel;
import com.github.dcysteine.neicustomdiagram.api.diagram.tooltip.Tooltip;
import com.github.dcysteine.neicustomdiagram.api.draw.Point;
import com.github.dcysteine.neicustomdiagram.main.Lang;
import com.github.dcysteine.neicustomdiagram.util.gregtech5.GregTechOreDictUtil;
import gregtech.api.enums.ItemList;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;

import java.util.EnumMap;
import java.util.function.Supplier;

class LabelHandler {
    enum ItemLabel {
        CRAFTING_TABLE(
                () -> ItemComponent.create(Blocks.crafting_table, 0).get(),
                "craftingtablelabel"),
        ASSEMBLING_MACHINE(
                () -> GregTechOreDictUtil.getComponent(ItemList.Machine_HV_Assembler),
                "assemblingmachinelabel"),
        ASSEMBLING_LINE(
                () -> GregTechOreDictUtil.getComponent(ItemList.Machine_Multi_Assemblyline),
                "assemblinglinelabel"),
        CIRCUIT_ASSEMBLING_MACHINE(
                () -> GregTechOreDictUtil.getComponent(ItemList.Machine_HV_CircuitAssembler),
                "circuitassemblingmachinelabel"),
        CLEAN_ROOM(
                () -> GregTechOreDictUtil.getComponent(ItemList.Machine_Multi_Cleanroom),
                "requirescleanroomlabel"),
        LOW_GRAVITY(() -> ItemComponent.create(Items.feather, 0), "requireslowgravitylabel");

        final Supplier<ItemComponent> itemComponentSupplier;
        final String tooltipKey;

        ItemLabel(Supplier<ItemComponent> itemComponentSupplier, String tooltipKey) {
            this.itemComponentSupplier = itemComponentSupplier;
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
            componentMap.put(itemLabel, itemLabel.itemComponentSupplier.get());
        }
    }

    CustomInteractable buildLabel(ItemLabel itemLabel, Point pos) {
        ComponentLabel label = ComponentLabel.create(componentMap.get(itemLabel), pos);
        Tooltip tooltip =
                Tooltip.create(
                        Lang.GREGTECH_5_CIRCUITS.trans(itemLabel.tooltipKey),
                        Tooltip.INFO_FORMATTING);

        return CustomInteractable.builder(label)
                .setTooltip(tooltip)
                .build();
    }
}
