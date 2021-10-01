package com.github.dcysteine.neicustomdiagram.util.gregtech5;

import com.github.dcysteine.neicustomdiagram.mod.config.ConfigOptions;
import gregtech.api.enums.Materials;

public final class GregTechFormatting {
    // Static class.
    private GregTechFormatting() {}

    private static String getMaterialName(Materials material) {
        // TODO maybe we can delete this null check? Fixed by:
        //  https://github.com/GTNewHorizons/bartworks/pull/35
        return material.mLocalizedName.equals("null") ? material.mName : material.mLocalizedName;
    }

    public static String getMaterialDescription(Materials material) {
        if (ConfigOptions.SHOW_IDS.get()) {
            return String.format("%s (#%d)", getMaterialName(material), material.mMetaItemSubID);
        } else {
            return getMaterialName(material);
        }
    }
}
