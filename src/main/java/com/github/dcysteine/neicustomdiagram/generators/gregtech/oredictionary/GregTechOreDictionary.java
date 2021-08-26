package com.github.dcysteine.neicustomdiagram.generators.gregtech.oredictionary;

import codechicken.lib.gui.GuiDraw;
import com.github.dcysteine.neicustomdiagram.api.Lang;
import com.github.dcysteine.neicustomdiagram.api.diagram.Diagram;
import com.github.dcysteine.neicustomdiagram.api.diagram.DiagramGenerator;
import com.github.dcysteine.neicustomdiagram.api.diagram.DiagramGroup;
import com.github.dcysteine.neicustomdiagram.api.diagram.DiagramGroupInfo;
import com.github.dcysteine.neicustomdiagram.api.diagram.component.Component;
import com.github.dcysteine.neicustomdiagram.api.diagram.component.ItemComponent;
import com.github.dcysteine.neicustomdiagram.api.diagram.interactable.CustomInteractable;
import com.github.dcysteine.neicustomdiagram.api.diagram.interactable.Interactable;
import com.github.dcysteine.neicustomdiagram.api.diagram.layout.Grid;
import com.github.dcysteine.neicustomdiagram.api.diagram.layout.Layout;
import com.github.dcysteine.neicustomdiagram.api.diagram.layout.SlotGroup;
import com.github.dcysteine.neicustomdiagram.api.diagram.layout.Text;
import com.github.dcysteine.neicustomdiagram.api.diagram.matcher.DynamicDiagramMatcher;
import com.github.dcysteine.neicustomdiagram.api.diagram.tooltip.Tooltip;
import com.github.dcysteine.neicustomdiagram.util.ComponentTransformer;
import com.github.dcysteine.neicustomdiagram.util.gregtech.GregTechOreDictUtils;
import com.google.common.collect.Lists;
import gregtech.api.enums.ItemList;
import gregtech.api.enums.Materials;
import gregtech.api.objects.ItemData;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public final class GregTechOreDictionary implements DiagramGenerator {
    public static final ItemComponent ICON =
            GregTechOreDictUtils.getComponent(ItemList.Book_Written_00);

    private static final String SLOT_GROUP_UNIFY = "unify";
    private static final String SLOT_GROUP_ALL = "all";

    private final DiagramGroupInfo info;

    public GregTechOreDictionary(String groupId) {
        this.info =
                DiagramGroupInfo.create(
                        Lang.GREGTECH_ORE_DICTIONARY.trans("groupname"),
                        groupId, ICON, 2, false);

    }

    @Override
    public DiagramGroupInfo info() {
        return info;
    }

    @Override
    public DiagramGroup generate() {
        return new DiagramGroup(
                info,
                new DynamicDiagramMatcher(GregTechOreDictionary::generateDiagrams));
    }

    private static Collection<Diagram> generateDiagrams(
            Interactable.RecipeType recipeType, Component component) {
        List<Component> allComponents = GregTechOreDictUtils.getAssociatedComponents(component);
        Optional<ItemData> itemDataOptional = GregTechOreDictUtils.getItemData(component);
        if (allComponents.size() <= 1 && !itemDataOptional.isPresent()) {
            // Don't bother returning a diagram that contains just the input element.
            return Lists.newArrayList();
        }
        List<Component> unifyComponents = GregTechOreDictUtils.reverseUnify(component);

        Diagram.Builder builder = Diagram.builder().addLayout(buildLayout(itemDataOptional));
        builder.autoInsertIntoSlotGroup(SLOT_GROUP_UNIFY)
                .insertEachSafe(ComponentTransformer.transformCollectionToDisplay(unifyComponents));
        builder.autoInsertIntoSlotGroup(SLOT_GROUP_ALL)
                .insertEachSafe(ComponentTransformer.transformCollectionToDisplay(allComponents));

        return Lists.newArrayList(builder.build());
    }

    private static Layout buildLayout(Optional<ItemData> itemDataOptional) {
        Layout.Builder builder = Layout.builder()
                .putSlotGroup(
                        SLOT_GROUP_UNIFY,
                        SlotGroup.builder(2, 6, Grid.GRID.grid(1, 2), Grid.Direction.S)
                                .setDefaultTooltip(
                                        Tooltip.create(
                                                Lang.GREGTECH_ORE_DICTIONARY.trans("unifyslot"),
                                                Tooltip.SLOT_FORMATTING))
                                .build())
                .putSlotGroup(
                        SLOT_GROUP_ALL,
                        SlotGroup.builder(6, 6, Grid.GRID.grid(4, 2), Grid.Direction.SE)
                                .setDefaultTooltip(
                                        Tooltip.create(
                                                Lang.GREGTECH_ORE_DICTIONARY.trans("allslot"),
                                                Tooltip.SLOT_FORMATTING))
                                .build());

        if (itemDataOptional.isPresent() && itemDataOptional.get().mMaterial != null) {
            Materials material = itemDataOptional.get().mMaterial.mMaterial;
            String materialName = material.mLocalizedName;
            boolean small = GuiDraw.getStringWidth(materialName) > Grid.TOTAL_WIDTH - 8;

            Text materialNameText =
                    Text.builder(materialName, Grid.GRID.grid(6, 0), Grid.Direction.C)
                            .setSmall(small)
                            .build();
            Interactable materialNameLabel =
                    CustomInteractable.builder(materialNameText)
                            .setTooltip(
                                    Tooltip.create(
                                            Lang.GREGTECH_ORE_DICTIONARY.trans("materialnamelabel"),
                                            Tooltip.SLOT_FORMATTING))
                            .build();

            builder.addInteractable(materialNameLabel);
        }

        return builder.build();
    }
}
