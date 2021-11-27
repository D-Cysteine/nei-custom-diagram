package com.github.dcysteine.neicustomdiagram.main.config;

import codechicken.nei.NEIClientUtils;
import com.github.dcysteine.neicustomdiagram.main.Logger;

/** Contains options for controlling when a diagram group is shown. */
public enum DiagramGroupVisibility {
    ALWAYS_SHOWN {
        @Override
        public boolean isShown() {
            return true;
        }
    },

    ALWAYS_HIDDEN {
        @Override
        public boolean isShown() {
            return false;
        }
    },

    /**
     * Use this option for diagram groups that you only occasionally want to see.
     */
    SHOW_ON_SHIFT {
        @Override
        public boolean isShown() {
            return NEIClientUtils.shiftKey();
        }
    },

    /**
     * Use this option for diagrams where you sometimes want to jump straight to other NEI tabs when
     * looking up a recipe. Holding {@code <Shift>} will then hide the diagram group, causing NEI to
     * show you a different NEI tab.
     */
    HIDE_ON_SHIFT {
        @Override
        public boolean isShown() {
            return !NEIClientUtils.shiftKey();
        }
    },

    /**
     * Use this option for diagram groups that you only occasionally want to see.
     */
    SHOW_ON_CTRL {
        @Override
        public boolean isShown() {
            return NEIClientUtils.controlKey();
        }
    },

    HIDE_ON_CTRL {
        @Override
        public boolean isShown() {
            return !NEIClientUtils.controlKey();
        }
    },

    /**
     * This is a special value which will not only hide the diagram group, but also skip
     * initialization of the diagram group.
     *
     * <p>Use this option to save CPU and RAM usage by not generating diagram groups that you don't
     * care about. Because this option affects mod initialization, changing from it requires a
     * restart of <em>Minecraft</em> to take effect. Changing to it will hide the diagram group, but
     * the group will remain in memory until <em>Minecraft</em> is restarted.
     */
    DISABLED {
        @Override
        public boolean isShown() {
            return false;
        }
    };

    /** Returns whether this diagram group should be shown. */
    public abstract boolean isShown();

    /**
     * Safer version of {@link #valueOf(String)}, which will return a default value instead of
     * throwing.
     */
    public static DiagramGroupVisibility getByName(String name) {
        try {
            return valueOf(name);
        } catch (IllegalArgumentException e) {
            Logger.MOD.error(
                    "Could not find DiagramGroupVisibility: [{}]. Falling back to ALWAYS_SHOWN.",
                    name);

            return ALWAYS_SHOWN;
        }
    }
}
