package com.github.dcysteine.neicustomdiagram.generators.forge.fluidcontainers;

import com.github.dcysteine.neicustomdiagram.api.diagram.Diagram;
import com.github.dcysteine.neicustomdiagram.api.diagram.DiagramGenerator;
import com.github.dcysteine.neicustomdiagram.api.diagram.DiagramGroup;
import com.github.dcysteine.neicustomdiagram.api.diagram.DiagramGroupInfo;
import com.github.dcysteine.neicustomdiagram.api.diagram.component.Component;
import com.github.dcysteine.neicustomdiagram.api.diagram.component.DisplayComponent;
import com.github.dcysteine.neicustomdiagram.api.diagram.component.FluidComponent;
import com.github.dcysteine.neicustomdiagram.api.diagram.component.ItemComponent;
import com.github.dcysteine.neicustomdiagram.api.diagram.interactable.AllDiagramsButton;
import com.github.dcysteine.neicustomdiagram.api.diagram.interactable.Interactable;
import com.github.dcysteine.neicustomdiagram.api.diagram.layout.Grid;
import com.github.dcysteine.neicustomdiagram.api.diagram.layout.Layout;
import com.github.dcysteine.neicustomdiagram.api.diagram.layout.SlotGroup;
import com.github.dcysteine.neicustomdiagram.api.diagram.matcher.CustomDiagramMatcher;
import com.github.dcysteine.neicustomdiagram.api.diagram.tooltip.Tooltip;
import com.github.dcysteine.neicustomdiagram.mod.Lang;
import com.github.dcysteine.neicustomdiagram.mod.Registry;
import com.github.dcysteine.neicustomdiagram.mod.config.DiagramGroupVisibility;
import com.github.dcysteine.neicustomdiagram.util.FluidDictUtil;
import com.github.dcysteine.neicustomdiagram.util.gregtech5.GregTechFluidDictUtil;
import com.google.common.collect.ImmutableBiMap;
import com.google.common.collect.ImmutableListMultimap;
import com.google.common.collect.Lists;
import com.google.common.collect.MultimapBuilder;
import com.google.common.collect.SetMultimap;
import net.minecraft.init.Items;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidRegistry;

import java.util.List;
import java.util.Optional;

/**
 * Generates diagrams showing all Forge-registered fluid containers for a given fluid.
 *
 * <p>This diagram group also supports showing all fluids that an empty fluid container can contain.
 */
public final class ForgeFluidContainers implements DiagramGenerator {
    public static final ItemComponent ICON = ItemComponent.create(Items.water_bucket, 0);

    private static final Layout.SlotGroupKey SLOT_GROUP_FLUIDS =
            Layout.SlotGroupKey.create("fluids");
    private static final Layout.SlotGroupKey SLOT_GROUP_CONTAINERS =
            Layout.SlotGroupKey.create("containers");

    private final DiagramGroupInfo info;

    private Layout layout;
    private ImmutableBiMap<FluidComponent, Diagram> fluidsMap;
    private ImmutableListMultimap<ItemComponent, Diagram> emptyContainersMultimap;

    public ForgeFluidContainers(String groupId) {
        this.info =
                DiagramGroupInfo.builder(
                                Lang.FORGE_FLUID_CONTAINERS.trans("groupname"),
                                groupId, ICON, 2)
                        .setDefaultVisibility(DiagramGroupVisibility.DISABLED)
                        .setDescription(
                                "This diagram displays Forge registered fluids"
                                        + " and fluid containers.")
                        .build();
    }

    @Override
    public DiagramGroupInfo info() {
        return info;
    }

    @Override
    public DiagramGroup generate() {
        layout = buildLayout();

        ImmutableBiMap.Builder<FluidComponent, Diagram> fluidsMapBuilder = ImmutableBiMap.builder();
        // We use a set multimap here to get free deduping, but we'll need to convert this to a list
        // multimap later because we need to return lists.
        SetMultimap<ItemComponent, Diagram> emptyContainersSetMultimap =
                MultimapBuilder.hashKeys().hashSetValues().build();
        for (Fluid fluid : FluidRegistry.getRegisteredFluids().values()) {
            FluidComponent fluidComponent = FluidComponent.create(fluid);
            fluidsMapBuilder.put(
                    fluidComponent,
                    generateDiagram(fluidComponent, emptyContainersSetMultimap));
        }
        fluidsMap = fluidsMapBuilder.build();
        emptyContainersMultimap = ImmutableListMultimap.copyOf(emptyContainersSetMultimap);

        return new DiagramGroup(
                info, new CustomDiagramMatcher(fluidsMap.values(), this::getDiagram));
    }

    private List<Diagram> getDiagram(Interactable.RecipeType unused, Component component) {
        Optional<FluidComponent> fluidOptional = FluidDictUtil.getFluidContents(component);
        if (!fluidOptional.isPresent() && Registry.ModDependency.GREGTECH_5.isLoaded()) {
            // Try looking up GregTech fluid display stack.
            if (component.type() == Component.ComponentType.ITEM) {
                fluidOptional = GregTechFluidDictUtil.displayItemToFluid((ItemComponent) component);
            }
        }
        if (fluidOptional.isPresent()) {
            return Lists.newArrayList(fluidsMap.get(fluidOptional.get()));
        }

        if (component.type() == Component.ComponentType.ITEM) {
            return emptyContainersMultimap.get((ItemComponent) component);
        } else {
            return Lists.newArrayList();
        }
    }

    private Diagram generateDiagram(
            FluidComponent fluid, SetMultimap<ItemComponent, Diagram> emptyContainersMultimap) {
        List<DisplayComponent> fluidContainers = FluidDictUtil.getFluidContainers(fluid);
        // Remove the first element, which is the fluid.
        fluidContainers = fluidContainers.subList(1, fluidContainers.size());

        Diagram.Builder builder = Diagram.builder().addLayout(layout);
        builder.autoInsertIntoSlotGroup(SLOT_GROUP_CONTAINERS).insertEachSafe(fluidContainers);
        Diagram.Builder.SlotGroupAutoSubBuilder fluidsBuilder =
                builder.autoInsertIntoSlotGroup(SLOT_GROUP_FLUIDS);

        fluidsBuilder.insertIntoNextSlot(
                DisplayComponent.builder(fluid)
                        .setAdditionalTooltip(
                                Tooltip.builder()
                                        .setFormatting(Tooltip.SLOT_FORMATTING)
                                        .addTextLine(
                                                Lang.FORGE_FLUID_CONTAINERS.trans("fluidlabel"))
                                        .addSpacing()
                                        .setFormatting(Tooltip.INFO_FORMATTING)
                                        .addTextLine(
                                                Lang.FORGE_FLUID_CONTAINERS.transf(
                                                        "fluidnamelabel", fluid.fluid().getName()))
                                        .build())
                        .build());

        Optional<ItemComponent> blockOptional = FluidDictUtil.fluidToItem(fluid);
        blockOptional.ifPresent(
                block ->
                        fluidsBuilder.insertIntoNextSlot(
                                DisplayComponent.builder(block)
                                        .setAdditionalTooltip(
                                                Tooltip.create(
                                                        Lang.FORGE_FLUID_CONTAINERS.trans(
                                                                "blocklabel"),
                                                        Tooltip.SLOT_FORMATTING))
                                        .build()));

        if (Registry.ModDependency.GREGTECH_5.isLoaded()) {
            Optional<ItemComponent> displayItemOptional =
                    GregTechFluidDictUtil.fluidToDisplayItem(fluid);
            displayItemOptional.ifPresent(
                    displayItem ->
                            fluidsBuilder.insertIntoNextSlot(
                                    DisplayComponent.builder(displayItem)
                                            .setAdditionalTooltip(
                                                    Tooltip.create(
                                                            Lang.FORGE_FLUID_CONTAINERS.trans(
                                                                    "gregtechitemlabel"),
                                                            Tooltip.SLOT_FORMATTING))
                                            .build()));
        }

        Diagram diagram = builder.build();
        fluidContainers.forEach(
                displayComponent -> FluidDictUtil.getEmptyContainer(displayComponent.component())
                        .ifPresent(container -> emptyContainersMultimap.put(container, diagram)));
        return diagram;
    }

    private Layout buildLayout() {
        return Layout.builder()
                .addInteractable(new AllDiagramsButton(info, Grid.GRID.grid(0, 0)))
                .putSlotGroup(
                        SLOT_GROUP_FLUIDS,
                        SlotGroup.builder(1, 6, Grid.GRID.grid(1, 2), Grid.Direction.S)
                                .build())
                .putSlotGroup(
                        SLOT_GROUP_CONTAINERS,
                        SlotGroup.builder(6, 6, Grid.GRID.grid(3, 2), Grid.Direction.SE)
                                .setDefaultTooltip(
                                        Tooltip.create(
                                                Lang.FORGE_FLUID_CONTAINERS.trans("containersslot"),
                                                Tooltip.SLOT_FORMATTING))
                                .build())
                .build();
    }
}
