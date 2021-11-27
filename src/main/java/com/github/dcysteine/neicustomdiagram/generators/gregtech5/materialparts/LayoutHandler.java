package com.github.dcysteine.neicustomdiagram.generators.gregtech5.materialparts;

import com.github.dcysteine.neicustomdiagram.api.diagram.DiagramGroupInfo;
import com.github.dcysteine.neicustomdiagram.api.diagram.interactable.AllDiagramsButton;
import com.github.dcysteine.neicustomdiagram.api.diagram.layout.Grid;
import com.github.dcysteine.neicustomdiagram.api.diagram.layout.Layout;
import com.github.dcysteine.neicustomdiagram.api.diagram.layout.Lines;
import com.github.dcysteine.neicustomdiagram.api.diagram.layout.Slot;
import com.github.dcysteine.neicustomdiagram.api.diagram.layout.SlotGroup;
import com.github.dcysteine.neicustomdiagram.api.diagram.tooltip.Tooltip;
import com.github.dcysteine.neicustomdiagram.api.draw.Point;
import com.github.dcysteine.neicustomdiagram.main.Lang;
import com.google.common.collect.ImmutableList;

class LayoutHandler {
    static final Point MATERIAL_INFO_POSITION = Grid.GRID.grid(2, 0);
    static final Point BLAST_FURNACE_INFO_POSITION = Grid.GRID.grid(4, 0);

    static final class SlotKeys {
        static final Layout.SlotKey GEM = Layout.SlotKey.create("gem");
        static final Layout.SlotKey LENS = Layout.SlotKey.create("lens");

        static final Layout.SlotKey HOT_INGOT = Layout.SlotKey.create("hot-ingot");
        static final Layout.SlotKey ALLOY_PLATE = Layout.SlotKey.create("alloy-plate");

        static final Layout.SlotKey RING = Layout.SlotKey.create("ring");
        static final Layout.SlotKey ROUND = Layout.SlotKey.create("round");
        static final Layout.SlotKey ROTOR = Layout.SlotKey.create("rotor");
        static final Layout.SlotKey CASING = Layout.SlotKey.create("casing");
        static final Layout.SlotKey BARS = Layout.SlotKey.create("bars");
        static final Layout.SlotKey FRAME_BOX = Layout.SlotKey.create("frame-box");

        static final Layout.SlotKey FINE_WIRE = Layout.SlotKey.create("fine-wire");
    }

    static final class SlotGroupKeys {
        static final Layout.SlotGroupKey RELATED_MATERIALS =
                Layout.SlotGroupKey.create("related-materials");

        static final Layout.SlotGroupKey FLUIDS = Layout.SlotGroupKey.create("fluids");
        static final Layout.SlotGroupKey HYDRO_CRACKED_FLUIDS =
                Layout.SlotGroupKey.create("hydro-cracked-fluids");
        static final Layout.SlotGroupKey STEAM_CRACKED_FLUIDS =
                Layout.SlotGroupKey.create("steam-cracked-fluids");

        static final Layout.SlotGroupKey GEMS = Layout.SlotGroupKey.create("gems");

        static final Layout.SlotGroupKey DUSTS = Layout.SlotGroupKey.create("dusts");

        static final Layout.SlotGroupKey INGOTS = Layout.SlotGroupKey.create("ingots");
        static final Layout.SlotGroupKey MULTI_INGOTS = Layout.SlotGroupKey.create("multi-ingots");

        static final Layout.SlotGroupKey PLATES = Layout.SlotGroupKey.create("plates");
        static final Layout.SlotGroupKey MULTI_PLATES = Layout.SlotGroupKey.create("multi-plates");

        static final Layout.SlotGroupKey RODS = Layout.SlotGroupKey.create("rods");
        static final Layout.SlotGroupKey BOLTS = Layout.SlotGroupKey.create("bolts");
        static final Layout.SlotGroupKey SPRINGS = Layout.SlotGroupKey.create("springs");
        static final Layout.SlotGroupKey GEARS = Layout.SlotGroupKey.create("gears");

        static final Layout.SlotGroupKey WIRES = Layout.SlotGroupKey.create("wires");
        static final Layout.SlotGroupKey CABLES = Layout.SlotGroupKey.create("cables");
        static final Layout.SlotGroupKey PIPES = Layout.SlotGroupKey.create("pipes");
        static final Layout.SlotGroupKey SPECIAL_PIPES =
                Layout.SlotGroupKey.create("special-pipes");
    }

    private final DiagramGroupInfo info;

    private ImmutableList<Layout> requiredLayouts;
    private ImmutableList<Layout> optionalLayouts;

    LayoutHandler(DiagramGroupInfo info) {
        this.info = info;
        this.requiredLayouts = null;
        this.optionalLayouts = null;
    }

    /** This method must be called before any other methods are called. */
    void initialize() {
        requiredLayouts = ImmutableList.of(buildHeaderLayout());

        ImmutableList.Builder<Layout> optionalLayoutsBuilder = new ImmutableList.Builder<>();
        optionalLayoutsBuilder.add(buildRelatedMaterialsLayout());
        optionalLayoutsBuilder.add(buildFluidsLayout());
        optionalLayoutsBuilder.add(buildCrackedFluidsLayout());
        optionalLayoutsBuilder.add(buildGemsLayout());
        optionalLayoutsBuilder.add(buildGemLayout());
        optionalLayoutsBuilder.add(buildLensLayout());
        optionalLayoutsBuilder.add(buildDustsLayout());
        optionalLayoutsBuilder.add(buildHotIngotLayout());
        optionalLayoutsBuilder.add(buildIngotsLayout());
        optionalLayoutsBuilder.add(buildMultiIngotsLayout());
        optionalLayoutsBuilder.add(buildAlloyPlateLayout());
        optionalLayoutsBuilder.add(buildPlatesLayout());
        optionalLayoutsBuilder.add(buildMultiPlatesLayout());
        optionalLayoutsBuilder.add(buildRodsLayout());
        optionalLayoutsBuilder.add(buildBoltsLayout());
        optionalLayoutsBuilder.add(buildRingLayout());
        optionalLayoutsBuilder.add(buildRoundLayout());
        optionalLayoutsBuilder.add(buildSpringsLayout());
        optionalLayoutsBuilder.add(buildGearsLayout());
        optionalLayoutsBuilder.add(buildRotorLayout());
        optionalLayoutsBuilder.add(buildCasingLayout());
        optionalLayoutsBuilder.add(buildBarsLayout());
        optionalLayoutsBuilder.add(buildFrameBoxLayout());
        optionalLayoutsBuilder.add(buildWiresLayout());
        optionalLayoutsBuilder.add(buildFineWireLayout());
        optionalLayoutsBuilder.add(buildCablesLayout());
        optionalLayoutsBuilder.add(buildPipesLayout());
        optionalLayoutsBuilder.add(buildSpecialPipesLayout());
        optionalLayouts = optionalLayoutsBuilder.build();
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
                .build();
    }

    private Layout buildRelatedMaterialsLayout() {
        return Layout.builder()
                .putSlotGroup(
                        SlotGroupKeys.RELATED_MATERIALS,
                        SlotGroup.builder(3, 1, Grid.GRID.grid(12, 0), Grid.Direction.W)
                                .setDefaultTooltip(
                                        Tooltip.create(
                                                Lang.GREGTECH_5_MATERIAL_PARTS.trans(
                                                        "relatedmaterialsslot"),
                                                Tooltip.SLOT_FORMATTING))
                                .build())
                .build();
    }

    private Layout buildFluidsLayout() {
        return Layout.builder()
                .putSlotGroup(
                        SlotGroupKeys.FLUIDS,
                        SlotGroup.builder(2, 2, Grid.GRID.grid(1, 3), Grid.Direction.C)
                                .setDefaultTooltip(
                                        Tooltip.create(
                                                Lang.GREGTECH_5_MATERIAL_PARTS.trans("fluidsslot"),
                                                Tooltip.SLOT_FORMATTING))
                                .build())
                .build();
    }

    private Layout buildCrackedFluidsLayout() {
        return Layout.builder()
                .addLines(
                        Lines.builder(Grid.GRID.grid(1, 3))
                                .addSegment(Grid.GRID.grid(3, 3))
                                .addSegment(Grid.GRID.grid(3, 2))
                                .addArrow(Grid.GRID.edge(4, 2, Grid.Direction.W))
                                .move(Grid.GRID.grid(3, 3))
                                .addSegment(Grid.GRID.grid(3, 4))
                                .addArrow(Grid.GRID.edge(4, 4, Grid.Direction.W))
                                .build())
                .putSlotGroup(
                        SlotGroupKeys.HYDRO_CRACKED_FLUIDS,
                        SlotGroup.builder(3, 1, Grid.GRID.grid(4, 2), Grid.Direction.E)
                                .setDefaultTooltip(
                                        Tooltip.create(
                                                Lang.GREGTECH_5_MATERIAL_PARTS.trans(
                                                        "hydrocrackedfluidsslot"),
                                                Tooltip.SLOT_FORMATTING))
                                .build())
                .putSlotGroup(
                        SlotGroupKeys.STEAM_CRACKED_FLUIDS,
                        SlotGroup.builder(3, 1, Grid.GRID.grid(4, 4), Grid.Direction.E)
                                .setDefaultTooltip(
                                        Tooltip.create(
                                                Lang.GREGTECH_5_MATERIAL_PARTS.trans(
                                                        "steamcrackedfluidsslot"),
                                                Tooltip.SLOT_FORMATTING))
                                .build())
                .build();
    }

    private Layout buildGemsLayout() {
        return Layout.builder()
                .addLines(
                        Lines.builder(Grid.GRID.grid(10, 4))
                                .addSegment(Grid.GRID.grid(10, 6))
                                .build())
                .putSlotGroup(
                        SlotGroupKeys.GEMS,
                        SlotGroup.builder(2, 2, Grid.GRID.grid(10, 2), Grid.Direction.SE)
                                .setDefaultTooltip(
                                        Tooltip.create(
                                                Lang.GREGTECH_5_MATERIAL_PARTS.trans("gemsslot"),
                                                Tooltip.SLOT_FORMATTING))
                                .build())
                .build();
    }

    private Layout buildGemLayout() {
        return Layout.builder()
                .putSlot(
                        SlotKeys.GEM,
                        Slot.builder(Grid.GRID.grid(10, 6))
                                .setTooltip(
                                        Tooltip.create(
                                                Lang.GREGTECH_5_MATERIAL_PARTS.trans("gemslot"),
                                                Tooltip.SLOT_FORMATTING))
                                .build())
                .build();
    }

    private Layout buildLensLayout() {
        return Layout.builder()
                .putSlot(
                        SlotKeys.LENS,
                        Slot.builder(Grid.GRID.grid(12, 6))
                                .setTooltip(
                                        Tooltip.create(
                                                Lang.GREGTECH_5_MATERIAL_PARTS.trans("lensslot"),
                                                Tooltip.SLOT_FORMATTING))
                                .build())
                .build();
    }

    private Layout buildDustsLayout() {
        return Layout.builder()
                .putSlotGroup(
                        SlotGroupKeys.DUSTS,
                        SlotGroup.builder(1, 3, Grid.GRID.grid(0, 8), Grid.Direction.S)
                                .setDefaultTooltip(
                                        Tooltip.create(
                                                Lang.GREGTECH_5_MATERIAL_PARTS.trans("dustsslot"),
                                                Tooltip.SLOT_FORMATTING))
                                .build())
                .build();
    }

    private Layout buildHotIngotLayout() {
        return Layout.builder()
                .addLines(
                        Lines.builder(Grid.GRID.grid(2, 6))
                                .addArrow(Grid.GRID.edge(2, 8, Grid.Direction.N))
                                .build())
                .putSlot(
                        SlotKeys.HOT_INGOT,
                        Slot.builder(Grid.GRID.grid(2, 6))
                                .setTooltip(
                                        Tooltip.create(
                                                Lang.GREGTECH_5_MATERIAL_PARTS.trans(
                                                        "hotingotslot"),
                                                Tooltip.SLOT_FORMATTING))
                                .build())
                .build();
    }

    private Layout buildIngotsLayout() {
        return Layout.builder()
                .putSlotGroup(
                        SlotGroupKeys.INGOTS,
                        SlotGroup.builder(1, 3, Grid.GRID.grid(2, 8), Grid.Direction.S)
                                .setDefaultTooltip(
                                        Tooltip.create(
                                                Lang.GREGTECH_5_MATERIAL_PARTS.trans("ingotsslot"),
                                                Tooltip.SLOT_FORMATTING))
                                .build())
                .build();
    }

    private Layout buildMultiIngotsLayout() {
        return Layout.builder()
                .addLines(
                        Lines.builder(Grid.GRID.grid(2, 8))
                                .addArrow(Grid.GRID.edge(4, 8, Grid.Direction.W))
                                .build())
                .putSlotGroup(
                        SlotGroupKeys.MULTI_INGOTS,
                        SlotGroup.builder(2, 2, Grid.GRID.grid(4, 8), Grid.Direction.SE)
                                .setDefaultTooltip(
                                        Tooltip.create(
                                                Lang.GREGTECH_5_MATERIAL_PARTS.trans(
                                                        "multiingotsslot"),
                                                Tooltip.SLOT_FORMATTING))
                                .build())
                .build();
    }

    private Layout buildAlloyPlateLayout() {
        return Layout.builder()
                .putSlot(
                        SlotKeys.ALLOY_PLATE,
                        Slot.builder(Grid.GRID.grid(8, 6))
                                .setTooltip(
                                        Tooltip.create(
                                                Lang.GREGTECH_5_MATERIAL_PARTS.trans(
                                                        "alloyplateslot"),
                                                Tooltip.SLOT_FORMATTING))
                                .build())
                .build();
    }

    private Layout buildPlatesLayout() {
        return Layout.builder()
                .putSlotGroup(
                        SlotGroupKeys.PLATES,
                        SlotGroup.builder(1, 3, Grid.GRID.grid(8, 8), Grid.Direction.S)
                                .setDefaultTooltip(
                                        Tooltip.create(
                                                Lang.GREGTECH_5_MATERIAL_PARTS.trans("platesslot"),
                                                Tooltip.SLOT_FORMATTING))
                                .build())
                .build();
    }

    private Layout buildMultiPlatesLayout() {
        return Layout.builder()
                .addLines(
                        Lines.builder(Grid.GRID.grid(8, 8))
                                .addArrow(Grid.GRID.edge(10, 8, Grid.Direction.W))
                                .build())
                .putSlotGroup(
                        SlotGroupKeys.MULTI_PLATES,
                        SlotGroup.builder(2, 2, Grid.GRID.grid(10, 8), Grid.Direction.SE)
                                .setDefaultTooltip(
                                        Tooltip.create(
                                                Lang.GREGTECH_5_MATERIAL_PARTS.trans(
                                                        "multiplatesslot"),
                                                Tooltip.SLOT_FORMATTING))
                                .build())
                .build();
    }

    private Layout buildRodsLayout() {
        return Layout.builder()
                .putSlotGroup(
                        SlotGroupKeys.RODS,
                        SlotGroup.builder(1, 2, Grid.GRID.grid(0, 14), Grid.Direction.S)
                                .setDefaultTooltip(
                                        Tooltip.create(
                                                Lang.GREGTECH_5_MATERIAL_PARTS.trans("rodsslot"),
                                                Tooltip.SLOT_FORMATTING))
                                .build())
                .build();
    }

    private Layout buildBoltsLayout() {
        return Layout.builder()
                .putSlotGroup(
                        SlotGroupKeys.BOLTS,
                        SlotGroup.builder(1, 2, Grid.GRID.grid(2, 14), Grid.Direction.S)
                                .setDefaultTooltip(
                                        Tooltip.create(
                                                Lang.GREGTECH_5_MATERIAL_PARTS.trans("boltsslot"),
                                                Tooltip.SLOT_FORMATTING))
                                .build())
                .build();
    }

    private Layout buildRingLayout() {
        return Layout.builder()
                .putSlot(
                        SlotKeys.RING,
                        Slot.builder(Grid.GRID.grid(4, 14))
                                .setTooltip(
                                        Tooltip.create(
                                                Lang.GREGTECH_5_MATERIAL_PARTS.trans("ringslot"),
                                                Tooltip.SLOT_FORMATTING))
                                .build())
                .build();
    }

    private Layout buildRoundLayout() {
        return Layout.builder()
                .putSlot(
                        SlotKeys.ROUND,
                        Slot.builder(Grid.GRID.grid(4, 16))
                                .setTooltip(
                                        Tooltip.create(
                                                Lang.GREGTECH_5_MATERIAL_PARTS.trans("roundslot"),
                                                Tooltip.SLOT_FORMATTING))
                                .build())
                .build();
    }

    private Layout buildSpringsLayout() {
        return Layout.builder()
                .putSlotGroup(
                        SlotGroupKeys.SPRINGS,
                        SlotGroup.builder(1, 2, Grid.GRID.grid(6, 14), Grid.Direction.S)
                                .setDefaultTooltip(
                                        Tooltip.create(
                                                Lang.GREGTECH_5_MATERIAL_PARTS.trans("springsslot"),
                                                Tooltip.SLOT_FORMATTING))
                                .build())
                .build();
    }

    private Layout buildGearsLayout() {
        return Layout.builder()
                .putSlotGroup(
                        SlotGroupKeys.GEARS,
                        SlotGroup.builder(1, 2, Grid.GRID.grid(8, 14), Grid.Direction.S)
                                .setDefaultTooltip(
                                        Tooltip.create(
                                                Lang.GREGTECH_5_MATERIAL_PARTS.trans("gearsslot"),
                                                Tooltip.SLOT_FORMATTING))
                                .build())
                .build();
    }

    private Layout buildRotorLayout() {
        return Layout.builder()
                .putSlot(
                        SlotKeys.ROTOR,
                        Slot.builder(Grid.GRID.grid(10, 14))
                                .setTooltip(
                                        Tooltip.create(
                                                Lang.GREGTECH_5_MATERIAL_PARTS.trans("rotorslot"),
                                                Tooltip.SLOT_FORMATTING))
                                .build())
                .build();
    }

    private Layout buildCasingLayout() {
        return Layout.builder()
                .putSlot(
                        SlotKeys.CASING,
                        Slot.builder(Grid.GRID.grid(10, 16))
                                .setTooltip(
                                        Tooltip.create(
                                                Lang.GREGTECH_5_MATERIAL_PARTS.trans("casingslot"),
                                                Tooltip.SLOT_FORMATTING))
                                .build())
                .build();
    }

    private Layout buildBarsLayout() {
        return Layout.builder()
                .putSlot(
                        SlotKeys.BARS,
                        Slot.builder(Grid.GRID.grid(12, 14))
                                .setTooltip(
                                        Tooltip.create(
                                                Lang.GREGTECH_5_MATERIAL_PARTS.trans("barsslot"),
                                                Tooltip.SLOT_FORMATTING))
                                .build())
                .build();
    }

    private Layout buildFrameBoxLayout() {
        return Layout.builder()
                .putSlot(
                        SlotKeys.FRAME_BOX,
                        Slot.builder(Grid.GRID.grid(12, 16))
                                .setTooltip(
                                        Tooltip.create(
                                                Lang.GREGTECH_5_MATERIAL_PARTS.trans(
                                                        "frameboxslot"),
                                                Tooltip.SLOT_FORMATTING))
                                .build())
                .build();
    }

    private Layout buildWiresLayout() {
        return Layout.builder()
                .putSlotGroup(
                        SlotGroupKeys.WIRES,
                        SlotGroup.builder(3, 2, Grid.GRID.grid(2, 20), Grid.Direction.S)
                                .setDefaultTooltip(
                                        Tooltip.create(
                                                Lang.GREGTECH_5_MATERIAL_PARTS.trans("wiresslot"),
                                                Tooltip.SLOT_FORMATTING))
                                .build())
                .build();
    }

    private Layout buildFineWireLayout() {
        return Layout.builder()
                .putSlot(
                        SlotKeys.FINE_WIRE,
                        Slot.builder(Grid.GRID.grid(2, 18))
                                .setTooltip(
                                        Tooltip.create(
                                                Lang.GREGTECH_5_MATERIAL_PARTS.trans(
                                                        "finewireslot"),
                                                Tooltip.SLOT_FORMATTING))
                                .build())
                .build();
    }

    private Layout buildCablesLayout() {
        return Layout.builder()
                .addLines(
                        Lines.builder(Grid.GRID.grid(2, 22))
                                .addArrow(Grid.GRID.edge(2, 24, Grid.Direction.N))
                                .build())
                .putSlotGroup(
                        SlotGroupKeys.CABLES,
                        SlotGroup.builder(3, 2, Grid.GRID.grid(2, 24), Grid.Direction.S)
                                .setDefaultTooltip(
                                        Tooltip.create(
                                                Lang.GREGTECH_5_MATERIAL_PARTS.trans("cablesslot"),
                                                Tooltip.SLOT_FORMATTING))
                                .build())
                .build();
    }

    private Layout buildPipesLayout() {
        return Layout.builder()
                .putSlotGroup(
                        SlotGroupKeys.PIPES,
                        SlotGroup.builder(3, 2, Grid.GRID.grid(10, 20), Grid.Direction.S)
                                .setDefaultTooltip(
                                        Tooltip.create(
                                                Lang.GREGTECH_5_MATERIAL_PARTS.trans("pipesslot"),
                                                Tooltip.SLOT_FORMATTING))
                                .build())
                .build();
    }

    private Layout buildSpecialPipesLayout() {
        return Layout.builder()
                .addLines(
                        Lines.builder(Grid.GRID.grid(10, 22))
                                .addArrow(Grid.GRID.edge(10, 24, Grid.Direction.N))
                                .build())
                .putSlotGroup(
                        SlotGroupKeys.SPECIAL_PIPES,
                        SlotGroup.builder(3, 2, Grid.GRID.grid(10, 24), Grid.Direction.S)
                                .setDefaultTooltip(
                                        Tooltip.create(
                                                Lang.GREGTECH_5_MATERIAL_PARTS.trans(
                                                        "specialpipesslot"),
                                                Tooltip.SLOT_FORMATTING))
                                .build())
                .build();
    }
}
