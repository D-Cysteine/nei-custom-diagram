package com.github.dcysteine.neicustomdiagram.generators.gregtech5.materialtools;

import com.github.dcysteine.neicustomdiagram.api.diagram.Diagram;
import com.github.dcysteine.neicustomdiagram.api.diagram.DiagramGenerator;
import com.github.dcysteine.neicustomdiagram.api.diagram.DiagramGroup;
import com.github.dcysteine.neicustomdiagram.api.diagram.DiagramGroupInfo;
import com.github.dcysteine.neicustomdiagram.api.diagram.component.Component;
import com.github.dcysteine.neicustomdiagram.api.diagram.component.ItemComponent;
import com.github.dcysteine.neicustomdiagram.api.diagram.interactable.Interactable;
import com.github.dcysteine.neicustomdiagram.api.diagram.matcher.CustomDiagramMatcher;
import com.github.dcysteine.neicustomdiagram.main.Lang;
import com.github.dcysteine.neicustomdiagram.main.Logger;
import com.github.dcysteine.neicustomdiagram.util.DiagramUtil;
import com.github.dcysteine.neicustomdiagram.util.gregtech5.GregTechFluidDictUtil;
import com.github.dcysteine.neicustomdiagram.util.gregtech5.GregTechFormatting;
import com.github.dcysteine.neicustomdiagram.util.gregtech5.GregTechOreDictUtil;
import com.google.common.collect.ImmutableBiMap;
import com.google.common.collect.Lists;
import gregtech.api.enums.Materials;
import gregtech.api.items.GT_MetaGenerated_Tool;
import gregtech.api.objects.ItemData;
import gregtech.common.items.GT_MetaGenerated_Tool_01;
import net.minecraft.item.ItemStack;

import java.util.List;
import java.util.Optional;

public final class GregTechMaterialTools implements DiagramGenerator {
    public static final ItemComponent ICON =
            ItemComponent.createWithNbt(
                    GT_MetaGenerated_Tool_01.INSTANCE.getToolWithStats(
                            GT_MetaGenerated_Tool_01.HARDHAMMER, 1,
                            Materials.Aluminium, Materials.Wood, null));

    private final DiagramGroupInfo info;

    private final LayoutHandler layoutHandler;
    private final RecipeHandler recipeHandler;
    private final DiagramFactory diagramFactory;

    private ImmutableBiMap<Materials, Diagram> materialsMap;

    public GregTechMaterialTools(String groupId) {
        this.info =
                DiagramGroupInfo.builder(
                                Lang.GREGTECH_5_MATERIAL_TOOLS.trans("groupname"),
                                groupId, ICON, 1)
                        .setIgnoreNbt(false)
                        // We'll almost always insert the handle item, so require at least 2
                        // components to be inserted to be non-empty.
                        .setEmptyDiagramPredicate(DiagramUtil.buildEmptyDiagramPredicate(2))
                        .setDescription(
                                "This diagram displays craftable GregTech tools for each"
                                        + " GregTech material.")
                        .build();

        this.layoutHandler = new LayoutHandler(this.info);
        this.recipeHandler = new RecipeHandler();
        this.diagramFactory = new DiagramFactory(this.layoutHandler, this.recipeHandler);

        this.materialsMap = null;
    }

    @Override
    public DiagramGroupInfo info() {
        return info;
    }

    @Override
    public DiagramGroup generate() {
        layoutHandler.initialize();
        recipeHandler.initialize();

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

        Materials material = null;

        if (component.type() == Component.ComponentType.ITEM
                && ((ItemComponent) component).item() instanceof GT_MetaGenerated_Tool) {
            material = GT_MetaGenerated_Tool.getPrimaryMaterial((ItemStack) component.stack());
        } else {
            Optional<ItemData> itemDataOptional = GregTechOreDictUtil.getItemData(component);
            if (itemDataOptional.isPresent() && itemDataOptional.get().mMaterial != null) {
                material = itemDataOptional.get().mMaterial.mMaterial;
            }
        }

        if (material != null) {
            if (materialsMap.containsKey(material)) {
                return Lists.newArrayList(materialsMap.get(material));
            } else {
                Logger.GREGTECH_5_MATERIAL_TOOLS.error(
                        "Did not generate diagram for material: {}",
                        GregTechFormatting.getMaterialDescription(material));
            }
        }

        return Lists.newArrayList();
    }
}
