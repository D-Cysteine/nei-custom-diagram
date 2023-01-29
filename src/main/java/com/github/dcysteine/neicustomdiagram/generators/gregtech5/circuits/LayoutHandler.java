package com.github.dcysteine.neicustomdiagram.generators.gregtech5.circuits;

import com.github.dcysteine.neicustomdiagram.api.diagram.DiagramGroupInfo;
import com.github.dcysteine.neicustomdiagram.api.diagram.interactable.AllDiagramsButton;
import com.github.dcysteine.neicustomdiagram.api.diagram.interactable.CustomInteractable;
import com.github.dcysteine.neicustomdiagram.api.diagram.layout.Grid;
import com.github.dcysteine.neicustomdiagram.api.diagram.layout.Layout;
import com.github.dcysteine.neicustomdiagram.api.diagram.layout.Lines;
import com.github.dcysteine.neicustomdiagram.api.diagram.layout.Slot;
import com.github.dcysteine.neicustomdiagram.api.diagram.layout.SlotGroup;
import com.github.dcysteine.neicustomdiagram.api.diagram.tooltip.Tooltip;
import com.github.dcysteine.neicustomdiagram.api.draw.Draw;
import com.github.dcysteine.neicustomdiagram.api.draw.Point;
import com.github.dcysteine.neicustomdiagram.main.Lang;
import com.google.common.collect.ImmutableList;

import java.util.stream.Collectors;
import java.util.stream.IntStream;

class LayoutHandler {
    static final class SlotKeys {
        static final Layout.SlotKey RECIPE_OUTPUT = Layout.SlotKey.create("recipe-output");
    }

    static final class SlotGroupKeys {
        static final Layout.SlotGroupKey CIRCUIT_LINES =
                Layout.SlotGroupKey.create("circuit-lines");
        static final Layout.SlotGroupKey INDIVIDUAL_CIRCUITS =
                Layout.SlotGroupKey.create("individual-circuits");
        static final Layout.SlotGroupKey CIRCUIT_PARTS =
                Layout.SlotGroupKey.create("circuit-parts");

        static final Layout.SlotGroupKey CIRCUIT_LINE_CIRCUITS =
                Layout.SlotGroupKey.create("circuit-line-circuits");
        static final Layout.SlotGroupKey TIER_CIRCUITS =
                Layout.SlotGroupKey.create("tier-circuits");
        static final Layout.SlotGroupKey RECIPE_FLUID_INPUTS =
                Layout.SlotGroupKey.create("recipe-fluid-inputs");
    }

    static final ImmutableList<Layout.SlotGroupKey> SLOT_GROUP_RECIPE_ITEM_INPUTS =
            ImmutableList.copyOf(
                    IntStream.range(0, CircuitRecipe.MAX_ITEM_INPUTS)
                            .mapToObj(i -> Layout.SlotGroupKey.create("recipe-item-inputs-" + i))
                            .collect(Collectors.toList()));

    static final class AdditionalRecipeLabelPositions {
        static final Point CRAFTING_TABLE = Grid.GRID.grid(2, 0);
        static final Point ASSEMBLING_MACHINE = Grid.GRID.grid(4, 0);
        static final Point ASSEMBLING_LINE = Grid.GRID.grid(6, 0);
        static final Point CIRCUIT_ASSEMBLING_MACHINE = Grid.GRID.grid(0, 2);
        static final Point CLEAN_ROOM = Grid.GRID.grid(2, 2);
        static final Point LOW_GRAVITY = Grid.GRID.grid(4, 2);
    }

    private final DiagramGroupInfo info;
    private final CircuitLineHandler circuitLineHandler;

    private Layout overviewLayout;
    private ImmutableList<Layout> requiredLayouts;
    private ImmutableList<Layout> optionalLayouts;

    LayoutHandler(DiagramGroupInfo info, CircuitLineHandler circuitLineHandler) {
        this.info = info;
        this.circuitLineHandler = circuitLineHandler;
    }

    /** This method must be called before any other methods are called. */
    void initialize() {
        overviewLayout = buildOverviewLayout();
        requiredLayouts = ImmutableList.of(buildHeaderLayout());

        ImmutableList.Builder<Layout> optionalLayoutsBuilder = ImmutableList.builder();
        optionalLayoutsBuilder.add(buildCircuitLineCircuitsLayout());
        optionalLayoutsBuilder.add(buildTierCircuitsLayout());
        for (int i = 0; i < CircuitRecipe.MAX_ITEM_INPUTS; i++) {
            optionalLayoutsBuilder.add(buildRecipeItemInputsLayout(i));
        }
        optionalLayoutsBuilder.add(buildRecipeFluidInputs());
        optionalLayoutsBuilder.add(buildRecipeOutput());
        optionalLayouts = optionalLayoutsBuilder.build();
    }

    Layout overviewLayout() {
        return overviewLayout;
    }

    ImmutableList<Layout> requiredLayouts() {
        return requiredLayouts;
    }

    ImmutableList<Layout> optionalLayouts() {
        return optionalLayouts;
    }

    private Layout buildOverviewLayout() {
        SlotGroup.Builder circuitLinesSlotGroupBuilder =
                SlotGroup.builder(
                                circuitLineHandler.circuitLinesSize(), CircuitLine.MAX_TIER + 2,
                                Grid.GRID.grid(0, 0), Grid.Direction.SE)
                        .setDefaultTooltip(
                                Tooltip.create(
                                        Lang.GREGTECH_5_CIRCUITS.trans("circuitlinesslot"),
                                        Tooltip.SLOT_FORMATTING));
        for (int i = 0; i < circuitLineHandler.circuitLinesSize(); i++) {
            circuitLinesSlotGroupBuilder.setSlot(
                    i, 0,
                    SlotGroup.slotBuilder()
                            .setTooltip(
                                    Tooltip.create(
                                            Lang.GREGTECH_5_CIRCUITS.trans("circuitboardsslot"),
                                            Tooltip.SLOT_FORMATTING))
                            .build());
        }

        SlotGroup.Builder individualCircuitsSlotGroupBuilder =
                SlotGroup.builder(
                                circuitLineHandler.individualCircuitsSize(), 2,
                                Grid.GRID.grid(3, 24), Grid.Direction.SW)
                        .setDefaultTooltip(
                                Tooltip.create(
                                        Lang.GREGTECH_5_CIRCUITS.trans("individualcircuitsslot"),
                                        Tooltip.SLOT_FORMATTING));
        for (int i = 0; i < circuitLineHandler.individualCircuitsSize(); i++) {
            individualCircuitsSlotGroupBuilder.setSlot(
                    i, 0,
                    SlotGroup.slotBuilder()
                            .setTooltip(
                                    Tooltip.create(
                                            Lang.GREGTECH_5_CIRCUITS.trans("circuitboardsslot"),
                                            Tooltip.SLOT_FORMATTING))
                            .build());
        }

        return Layout.builder()
                .putSlotGroup(SlotGroupKeys.CIRCUIT_LINES, circuitLinesSlotGroupBuilder.build())
                .putSlotGroup(
                        SlotGroupKeys.INDIVIDUAL_CIRCUITS,
                        individualCircuitsSlotGroupBuilder.build())
                .putSlotGroup(
                        SlotGroupKeys.CIRCUIT_PARTS,
                        SlotGroup.builder(
                                        circuitLineHandler.circuitPartsSize(),
                                        circuitLineHandler.circuitPartsSubListMaxSize(),
                                        Grid.GRID.grid(5, 24), Grid.Direction.SE)
                                .setDefaultTooltip(
                                        Tooltip.create(
                                                Lang.GREGTECH_5_CIRCUITS.trans("circuitpartsslot"),
                                                Tooltip.SLOT_FORMATTING))
                                .build())
                .build();
    }

    private Layout buildHeaderLayout() {
        CustomInteractable overviewButton = new AllDiagramsButton(
                info, Grid.GRID.grid(0, 0),
                Lang.GREGTECH_5_CIRCUITS.trans("overviewbutton"));

        return Layout.builder()
                .addInteractable(overviewButton)
                .build();
    }

    private Layout buildCircuitLineCircuitsLayout() {
        return Layout.builder()
                .putSlotGroup(
                        SlotGroupKeys.CIRCUIT_LINE_CIRCUITS,
                        SlotGroup.builder(3, 1, Grid.GRID.grid(12, 0), Grid.Direction.W)
                                .setSlot(
                                        0, 0,
                                        SlotGroup.slotBuilder()
                                                .setTooltip(
                                                        Tooltip.create(
                                                                Lang.GREGTECH_5_CIRCUITS.trans(
                                                                        "previouscircuitslot"),
                                                                Tooltip.SLOT_FORMATTING))
                                                .build())
                                .setSlot(
                                        1, 0,
                                        SlotGroup.slotBuilder()
                                                .setTooltip(
                                                        Tooltip.create(
                                                                Lang.GREGTECH_5_CIRCUITS.trans(
                                                                        "currentcircuitslot"),
                                                                Tooltip.SLOT_FORMATTING))
                                                .build())
                                .setSlot(
                                        2, 0,
                                        SlotGroup.slotBuilder()
                                                .setTooltip(
                                                        Tooltip.create(
                                                                Lang.GREGTECH_5_CIRCUITS.trans(
                                                                        "nextcircuitslot"),
                                                                Tooltip.SLOT_FORMATTING))
                                                .build())
                                .build())
                .build();
    }

    private Layout buildTierCircuitsLayout() {
        return Layout.builder()
                .putSlotGroup(
                        SlotGroupKeys.TIER_CIRCUITS,
                        SlotGroup.builder(5, 1, Grid.GRID.grid(12, 2), Grid.Direction.W)
                                .setDefaultTooltip(
                                        Tooltip.create(
                                                Lang.GREGTECH_5_CIRCUITS.trans("tiercircuitsslot"),
                                                Tooltip.SLOT_FORMATTING))
                                .build())
                .build();
    }

    private Layout buildRecipeItemInputsLayout(int index) {
        int gridX = 2 + (index % 3) * 4;
        int gridY = 6 + (index / 3) * 4;

        return Layout.builder()
                .addLines(
                        Lines.builder(Grid.GRID.grid(gridX, gridY))
                                .addSegment(Grid.GRID.grid(gridX, 12))
                                .addSegment(Grid.GRID.grid(8, 12))
                                .addArrow(Grid.GRID.bigEdge(8, 14, Grid.Direction.N))
                                .build())
                .putSlotGroup(
                        SLOT_GROUP_RECIPE_ITEM_INPUTS.get(index),
                        SlotGroup.builder(
                                        2, 2,
                                        Grid.GRID.grid(gridX, gridY),
                                        Grid.Direction.C)
                                .setDefaultTooltip(
                                        Tooltip.create(
                                                Lang.GREGTECH_5_CIRCUITS.transf(
                                                        "recipeiteminputsslot", index + 1),
                                                Tooltip.SLOT_FORMATTING))
                                .build())
                .build();
    }

    private Layout buildRecipeFluidInputs() {
        return Layout.builder()
                .addLines(
                        Lines.builder(Grid.GRID.grid(4, 14))
                                .addArrow(Grid.GRID.bigEdge(8, 14, Grid.Direction.W))
                                .build())
                .putSlotGroup(
                        SlotGroupKeys.RECIPE_FLUID_INPUTS,
                        SlotGroup.builder(2, 2, Grid.GRID.grid(4, 14), Grid.Direction.C)
                                .setDefaultTooltip(
                                        Tooltip.create(
                                                Lang.GREGTECH_5_CIRCUITS.trans(
                                                        "recipefluidinputsslot"),
                                                Tooltip.SLOT_FORMATTING))
                                .build())
                .build();
    }

    private Layout buildRecipeOutput() {
        return Layout.builder()
                .putSlot(
                        SlotKeys.RECIPE_OUTPUT,
                        Slot.builder(Grid.GRID.grid(8, 14))
                                .setDrawFunction(Draw::drawBigSlot)
                                .setTooltip(
                                        Tooltip.create(
                                                Lang.GREGTECH_5_CIRCUITS.trans("recipeoutputslot"),
                                                Tooltip.SLOT_FORMATTING))
                                .build())
                .build();
    }
}
