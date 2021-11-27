package com.github.dcysteine.neicustomdiagram.generators.gregtech5.oreprefixes;

import com.github.dcysteine.neicustomdiagram.api.diagram.Diagram;
import com.github.dcysteine.neicustomdiagram.api.diagram.DiagramGenerator;
import com.github.dcysteine.neicustomdiagram.api.diagram.DiagramGroup;
import com.github.dcysteine.neicustomdiagram.api.diagram.DiagramGroupInfo;
import com.github.dcysteine.neicustomdiagram.api.diagram.component.Component;
import com.github.dcysteine.neicustomdiagram.api.diagram.component.DisplayComponent;
import com.github.dcysteine.neicustomdiagram.api.diagram.component.ItemComponent;
import com.github.dcysteine.neicustomdiagram.api.diagram.interactable.AllDiagramsButton;
import com.github.dcysteine.neicustomdiagram.api.diagram.interactable.Interactable;
import com.github.dcysteine.neicustomdiagram.api.diagram.layout.Grid;
import com.github.dcysteine.neicustomdiagram.api.diagram.layout.Layout;
import com.github.dcysteine.neicustomdiagram.api.diagram.layout.SlotGroup;
import com.github.dcysteine.neicustomdiagram.api.diagram.matcher.CustomDiagramMatcher;
import com.github.dcysteine.neicustomdiagram.api.diagram.tooltip.Tooltip;
import com.github.dcysteine.neicustomdiagram.main.Lang;
import com.github.dcysteine.neicustomdiagram.main.Logger;
import com.github.dcysteine.neicustomdiagram.main.config.DiagramGroupVisibility;
import com.github.dcysteine.neicustomdiagram.util.DiagramUtil;
import com.github.dcysteine.neicustomdiagram.util.gregtech5.GregTechDiagramUtil;
import com.github.dcysteine.neicustomdiagram.util.gregtech5.GregTechFluidDictUtil;
import com.github.dcysteine.neicustomdiagram.util.gregtech5.GregTechFormatting;
import com.github.dcysteine.neicustomdiagram.util.gregtech5.GregTechOreDictUtil;
import com.google.common.collect.ImmutableBiMap;
import com.google.common.collect.Lists;
import gregtech.api.enums.ItemList;
import gregtech.api.enums.Materials;
import gregtech.api.enums.OrePrefixes;
import gregtech.api.objects.ItemData;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Generates diagrams showing GregTech ore dictionary data for any item.
 *
 * <p>This diagram generator generates its diagrams dynamically, and so does not support showing all
 * diagrams.
 */
public final class GregTechOrePrefixes implements DiagramGenerator {
    public static final ItemComponent ICON =
            GregTechOreDictUtil.getComponent(ItemList.Book_Written_02);

    private static final Layout.SlotGroupKey SLOT_GROUP_KEY = Layout.SlotGroupKey.create("key");

    private final DiagramGroupInfo info;
    private ImmutableBiMap<Materials, Diagram> materialsMap;

    public GregTechOrePrefixes(String groupId) {
        this.info =
                DiagramGroupInfo.builder(
                                Lang.GREGTECH_5_ORE_PREFIXES.trans("groupname"),
                                groupId, ICON, 1)
                        // No point in showing the diagram for a single item. So require at least 2.
                        .setEmptyDiagramPredicate(DiagramUtil.buildEmptyDiagramPredicate(2))
                        .setDefaultVisibility(DiagramGroupVisibility.DISABLED)
                        .setDescription(
                                "This diagram displays all GregTech ore prefixes for each GregTech"
                                        + " material."
                                        + "\nMostly useful for modpack development.")
                        .build();
        this.materialsMap = null;
    }

    @Override
    public DiagramGroupInfo info() {
        return info;
    }

    @Override
    public DiagramGroup generate() {
        ImmutableBiMap.Builder<Materials, Diagram> materialsMapBuilder = ImmutableBiMap.builder();
        for (Materials material : Materials.getAll()) {
            materialsMapBuilder.put(material, generateDiagram(material));
        }
        materialsMap = materialsMapBuilder.build();

        return new DiagramGroup(
                info, new CustomDiagramMatcher(materialsMap.values(), this::getDiagram));
    }

    private Diagram generateDiagram(Materials material) {
        Diagram.Builder builder = Diagram.builder().addLayout(buildLayout(material));

        List<DisplayComponent> components = new ArrayList<>();
        for (OrePrefixes prefix : OrePrefixes.values()) {
            Optional<ItemComponent> componentOptional =
                    GregTechOreDictUtil.getComponent(prefix, material);
            if (!componentOptional.isPresent()) {
                continue;
            }

            components.add(
                    DisplayComponent.builder(componentOptional.get())
                            .setAdditionalTooltip(
                                    Tooltip.create(
                                            Lang.GREGTECH_5_ORE_PREFIXES.transf(
                                                    "prefixlabel", prefix.mRegularLocalName),
                                            Tooltip.INFO_FORMATTING))
                            .build());
        }
        builder.autoInsertIntoSlotGroup(SLOT_GROUP_KEY).insertEachSafe(components);

        return builder.build();
    }

    /** Returns either a single-element list, or an empty list. */
    private List<Diagram> getDiagram(Interactable.RecipeType unused, Component component) {
        // Try handling fluids and fluid display stacks by converting into a filled cell.
        component = GregTechFluidDictUtil.fillCell(component)
                .map(Component.class::cast).orElse(component);

        Optional<ItemData> itemDataOptional = GregTechOreDictUtil.getItemData(component);
        if (itemDataOptional.isPresent() && itemDataOptional.get().mMaterial != null) {
            Materials material = itemDataOptional.get().mMaterial.mMaterial;
            if (material != null) {
                if (materialsMap.containsKey(material)) {
                    return Lists.newArrayList(materialsMap.get(material));
                } else {
                    Logger.GREGTECH_5_ORE_PREFIXES.error(
                            "Did not generate diagram for material: {}",
                            GregTechFormatting.getMaterialDescription(material));
                }
            }
        }

        return Lists.newArrayList();
    }

    private Layout buildLayout(Materials material) {
        return Layout.builder()
                .addInteractable(new AllDiagramsButton(info, Grid.GRID.grid(0, 0)))
                .addInteractable(
                        GregTechDiagramUtil.buildMaterialInfoButton(Grid.GRID.grid(2, 0), material))
                .putSlotGroup(
                        SLOT_GROUP_KEY,
                        SlotGroup.builder(9, 12, Grid.GRID.grid(6, 2), Grid.Direction.S)
                                .build())
                .build();
    }
}
