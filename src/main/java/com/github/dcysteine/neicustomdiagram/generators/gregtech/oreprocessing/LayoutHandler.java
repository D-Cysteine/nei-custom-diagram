package com.github.dcysteine.neicustomdiagram.generators.gregtech.oreprocessing;

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
import com.github.dcysteine.neicustomdiagram.mod.Lang;
import com.google.common.collect.ImmutableList;

class LayoutHandler {
    static final class SlotKeys {
        static final String RAW_ORE = "raw-ore";
    }

    static final class SlotGroupKeys {
        static final String RAW_ORE_MACERATE = "raw-ore-macerate";
        static final String CRUSHED_ORE_MACERATE = "crushed-ore-macerate";
        static final String CRUSHED_ORE_THERMAL_CENTRIFUGE = "crushed-ore-thermal-centrifuge";
        static final String CRUSHED_ORE_WASH = "crushed-ore-wash";
        static final String PURIFIED_ORE_MACERATE = "purified-ore-macerate";
        static final String PURIFIED_ORE_THERMAL_CENTRIFUGE = "purified-ore-thermal-centrifuge";
        static final String PURIFIED_ORE_SIFT = "purified-ore-sift";
        static final String IMPURE_DUST_CENTRIFUGE = "impure-dust-centrifuge";
        static final String PURIFIED_DUST_CENTRIFUGE = "purified-dust-centrifuge";
        static final String PURIFIED_DUST_ELECTROMAGNETIC_SEPARATE =
                "purified-dust-electromagnetic-separate";
        static final String CRUSHED_ORE_THERMAL_CENTRIFUGE_MACERATE =
                "crushed-ore-thermal-centrifuge-macerate";
        static final String PURIFIED_ORE_THERMAL_CENTRIFUGE_MACERATE =
                "purified-ore-thermal-centrifuge-macerate";
        static final String ADDITIONAL_RECIPE_OUTPUTS = "additional-recipe-outputs";
    }

    static final class AdditionalRecipeLabelPositions {
        static final Point FURNACE = Grid.GRID.grid(2, 0);
        static final Point ELECTRIC_BLAST_FURNACE = Grid.GRID.grid(4, 0);
        static final Point CHEMICAL_BATH = Grid.GRID.grid(0, 2);
        static final Point CHEMICAL_REACTOR = Grid.GRID.grid(2, 2);
        static final Point AUTOCLAVE = Grid.GRID.grid(4, 2);
    }

    private final DiagramGroupInfo info;
    private final LabelHandler labelHandler;

    private ImmutableList<Layout> layouts;

    LayoutHandler(DiagramGroupInfo info, LabelHandler labelHandler) {
        this.info = info;
        this.labelHandler = labelHandler;
        this.layouts = null;
    }

    /** This method must be called before any other methods are called. */
    void initialize() {
        ImmutableList.Builder<Layout> layoutsBuilder = new ImmutableList.Builder<>();
        layoutsBuilder.add(buildRawOreLayout());
        layoutsBuilder.add(buildRawOreMacerateLayout());
        layoutsBuilder.add(buildCrushedOreWashingLayout());
        layoutsBuilder.add(buildCrushedOreMacerateLayout());
        layoutsBuilder.add(buildCrushedOreThermalCentrifugeLayout());
        layoutsBuilder.add(buildPurifiedOreMacerateLayout());
        layoutsBuilder.add(buildPurifiedOreThermalCentrifugeLayout());
        layoutsBuilder.add(buildPurifiedOreSiftLayout());
        layoutsBuilder.add(buildImpureDustCentrifugeLayout());
        layoutsBuilder.add(buildPurifiedDustCentrifugeLayout());
        layoutsBuilder.add(buildPurifiedDustElectromagneticSeparateLayout());
        layoutsBuilder.add(buildCrushedOreThermalCentrifugeMacerateLayout());
        layoutsBuilder.add(buildPurifiedOreThermalCentrifugeMacerateLayout());
        layoutsBuilder.add(buildAdditionalRecipeOutputsLayout());
        layouts = layoutsBuilder.build();
    }

    /** All layouts are optional. */
    ImmutableList<Layout> layouts() {
        return layouts;
    }

    private Layout buildRawOreLayout() {
        Slot inputSlot =
                Slot.builder(Grid.GRID.grid(3, 5))
                        .setDrawFunction(Draw::drawBigSlot)
                        .build();

        AllDiagramsButton allDiagramsButton = new AllDiagramsButton(info, Grid.GRID.grid(0, 0));

        return Layout.builder()
                .putSlot(SlotKeys.RAW_ORE, inputSlot)
                .addInteractable(allDiagramsButton)
                .build();
    }

    private Layout buildRawOreMacerateLayout() {
        Lines lines =
                Lines.builder(Grid.GRID.grid(3, 5))
                        .addArrow(Grid.GRID.edge(3, 10, Grid.Direction.N))
                        .build();

        CustomInteractable label =
                labelHandler.buildLabel(LabelHandler.ItemLabel.MACERATOR, Grid.GRID.grid(3, 8));

        SlotGroup outputSlots =
                SlotGroup.builder(1, 2, Grid.GRID.grid(3, 10), Grid.Direction.S)
                        .setDefaultTooltip(
                                Tooltip.create(
                                        Lang.GREGTECH_ORE_PROCESSING.trans("maceratorslot"),
                                        Tooltip.SLOT_FORMATTING))
                        .build();

        return Layout.builder()
                .addLines(lines)
                .addInteractable(label)
                .putSlotGroup(SlotGroupKeys.RAW_ORE_MACERATE, outputSlots)
                .build();
    }

    private Layout buildCrushedOreWashingLayout() {
        Lines lines =
                Lines.builder(Grid.GRID.grid(3, 10))
                        .addSegment(Grid.GRID.grid(5, 10))
                        .addSegment(Grid.GRID.grid(5, 8))
                        .addSegment(Grid.GRID.grid(9, 8))
                        .addArrow(Grid.GRID.edge(9, 10, Grid.Direction.N))
                        .build();

        CustomInteractable label =
                labelHandler.buildLabel(
                        LabelHandler.ItemLabel.ORE_WASHER, Grid.GRID.grid(9, 8));

        SlotGroup outputSlots =
                SlotGroup.builder(1, 2, Grid.GRID.grid(9, 10), Grid.Direction.S)
                        .setDefaultTooltip(
                                Tooltip.create(
                                        Lang.GREGTECH_ORE_PROCESSING.trans("orewasherslot"),
                                        Tooltip.SLOT_FORMATTING))
                        .build();

        return Layout.builder()
                .addLines(lines)
                .addInteractable(label)
                .putSlotGroup(SlotGroupKeys.CRUSHED_ORE_WASH, outputSlots)
                .build();
    }

    private Layout buildCrushedOreMacerateLayout() {
        Lines lines =
                Lines.builder(Grid.GRID.grid(3, 10))
                        .addSegment(Grid.GRID.grid(2, 10))
                        .addArrow(Grid.GRID.edge(2, 16, Grid.Direction.N))
                        .build();

        CustomInteractable label =
                labelHandler.buildLabel(LabelHandler.ItemLabel.MACERATOR, Grid.GRID.grid(2, 14));

        SlotGroup outputSlots =
                SlotGroup.builder(1, 2, Grid.GRID.grid(2, 16), Grid.Direction.S)
                        .setDefaultTooltip(
                                Tooltip.create(
                                        Lang.GREGTECH_ORE_PROCESSING.trans("maceratorslot"),
                                        Tooltip.SLOT_FORMATTING))
                        .build();

        return Layout.builder()
                .addLines(lines)
                .addInteractable(label)
                .putSlotGroup(SlotGroupKeys.CRUSHED_ORE_MACERATE, outputSlots)
                .build();
    }

    private Layout buildCrushedOreThermalCentrifugeLayout() {
        Lines lines =
                Lines.builder(Grid.GRID.grid(3, 10))
                        .addSegment(Grid.GRID.grid(5, 10))
                        .addArrow(Grid.GRID.edge(5, 14, Grid.Direction.N))
                        .build();

        CustomInteractable label =
                labelHandler.buildLabel(
                        LabelHandler.ItemLabel.THERMAL_CENTRIFUGE, Grid.GRID.grid(5, 12));

        SlotGroup outputSlots =
                SlotGroup.builder(1, 2, Grid.GRID.grid(5, 14), Grid.Direction.S)
                        .setDefaultTooltip(
                                Tooltip.create(
                                        Lang.GREGTECH_ORE_PROCESSING.trans("thermalcentrifugeslot"),
                                        Tooltip.SLOT_FORMATTING))
                        .build();

        return Layout.builder()
                .addLines(lines)
                .addInteractable(label)
                .putSlotGroup(SlotGroupKeys.CRUSHED_ORE_THERMAL_CENTRIFUGE, outputSlots)
                .build();
    }

    private Layout buildPurifiedOreMacerateLayout() {
        Lines lines =
                Lines.builder(Grid.GRID.grid(9, 10))
                        .addSegment(Grid.GRID.grid(10, 10))
                        .addArrow(Grid.GRID.edge(10, 16, Grid.Direction.N))
                        .build();

        CustomInteractable label =
                labelHandler.buildLabel(LabelHandler.ItemLabel.MACERATOR, Grid.GRID.grid(10, 14));

        SlotGroup outputSlots =
                SlotGroup.builder(1, 2, Grid.GRID.grid(10, 16), Grid.Direction.S)
                        .setDefaultTooltip(
                                Tooltip.create(
                                        Lang.GREGTECH_ORE_PROCESSING.trans("maceratorslot"),
                                        Tooltip.SLOT_FORMATTING))
                        .build();

        return Layout.builder()
                .addLines(lines)
                .addInteractable(label)
                .putSlotGroup(SlotGroupKeys.PURIFIED_ORE_MACERATE, outputSlots)
                .build();
    }

    private Layout buildPurifiedOreThermalCentrifugeLayout() {
        Lines lines =
                Lines.builder(Grid.GRID.grid(9, 10))
                        .addSegment(Grid.GRID.grid(7, 10))
                        .addArrow(Grid.GRID.edge(7, 14, Grid.Direction.N))
                        .build();

        CustomInteractable label =
                labelHandler.buildLabel(
                        LabelHandler.ItemLabel.THERMAL_CENTRIFUGE, Grid.GRID.grid(7, 12));

        SlotGroup outputSlots =
                SlotGroup.builder(1, 2, Grid.GRID.grid(7, 14), Grid.Direction.S)
                        .setDefaultTooltip(
                                Tooltip.create(
                                        Lang.GREGTECH_ORE_PROCESSING.trans("thermalcentrifugeslot"),
                                        Tooltip.SLOT_FORMATTING))
                        .build();

        return Layout.builder()
                .addLines(lines)
                .addInteractable(label)
                .putSlotGroup(SlotGroupKeys.PURIFIED_ORE_THERMAL_CENTRIFUGE, outputSlots)
                .build();
    }

    private Layout buildPurifiedOreSiftLayout() {
        Lines lines =
                Lines.builder(Grid.GRID.grid(9, 10))
                        .addSegment(Grid.GRID.grid(10, 10))
                        .addArrow(Grid.GRID.edge(10, 4, Grid.Direction.S))
                        .build();

        CustomInteractable label =
                labelHandler.buildLabel(LabelHandler.ItemLabel.SIFTER, Grid.GRID.grid(10, 6));

        SlotGroup outputSlots =
                SlotGroup.builder(3, 3, Grid.GRID.grid(10, 4), Grid.Direction.N)
                        .setDefaultTooltip(
                                Tooltip.create(
                                        Lang.GREGTECH_ORE_PROCESSING.trans("sifterslot"),
                                        Tooltip.SLOT_FORMATTING))
                        .build();

        return Layout.builder()
                .addLines(lines)
                .addInteractable(label)
                .putSlotGroup(SlotGroupKeys.PURIFIED_ORE_SIFT, outputSlots)
                .build();
    }

    private Layout buildImpureDustCentrifugeLayout() {
        Lines lines =
                Lines.builder(Grid.GRID.grid(2, 16))
                        .addSegment(Grid.GRID.grid(0, 16))
                        .addArrow(Grid.GRID.edge(0, 20, Grid.Direction.N))
                        .build();

        CustomInteractable label =
                labelHandler.buildLabel(LabelHandler.ItemLabel.CENTRIFUGE, Grid.GRID.grid(0, 18));

        SlotGroup outputSlots =
                SlotGroup.builder(1, 2, Grid.GRID.grid(0, 20), Grid.Direction.S)
                        .setDefaultTooltip(
                                Tooltip.create(
                                        Lang.GREGTECH_ORE_PROCESSING.trans("centrifugeslot"),
                                        Tooltip.SLOT_FORMATTING))
                        .build();

        return Layout.builder()
                .addLines(lines)
                .addInteractable(label)
                .putSlotGroup(SlotGroupKeys.IMPURE_DUST_CENTRIFUGE, outputSlots)
                .build();
    }

    private Layout buildPurifiedDustCentrifugeLayout() {
        Lines lines =
                Lines.builder(Grid.GRID.grid(10, 16))
                        .addSegment(Grid.GRID.grid(12, 16))
                        .addArrow(Grid.GRID.edge(12, 20, Grid.Direction.N))
                        .build();

        CustomInteractable label =
                labelHandler.buildLabel(LabelHandler.ItemLabel.CENTRIFUGE, Grid.GRID.grid(12, 18));

        SlotGroup outputSlots =
                SlotGroup.builder(1, 2, Grid.GRID.grid(12, 20), Grid.Direction.S)
                        .setDefaultTooltip(
                                Tooltip.create(
                                        Lang.GREGTECH_ORE_PROCESSING.trans("centrifugeslot"),
                                        Tooltip.SLOT_FORMATTING))
                        .build();

        return Layout.builder()
                .addLines(lines)
                .addInteractable(label)
                .putSlotGroup(SlotGroupKeys.PURIFIED_DUST_CENTRIFUGE, outputSlots)
                .build();
    }

    private Layout buildPurifiedDustElectromagneticSeparateLayout() {
        Lines lines =
                Lines.builder(Grid.GRID.grid(10, 16))
                        .addSegment(Grid.GRID.grid(12, 16))
                        .addArrow(Grid.GRID.edge(12, 12, Grid.Direction.S))
                        .build();

        CustomInteractable label =
                labelHandler.buildLabel(
                        LabelHandler.ItemLabel.ELECTROMAGNETIC_SEPARATOR, Grid.GRID.grid(12, 14));

        SlotGroup outputSlots =
                SlotGroup.builder(1, 3, Grid.GRID.grid(12, 12), Grid.Direction.N)
                        .setDefaultTooltip(
                                Tooltip.create(
                                        Lang.GREGTECH_ORE_PROCESSING.trans(
                                                "electromagneticseparatorslot"),
                                        Tooltip.SLOT_FORMATTING))
                        .build();

        return Layout.builder()
                .addLines(lines)
                .addInteractable(label)
                .putSlotGroup(SlotGroupKeys.PURIFIED_DUST_ELECTROMAGNETIC_SEPARATE, outputSlots)
                .build();
    }

    private Layout buildCrushedOreThermalCentrifugeMacerateLayout() {
        Lines lines =
                Lines.builder(Grid.GRID.grid(5, 14))
                        .addSegment(Grid.GRID.grid(4, 14))
                        .addSegment(Grid.GRID.grid(4, 18))
                        .addSegment(Grid.GRID.grid(5, 18))
                        .addArrow(Grid.GRID.edge(5, 20, Grid.Direction.N))
                        .build();

        CustomInteractable label =
                labelHandler.buildLabel(LabelHandler.ItemLabel.MACERATOR, Grid.GRID.grid(5, 18));

        SlotGroup outputSlots =
                SlotGroup.builder(1, 2, Grid.GRID.grid(5, 20), Grid.Direction.S)
                        .setDefaultTooltip(
                                Tooltip.create(
                                        Lang.GREGTECH_ORE_PROCESSING.trans("maceratorslot"),
                                        Tooltip.SLOT_FORMATTING))
                        .build();

        return Layout.builder()
                .addLines(lines)
                .addInteractable(label)
                .putSlotGroup(SlotGroupKeys.CRUSHED_ORE_THERMAL_CENTRIFUGE_MACERATE, outputSlots)
                .build();
    }

    private Layout buildPurifiedOreThermalCentrifugeMacerateLayout() {
        Lines lines =
                Lines.builder(Grid.GRID.grid(7, 14))
                        .addSegment(Grid.GRID.grid(8, 14))
                        .addSegment(Grid.GRID.grid(8, 18))
                        .addSegment(Grid.GRID.grid(7, 18))
                        .addArrow(Grid.GRID.edge(7, 20, Grid.Direction.N))
                        .build();

        CustomInteractable label =
                labelHandler.buildLabel(LabelHandler.ItemLabel.MACERATOR, Grid.GRID.grid(7, 18));

        SlotGroup outputSlots =
                SlotGroup.builder(1, 2, Grid.GRID.grid(7, 20), Grid.Direction.S)
                        .setDefaultTooltip(
                                Tooltip.create(
                                        Lang.GREGTECH_ORE_PROCESSING.trans("maceratorslot"),
                                        Tooltip.SLOT_FORMATTING))
                        .build();

        return Layout.builder()
                .addLines(lines)
                .addInteractable(label)
                .putSlotGroup(SlotGroupKeys.PURIFIED_ORE_THERMAL_CENTRIFUGE_MACERATE, outputSlots)
                .build();
    }

    private Layout buildAdditionalRecipeOutputsLayout() {
        SlotGroup outputSlots =
                SlotGroup.builder(9, 2, Grid.GRID.grid(6, 24), Grid.Direction.S)
                        .setDefaultTooltip(
                                Tooltip.create(
                                        Lang.GREGTECH_ORE_PROCESSING.trans(
                                                "additionalrecipeoutputsslot"),
                                        Tooltip.SLOT_FORMATTING))
                        .build();

        return Layout.builder()
                .putSlotGroup(SlotGroupKeys.ADDITIONAL_RECIPE_OUTPUTS, outputSlots)
                .build();
    }
}
