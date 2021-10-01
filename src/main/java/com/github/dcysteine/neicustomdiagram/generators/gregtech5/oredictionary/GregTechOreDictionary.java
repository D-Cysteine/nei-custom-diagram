package com.github.dcysteine.neicustomdiagram.generators.gregtech5.oredictionary;

import codechicken.lib.gui.GuiDraw;
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
import com.github.dcysteine.neicustomdiagram.api.diagram.matcher.CustomDiagramMatcher;
import com.github.dcysteine.neicustomdiagram.api.diagram.tooltip.Tooltip;
import com.github.dcysteine.neicustomdiagram.mod.Lang;
import com.github.dcysteine.neicustomdiagram.mod.config.DiagramGroupVisibility;
import com.github.dcysteine.neicustomdiagram.util.ComponentTransformer;
import com.github.dcysteine.neicustomdiagram.util.gregtech5.GregTechFormatting;
import com.github.dcysteine.neicustomdiagram.util.gregtech5.GregTechOreDictUtil;
import com.google.common.collect.Lists;
import gregtech.api.enums.ItemList;
import gregtech.api.enums.Materials;
import gregtech.api.objects.ItemData;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

/**
 * Generates diagrams showing GregTech ore dictionary data for any item.
 *
 * <p>This diagram generator generates its diagrams dynamically, and so does not support showing all
 * diagrams.
 */
public final class GregTechOreDictionary implements DiagramGenerator {
    public static final ItemComponent ICON =
            GregTechOreDictUtil.getComponent(ItemList.Book_Written_01);

    private static final Layout.SlotGroupKey SLOT_GROUP_UNIFY = Layout.SlotGroupKey.create("unify");
    private static final Layout.SlotGroupKey SLOT_GROUP_ALL = Layout.SlotGroupKey.create("all");

    private final DiagramGroupInfo info;

    public GregTechOreDictionary(String groupId) {
        this.info =
                DiagramGroupInfo.builder(
                                Lang.GREGTECH_5_ORE_DICTIONARY.trans("groupname"),
                                groupId, ICON, 2)
                        .setDefaultVisibility(DiagramGroupVisibility.DISABLED)
                        .setDescription(
                                "This diagram displays GregTech ore dictionary synonyms."
                                        + "\nMostly useful for modpack development.")
                        .build();

    }

    @Override
    public DiagramGroupInfo info() {
        return info;
    }

    @Override
    public DiagramGroup generate() {
        return new DiagramGroup(
                info,
                new CustomDiagramMatcher(GregTechOreDictionary::generateDiagrams));
    }

    private static Collection<Diagram> generateDiagrams(
            Interactable.RecipeType recipeType, Component component) {
        List<Component> allComponents = GregTechOreDictUtil.getAssociatedComponents(component);
        Optional<ItemData> itemDataOptional = GregTechOreDictUtil.getItemData(component);
        if (allComponents.size() <= 1 && !itemDataOptional.isPresent()) {
            // Don't bother returning a diagram that contains just the input element.
            return Lists.newArrayList();
        }
        List<Component> unifyComponents = GregTechOreDictUtil.reverseUnify(component);

        Diagram.Builder builder = Diagram.builder().addLayout(buildLayout(itemDataOptional));
        builder.autoInsertIntoSlotGroup(SLOT_GROUP_UNIFY)
                .insertEachSafe(ComponentTransformer.transformToDisplay(unifyComponents));
        builder.autoInsertIntoSlotGroup(SLOT_GROUP_ALL)
                .insertEachSafe(ComponentTransformer.transformToDisplay(allComponents));

        return Lists.newArrayList(builder.build());
    }

    private static Layout buildLayout(Optional<ItemData> itemDataOptional) {
        Layout.Builder builder = Layout.builder()
                .putSlotGroup(
                        SLOT_GROUP_UNIFY,
                        SlotGroup.builder(2, 6, Grid.GRID.grid(1, 3), Grid.Direction.S)
                                .setDefaultTooltip(
                                        Tooltip.create(
                                                Lang.GREGTECH_5_ORE_DICTIONARY.trans("unifyslot"),
                                                Tooltip.SLOT_FORMATTING))
                                .build())
                .putSlotGroup(
                        SLOT_GROUP_ALL,
                        SlotGroup.builder(6, 6, Grid.GRID.grid(4, 3), Grid.Direction.SE)
                                .setDefaultTooltip(
                                        Tooltip.create(
                                                Lang.GREGTECH_5_ORE_DICTIONARY.trans("allslot"),
                                                Tooltip.SLOT_FORMATTING))
                                .build());

        if (itemDataOptional.isPresent() && itemDataOptional.get().mMaterial != null) {
            Materials material = itemDataOptional.get().mMaterial.mMaterial;
            String materialName = GregTechFormatting.getMaterialDescription(material);
            String prefixName = itemDataOptional.get().mPrefix.mRegularLocalName;
            boolean materialSmall = GuiDraw.getStringWidth(materialName) > Grid.TOTAL_WIDTH - 4;
            boolean prefixSmall = GuiDraw.getStringWidth(prefixName) > Grid.TOTAL_WIDTH - 4;

            Text materialNameText =
                    Text.builder(materialName, Grid.GRID.grid(6, 0), Grid.Direction.C)
                            .setSmall(materialSmall)
                            .build();
            Interactable materialNameLabel =
                    CustomInteractable.builder(materialNameText)
                            .setTooltip(
                                    Tooltip.create(
                                            Lang.GREGTECH_5_ORE_DICTIONARY.trans("materialnamelabel"),
                                            Tooltip.SLOT_FORMATTING))
                            .build();

            Text prefixNameText =
                    Text.builder(prefixName, Grid.GRID.grid(6, 1), Grid.Direction.C)
                            .setSmall(prefixSmall)
                            .build();
            Interactable prefixNameLabel =
                    CustomInteractable.builder(prefixNameText)
                            .setTooltip(
                                    Tooltip.create(
                                            Lang.GREGTECH_5_ORE_DICTIONARY.trans("prefixnamelabel"),
                                            Tooltip.SLOT_FORMATTING))
                            .build();

            builder.addInteractable(materialNameLabel).addInteractable(prefixNameLabel);
        }

        return builder.build();
    }
}
