package com.github.dcysteine.neicustomdiagram.main;

import net.minecraft.util.StatCollector;

/** Class that provides some convenience methods for getting translated strings. */
public final class Lang {
    public static final Lang API = new Lang("neicustomdiagram.api.");
    public static final Lang UTIL = new Lang("neicustomdiagram.util.");

    public static final Lang GREGTECH_5_UTIL = new Lang("neicustomdiagram.util.gregtech5.");

    public static final Lang DEBUG_RULER = new Lang("neicustomdiagram.generators.debug.ruler.");

    public static final Lang ENDER_STORAGE_CHEST_OVERVIEW =
            new Lang("neicustomdiagram.generators.enderstorage.chestoverview.");
    public static final Lang ENDER_STORAGE_TANK_OVERVIEW =
            new Lang("neicustomdiagram.generators.enderstorage.tankoverview.");

    public static final Lang FORGE_FLUID_CONTAINERS =
            new Lang("neicustomdiagram.generators.forge.fluidcontainers.");
    public static final Lang FORGE_ORE_DICTIONARY =
            new Lang("neicustomdiagram.generators.forge.oredictionary.");

    public static final Lang GREGTECH_5_CIRCUITS =
            new Lang("neicustomdiagram.generators.gregtech5.circuits.");
    public static final Lang GREGTECH_5_LENSES =
            new Lang("neicustomdiagram.generators.gregtech5.lenses.");
    public static final Lang GREGTECH_5_MATERIAL_PARTS =
            new Lang("neicustomdiagram.generators.gregtech5.materialparts.");
    public static final Lang GREGTECH_5_MATERIAL_TOOLS =
            new Lang("neicustomdiagram.generators.gregtech5.materialtools.");
    public static final Lang GREGTECH_5_ORE_DICTIONARY =
            new Lang("neicustomdiagram.generators.gregtech5.oredictionary.");
    public static final Lang GREGTECH_5_ORE_PREFIXES =
            new Lang("neicustomdiagram.generators.gregtech5.oreprefixes.");
    public static final Lang GREGTECH_5_ORE_PROCESSING =
            new Lang("neicustomdiagram.generators.gregtech5.oreprocessing.");
    public static final Lang GREGTECH_5_RECIPE_DEBUGGER =
            new Lang("neicustomdiagram.generators.gregtech5.recipedebugger.");

    private final String prefix;

    private Lang(String prefix) {
        this.prefix = prefix;
    }

    public String trans(String key) {
        return StatCollector.translateToLocal(prefix + key);
    }

    public String transf(String key, Object... args) {
        return StatCollector.translateToLocalFormatted(prefix + key, args);
    }
}
