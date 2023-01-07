package com.github.dcysteine.neicustomdiagram.main.config;

import com.github.dcysteine.neicustomdiagram.api.diagram.DiagramGroupInfo;
import com.github.dcysteine.neicustomdiagram.main.Logger;
import com.google.common.collect.ImmutableList;
import net.minecraftforge.common.config.Property;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Supplier;

public final class ConfigOptions {
    private static final List<Option<?>> allOptions = new ArrayList<>();

    public static final Option<Boolean> CTRL_FAST_FORWARD =
            new BooleanOption(
                    Category.OPTIONS, "ctrl_fast_forward", true,
                    "Enables fast-forwarding through component cycles by holding down <Ctrl>."
                            + "\nFast-forward backwards with <Ctrl + Shift>.")
                    .register();

    public static final Option<Boolean> DISABLE_PAGE_SCROLL =
            new BooleanOption(
                    Category.OPTIONS, "disable_page_scroll", false,
                    "The default behavior is that if a diagram is too large to fit,"
                            + " scrolling will scroll the diagram;"
                            + "\notherwise, you will get the default behavior of scrolling through"
                            + " pages."
                            + "\nThis option disables that default behavior. This is convenient if"
                            + " you need to scroll a lot,"
                            + "\nand want to avoid accidentally scrolling through pages."
                            + "\nYou can still scroll through pages while mousing over the page"
                            + " number.")
                    .register();

    public static final Option<Boolean> GENERATE_DIAGRAMS_ON_CLIENT_CONNECT =
            new BooleanOption(
                    Category.OPTIONS, "generate_diagrams_on_client_connect", true,
                    "If this option is enabled, diagrams will be generated the first time"
                            + " you join a world."
                            + "\nThis option must be enabled for diagrams to be affected by"
                            + " MineTweaker scripts."
                            + "\nChanging this option requires a restart to take effect.",
                    true)
                    .register();

    public static final Option<List<String>> HARD_DISABLED_DIAGRAM_GROUPS =
            new StringListOption(
                    Category.OPTIONS, "hard_disabled_diagram_groups", new ArrayList<>(),
                    "Add a diagram group ID here to disable that diagram group before"
                            + " initialization."
                            + "\nThis option is intended to fix compatibility with old versions of"
                            + " mods,"
                            + "\nwhere diagram groups have their dependencies satisfied, but crash"
                            + " on initialization."
                            + "\nYou should not need to modify this option unless you are getting a"
                            + " crash."
                            + "\nEntries in this option should have the form (no spaces, all"
                            + " lower-case):"
                            + "\n  neicustomdiagram.diagramgroup.<mod name>.<diagram group name>"
                            + "\nChanging this option requires a restart to take effect.",
                    true)
                    .register();

    public static final Option<Integer> SCROLL_SPEED =
            new IntegerOption(
                    Category.OPTIONS, "scroll_speed", 12,
                    "Sets the scroll speed, in pixels."
                            + " Use a negative value to invert the scroll direction.")
                    .register();

    public static final Option<Boolean> SHOW_EMPTY_DIAGRAMS =
            new BooleanOption(
                    Category.OPTIONS, "show_empty_diagrams", false,
                    "Enables showing diagrams that contain few or no components."
                            + "\nSometimes they still have some useful info.")
                    .register();

    public static final Option<Boolean> SHOW_IDS =
            new BooleanOption(
                    Category.OPTIONS, "show_ids", false,
                    "Enables showing ID numbers, such as item ID, item metadata, and fluid ID."
                            + "\nSome diagrams may also show other IDs if this option is enabled.")
                    .register();

    public static final Option<Boolean> SHOW_STACK_SIZE_ONE =
            new BooleanOption(
                    Category.OPTIONS, "show_stack_size_one", false,
                    "Enables always showing stack size on item components, even if it's 1.")
                    .register();

    public static final Option<Integer> TOOLTIP_MAX_CYCLE_COUNT =
            new IntegerOption(
                    Category.OPTIONS, "tooltip_max_cycle_count", 8,
                    "Sets the maximum # of cycle components that will be shown in a tooltip"
                            + " when <Shift> is held."
                            + "\nSet to 0 to disable this feature.")
                    .register();

    public enum Category {
        OPTIONS("options"),
        DIAGRAM_GROUPS("diagram_groups");

        private final String name;

        Category(String name) {
            this.name = name;
        }

        @Override
        public String toString() {
            return name;
        }
    }

    public abstract static class Option<T> implements Supplier<T> {
        final Category category;
        final String key;
        final T defaultValue;
        final String comment;
        final boolean requiresRestart;

        Property property;

        Option(
                Category category, String key, T defaultValue, String comment,
                boolean requiresRestart) {
            this.category = category;
            this.key = key;
            this.defaultValue = defaultValue;
            this.comment = comment + buildDefaultComment(defaultValue);
            this.requiresRestart = requiresRestart;
        }

        Option(Category category, String key, T defaultValue, String comment) {
            this(category, key, defaultValue, comment, false);
        }

        /** Chain this method right after construction. */
        Option<T> register() {
            allOptions.add(this);
            return this;
        }

        public void initialize() {
            property = getProperty();
            property.setRequiresMcRestart(requiresRestart);

            // Load this option, so that it gets saved if it's missing from the config.
            get();
        }

        /**
         * Sadly, this abstract method is needed because we cannot in-line getting the property in
         * {@link #initialize()} due to type shenanigans.
         */
        abstract Property getProperty();

        @Override
        public abstract T get();
    }

    public static final class BooleanOption extends Option<Boolean> {
        private BooleanOption(Category category, String key, boolean defaultValue, String comment) {
            super(category, key, defaultValue, comment);
        }

        private BooleanOption(
                Category category, String key, boolean defaultValue, String comment,
                boolean requiresRestart) {
            super(category, key, defaultValue, comment, requiresRestart);
        }

        @Override
        Property getProperty() {
            return Config.CONFIG.get(category.toString(), key, defaultValue, comment);
        }

        @Override
        public Boolean get() {
            return property.getBoolean();
        }
    }

    public static final class IntegerOption extends Option<Integer> {
        private IntegerOption(Category category, String key, int defaultValue, String comment) {
            super(category, key, defaultValue, comment);
        }

        private IntegerOption(
                Category category, String key, int defaultValue, String comment,
                boolean requiresRestart) {
            super(category, key, defaultValue, comment, requiresRestart);
        }

        @Override
        Property getProperty() {
            return Config.CONFIG.get(category.toString(), key, defaultValue, comment);
        }

        @Override
        public Integer get() {
            return property.getInt();
        }
    }

    public static final class StringListOption extends Option<List<String>> {
        private StringListOption(
                Category category, String key, List<String> defaultValue, String comment) {
            super(category, key, defaultValue, comment);
        }

        private StringListOption(
                Category category, String key, List<String> defaultValue, String comment,
                boolean requiresRestart) {
            super(category, key, defaultValue, comment, requiresRestart);
        }

        @Override
        Property getProperty() {
            return Config.CONFIG.get(
                    category.toString(), key, defaultValue.toArray(new String[0]), comment);
        }

        @Override
        public List<String> get() {
            return Arrays.asList(property.getStringList());
        }
    }

    // Static class.
    private ConfigOptions() {}

    /** This method is only intended to be called during mod initialization. */
    static void setCategoryComments() {
        Config.CONFIG.setCategoryComment(
                Category.OPTIONS.toString(),
                "General usage options."
                        + " These should be safe to change without requiring a restart.");

        StringBuilder diagramGroupCategoryCommentBuilder = new StringBuilder();
        diagramGroupCategoryCommentBuilder
                .append("Visibility options for diagram groups."
                        + " These control when diagram groups are shown."
                        + "\nAll options are safe to change without requiring a restart,"
                        + " except for the special DISABLED value."
                        + "\nChanging from DISABLED requires a restart,"
                        + " because it causes diagram groups to not be generated at all."
                        + "\n\nValid values:");
        Arrays.stream(DiagramGroupVisibility.values()).forEach(
                visibility -> diagramGroupCategoryCommentBuilder
                        .append("\n * ").append(visibility.toString()));
        Config.CONFIG.setCategoryComment(
                Category.DIAGRAM_GROUPS.toString(), diagramGroupCategoryCommentBuilder.toString());
    }

    public static ImmutableList<Option<?>> getAllOptions() {
        return ImmutableList.copyOf(allOptions);
    }

    public static DiagramGroupVisibility getDiagramGroupVisibility(DiagramGroupInfo info) {
        Property property =
                Config.CONFIG.get(
                        Category.DIAGRAM_GROUPS.toString(), info.groupId(),
                        info.defaultVisibility().toString(),
                        buildDiagramGroupVisibilityComment(info));
        String visibilityName = property.getString();

        // Handle old boolean config values from before v0.8.1
        if (visibilityName.equals("true") || visibilityName.equals("false")) {
            Logger.MOD.warn(
                    "Detected old boolean config value [{}] for diagram group [{}]!"
                            + " Updating...",
                    visibilityName, info.groupId());

            DiagramGroupVisibility visibility =
                    Boolean.parseBoolean(visibilityName)
                            ? DiagramGroupVisibility.ALWAYS_SHOWN
                            : DiagramGroupVisibility.DISABLED;
            property.set(visibility.name());
            return visibility;
        }

        return DiagramGroupVisibility.getByName(visibilityName);
    }

    private static String buildDefaultComment(Object defaultValue) {
        return String.format("\nDefault: %s", defaultValue);
    }

    private static String buildDiagramGroupVisibilityComment(DiagramGroupInfo info) {
        StringBuilder builder = new StringBuilder();
        builder.append(
                String.format("Sets the visibility of the %s diagram group.", info.groupName()));

        if (!info.description().isEmpty()) {
            builder.append('\n');
            builder.append(info.description());
        }

        builder.append(buildDefaultComment(info.defaultVisibility()));
        return builder.toString();
    }
}
