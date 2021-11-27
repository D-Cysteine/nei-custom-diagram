package com.github.dcysteine.neicustomdiagram.generators.gregtech5.lenses;

import com.github.dcysteine.neicustomdiagram.api.diagram.component.ItemComponent;
import com.github.dcysteine.neicustomdiagram.api.diagram.interactable.CustomInteractable;
import com.github.dcysteine.neicustomdiagram.api.diagram.layout.ComponentLabel;
import com.github.dcysteine.neicustomdiagram.api.diagram.tooltip.Tooltip;
import com.github.dcysteine.neicustomdiagram.main.Lang;
import com.google.common.base.CaseFormat;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;

enum LensColour {
    WHITE(ItemComponent.create(Blocks.wool, 0).get()),
    ORANGE(ItemComponent.create(Blocks.wool, 1).get()),
    MAGENTA(ItemComponent.create(Blocks.wool, 2).get()),
    LIGHT_BLUE(ItemComponent.create(Blocks.wool, 3).get()),
    YELLOW(ItemComponent.create(Blocks.wool, 4).get()),
    LIME(ItemComponent.create(Blocks.wool, 5).get()),
    PINK(ItemComponent.create(Blocks.wool, 6).get()),
    GRAY(ItemComponent.create(Blocks.wool, 7).get()),
    LIGHT_GRAY(ItemComponent.create(Blocks.wool, 8).get()),
    CYAN(ItemComponent.create(Blocks.wool, 9).get()),
    PURPLE(ItemComponent.create(Blocks.wool, 10).get()),
    BLUE(ItemComponent.create(Blocks.wool, 11).get()),
    BROWN(ItemComponent.create(Blocks.wool, 12).get()),
    GREEN(ItemComponent.create(Blocks.wool, 13).get()),
    RED(ItemComponent.create(Blocks.wool, 14).get()),
    BLACK(ItemComponent.create(Blocks.wool, 15).get()),
    UNIQUE(ItemComponent.create(Items.nether_star, 0));

    private final ItemComponent itemComponent;

    LensColour(ItemComponent itemComponent) {
        this.itemComponent = itemComponent;
    }

    static LensColour get(String oreName) {
        String colour = oreName.substring(RecipeHandler.LENS_COLOUR_ORE_NAME_PREFIX.length());
        return valueOf(CaseFormat.UPPER_CAMEL.to(CaseFormat.UPPER_UNDERSCORE, colour));
    }

    String translateColour() {
        return Lang.GREGTECH_5_LENSES.trans("colour." + name().toLowerCase().replace("_", ""));
    }

    CustomInteractable buildLabel() {
        return CustomInteractable.builder(
                        ComponentLabel.create(itemComponent, LayoutHandler.LENS_COLOUR_POSITION))
                .setTooltip(
                        Tooltip.create(
                                Lang.GREGTECH_5_LENSES.transf(
                                        "colourlenseslabel", translateColour()),
                                Tooltip.INFO_FORMATTING))
                .build();
    }
}
