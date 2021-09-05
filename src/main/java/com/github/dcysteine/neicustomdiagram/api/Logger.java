package com.github.dcysteine.neicustomdiagram.api;

import com.github.dcysteine.neicustomdiagram.NeiCustomDiagram;
import org.apache.logging.log4j.LogManager;

/** Class that provides convenient access to loggers. */
public final class Logger {
    public static final org.apache.logging.log4j.Logger MOD =
            LogManager.getLogger(NeiCustomDiagram.MOD_NAME);
    public static final org.apache.logging.log4j.Logger GREGTECH_MATERIAL_PARTS =
            LogManager.getLogger(buildGroupName(Lang.GREGTECH_MATERIAL_PARTS));
    public static final org.apache.logging.log4j.Logger GREGTECH_ORE_PROCESSING =
            LogManager.getLogger(buildGroupName(Lang.GREGTECH_ORE_PROCESSING));

    private static String buildGroupName(Lang lang) {
        return NeiCustomDiagram.MOD_NAME + "/" + lang.trans("groupname");
    }

    // Static class.
    private Logger() {}
}
