package com.github.dcysteine.neicustomdiagram.util.gregtech;

import gregtech.api.enums.Materials;

public final class GregTechFormatting {
    // Static class.
    private GregTechFormatting() {}

    public static String getMaterialName(Materials material) {
        return material.mLocalizedName.equals("null") ? material.mName : material.mLocalizedName;
    }

    public static String getMaterialDescription(Materials material) {
        return String.format("%s (#%d)", getMaterialName(material), material.mMetaItemSubID);
    }
}
