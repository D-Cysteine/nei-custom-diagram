package com.github.dcysteine.neicustomdiagram.generators.gregtech.materialtools;

import com.github.dcysteine.neicustomdiagram.api.Lang;
import com.github.dcysteine.neicustomdiagram.api.diagram.Diagram;
import com.github.dcysteine.neicustomdiagram.api.diagram.DiagramGenerator;
import com.github.dcysteine.neicustomdiagram.api.diagram.DiagramGroup;
import com.github.dcysteine.neicustomdiagram.api.diagram.DiagramGroupInfo;
import com.github.dcysteine.neicustomdiagram.api.diagram.component.Component;
import com.github.dcysteine.neicustomdiagram.api.diagram.component.ItemComponent;
import com.github.dcysteine.neicustomdiagram.api.diagram.interactable.Interactable;
import com.github.dcysteine.neicustomdiagram.api.diagram.matcher.CustomDiagramMatcher;
import com.github.dcysteine.neicustomdiagram.util.gregtech.GregTechFluidDictUtil;
import com.github.dcysteine.neicustomdiagram.util.gregtech.GregTechOreDictUtil;
import com.google.common.collect.ImmutableBiMap;
import com.google.common.collect.ImmutableList;
import gregtech.api.enums.Materials;
import gregtech.api.items.GT_MetaGenerated_Tool;
import gregtech.api.objects.ItemData;
import gregtech.common.items.GT_MetaGenerated_Tool_01;
import net.minecraft.item.ItemStack;

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
                DiagramGroupInfo.create(
                        Lang.GREGTECH_MATERIAL_TOOLS.trans("groupname"),
                        groupId, ICON, 1);

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
    private ImmutableList<Diagram> getDiagram(Interactable.RecipeType unused, Component component) {
        // Try handling fluids and fluid display stacks by converting into a filled cell.
        component = GregTechFluidDictUtil.fillCell(component)
                .map(Component.class::cast).orElse(component);

        Materials material = null;

        if (component.type() == Component.ComponentType.ITEM
                && ((ItemComponent) component).item() == GT_MetaGenerated_Tool_01.INSTANCE) {
            material = GT_MetaGenerated_Tool.getPrimaryMaterial((ItemStack) component.stack());
        } else {
            Optional<ItemData> itemDataOptional = GregTechOreDictUtil.getItemData(component);
            if (itemDataOptional.isPresent() && itemDataOptional.get().mMaterial != null) {
                material = itemDataOptional.get().mMaterial.mMaterial;
            }
        }

        return material == null ? ImmutableList.of() : ImmutableList.of(materialsMap.get(material));
    }
}
