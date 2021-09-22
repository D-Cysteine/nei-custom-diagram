package com.github.dcysteine.neicustomdiagram.mod.config;

import com.github.dcysteine.neicustomdiagram.mod.Logger;
import com.github.dcysteine.neicustomdiagram.mod.Registry;
import cpw.mods.fml.relauncher.FMLInjectionData;
import net.minecraftforge.common.config.Configuration;

import java.io.File;

public final class Config {
    private static final File CONFIG_FILE =
            new File(
                    (File) FMLInjectionData.data()[6],
                    "config" + File.separator + "NEICustomDiagram.cfg");
    private static Configuration config;

    // Static class.
    private Config() {}

    /** This method is only intended to be called during mod initialization. */
    public static void initialize() {
        config = new Configuration(CONFIG_FILE);

        // Load all options, so that they get saved if they're missing from the config.
        ConfigOptions.getAllOptions().forEach(option -> option.initialize(config));
        Registry.info().forEach(ConfigOptions::getDiagramGroupVisibility);
        ConfigOptions.setCategoryComments(config);

        if (config.hasChanged()) {
            config.save();
            Logger.MOD.warn("Found changed config options! Config file has been updated.");
        }
    }

    static Configuration getConfig() {
        return config;
    }

    static void saveConfig() {
        if (config.hasChanged()) {
            config.save();
        }
    }

    static String getConfigFilePath() {
        return CONFIG_FILE.getAbsolutePath();
    }
}