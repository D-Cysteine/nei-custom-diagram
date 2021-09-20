package com.github.dcysteine.neicustomdiagram.api.diagram;

import codechicken.nei.recipe.HandlerInfo;
import com.github.dcysteine.neicustomdiagram.api.diagram.component.ItemComponent;
import com.github.dcysteine.neicustomdiagram.api.diagram.layout.Grid;
import com.github.dcysteine.neicustomdiagram.mod.config.DiagramGroupVisibility;
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
     * <p>A full page allows diagrams with up to 14 rows of slots; see {@link Grid#TOTAL_HEIGHT} for
     * the height in pixels. Smaller resolutions will have less space, though.
     */
    public abstract int diagramsPerPage();
    // TODO allow specifying this based on # rows (and maybe # of columns) to support scrollbar?

    /** If {@code true}, then NBT data will be removed when looking up a component. */
    public abstract boolean ignoreNbt();

    /**
     * Determines when the diagram group is shown.
     *
     * <p>The special value {@link DiagramGroupVisibility#DISABLED} will cause this diagram group to
     * not be generated at all, saving CPU and RAM usage.
     */
    public abstract DiagramGroupVisibility defaultVisibility();

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
                    .setDefaultVisibility(DiagramGroupVisibility.ALWAYS_SHOWN);
    }

    public abstract Builder toBuilder();

    @AutoValue.Builder
    public abstract static class Builder {
        public abstract Builder setGroupName(String groupName);
        public abstract Builder setGroupId(String groupId);
        public abstract Builder setIcon(ItemComponent icon);
        public abstract Builder setDiagramsPerPage(int diagramsPerPage);
        public abstract Builder setIgnoreNbt(boolean ignoreNbt);
        public abstract Builder setDefaultVisibility(DiagramGroupVisibility visibility);

        public abstract DiagramGroupInfo build();
    }
}
