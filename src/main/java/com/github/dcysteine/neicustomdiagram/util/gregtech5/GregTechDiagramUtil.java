package com.github.dcysteine.neicustomdiagram.util.gregtech5;

import com.github.dcysteine.neicustomdiagram.api.diagram.component.ItemComponent;
import com.github.dcysteine.neicustomdiagram.api.diagram.interactable.CustomInteractable;
import com.github.dcysteine.neicustomdiagram.api.diagram.interactable.Interactable;
import com.github.dcysteine.neicustomdiagram.api.diagram.layout.ComponentLabel;
import com.github.dcysteine.neicustomdiagram.api.diagram.tooltip.Tooltip;
import com.github.dcysteine.neicustomdiagram.api.draw.Point;
import com.github.dcysteine.neicustomdiagram.mod.Lang;
import gregtech.api.enums.Materials;
import net.minecraft.init.Items;

public final class GregTechDiagramUtil {
    public static final ItemComponent ICON = ItemComponent.create(Items.book, 0);

    // Static class.
    private GregTechDiagramUtil() {}

    public static Interactable buildMaterialInfoButton(Point pos, Materials material) {
        Tooltip.Builder tooltipBuilder =
                Tooltip.builder()
                        .addTextLine(GregTechFormatting.getMaterialDescription(material))
                        .setFormatting(Tooltip.INFO_FORMATTING)
                        .addTextLine(material.mChemicalFormula);

        if (material.isRadioactive() || material.mHeatDamage != 0) {
            tooltipBuilder.addSpacing().setFormatting(Tooltip.URGENT_FORMATTING);

            if (material.isRadioactive()) {
                tooltipBuilder.addTextLine(
                        Lang.GREGTECH_5_UTIL.trans("materialinforadioactive"));
            }
            if (material.mHeatDamage > 0) {
                tooltipBuilder.addTextLine(
                        Lang.GREGTECH_5_UTIL.trans("materialinfohot"));
            } else if (material.mHeatDamage < 0) {
                tooltipBuilder.addTextLine(
                        Lang.GREGTECH_5_UTIL.trans("materialinfocold"));
            }
        }

        return CustomInteractable.builder(ComponentLabel.create(ICON, pos))
                .setTooltip(tooltipBuilder.build())
                .build();
    }
}
