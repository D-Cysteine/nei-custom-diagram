package com.github.dcysteine.neicustomdiagram.generators.gregtech5.lenses;

import com.github.dcysteine.neicustomdiagram.api.diagram.DiagramGroupInfo;
import com.github.dcysteine.neicustomdiagram.api.diagram.interactable.AllDiagramsButton;
import com.github.dcysteine.neicustomdiagram.api.diagram.layout.Grid;
import com.github.dcysteine.neicustomdiagram.api.diagram.layout.Layout;
import com.github.dcysteine.neicustomdiagram.api.diagram.layout.Lines;
import com.github.dcysteine.neicustomdiagram.api.diagram.layout.Slot;
import com.github.dcysteine.neicustomdiagram.api.diagram.layout.SlotGroup;
import com.github.dcysteine.neicustomdiagram.api.diagram.tooltip.Tooltip;
import com.github.dcysteine.neicustomdiagram.api.draw.Point;
import com.github.dcysteine.neicustomdiagram.mod.Lang;
import com.google.common.collect.ImmutableList;

import java.util.stream.Collectors;
import java.util.stream.IntStream;

class LayoutHandler {
    static final class SlotGroupKeys {
        static final Layout.SlotGroupKey SAME_COLOR_LENSES =
                Layout.SlotGroupKey.create("same-color-lenses");
    }

    static final Point LENS_COLOR_POSITION = Grid.GRID.grid(2, 0);

    static final int MAX_RECIPES_PER_PAGE = 24;
    static final ImmutableList<Layout.SlotKey> SLOT_RECIPE_INPUTS =
            ImmutableList.copyOf(
                    IntStream.range(0, MAX_RECIPES_PER_PAGE)
                            .mapToObj(i -> Layout.SlotKey.create("recipe-input-" + i))
                            .collect(Collectors.toList()));
    static final ImmutableList<Layout.SlotKey> SLOT_RECIPE_OUTPUTS =
            ImmutableList.copyOf(
                    IntStream.range(0, MAX_RECIPES_PER_PAGE)
                            .mapToObj(i -> Layout.SlotKey.create("recipe-output-" + i))
                            .collect(Collectors.toList()));

    private final DiagramGroupInfo info;

    private ImmutableList<Layout> requiredLayouts;
    private ImmutableList<Layout> optionalLayouts;

    LayoutHandler(DiagramGroupInfo info) {
        this.info = info;
    }

    /** This method must be called before any other methods are called. */
    void initialize() {
        requiredLayouts = ImmutableList.of(buildHeaderLayout());

        optionalLayouts =
                ImmutableList.copyOf(
                        IntStream.range(0, MAX_RECIPES_PER_PAGE)
                                .mapToObj(this::buildRecipeLayout)
                                .collect(Collectors.toList()));
    }

    ImmutableList<Layout> requiredLayouts() {
        return requiredLayouts;
    }

    ImmutableList<Layout> optionalLayouts() {
        return optionalLayouts;
    }

    private Layout buildHeaderLayout() {
        return Layout.builder()
                .addInteractable(new AllDiagramsButton(info, Grid.GRID.grid(0, 0)))
                .putSlotGroup(
                        SlotGroupKeys.SAME_COLOR_LENSES,
                        SlotGroup.builder(9, 1, Grid.GRID.grid(6, 2), Grid.Direction.C)
                                .setDefaultTooltip(
                                        Tooltip.create(
                                                Lang.GREGTECH_5_LENSES.trans("samecolorlensesslot"),
                                                Tooltip.SLOT_FORMATTING))
                                .build())
                .build();
    }

    private Layout buildRecipeLayout(int index) {
        int gridX = index % 2 == 0 ? 3 : 9;
        int gridY = 4 + 2 * (index / 2);

        int inputX = gridX - 1;
        int outputX = gridX + 1;

        return Layout.builder()
                .addLines(
                        Lines.builder(Grid.GRID.grid(inputX, gridY))
                                .addArrow(Grid.GRID.edge(outputX, gridY, Grid.Direction.W))
                                .build())
                .putSlot(
                        SLOT_RECIPE_INPUTS.get(index),
                        Slot.builder(Grid.GRID.grid(inputX, gridY))
                                .setTooltip(
                                        Tooltip.create(
                                                Lang.GREGTECH_5_LENSES.trans("recipeinputslot"),
                                                Tooltip.SLOT_FORMATTING))
                                .build())
                .putSlot(
                        SLOT_RECIPE_OUTPUTS.get(index),
                        Slot.builder(Grid.GRID.grid(outputX, gridY))
                                .setTooltip(
                                        Tooltip.create(
                                                Lang.GREGTECH_5_LENSES.trans("recipeoutputslot"),
                                                Tooltip.SLOT_FORMATTING))
                                .build())
                .build();
    }
}
