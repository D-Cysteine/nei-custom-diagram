package com.github.dcysteine.neicustomdiagram.generators.gregtech5.recipedebugger;

import com.github.dcysteine.neicustomdiagram.api.diagram.component.ItemComponent;
import com.github.dcysteine.neicustomdiagram.api.diagram.interactable.CustomInteractable;
import com.github.dcysteine.neicustomdiagram.api.diagram.layout.ComponentLabel;
import com.github.dcysteine.neicustomdiagram.api.diagram.tooltip.Tooltip;
import com.github.dcysteine.neicustomdiagram.api.draw.Point;
import com.github.dcysteine.neicustomdiagram.mod.Lang;
import com.github.dcysteine.neicustomdiagram.util.gregtech5.GregTechOreDictUtil;

import java.util.EnumMap;

class LabelHandler {
    private final EnumMap<RecipeHandler.RecipeMap, ItemComponent> componentMap;

    LabelHandler() {
        componentMap = new EnumMap<>(RecipeHandler.RecipeMap.class);
    }

    /** This method must be called before any other methods are called. */
    void initialize() {
        for (RecipeHandler.RecipeMap recipeMap : RecipeHandler.RecipeMap.values()) {
            componentMap.put(recipeMap, GregTechOreDictUtil.getComponent(recipeMap.item));
        }
    }

    CustomInteractable buildLabel(RecipeHandler.RecipeMap recipeMap, Point pos) {
        ComponentLabel label = ComponentLabel.create(componentMap.get(recipeMap), pos);
        Tooltip tooltip =
                Tooltip.create(
                        Lang.GREGTECH_5_RECIPE_DEBUGGER.trans(recipeMap.tooltipKey),
                        Tooltip.INFO_FORMATTING);

        return CustomInteractable.builder(label)
                .setTooltip(tooltip)
                .build();
    }
}
