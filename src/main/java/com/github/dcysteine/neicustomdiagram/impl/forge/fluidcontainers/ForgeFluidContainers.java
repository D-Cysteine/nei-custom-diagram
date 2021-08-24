package com.github.dcysteine.neicustomdiagram.impl.forge.fluidcontainers;

import com.github.dcysteine.neicustomdiagram.api.Lang;
import com.github.dcysteine.neicustomdiagram.api.diagram.Diagram;
import com.github.dcysteine.neicustomdiagram.api.diagram.DiagramGenerator;
import com.github.dcysteine.neicustomdiagram.api.diagram.DiagramGroup;
import com.github.dcysteine.neicustomdiagram.api.diagram.DiagramGroupInfo;
import com.github.dcysteine.neicustomdiagram.api.diagram.component.Component;
import com.github.dcysteine.neicustomdiagram.api.diagram.component.FluidComponent;
import com.github.dcysteine.neicustomdiagram.api.diagram.component.ItemComponent;
import com.github.dcysteine.neicustomdiagram.api.diagram.interactable.Interactable;
import com.github.dcysteine.neicustomdiagram.api.diagram.layout.Grid;
import com.github.dcysteine.neicustomdiagram.api.diagram.layout.Layout;
import com.github.dcysteine.neicustomdiagram.api.diagram.layout.SlotGroup;
import com.github.dcysteine.neicustomdiagram.api.diagram.matcher.DynamicDiagramMatcher;
import com.github.dcysteine.neicustomdiagram.impl.common.ComponentTransformer;
import com.github.dcysteine.neicustomdiagram.impl.common.FluidDictUtils;
import com.google.common.collect.Lists;
import net.minecraft.item.Item;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public final class ForgeFluidContainers implements DiagramGenerator {
    public static final ItemComponent ICON =
            ItemComponent.create((Item) Item.itemRegistry.getObject("water_bucket"), 0);
    public static final DiagramGroupInfo DIAGRAM_GROUP_INFO =
            DiagramGroupInfo.create(
                    Lang.FORGE_FLUID_CONTAINERS.trans("groupname"),
                    "forge.fluidcontainers", ICON, 2, false);

    private static final String SLOT_GROUP_KEY = "key";

    private Layout layout;

    @Override
    public DiagramGroupInfo info() {
        return DIAGRAM_GROUP_INFO;
    }

    @Override
    public DiagramGroup generate() {
        layout = buildLayout();
        return new DiagramGroup(
                DIAGRAM_GROUP_INFO, new DynamicDiagramMatcher(this::generateDiagrams));
    }

    private Collection<Diagram> generateDiagrams(
            Interactable.RecipeType recipeType, Component component) {
        Optional<FluidComponent> fluidOptional = FluidDictUtils.getFluidContents(component);
        if (!fluidOptional.isPresent()) {
            return Lists.newArrayList();
        }

        List<Component> fluidContainers = FluidDictUtils.getFluidContainers(component);
        FluidDictUtils.fluidToItem(fluidOptional.get())
                .ifPresent(item -> fluidContainers.add(1, item));

        Diagram.Builder builder = Diagram.builder().addLayout(layout);
        builder.autoInsertIntoSlotGroup(SLOT_GROUP_KEY)
                .insertEachSafe(ComponentTransformer.transformCollectionToDisplay(fluidContainers));

        return Lists.newArrayList(builder.build());
    }

    private static Layout buildLayout() {
        return Layout.builder()
                .putSlotGroup(
                        SLOT_GROUP_KEY,
                        SlotGroup.builder(6, 6, Grid.GRID.grid(6, 0), Grid.Direction.S)
                                .build())
                .build();
    }
}
