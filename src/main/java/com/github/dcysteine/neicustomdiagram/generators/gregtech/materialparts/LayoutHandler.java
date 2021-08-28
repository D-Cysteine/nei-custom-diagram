package com.github.dcysteine.neicustomdiagram.generators.gregtech.materialparts;

import com.github.dcysteine.neicustomdiagram.api.Lang;
import com.github.dcysteine.neicustomdiagram.api.diagram.DiagramGroupInfo;
import com.github.dcysteine.neicustomdiagram.api.diagram.interactable.AllDiagramsButton;
import com.github.dcysteine.neicustomdiagram.api.diagram.layout.Grid;
import com.github.dcysteine.neicustomdiagram.api.diagram.layout.Layout;
import com.github.dcysteine.neicustomdiagram.api.diagram.layout.Lines;
import com.github.dcysteine.neicustomdiagram.api.diagram.layout.Slot;
import com.github.dcysteine.neicustomdiagram.api.diagram.layout.SlotGroup;
import com.github.dcysteine.neicustomdiagram.api.diagram.tooltip.Tooltip;
import com.github.dcysteine.neicustomdiagram.api.draw.Point;
import com.google.common.collect.ImmutableList;

class LayoutHandler {
    static final Point MATERIAL_INFO_POSITION = Grid.GRID.grid(2, 0);
    static final Point BLAST_FURNACE_INFO_POSITION = Grid.GRID.grid(4, 0);

    static final class SlotKeys {
        static final String GEM = "gem";
        static final String LENS = "lens";

        static final String HOT_INGOT = "hot-ingot";

        static final String RING = "ring";
        static final String ROUND = "round";
        static final String CASING = "casing";
        static final String FRAME_BOX = "frame-box";

        static final String FINE_WIRE = "fine-wire";
    }

    static final class SlotGroupKeys {
        static final String RELATED_MATERIALS = "related-materials";

        static final String FLUIDS = "fluids";
        static final String HYDRO_CRACKED_FLUIDS = "hydro-cracked-fluids";
        static final String STEAM_CRACKED_FLUIDS = "steam-cracked-fluids";

        static final String GEMS = "gems";

        static final String DUSTS = "dusts";

        static final String INGOTS = "ingots";
        static final String MULTI_INGOTS = "multi-ingots";

        static final String PLATES = "plates";
        static final String MULTI_PLATES = "multi-plates";

        static final String RODS = "rods";
        static final String BOLTS = "bolts";
        static final String SPRINGS = "springs";
        static final String GEARS = "gears";
        static final String ROTORS = "rotors";

        static final String WIRES = "wires";
        static final String CABLES = "cables";
        static final String PIPES = "pipes";
        static final String SPECIAL_PIPES = "special-pipes";
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
        ImmutableList.Builder<Layout> requiredLayoutsBuilder = new ImmutableList.Builder<>();
        requiredLayoutsBuilder.add(buildHeaderLayout());
        requiredLayouts = requiredLayoutsBuilder.build();

        ImmutableList.Builder<Layout> optionalLayoutsBuilder = new ImmutableList.Builder<>();
        optionalLayoutsBuilder.add(buildRelatedMaterialsLayout());
        optionalLayoutsBuilder.add(buildFluidsLayout());
        optionalLayoutsBuilder.add(buildCrackedFluidsLayout());
        optionalLayoutsBuilder.add(buildGemsLayout());
        optionalLayoutsBuilder.add(buildGemLayout());
        optionalLayoutsBuilder.add(buildLensLayout());
        optionalLayoutsBuilder.add(buildDustsLayout());
        optionalLayoutsBuilder.add(buildIngotsLayout());
        optionalLayoutsBuilder.add(buildHotIngotLayout());
        optionalLayoutsBuilder.add(buildMultiIngotsLayout());
        optionalLayoutsBuilder.add(buildPlatesLayout());
        optionalLayoutsBuilder.add(buildMultiPlatesLayout());
        optionalLayoutsBuilder.add(buildRodsLayout());
        optionalLayoutsBuilder.add(buildBoltsLayout());
        optionalLayoutsBuilder.add(buildRingLayout());
        optionalLayoutsBuilder.add(buildRoundLayout());
        optionalLayoutsBuilder.add(buildSpringsLayout());
        optionalLayoutsBuilder.add(buildGearsLayout());
        optionalLayoutsBuilder.add(buildCasingLayout());
        optionalLayoutsBuilder.add(buildFrameBoxLayout());
        optionalLayoutsBuilder.add(buildRotorsLayout());
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
                        SlotGroup.builder(4, 1, Grid.GRID.grid(12, 0), Grid.Direction.W)
                                .setDefaultTooltip(
                                        Tooltip.create(
                                                Lang.GREGTECH_MATERIAL_PARTS.trans(
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
                                                Lang.GREGTECH_MATERIAL_PARTS.trans("fluidsslot"),
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
                                                Lang.GREGTECH_MATERIAL_PARTS.trans(
                                                        "hydrocrackedfluidsslot"),
                                                Tooltip.SLOT_FORMATTING))
                                .build())
                .putSlotGroup(
                        SlotGroupKeys.STEAM_CRACKED_FLUIDS,
                        SlotGroup.builder(3, 1, Grid.GRID.grid(4, 4), Grid.Direction.E)
                                .setDefaultTooltip(
                                        Tooltip.create(
                                                Lang.GREGTECH_MATERIAL_PARTS.trans(
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
                                                Lang.GREGTECH_MATERIAL_PARTS.trans("gemsslot"),
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
                                                Lang.GREGTECH_MATERIAL_PARTS.trans("gemslot"),
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
                                                Lang.GREGTECH_MATERIAL_PARTS.trans("lensslot"),
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
                                                Lang.GREGTECH_MATERIAL_PARTS.trans("dustsslot"),
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
                                                Lang.GREGTECH_MATERIAL_PARTS.trans("ingotsslot"),
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
                                                Lang.GREGTECH_MATERIAL_PARTS.trans("hotingotslot"),
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
                                                Lang.GREGTECH_MATERIAL_PARTS.trans(
                                                        "multiingotsslot"),
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
                                                Lang.GREGTECH_MATERIAL_PARTS.trans("platesslot"),
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
                                                Lang.GREGTECH_MATERIAL_PARTS.trans(
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
                                                Lang.GREGTECH_MATERIAL_PARTS.trans("rodsslot"),
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
                                                Lang.GREGTECH_MATERIAL_PARTS.trans("boltsslot"),
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
                                                Lang.GREGTECH_MATERIAL_PARTS.trans("ringslot"),
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
                                                Lang.GREGTECH_MATERIAL_PARTS.trans("roundslot"),
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
                                                Lang.GREGTECH_MATERIAL_PARTS.trans("springsslot"),
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
                                                Lang.GREGTECH_MATERIAL_PARTS.trans("gearsslot"),
                                                Tooltip.SLOT_FORMATTING))
                                .build())
                .build();
    }

    private Layout buildCasingLayout() {
        return Layout.builder()
                .putSlot(
                        SlotKeys.CASING,
                        Slot.builder(Grid.GRID.grid(10, 14))
                                .setTooltip(
                                        Tooltip.create(
                                                Lang.GREGTECH_MATERIAL_PARTS.trans("casingslot"),
                                                Tooltip.SLOT_FORMATTING))
                                .build())
                .build();
    }

    private Layout buildFrameBoxLayout() {
        return Layout.builder()
                .putSlot(
                        SlotKeys.FRAME_BOX,
                        Slot.builder(Grid.GRID.grid(10, 16))
                                .setTooltip(
                                        Tooltip.create(
                                                Lang.GREGTECH_MATERIAL_PARTS.trans("frameboxslot"),
                                                Tooltip.SLOT_FORMATTING))
                                .build())
                .build();
    }

    private Layout buildRotorsLayout() {
        return Layout.builder()
                .putSlotGroup(
                        SlotGroupKeys.ROTORS,
                        SlotGroup.builder(1, 2, Grid.GRID.grid(12, 14), Grid.Direction.S)
                                .setDefaultTooltip(
                                        Tooltip.create(
                                                Lang.GREGTECH_MATERIAL_PARTS.trans("rotorsslot"),
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
                                                Lang.GREGTECH_MATERIAL_PARTS.trans("wiresslot"),
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
                                                Lang.GREGTECH_MATERIAL_PARTS.trans("finewireslot"),
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
                                                Lang.GREGTECH_MATERIAL_PARTS.trans("cablesslot"),
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
                                                Lang.GREGTECH_MATERIAL_PARTS.trans("pipesslot"),
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
                                                Lang.GREGTECH_MATERIAL_PARTS.trans(
                                                        "specialpipesslot"),
                                                Tooltip.SLOT_FORMATTING))
                                .build())
                .build();
    }
}
