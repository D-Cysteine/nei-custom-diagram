package com.github.dcysteine.neicustomdiagram.api.diagram.component;

import codechicken.nei.ItemPanels;
import codechicken.nei.recipe.GuiCraftingRecipe;
import codechicken.nei.recipe.GuiUsageRecipe;
import com.github.dcysteine.neicustomdiagram.api.diagram.interactable.Interactable;
import com.github.dcysteine.neicustomdiagram.api.draw.Draw;
import com.github.dcysteine.neicustomdiagram.api.draw.Point;
import com.google.auto.value.AutoValue;
import net.minecraft.block.Block;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraftforge.oredict.OreDictionary;

import java.util.Optional;

@AutoValue
public abstract class ItemComponent implements Component {
    public static ItemComponent create(Item item, int damage) {
        if (item.isDamageable()) {
            return new AutoValue_ItemComponent(item, 0);
        } else {
            return new AutoValue_ItemComponent(item, damage);
        }
    }

    public static ItemComponent create(ItemStack itemStack) {
        return create(itemStack.getItem(), itemStack.getItemDamage());
    }

    public static Optional<ItemComponent> create(Block block, int damage) {
        Item item = Item.getItemFromBlock(block);
        if (item == null) {
            return Optional.empty();
        } else {
            return Optional.of(create(Item.getItemFromBlock(block), damage));
        }
    }

    public abstract Item item();
    public abstract int damage();

    public boolean hasWildcardDamage() {
        return damage() == OreDictionary.WILDCARD_VALUE;
    }

    @Override
    public ComponentType type() {
        return ComponentType.ITEM;
    }

    @Override
    public ItemStack stack() {
        return stack(1);
    }

    @Override
    public ItemStack stack(int stackSize) {
        return new ItemStack(item(), stackSize, damage());
    }

    @Override
    public String description() {
        return String.format("%s (#%d/%d)",
                stack().getDisplayName(), Item.getIdFromItem(item()), damage());
    }

    @Override
    public void interact(Interactable.RecipeType recipeType) {
        switch (recipeType) {
            case CRAFTING:
                GuiCraftingRecipe.openRecipeGui("item", stack());
                break;

            case USAGE:
                GuiUsageRecipe.openRecipeGui("item", stack());
                break;

            case BOOKMARK:
                ItemPanels.bookmarkPanel.addOrRemoveItem(stack());
                break;
        }
    }

    @Override
    public void draw(Point pos) {
        Draw.drawItem(stack(), pos);
    }

    @Override
    public final String toString() {
        return description();
    }
}
