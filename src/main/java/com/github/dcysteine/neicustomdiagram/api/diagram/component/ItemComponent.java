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
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.oredict.OreDictionary;

import java.util.Optional;

@AutoValue
public abstract class ItemComponent implements Component {
    public static final int DEFAULT_STACK_SIZE = 1;

    public static ItemComponent create(Item item, int damage, Optional<NBTTagCompound> nbt) {
        if (item.isDamageable()) {
            return new AutoValue_ItemComponent(item, 0, nbt.map(ImmutableNbtWrapper::create));
        } else {
            return new AutoValue_ItemComponent(item, damage, nbt.map(ImmutableNbtWrapper::create));
        }
    }

    public static ItemComponent create(Item item, int damage) {
        return create(item, damage, Optional.empty());
    }

    /** NBT will be discarded. Use {@link #createWithNbt(ItemStack)} if you want NBT. */
    public static ItemComponent create(ItemStack itemStack) {
        return create(itemStack.getItem(), itemStack.getItemDamage());
    }

    public static ItemComponent createWithNbt(ItemStack itemStack) {
        return create(
                itemStack.getItem(), itemStack.getItemDamage(),
                Optional.ofNullable(itemStack.stackTagCompound));
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

    @Override
    public abstract Optional<ImmutableNbtWrapper> nbtWrapper();

    public boolean hasWildcardDamage() {
        return damage() == OreDictionary.WILDCARD_VALUE;
    }

    @Override
    public ComponentType type() {
        return ComponentType.ITEM;
    }

    @Override
    public ItemComponent withNbt(NBTTagCompound nbt) {
        return create(item(), damage(), Optional.of(nbt));
    }

    @Override
    public ItemComponent withoutNbt() {
        return create(item(), damage(), Optional.empty());
    }

    @Override
    public ItemStack stack() {
        return stack(DEFAULT_STACK_SIZE);
    }

    @Override
    public ItemStack stack(int stackSize) {
        ItemStack itemStack = new ItemStack(item(), stackSize, damage());
        nbt().ifPresent(n -> itemStack.stackTagCompound = n);
        return itemStack;
    }

    @Override
    public String description() {
        return String.format("%s (#%d/%d)",
                stack().getDisplayName(), Item.getIdFromItem(item()), damage());
    }

    @Override
    public void interact(Interactable.RecipeType recipeType) {
        ItemStack itemStack = stack();
        switch (recipeType) {
            case CRAFTING:
                GuiCraftingRecipe.openRecipeGui("item", itemStack);
                break;

            case USAGE:
                GuiUsageRecipe.openRecipeGui("item", itemStack);
                break;

            case BOOKMARK:
                ItemPanels.bookmarkPanel.addOrRemoveItem(itemStack);
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
