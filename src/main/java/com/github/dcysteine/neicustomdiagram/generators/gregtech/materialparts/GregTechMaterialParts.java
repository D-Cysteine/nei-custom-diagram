package com.github.dcysteine.neicustomdiagram.generators.gregtech.materialparts;

import com.github.dcysteine.neicustomdiagram.api.diagram.Diagram;
import com.github.dcysteine.neicustomdiagram.api.diagram.DiagramGenerator;
import com.github.dcysteine.neicustomdiagram.api.diagram.DiagramGroup;
import com.github.dcysteine.neicustomdiagram.api.diagram.DiagramGroupInfo;
import com.github.dcysteine.neicustomdiagram.api.diagram.component.Component;
import com.github.dcysteine.neicustomdiagram.api.diagram.component.ItemComponent;
import com.github.dcysteine.neicustomdiagram.api.diagram.interactable.Interactable;
import com.github.dcysteine.neicustomdiagram.api.diagram.matcher.CustomDiagramMatcher;
import com.github.dcysteine.neicustomdiagram.mod.Lang;
import com.github.dcysteine.neicustomdiagram.util.gregtech.GregTechFluidDictUtil;
import com.github.dcysteine.neicustomdiagram.util.gregtech.GregTechOreDictUtil;
import com.google.common.collect.ImmutableBiMap;
import com.google.common.collect.ImmutableList;
import gregtech.api.enums.Materials;
import gregtech.api.enums.OrePrefixes;
import gregtech.api.objects.ItemData;
import net.minecraft.init.Items;

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
                                Lang.GREGTECH_MATERIAL_PARTS.trans("groupname"),
                                groupId, ICON, 1)
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
    private ImmutableList<Diagram> getDiagram(Interactable.RecipeType unused, Component component) {
        // Try handling fluids and fluid display stacks by converting into a filled cell.
        component = GregTechFluidDictUtil.fillCell(component)
                .map(Component.class::cast).orElse(component);

        Optional<ItemData> itemDataOptional = GregTechOreDictUtil.getItemData(component);
        if (itemDataOptional.isPresent() && itemDataOptional.get().mMaterial != null) {
            Materials material = itemDataOptional.get().mMaterial.mMaterial;
            return material == null
                    ? ImmutableList.of() : ImmutableList.of(materialsMap.get(material));
        }

        return ImmutableList.of();
    }
}
