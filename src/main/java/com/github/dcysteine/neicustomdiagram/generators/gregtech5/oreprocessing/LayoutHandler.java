package com.github.dcysteine.neicustomdiagram.generators.gregtech5.oreprocessing;

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

class LayoutHandler {
    static final class SlotKeys {
        static final Layout.SlotKey RAW_ORE = Layout.SlotKey.create("raw-ore");
    }

    static final class SlotGroupKeys {
        static final Layout.SlotGroupKey RAW_ORE_MACERATE =
                Layout.SlotGroupKey.create("raw-ore-macerate");
        static final Layout.SlotGroupKey CRUSHED_ORE_MACERATE =
                Layout.SlotGroupKey.create("crushed-ore-macerate");
        static final Layout.SlotGroupKey CRUSHED_ORE_WASH =
                Layout.SlotGroupKey.create("crushed-ore-wash");
        static final Layout.SlotGroupKey CRUSHED_ORE_BATH_MERCURY =
                Layout.SlotGroupKey.create("crushed-ore-bath-mercury");
        static final Layout.SlotGroupKey CRUSHED_ORE_BATH_SODIUM_PERSULFATE =
                Layout.SlotGroupKey.create("crushed-ore-bath-sodium-persulfate");
        static final Layout.SlotGroupKey PURIFIED_ORE_MACERATE =
                Layout.SlotGroupKey.create("purified-ore-macerate");
        static final Layout.SlotGroupKey PURIFIED_ORE_SIFT =
                Layout.SlotGroupKey.create("purified-ore-sift");
        static final Layout.SlotGroupKey IMPURE_DUST_CENTRIFUGE =
                Layout.SlotGroupKey.create("impure-dust-centrifuge");
        static final Layout.SlotGroupKey PURIFIED_DUST_CENTRIFUGE =
                Layout.SlotGroupKey.create("purified-dust-centrifuge");
        static final Layout.SlotGroupKey PURIFIED_DUST_ELECTROMAGNETIC_SEPARATE =
                Layout.SlotGroupKey.create("purified-dust-electromagnetic-separate");
        static final Layout.SlotGroupKey ORE_THERMAL_CENTRIFUGE =
                Layout.SlotGroupKey.create("ore-thermal-centrifuge");
        static final Layout.SlotGroupKey ORE_THERMAL_CENTRIFUGE_MACERATE =
                Layout.SlotGroupKey.create("ore-thermal-centrifuge-macerate");
        static final Layout.SlotGroupKey ADDITIONAL_RECIPE_OUTPUTS =
                Layout.SlotGroupKey.create("additional-recipe-outputs");
    }

    static final class AdditionalRecipeLabelPositions {
        static final Point FURNACE = Grid.GRID.grid(2, 0);
        static final Point ELECTRIC_BLAST_FURNACE = Grid.GRID.grid(4, 0);
        static final Point CHEMICAL_REACTOR = Grid.GRID.grid(6, 0);
        static final Point AUTOCLAVE = Grid.GRID.grid(8, 0);
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
        layoutsBuilder.add(buildCrushedOreWashLayout());
        layoutsBuilder.add(buildCrushedOreBathMercuryLayout());
        layoutsBuilder.add(buildCrushedOreBathSodiumPersulfateLayout());
        layoutsBuilder.add(buildCrushedOreMacerateLayout());
        layoutsBuilder.add(buildPurifiedOreMacerateLayout());
        layoutsBuilder.add(buildPurifiedOreSiftLayout());
        layoutsBuilder.add(buildImpureDustCentrifugeLayout());
        layoutsBuilder.add(buildPurifiedDustCentrifugeLayout());
        layoutsBuilder.add(buildPurifiedDustElectromagneticSeparateLayout());
        layoutsBuilder.add(buildOreThermalCentrifugeLayout());
        layoutsBuilder.add(buildOreThermalCentrifugeMacerateLayout());
        layoutsBuilder.add(buildAdditionalRecipeOutputsLayout());
        layouts = layoutsBuilder.build();
    }

    /** All layouts are optional. */
    ImmutableList<Layout> layouts() {
        return layouts;
    }

    private Layout buildRawOreLayout() {
        Slot inputSlot =
                Slot.builder(Grid.GRID.grid(6, 4))
                        .setDrawFunction(Draw::drawBigSlot)
                        .setTooltip(
                                Tooltip.create(
                                        Lang.GREGTECH_5_ORE_PROCESSING.trans("oreslot"),
                                        Tooltip.SLOT_FORMATTING))
                        .build();

        AllDiagramsButton allDiagramsButton = new AllDiagramsButton(info, Grid.GRID.grid(0, 0));

        return Layout.builder()
                .putSlot(SlotKeys.RAW_ORE, inputSlot)
                .addInteractable(allDiagramsButton)
                .build();
    }

    private Layout buildRawOreMacerateLayout() {
        Lines lines =
                Lines.builder(Grid.GRID.grid(6, 4))
                        .addSegment(Grid.GRID.grid(4, 4))
                        .addArrow(Grid.GRID.edge(4, 10, Grid.Direction.N))
                        .build();

        CustomInteractable label =
                labelHandler.buildLabel(LabelHandler.ItemLabel.MACERATOR, Grid.GRID.grid(4, 8));

        SlotGroup outputSlots =
                SlotGroup.builder(1, 2, Grid.GRID.grid(4, 10), Grid.Direction.S)
                        .setDefaultTooltip(
                                Tooltip.create(
                                        Lang.GREGTECH_5_ORE_PROCESSING.trans("maceratorslot"),
                                        Tooltip.SLOT_FORMATTING))
                        .build();

        return Layout.builder()
                .addLines(lines)
                .addInteractable(label)
                .putSlotGroup(SlotGroupKeys.RAW_ORE_MACERATE, outputSlots)
                .build();
    }

    private Layout buildCrushedOreMacerateLayout() {
        Lines lines =
                Lines.builder(Grid.GRID.grid(4, 10))
                        .addSegment(Grid.GRID.grid(2, 10))
                        .addArrow(Grid.GRID.edge(2, 14, Grid.Direction.N))
                        .build();

        CustomInteractable label =
                labelHandler.buildLabel(LabelHandler.ItemLabel.MACERATOR, Grid.GRID.grid(2, 12));

        SlotGroup outputSlots =
                SlotGroup.builder(1, 2, Grid.GRID.grid(2, 14), Grid.Direction.S)
                        .setDefaultTooltip(
                                Tooltip.create(
                                        Lang.GREGTECH_5_ORE_PROCESSING.trans("maceratorslot"),
                                        Tooltip.SLOT_FORMATTING))
                        .build();

        return Layout.builder()
                .addLines(lines)
                .addInteractable(label)
                .putSlotGroup(SlotGroupKeys.CRUSHED_ORE_MACERATE, outputSlots)
                .build();
    }

    private Layout buildCrushedOreWashLayout() {
        Lines lines =
                Lines.builder(Grid.GRID.grid(4, 10))
                        .addSegment(Grid.GRID.grid(5, 10))
                        .addSegment(Grid.GRID.grid(5, 8))
                        .addSegment(Grid.GRID.grid(8, 8))
                        .addArrow(Grid.GRID.edge(8, 10, Grid.Direction.N))
                        .build();

        CustomInteractable label =
                labelHandler.buildLabel(
                        LabelHandler.ItemLabel.ORE_WASHING_PLANT, Grid.GRID.grid(8, 8));

        SlotGroup outputSlots =
                SlotGroup.builder(1, 2, Grid.GRID.grid(8, 10), Grid.Direction.S)
                        .setDefaultTooltip(
                                Tooltip.create(
                                        Lang.GREGTECH_5_ORE_PROCESSING.trans("orewashingplantslot"),
                                        Tooltip.SLOT_FORMATTING))
                        .build();

        return Layout.builder()
                .addLines(lines)
                .addInteractable(label)
                .putSlotGroup(SlotGroupKeys.CRUSHED_ORE_WASH, outputSlots)
                .build();
    }

    private Layout buildCrushedOreBathMercuryLayout() {
        Lines lines =
                Lines.builder(Grid.GRID.grid(4, 10))
                        .addSegment(Grid.GRID.grid(0, 10))
                        .addArrow(Grid.GRID.edge(0, 6, Grid.Direction.S))
                        .build();

        CustomInteractable label =
                labelHandler.buildLabel(
                        LabelHandler.ItemLabel.CHEMICAL_BATH, Grid.GRID.grid(0, 8));

        SlotGroup outputSlots =
                SlotGroup.builder(1, 3, Grid.GRID.grid(0, 6), Grid.Direction.N)
                        .setDefaultTooltip(
                                Tooltip.create(
                                        Lang.GREGTECH_5_ORE_PROCESSING.trans("chemicalbathslot"),
                                        Tooltip.SLOT_FORMATTING))
                        .setSlot(
                                0, 0,
                                SlotGroup.slotBuilder()
                                        .setTooltip(
                                                Tooltip.create(
                                                        Lang.GREGTECH_5_ORE_PROCESSING.trans(
                                                                "chemicalbathfluidslot"),
                                                        Tooltip.SLOT_FORMATTING))
                                        .build())
                        .build();

        return Layout.builder()
                .addLines(lines)
                .addInteractable(label)
                .putSlotGroup(SlotGroupKeys.CRUSHED_ORE_BATH_MERCURY, outputSlots)
                .build();
    }

    private Layout buildCrushedOreBathSodiumPersulfateLayout() {
        Lines lines =
                Lines.builder(Grid.GRID.grid(4, 10))
                        .addSegment(Grid.GRID.grid(2, 10))
                        .addArrow(Grid.GRID.edge(2, 6, Grid.Direction.S))
                        .build();

        CustomInteractable label =
                labelHandler.buildLabel(
                        LabelHandler.ItemLabel.CHEMICAL_BATH, Grid.GRID.grid(2, 8));

        SlotGroup outputSlots =
                SlotGroup.builder(1, 3, Grid.GRID.grid(2, 6), Grid.Direction.N)
                        .setDefaultTooltip(
                                Tooltip.create(
                                        Lang.GREGTECH_5_ORE_PROCESSING.trans("chemicalbathslot"),
                                        Tooltip.SLOT_FORMATTING))
                        .setSlot(
                                0, 0,
                                SlotGroup.slotBuilder()
                                        .setTooltip(
                                                Tooltip.create(
                                                        Lang.GREGTECH_5_ORE_PROCESSING.trans(
                                                                "chemicalbathfluidslot"),
                                                        Tooltip.SLOT_FORMATTING))
                                        .build())
                        .build();

        return Layout.builder()
                .addLines(lines)
                .addInteractable(label)
                .putSlotGroup(SlotGroupKeys.CRUSHED_ORE_BATH_SODIUM_PERSULFATE, outputSlots)
                .build();
    }

    private Layout buildPurifiedOreMacerateLayout() {
        Lines lines =
                Lines.builder(Grid.GRID.grid(8, 10))
                        .addSegment(Grid.GRID.grid(10, 10))
                        .addArrow(Grid.GRID.edge(10, 14, Grid.Direction.N))
                        .build();

        CustomInteractable label =
                labelHandler.buildLabel(LabelHandler.ItemLabel.MACERATOR, Grid.GRID.grid(10, 12));

        SlotGroup outputSlots =
                SlotGroup.builder(1, 2, Grid.GRID.grid(10, 14), Grid.Direction.S)
                        .setDefaultTooltip(
                                Tooltip.create(
                                        Lang.GREGTECH_5_ORE_PROCESSING.trans("maceratorslot"),
                                        Tooltip.SLOT_FORMATTING))
                        .build();

        return Layout.builder()
                .addLines(lines)
                .addInteractable(label)
                .putSlotGroup(SlotGroupKeys.PURIFIED_ORE_MACERATE, outputSlots)
                .build();
    }

    private Layout buildPurifiedOreSiftLayout() {
        Lines lines =
                Lines.builder(Grid.GRID.grid(8, 10))
                        .addSegment(Grid.GRID.grid(10, 10))
                        .addArrow(Grid.GRID.edge(10, 6, Grid.Direction.S))
                        .build();

        CustomInteractable label =
                labelHandler.buildLabel(LabelHandler.ItemLabel.SIFTER, Grid.GRID.grid(10, 8));

        SlotGroup outputSlots =
                SlotGroup.builder(3, 3, Grid.GRID.grid(10, 6), Grid.Direction.N)
                        .setDefaultTooltip(
                                Tooltip.create(
                                        Lang.GREGTECH_5_ORE_PROCESSING.trans("sifterslot"),
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
                Lines.builder(Grid.GRID.grid(2, 14))
                        .addSegment(Grid.GRID.grid(0, 14))
                        .addArrow(Grid.GRID.edge(0, 16, Grid.Direction.N))
                        .build();

        CustomInteractable label =
                labelHandler.buildLabel(LabelHandler.ItemLabel.CENTRIFUGE, Grid.GRID.grid(0, 14));

        SlotGroup outputSlots =
                SlotGroup.builder(1, 2, Grid.GRID.grid(0, 16), Grid.Direction.S)
                        .setDefaultTooltip(
                                Tooltip.create(
                                        Lang.GREGTECH_5_ORE_PROCESSING.trans("centrifugeslot"),
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
                Lines.builder(Grid.GRID.grid(10, 14))
                        .addSegment(Grid.GRID.grid(8, 14))
                        .addArrow(Grid.GRID.edge(8, 16, Grid.Direction.N))
                        .build();

        CustomInteractable label =
                labelHandler.buildLabel(LabelHandler.ItemLabel.CENTRIFUGE, Grid.GRID.grid(8, 14));

        SlotGroup outputSlots =
                SlotGroup.builder(1, 2, Grid.GRID.grid(8, 16), Grid.Direction.S)
                        .setDefaultTooltip(
                                Tooltip.create(
                                        Lang.GREGTECH_5_ORE_PROCESSING.trans("centrifugeslot"),
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
                Lines.builder(Grid.GRID.grid(10, 14))
                        .addSegment(Grid.GRID.grid(12, 14))
                        .addArrow(Grid.GRID.edge(12, 12, Grid.Direction.S))
                        .build();

        CustomInteractable label =
                labelHandler.buildLabel(
                        LabelHandler.ItemLabel.ELECTROMAGNETIC_SEPARATOR, Grid.GRID.grid(12, 14));

        SlotGroup outputSlots =
                SlotGroup.builder(1, 3, Grid.GRID.grid(12, 12), Grid.Direction.N)
                        .setDefaultTooltip(
                                Tooltip.create(
                                        Lang.GREGTECH_5_ORE_PROCESSING.trans(
                                                "electromagneticseparatorslot"),
                                        Tooltip.SLOT_FORMATTING))
                        .build();

        return Layout.builder()
                .addLines(lines)
                .addInteractable(label)
                .putSlotGroup(SlotGroupKeys.PURIFIED_DUST_ELECTROMAGNETIC_SEPARATE, outputSlots)
                .build();
    }

    private Layout buildOreThermalCentrifugeLayout() {
        Lines lines =
                Lines.builder(Grid.GRID.grid(4, 10))
                        .addSegment(Grid.GRID.grid(5, 10))
                        .addSegment(Grid.GRID.grid(5, 12))
                        .addSegment(Grid.GRID.grid(7, 12))
                        .addSegment(Grid.GRID.grid(7, 10))
                        .addSegment(Grid.GRID.grid(8, 10))
                        .move(Grid.GRID.grid(6, 12))
                        .addArrow(Grid.GRID.edge(6, 14, Grid.Direction.N))
                        .build();

        CustomInteractable label =
                labelHandler.buildLabel(
                        LabelHandler.ItemLabel.THERMAL_CENTRIFUGE, Grid.GRID.grid(6, 12));

        SlotGroup outputSlots =
                SlotGroup.builder(1, 2, Grid.GRID.grid(6, 14), Grid.Direction.S)
                        .setDefaultTooltip(
                                Tooltip.create(
                                        Lang.GREGTECH_5_ORE_PROCESSING.trans(
                                                "thermalcentrifugeslot"),
                                        Tooltip.SLOT_FORMATTING))
                        .build();

        return Layout.builder()
                .addLines(lines)
                .addInteractable(label)
                .putSlotGroup(SlotGroupKeys.ORE_THERMAL_CENTRIFUGE, outputSlots)
                .build();
    }

    private Layout buildOreThermalCentrifugeMacerateLayout() {
        Lines lines =
                Lines.builder(Grid.GRID.grid(6, 14))
                        .addSegment(Grid.GRID.grid(4, 14))
                        .addArrow(Grid.GRID.edge(4, 16, Grid.Direction.N))
                        .build();

        CustomInteractable label =
                labelHandler.buildLabel(LabelHandler.ItemLabel.MACERATOR, Grid.GRID.grid(4, 14));

        SlotGroup outputSlots =
                SlotGroup.builder(1, 2, Grid.GRID.grid(4, 16), Grid.Direction.S)
                        .setDefaultTooltip(
                                Tooltip.create(
                                        Lang.GREGTECH_5_ORE_PROCESSING.trans("maceratorslot"),
                                        Tooltip.SLOT_FORMATTING))
                        .build();

        return Layout.builder()
                .addLines(lines)
                .addInteractable(label)
                .putSlotGroup(SlotGroupKeys.ORE_THERMAL_CENTRIFUGE_MACERATE, outputSlots)
                .build();
    }

    private Layout buildAdditionalRecipeOutputsLayout() {
        SlotGroup outputSlots =
                SlotGroup.builder(9, 2, Grid.GRID.grid(6, 20), Grid.Direction.S)
                        .setDefaultTooltip(
                                Tooltip.create(
                                        Lang.GREGTECH_5_ORE_PROCESSING.trans(
                                                "additionalrecipeoutputsslot"),
                                        Tooltip.SLOT_FORMATTING))
                        .build();

        return Layout.builder()
                .putSlotGroup(SlotGroupKeys.ADDITIONAL_RECIPE_OUTPUTS, outputSlots)
                .build();
    }
}
