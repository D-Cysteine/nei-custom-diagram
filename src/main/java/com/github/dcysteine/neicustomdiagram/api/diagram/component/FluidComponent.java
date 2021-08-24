package com.github.dcysteine.neicustomdiagram.api.diagram.component;

import codechicken.nei.recipe.GuiCraftingRecipe;
import codechicken.nei.recipe.GuiUsageRecipe;
import com.github.dcysteine.neicustomdiagram.api.diagram.interactable.Interactable;
import com.github.dcysteine.neicustomdiagram.api.draw.Draw;
import com.github.dcysteine.neicustomdiagram.api.draw.Point;
import com.google.auto.value.AutoValue;
import net.minecraft.block.Block;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidRegistry;
import net.minecraftforge.fluids.FluidStack;

import java.util.Optional;


@AutoValue
public abstract class FluidComponent implements Component {
    public static FluidComponent create(Fluid fluid) {
        return new AutoValue_FluidComponent(fluid);
    }

    public static FluidComponent create(FluidStack stack) {
        return create(stack.getFluid());
    }

    public static Optional<FluidComponent> create(Block block) {
        Fluid fluid = FluidRegistry.lookupFluidForBlock(block);
        if (fluid == null) {
            return Optional.empty();
        } else {
            return Optional.of(create(fluid));
        }
    }

    public abstract Fluid fluid();

    @Override
    public ComponentType type() {
        return ComponentType.FLUID;
    }

    @Override
    public FluidStack stack() {
        return stack(1_000);
    }

    @Override
    public FluidStack stack(int stackSize) {
        return new FluidStack(fluid(), stackSize);
    }

    @Override
    public String description() {
        return String.format("%s (#%d)",stack().getLocalizedName(), fluid().getID());
    }

    @Override
    public void interact(Interactable.RecipeType recipeType) {
        switch (recipeType) {
            case CRAFTING:
                GuiCraftingRecipe.openRecipeGui("liquid", stack());
                break;

            case USAGE:
                GuiUsageRecipe.openRecipeGui("liquid", stack());
                break;

            case BOOKMARK:
                // Can't bookmark fluids =(
                break;
        }
    }

    @Override
    public void draw(Point pos) {
        Draw.drawFluid(fluid(), pos);
    }

    @Override
    public final String toString() {
        return description();
    }
}