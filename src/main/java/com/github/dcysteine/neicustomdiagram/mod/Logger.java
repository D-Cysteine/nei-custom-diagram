package com.github.dcysteine.neicustomdiagram.mod;

import com.github.dcysteine.neicustomdiagram.NeiCustomDiagram;
import org.apache.logging.log4j.LogManager;

/** Class that provides convenient access to loggers. */
public final class Logger {
    public static final org.apache.logging.log4j.Logger MOD =
            LogManager.getLogger(NeiCustomDiagram.MOD_NAME);

    public static final org.apache.logging.log4j.Logger GREGTECH_5_CIRCUITS =
            LogManager.getLogger(buildGroupName(Lang.GREGTECH_5_CIRCUITS));
    public static final org.apache.logging.log4j.Logger GREGTECH_5_LENSES =
            LogManager.getLogger(buildGroupName(Lang.GREGTECH_5_LENSES));
    public static final org.apache.logging.log4j.Logger GREGTECH_5_MATERIAL_PARTS =
            LogManager.getLogger(buildGroupName(Lang.GREGTECH_5_MATERIAL_PARTS));
    public static final org.apache.logging.log4j.Logger GREGTECH_5_MATERIAL_TOOLS =
            LogManager.getLogger(buildGroupName(Lang.GREGTECH_5_MATERIAL_TOOLS));
    public static final org.apache.logging.log4j.Logger GREGTECH_5_ORE_PREFIXES =
            LogManager.getLogger(buildGroupName(Lang.GREGTECH_5_ORE_PREFIXES));
    public static final org.apache.logging.log4j.Logger GREGTECH_5_ORE_PROCESSING =
            LogManager.getLogger(buildGroupName(Lang.GREGTECH_5_ORE_PROCESSING));
    public static final org.apache.logging.log4j.Logger GREGTECH_5_RECIPE_DEBUGGER =
            LogManager.getLogger(buildGroupName(Lang.GREGTECH_5_RECIPE_DEBUGGER));

    private static String buildGroupName(Lang lang) {
        return NeiCustomDiagram.MOD_NAME + "/" + lang.trans("groupname");
    }

    // Static class.
    private Logger() {}
}
