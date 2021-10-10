package com.github.dcysteine.neicustomdiagram.generators.gregtech5.materialparts;

import com.github.dcysteine.neicustomdiagram.api.diagram.Diagram;
import com.github.dcysteine.neicustomdiagram.api.diagram.DiagramGenerator;
import com.github.dcysteine.neicustomdiagram.api.diagram.DiagramGroup;
import com.github.dcysteine.neicustomdiagram.api.diagram.DiagramGroupInfo;
import com.github.dcysteine.neicustomdiagram.api.diagram.component.Component;
import com.github.dcysteine.neicustomdiagram.api.diagram.component.ItemComponent;
import com.github.dcysteine.neicustomdiagram.api.diagram.interactable.Interactable;
import com.github.dcysteine.neicustomdiagram.api.diagram.matcher.CustomDiagramMatcher;
import com.github.dcysteine.neicustomdiagram.mod.Lang;
import com.github.dcysteine.neicustomdiagram.mod.Logger;
import com.github.dcysteine.neicustomdiagram.util.DiagramUtil;
import com.github.dcysteine.neicustomdiagram.util.gregtech5.GregTechFluidDictUtil;
import com.github.dcysteine.neicustomdiagram.util.gregtech5.GregTechFormatting;
import com.github.dcysteine.neicustomdiagram.util.gregtech5.GregTechOreDictUtil;
import com.google.common.collect.ImmutableBiMap;
import com.google.common.collect.Lists;
import gregtech.api.enums.Materials;
import gregtech.api.enums.OrePrefixes;
import gregtech.api.objects.ItemData;
import net.minecraft.init.Items;

import java.util.List;
import java.util.Optional;

/** Generates part diagrams for GregTech materials. */
public final class GregTechMaterialParts implements DiagramGenerator {
    public static final ItemComponent ICON =
            GregTechOreDictUtil.getComponent(OrePrefixes.gearGt, Materials.Aluminium)
                    .orElse(ItemComponent.create(Items.iron_ingot, 0));

    private final DiagramGroupInfo info;

    private final LayoutHandler layoutHandler;
    private final HeatingCoilHandler heatingCoilHandler;
    private final RelatedMaterialsHandler relatedMaterialsHandler;
    private final DiagramFactory diagramFactory;

    private ImmutableBiMap<Materials, Diagram> materialsMap;

    public GregTechMaterialParts(String groupId) {
        this.info =
                DiagramGroupInfo.builder(
                                Lang.GREGTECH_5_MATERIAL_PARTS.trans("groupname"),
                                groupId, ICON, 1)
                        // No point in showing the diagram for a single item. So require at least 2.
                        .setEmptyDiagramPredicate(DiagramUtil.buildEmptyDiagramPredicate(2))
                        .setDescription(
                                "This diagram displays GregTech crafting items for each"
                                        + " GregTech material.")
                        .build();

        this.layoutHandler = new LayoutHandler(this.info);
        this.heatingCoilHandler = new HeatingCoilHandler();
        this.relatedMaterialsHandler = new RelatedMaterialsHandler();
        this.diagramFactory = new DiagramFactory(
                this.layoutHandler, this.heatingCoilHandler, this.relatedMaterialsHandler);

        this.materialsMap = null;
    }

    @Override
    public DiagramGroupInfo info() {
        return info;
    }

    @Override
    public DiagramGroup generate() {
        layoutHandler.initialize();
        heatingCoilHandler.initialize();
        relatedMaterialsHandler.initialize();

        ImmutableBiMap.Builder<Materials, Diagram> materialsMapBuilder = ImmutableBiMap.builder();
        for (Materials material : Materials.getAll()) {
            materialsMapBuilder.put(material, diagramFactory.buildDiagram(material));
        }
        materialsMap = materialsMapBuilder.build();

        return new DiagramGroup(
                info, new CustomDiagramMatcher(materialsMap.values(), this::getDiagram));
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
                    Logger.GREGTECH_5_MATERIAL_PARTS.error(
                            "Did not generate diagram for material: {}",
                            GregTechFormatting.getMaterialDescription(material));
                }
            }
        }

        return Lists.newArrayList();
    }
}
