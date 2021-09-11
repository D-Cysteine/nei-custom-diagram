package com.github.dcysteine.neicustomdiagram.api.diagram;

import codechicken.nei.recipe.HandlerInfo;
import com.github.dcysteine.neicustomdiagram.api.diagram.component.ItemComponent;
import com.github.dcysteine.neicustomdiagram.api.diagram.layout.Grid;
import com.google.auto.value.AutoValue;

@AutoValue
public abstract class DiagramGroupInfo {
    /** Display description for this diagram group. */
    public abstract String groupName();

    /**
     * Used as this diagram group's unique identifier, as well as the key to look up all diagrams in
     * a diagram group.
     */
    public abstract String groupId();

    /** The item to display on this diagram group's NEI tab. */
    public abstract ItemComponent icon();

    /**
     * Number of diagrams that can fit on a single page.
     *
     * <p>{@code 1} allows diagrams with up to 14 rows of slots; {@code 2} allows diagrams with up
     * to 7.
     *
     * <p>NEI seems to expect this to always be either {@code 1} or {@code 2}, so larger values
     * might not work.
     */
    public abstract int diagramsPerPage();

    /** If {@code true}, then NBT data will be removed when looking up a component. */
    public abstract boolean ignoreNbt();

    /** Whether this diagram is enabled by default, or must be enabled via config. */
    public abstract boolean enabledByDefault();

    public void buildHandlerInfo(HandlerInfo.Builder builder) {
        builder.setDisplayStack(icon().stack())
                .setHeight(Grid.TOTAL_HEIGHT / diagramsPerPage())
                .setMaxRecipesPerPage(diagramsPerPage());
    }

    public static Builder builder(
        String groupName, String groupId, ItemComponent icon, int diagramsPerPage) {
            return new AutoValue_DiagramGroupInfo.Builder()
                    .setGroupName(groupName)
                    .setGroupId(groupId)
                    .setIcon(icon)
                    .setDiagramsPerPage(diagramsPerPage)
                    .setIgnoreNbt(true)
                    .setEnabledByDefault(true);
    }

    public abstract Builder toBuilder();

    @AutoValue.Builder
    public abstract static class Builder {
        public abstract Builder setGroupName(String groupName);
        public abstract Builder setGroupId(String groupId);
        public abstract Builder setIcon(ItemComponent icon);
        public abstract Builder setDiagramsPerPage(int diagramsPerPage);
        public abstract Builder setIgnoreNbt(boolean ignoreNbt);
        public abstract Builder setEnabledByDefault(boolean enabledByDefault);

        public abstract DiagramGroupInfo build();
    }
}
