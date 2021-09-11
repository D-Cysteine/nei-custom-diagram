package com.github.dcysteine.neicustomdiagram.api;

import com.github.dcysteine.neicustomdiagram.api.diagram.DiagramGroupInfo;
import cpw.mods.fml.relauncher.FMLInjectionData;
import net.minecraftforge.common.config.Configuration;

import java.io.File;
import java.util.Arrays;

public final class Config {
    private static final File CONFIG_FILE =
            new File(
                    (File) FMLInjectionData.data()[6],
                    "config" + File.separator + "NEICustomDiagram.cfg");
    private static Configuration config;

    private static final String OPTIONS_CATEGORY = "options";
    private static final String DIAGRAM_GROUP_CATEGORY = "diagram_groups";
    private static final String DIAGRAM_GROUP_COMMENT_FORMAT_STRING =
            "Enables the %s diagram group.";

    public enum Options {
        CTRL_FAST_FORWARD(
                "ctrl_fast_forward", false,
                "Enables fast-forwarding through component groups by holding down <Ctrl>.");

        // TODO add config option to show / hide item IDs / damage?
        // TODO add config option to hide "1" for stack size 1?
        // TODO add config option for smaller resolutions? Will probably need to modify layouts...

        private final String key;
        private final boolean defaultValue;
        private final String comment;

        Options(String key, boolean defaultValue, String comment) {
            this.key = key;
            this.defaultValue = defaultValue;
            this.comment = comment;
        }

        public boolean get() {
            return config.get(
                            OPTIONS_CATEGORY, key, defaultValue,
                            comment + buildDefaultComment(defaultValue))
                    .getBoolean();
        }
    }

    // Static class.
    private Config() {}

    /** This method is only intended to be called during mod initialization. */
    public static void initialize() {
        config = new Configuration(CONFIG_FILE);

        // Load all options, so that they get saved if they're missing from the config.
        Arrays.stream(Options.values()).forEach(Options::get);
        Registry.info().forEach(Config::getDiagramEnabled);
    }

    /** This method is only intended to be called during mod initialization. */
    public static void saveConfig() {
        if (config.hasChanged()) {
            config.save();
        }
    }

    public static boolean getDiagramEnabled(DiagramGroupInfo info) {
        return config.get(
                        DIAGRAM_GROUP_CATEGORY, info.groupId(), info.enabledByDefault(),
                        String.format(DIAGRAM_GROUP_COMMENT_FORMAT_STRING, info.groupName())
                                + buildDefaultComment(info.enabledByDefault()))
                .getBoolean();
    }

    private static String buildDefaultComment(boolean defaultValue) {
        return String.format(" [Default: %s]", defaultValue);
    }
}