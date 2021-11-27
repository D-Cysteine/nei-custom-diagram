package com.github.dcysteine.neicustomdiagram.api.diagram;

import codechicken.nei.recipe.HandlerInfo;
import com.github.dcysteine.neicustomdiagram.api.diagram.component.ItemComponent;
import com.github.dcysteine.neicustomdiagram.api.diagram.layout.Grid;
import com.github.dcysteine.neicustomdiagram.main.config.DiagramGroupVisibility;
import com.google.auto.value.AutoValue;

import java.util.function.Predicate;

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

    /** If {@code true}, then NBT data will be removed when looking up a component. */
    public abstract boolean ignoreNbt();

    /**
     * An optional predicate that will be used to filter out empty diagrams, if the config setting
     * is enabled.
     *
     * <p>Diagrams that this predicate returns {@code true} for will be filtered out.
     */
    public abstract Predicate<Diagram> emptyDiagramPredicate();

    /**
     * Determines when the diagram group is shown.
     *
     * <p>The special value {@link DiagramGroupVisibility#DISABLED} will cause this diagram group to
     * not be generated at all, saving CPU and RAM usage.
     */
    public abstract DiagramGroupVisibility defaultVisibility();

    /**
     * A description of what this diagram group does; will be added to the visibility config
     * comment.
     *
     * <p>Include {@code '\n'} to add line breaks. Set to empty string to disable.
     */
    public abstract String description();

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
                    .setEmptyDiagramPredicate(diagram -> false)
                    .setDefaultVisibility(DiagramGroupVisibility.ALWAYS_SHOWN)
                    .setDescription("");
    }

    public abstract Builder toBuilder();

    @AutoValue.Builder
    public abstract static class Builder {
        public abstract Builder setGroupName(String groupName);
        public abstract Builder setGroupId(String groupId);
        public abstract Builder setIcon(ItemComponent icon);
        public abstract Builder setDiagramsPerPage(int diagramsPerPage);
        public abstract Builder setIgnoreNbt(boolean ignoreNbt);
        public abstract Builder setEmptyDiagramPredicate(Predicate<Diagram> emptyDiagramPredicate);
        public abstract Builder setDefaultVisibility(DiagramGroupVisibility visibility);
        public abstract Builder setDescription(String description);

        public abstract DiagramGroupInfo build();
    }
}
