package com.github.dcysteine.neicustomdiagram.main.config;

import com.github.dcysteine.neicustomdiagram.api.diagram.DiagramGroupInfo;
import com.github.dcysteine.neicustomdiagram.main.Logger;
import cpw.mods.fml.relauncher.FMLInjectionData;
import net.minecraftforge.common.config.Configuration;

import java.io.File;
import java.util.List;

public final class Config {
    static final File CONFIG_FILE =
            new File(
                    (File) FMLInjectionData.data()[6],
                    "config" + File.separator + "NEICustomDiagram.cfg");
    static Configuration CONFIG = new Configuration(CONFIG_FILE);

    // Static class.
    private Config() {}

    /** This method is only intended to be called during mod initialization. */
    public static void initialize() {
        // Load all options, so that they get saved if they're missing from the config.
        ConfigOptions.getAllOptions().forEach(ConfigOptions.Option::initialize);
        ConfigOptions.setCategoryComments();
    }

    /** This method is only intended to be called during mod initialization. */
    public static void initializeDiagramGroupVisibility(List<DiagramGroupInfo> infoList) {
        // Load diagram group visibility options, so that they get saved if they're missing from the
        // config.
        infoList.forEach(ConfigOptions::getDiagramGroupVisibility);
    }

    public static void saveConfig() {
        if (CONFIG.hasChanged()) {
            CONFIG.save();
            Logger.MOD.warn("Found changed config options! Config file has been updated.");
        }
    }

    static String getConfigFilePath() {
        return CONFIG_FILE.getAbsolutePath();
    }
}