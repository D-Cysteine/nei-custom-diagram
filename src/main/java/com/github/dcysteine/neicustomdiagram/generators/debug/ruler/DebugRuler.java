package com.github.dcysteine.neicustomdiagram.generators.debug.ruler;

import com.github.dcysteine.neicustomdiagram.api.diagram.Diagram;
import com.github.dcysteine.neicustomdiagram.api.diagram.DiagramGenerator;
import com.github.dcysteine.neicustomdiagram.api.diagram.DiagramGroup;
import com.github.dcysteine.neicustomdiagram.api.diagram.DiagramGroupInfo;
import com.github.dcysteine.neicustomdiagram.api.diagram.component.Component;
import com.github.dcysteine.neicustomdiagram.api.diagram.component.DisplayComponent;
import com.github.dcysteine.neicustomdiagram.api.diagram.component.ItemComponent;
import com.github.dcysteine.neicustomdiagram.api.diagram.interactable.Interactable;
import com.github.dcysteine.neicustomdiagram.api.diagram.layout.Grid;
import com.github.dcysteine.neicustomdiagram.api.diagram.layout.Layout;
import com.github.dcysteine.neicustomdiagram.api.diagram.layout.Lines;
import com.github.dcysteine.neicustomdiagram.api.diagram.layout.Slot;
import com.github.dcysteine.neicustomdiagram.api.diagram.matcher.CustomDiagramMatcher;
import com.github.dcysteine.neicustomdiagram.api.diagram.tooltip.Tooltip;
import com.github.dcysteine.neicustomdiagram.api.draw.Draw;
import com.github.dcysteine.neicustomdiagram.api.draw.Point;
import com.github.dcysteine.neicustomdiagram.main.Lang;
import com.github.dcysteine.neicustomdiagram.main.config.DiagramGroupVisibility;
import com.google.common.collect.Lists;
import net.minecraft.init.Items;

import java.util.Collection;

/**
 * Generates a debug diagram that helps measure available NEI space.
 *
 * <p>As a bonus, also allows viewing NBT data of whichever component it is invoked with, and the
 * NEI tab icon functions as a compass!
 */
public final class DebugRuler implements DiagramGenerator {
    public static final ItemComponent ICON = ItemComponent.create(Items.compass, 0);

    private static final int RULER_WIDTH_PIXELS = 2 * Grid.TOTAL_WIDTH;
    private static final int RULER_HEIGHT_PIXELS = 2 * Grid.TOTAL_HEIGHT;
    private static final int RULER_WIDTH_SLOTS = 2 * Grid.GRID_WIDTH;
    private static final int RULER_HEIGHT_SLOTS = 2 * Grid.GRID_HEIGHT;

    private static final int RULER_SEGMENT_PIXELS = 10;
    private static final int RULER_COLOUR_1 = Draw.Colour.RED;
    private static final int RULER_COLOUR_2 = Draw.Colour.BLUE;

    /** Format this with the slot {@code x}-index and {@code y}-index. */
    private static final String SLOT_KEY_FORMAT_STRING = "slot(%d,%d)";

    private final DiagramGroupInfo info;
    private Layout layout;

    public DebugRuler(String groupId) {
        this.info =
                DiagramGroupInfo.builder(
                                Lang.DEBUG_RULER.trans("groupname"),
                                groupId, ICON, 1)
                        .setIgnoreNbt(false)
                        .setDefaultVisibility(DiagramGroupVisibility.DISABLED)
                        .setDescription(
                                "This diagram has markings to measure the GUI height in pixels."
                                        + "\nYou can also use it to view NBT data for any item."
                                        + "\nThe tab icon also works as a compass!")
                        .build();
    }

    @Override
    public DiagramGroupInfo info() {
        return info;
    }

    @Override
    public DiagramGroup generate() {
        layout = buildLayout();
        return new DiagramGroup(
                info, new CustomDiagramMatcher(this::generateDiagrams));
    }

    private Collection<Diagram> generateDiagrams(
            Interactable.RecipeType recipeType, Component component) {
        return Lists.newArrayList(
                Diagram.builder()
                        .addLayout(layout)
                        .insertIntoSlot(
                                Layout.SlotKey.create(String.format(SLOT_KEY_FORMAT_STRING, 0, 0)),
                                DisplayComponent.builder(component).build())
                        .build());
    }

    private static Layout buildLayout() {
        Layout.Builder layoutBuilder = Layout.builder();

        Lines.Builder rulerColour1 =
                Lines.builder(Point.create(0, 0)).setColour(RULER_COLOUR_1);
        Lines.Builder rulerColour2 =
                Lines.builder(Point.create(0, 0)).setColour(RULER_COLOUR_2);
        for (int i = RULER_SEGMENT_PIXELS; i <= RULER_WIDTH_PIXELS; i += RULER_SEGMENT_PIXELS) {
            Lines.Builder linesBuilder =
                    i % (2 * RULER_SEGMENT_PIXELS) > 0 ? rulerColour1 : rulerColour2;

            // To account for lines having thickness 2,
            // we must shorten both ends by 1 pixel to avoid overlap.
            linesBuilder.move(Point.create(i + 1 - RULER_SEGMENT_PIXELS, 0));
            linesBuilder.addSegment(Point.create(i - 1, 0));
        }

        for (int i = RULER_SEGMENT_PIXELS; i <= RULER_HEIGHT_PIXELS; i += RULER_SEGMENT_PIXELS) {
            Lines.Builder linesBuilder =
                    i % (2 * RULER_SEGMENT_PIXELS) > 0 ? rulerColour1 : rulerColour2;

            // To account for lines having thickness 2,
            // we must shorten both ends by 1 pixel to avoid overlap.
            linesBuilder.move(Point.create(0, i + 1 - RULER_SEGMENT_PIXELS));
            linesBuilder.addSegment(Point.create(0, i - 1));
        }

        for (int i = 0; i < RULER_WIDTH_SLOTS; i++) {
            for (int j = 0; j < RULER_HEIGHT_SLOTS; j++) {
                layoutBuilder.putSlot(
                        Layout.SlotKey.create(String.format(SLOT_KEY_FORMAT_STRING, i, j)),
                        Slot.builder(Grid.GRID.grid(2 * i, 2 * j))
                                .setTooltip(
                                        Tooltip.create(
                                                Lang.DEBUG_RULER.transf("slotlabel", i + 1, j + 1),
                                                Tooltip.INFO_FORMATTING))
                                .build());
            }
        }

        return layoutBuilder
                .addLines(rulerColour1.build())
                .addLines(rulerColour2.build())
                .build();
    }
}
