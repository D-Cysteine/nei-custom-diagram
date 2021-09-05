package com.github.dcysteine.neicustomdiagram.generators.gregtech.materialtools;

import com.github.dcysteine.neicustomdiagram.api.Lang;
import com.github.dcysteine.neicustomdiagram.api.diagram.Diagram;
import com.github.dcysteine.neicustomdiagram.api.diagram.component.DisplayComponent;
import com.github.dcysteine.neicustomdiagram.api.diagram.component.ItemComponent;
import com.github.dcysteine.neicustomdiagram.api.diagram.interactable.CustomInteractable;
import com.github.dcysteine.neicustomdiagram.api.diagram.interactable.Interactable;
import com.github.dcysteine.neicustomdiagram.api.diagram.layout.ComponentLabel;
import com.github.dcysteine.neicustomdiagram.api.diagram.tooltip.Tooltip;
import com.github.dcysteine.neicustomdiagram.util.ComponentTransformer;
import com.github.dcysteine.neicustomdiagram.util.gregtech.GregTechFormatting;
import com.github.dcysteine.neicustomdiagram.util.gregtech.GregTechOreDictUtil;
import com.google.common.collect.ImmutableList;
import gregtech.api.enums.Materials;
import gregtech.api.enums.OrePrefixes;
import gregtech.common.items.GT_MetaGenerated_Tool_01;
import net.minecraft.init.Items;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

class DiagramFactory {
    private static final ComponentLabel MATERIAL_INFO_ICON =
            ComponentLabel.create(
                    ItemComponent.create(Items.book, 0), LayoutHandler.MATERIAL_INFO_POSITION);

    private static final ImmutableList<Integer> TURBINE_TOOL_IDS =
            ImmutableList.of(
                    (int) GT_MetaGenerated_Tool_01.TURBINE_SMALL,
                    (int) GT_MetaGenerated_Tool_01.TURBINE,
                    (int) GT_MetaGenerated_Tool_01.TURBINE_LARGE,
                    (int) GT_MetaGenerated_Tool_01.TURBINE_HUGE);

    private enum MaterialPart {
        TOOL_HEADS(LayoutHandler.SlotGroupKeys.TOOL_HEADS,
                OrePrefixes.toolHeadSword, OrePrefixes.toolHeadPickaxe, OrePrefixes.toolHeadShovel,
                OrePrefixes.toolHeadAxe, OrePrefixes.toolHeadHoe, OrePrefixes.toolHeadSaw,
                OrePrefixes.toolHeadHammer, OrePrefixes.toolHeadFile,
                OrePrefixes.toolHeadUniversalSpade, OrePrefixes.toolHeadSense,
                OrePrefixes.toolHeadPlow, OrePrefixes.toolHeadDrill, OrePrefixes.toolHeadChainsaw,
                OrePrefixes.toolHeadWrench, OrePrefixes.toolHeadBuzzSaw),

        TURBINE_BLADE(LayoutHandler.SlotKeys.TURBINE_BLADE, OrePrefixes.turbineBlade),

        ARROWHEAD(LayoutHandler.SlotKeys.ARROWHEAD, OrePrefixes.toolHeadArrow),
        ARROWS(LayoutHandler.SlotGroupKeys.ARROWS,
                OrePrefixes.arrowGtWood, OrePrefixes.arrowGtPlastic);

        private final String slotKey;
        private final ImmutableList<OrePrefixes> prefixes;

        MaterialPart(String slotKey, OrePrefixes... prefixes) {
            this.slotKey = slotKey;
            this.prefixes = ImmutableList.copyOf(prefixes);
        }

        private void insertIntoSlot(Diagram.Builder builder, Materials material) {
            if (prefixes.size() == 1) {
                builder.insertIntoSlot(slotKey, getPrefixComponents(prefixes, material));
            } else {
                builder.autoInsertIntoSlotGroup(slotKey)
                        .insertEachSafe(getPrefixComponents(prefixes, material));
            }
        }
    }

    private final LayoutHandler layoutHandler;
    private final RecipeHandler recipeHandler;

    DiagramFactory(LayoutHandler layoutHandler, RecipeHandler recipeHandler) {
        this.layoutHandler = layoutHandler;
        this.recipeHandler = recipeHandler;
    }

    Diagram buildDiagram(Materials material) {
        Diagram.Builder diagramBuilder = Diagram.builder()
                .addAllLayouts(layoutHandler.requiredLayouts())
                .addAllOptionalLayouts(layoutHandler.optionalLayouts())
                .addInteractable(buildMaterialInfoButton(material));

        List<ItemComponent> tools = new ArrayList<>();
        List<ItemComponent> turbines = new ArrayList<>();
        for (ItemComponent tool : recipeHandler.getTools(material)) {
            if (TURBINE_TOOL_IDS.contains(tool.damage())) {
                turbines.add(tool);
            } else {
                tools.add(tool);
            }
        }
        diagramBuilder.autoInsertIntoSlotGroup(LayoutHandler.SlotGroupKeys.TOOLS)
                .insertEachSafe(ComponentTransformer.transformToDisplay(tools));
        diagramBuilder.autoInsertIntoSlotGroup(LayoutHandler.SlotGroupKeys.TURBINES)
                .insertEachSafe(ComponentTransformer.transformToDisplay(turbines));

        Arrays.stream(DiagramFactory.MaterialPart.values())
                .forEach(part -> part.insertIntoSlot(diagramBuilder, material));
        return diagramBuilder.build();
    }

    private static List<DisplayComponent> getPrefixComponents(
            ImmutableList<OrePrefixes> prefixes, Materials material) {
        List<DisplayComponent> list = new ArrayList<>();
        for (OrePrefixes prefix : prefixes) {
            Optional<ItemComponent> componentOptional =
                    GregTechOreDictUtil.getComponent(prefix, material);
            if (!componentOptional.isPresent()) {
                continue;
            }

            list.add(
                    DisplayComponent.builder(componentOptional.get())
                            .setAdditionalTooltip(
                                    Tooltip.create(
                                            Lang.GREGTECH_MATERIAL_TOOLS.transf(
                                                    "prefixlabel", prefix.mRegularLocalName),
                                            Tooltip.INFO_FORMATTING))
                            .build());
        }
        return list;
    }

    private static Interactable buildMaterialInfoButton(Materials material) {
        Tooltip tooltip =
                Tooltip.builder()
                        .addTextLine(GregTechFormatting.getMaterialDescription(material))
                        .setFormatting(Tooltip.INFO_FORMATTING)
                        .addTextLine(material.mChemicalFormula)
                        .addSpacing()
                        .addTextLine(
                                Lang.GREGTECH_MATERIAL_TOOLS.transf(
                                        "materialinfohandlematerial",
                                        GregTechFormatting.getMaterialDescription(
                                                material.mHandleMaterial)))
                        .build();

        return CustomInteractable.builder(MATERIAL_INFO_ICON)
                .setTooltip(tooltip)
                .build();
    }
}
