package com.github.dcysteine.neicustomdiagram.api.diagram.component;

import codechicken.nei.recipe.GuiCraftingRecipe;
import codechicken.nei.recipe.GuiUsageRecipe;
import com.github.dcysteine.neicustomdiagram.api.diagram.interactable.Interactable;
import com.github.dcysteine.neicustomdiagram.api.draw.Draw;
import com.github.dcysteine.neicustomdiagram.api.draw.Point;
import com.github.dcysteine.neicustomdiagram.mod.config.ConfigOptions;
import com.google.auto.value.AutoValue;
import net.minecraft.block.Block;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidRegistry;
import net.minecraftforge.fluids.FluidStack;

import java.util.Comparator;
import java.util.Optional;


@AutoValue
public abstract class FluidComponent implements Component {
    public static final Comparator<FluidComponent> COMPARATOR =
            Comparator.<FluidComponent, Integer>comparing(c -> c.fluid().getID())
                    .thenComparing(
                            c -> c.nbtWrapper().orElse(null), ImmutableNbtWrapper.COMPARATOR);

    public static final int DEFAULT_STACK_SIZE = 1_000;

    public static FluidComponent create(Fluid fluid, Optional<NBTTagCompound> nbt) {
        return new AutoValue_FluidComponent(fluid, nbt.map(ImmutableNbtWrapper::create));
    }

    public static FluidComponent create(Fluid fluid) {
        return create(fluid, Optional.empty());
    }

    /** NBT will be discarded. Use {@link #createWithNbt(FluidStack)} if you want NBT. */
    public static FluidComponent create(FluidStack stack) {
        return create(stack.getFluid());
    }

    public static FluidComponent createWithNbt(FluidStack stack) {
        return create(stack.getFluid(), Optional.ofNullable(stack.tag));
    }

    public static FluidComponent createWithNbt(FluidStack stack, NBTTagCompound nbt) {
        return create(stack.getFluid(), Optional.of(nbt));
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
    public abstract Optional<ImmutableNbtWrapper> nbtWrapper();

    @Override
    public ComponentType type() {
        return ComponentType.FLUID;
    }

    @Override
    public FluidComponent withNbt(NBTTagCompound nbt) {
        return create(fluid(), Optional.of(nbt));
    }

    @Override
    public FluidComponent withoutNbt() {
        return create(fluid(), Optional.empty());
    }

    @Override
    public FluidStack stack() {
        return stack(DEFAULT_STACK_SIZE);
    }

    @Override
    public FluidStack stack(int stackSize) {
        FluidStack fluidStack = new FluidStack(fluid(), stackSize);
        nbt().ifPresent(n -> fluidStack.tag = n);
        return fluidStack;
    }

    @Override
    public String description() {
        if (ConfigOptions.SHOW_IDS.get()) {
            return String.format("%s (#%d)", stack().getLocalizedName(), fluid().getID());
        } else {
            return stack().getLocalizedName();
        }
    }

    @Override
    public void interact(Interactable.RecipeType recipeType) {
        FluidStack fluidStack = stack();
        switch (recipeType) {
            case CRAFTING:
                GuiCraftingRecipe.openRecipeGui("liquid", fluidStack);
                break;

            case USAGE:
                GuiUsageRecipe.openRecipeGui("liquid", fluidStack);
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

    @Override
    public int compareTo(Component other) {
        if (other == null) {
            return 1;
        }

        if (other instanceof FluidComponent) {
            return COMPARATOR.compare(this, (FluidComponent) other);
        } else {
            return type().compareTo(other.type());
        }
    }
}