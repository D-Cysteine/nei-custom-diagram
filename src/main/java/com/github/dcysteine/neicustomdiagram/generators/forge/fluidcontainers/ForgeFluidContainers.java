package com.github.dcysteine.neicustomdiagram.generators.forge.fluidcontainers;

import com.github.dcysteine.neicustomdiagram.api.Lang;
import com.github.dcysteine.neicustomdiagram.api.Registry;
import com.github.dcysteine.neicustomdiagram.api.diagram.Diagram;
import com.github.dcysteine.neicustomdiagram.api.diagram.DiagramGenerator;
import com.github.dcysteine.neicustomdiagram.api.diagram.DiagramGroup;
import com.github.dcysteine.neicustomdiagram.api.diagram.DiagramGroupInfo;
import com.github.dcysteine.neicustomdiagram.api.diagram.component.Component;
import com.github.dcysteine.neicustomdiagram.api.diagram.component.DisplayComponent;
import com.github.dcysteine.neicustomdiagram.api.diagram.component.FluidComponent;
import com.github.dcysteine.neicustomdiagram.api.diagram.component.ItemComponent;
import com.github.dcysteine.neicustomdiagram.api.diagram.interactable.Interactable;
import com.github.dcysteine.neicustomdiagram.api.diagram.layout.Grid;
import com.github.dcysteine.neicustomdiagram.api.diagram.layout.Layout;
import com.github.dcysteine.neicustomdiagram.api.diagram.layout.SlotGroup;
import com.github.dcysteine.neicustomdiagram.api.diagram.matcher.CustomDiagramMatcher;
import com.github.dcysteine.neicustomdiagram.api.diagram.tooltip.Tooltip;
import com.github.dcysteine.neicustomdiagram.util.FluidDictUtil;
import com.github.dcysteine.neicustomdiagram.util.gregtech.GregTechFluidDictUtil;
import com.google.common.collect.Lists;
import net.minecraft.init.Items;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

/**
 * Generates diagrams showing all Forge-registered fluid containers for a given fluid.
 *
 * <p>This diagram generator generates its diagrams dynamically, and so does not support showing all
 * diagrams.
 */
public final class ForgeFluidContainers implements DiagramGenerator {
    public static final ItemComponent ICON = ItemComponent.create(Items.water_bucket, 0);

    private static final String SLOT_GROUP_FLUIDS = "fluids";
    private static final String SLOT_GROUP_CONTAINERS = "containers";

    private final DiagramGroupInfo info;
    private Layout layout;

    public ForgeFluidContainers(String groupId) {
        this.info =
                DiagramGroupInfo.create(
                        Lang.FORGE_FLUID_CONTAINERS.trans("groupname"),
                        groupId, ICON, 2, false);
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
        Optional<FluidComponent> fluidOptional = FluidDictUtil.getFluidContents(component);
        if (!fluidOptional.isPresent() && Registry.ModIds.isModLoaded(Registry.ModIds.GREGTECH)) {
            // Try looking up GregTech fluid display stack.
            fluidOptional = GregTechFluidDictUtil.displayItemToFluid(component);
        }
        if (!fluidOptional.isPresent()) {
            return Lists.newArrayList();
        }

        FluidComponent fluid = fluidOptional.get();
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
                                Tooltip.create(
                                        Lang.FORGE_FLUID_CONTAINERS.trans("fluidlabel"),
                                        Tooltip.SLOT_FORMATTING))
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

        if (Registry.ModIds.isModLoaded(Registry.ModIds.GREGTECH)) {
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

        return Lists.newArrayList(builder.build());
    }

    private static Layout buildLayout() {
        return Layout.builder()
                .putSlotGroup(
                        SLOT_GROUP_FLUIDS,
                        SlotGroup.builder(1, 6, Grid.GRID.grid(1, 0), Grid.Direction.S)
                                .build())
                .putSlotGroup(
                        SLOT_GROUP_CONTAINERS,
                        SlotGroup.builder(6, 6, Grid.GRID.grid(3, 0), Grid.Direction.SE)
                                .setDefaultTooltip(
                                        Tooltip.create(
                                                Lang.FORGE_FLUID_CONTAINERS.trans("containersslot"),
                                                Tooltip.SLOT_FORMATTING))
                                .build())
                .build();
    }
}
