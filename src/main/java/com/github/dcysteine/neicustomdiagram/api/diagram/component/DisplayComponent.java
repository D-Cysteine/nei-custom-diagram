package com.github.dcysteine.neicustomdiagram.api.diagram.component;

import codechicken.nei.NEIClientUtils;
import com.github.dcysteine.neicustomdiagram.api.Lang;
import com.github.dcysteine.neicustomdiagram.api.diagram.interactable.Interactable;
import com.github.dcysteine.neicustomdiagram.api.diagram.tooltip.TextFormatting;
import com.github.dcysteine.neicustomdiagram.api.diagram.tooltip.Tooltip;
import com.github.dcysteine.neicustomdiagram.api.draw.Draw;
import com.github.dcysteine.neicustomdiagram.api.draw.Point;
import com.google.auto.value.AutoValue;
import com.google.auto.value.extension.toprettystring.ToPrettyString;
import com.google.common.base.Splitter;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.fluids.FluidStack;

import javax.annotation.Nullable;
import java.util.Optional;

/**
 * A display component is a component that may have additional display information attached, such as
 * stack size or tooltip.
 *
 * <p>Note that instances of this class will only compare equal if <em>all</em> of their fields
 * match, including tooltip!
 */
@AutoValue
public abstract class DisplayComponent {
    /** NBT strings will be split if they are too long. */
    private static final Splitter NBT_SPLITTER = Splitter.fixedLength(64);

    public abstract Component component();
    public abstract Optional<Integer> stackSize();

    /** Additional information to be drawn on the component, if any. Keep this short! */
    public abstract Optional<String> additionalInfo();

    /**
     * Returns a tooltip containing additional information for this particular display component;
     * may be empty.
     */
    public abstract Tooltip additionalTooltip();

    public Component.ComponentType type() {
        return component().type();
    }

    /** Returns an {@link ItemStack} or {@link FluidStack}, depending on the component type. */
    public Object stack() {
        return component().stack(stackSize());
    }

    /** Returns a localized description of the item or fluid component, for printing as text. */
    public String description() {
        return component().description();
    }

    /** Returns a localized description of the item or fluid stack, for display in a tooltip. */
    public Tooltip descriptionTooltip() {
        Tooltip.Builder builder = Tooltip.builder().addTextLine(description());
        stackSize().ifPresent(
                stackSize -> builder
                        .setFormatting(TextFormatting.create(true))
                        .addTextLine(Lang.API.transf("stacksize", stackSize)));

        if (component().nbt().isPresent()) {
            NBTTagCompound nbt = component().nbt().get();
            if (NEIClientUtils.shiftKey()) {
                builder.addSpacing()
                        .setFormatting(Tooltip.TRIVIAL_FORMATTING)
                        .addAllTextLines(NBT_SPLITTER.split(nbt.toString()));
            } else {
                builder.addSpacing()
                        .setFormatting(Tooltip.INFO_FORMATTING)
                        .addTextLine(Lang.API.trans("hasnbt"));
            }
        }

        return builder.build();
    }

    public void interact(Interactable.RecipeType recipeType) {
        component().interact(recipeType);
    }

    public void draw(Point pos) {
        component().draw(pos);
        stackSize().ifPresent(
                stackSize ->
                        Draw.drawStackSize(
                                stackSize, pos,
                                component().type() == Component.ComponentType.FLUID));
        additionalInfo().ifPresent(
                additionalInfo -> Draw.drawAdditionalInfo(additionalInfo, pos, true));
    }

    @ToPrettyString
    public abstract String toPrettyString();

    public static Builder builder(Component component) {
        return new AutoValue_DisplayComponent.Builder()
                .setComponent(component)
                .setAdditionalTooltip(Tooltip.EMPTY_TOOLTIP);
    }

    public static Builder builder(ItemStack itemStack) {
        return new AutoValue_DisplayComponent.Builder()
                .setComponent(ItemComponent.create(itemStack))
                .setStackSize(itemStack.stackSize)
                .setAdditionalTooltip(Tooltip.EMPTY_TOOLTIP);
    }

    public static Builder builder(FluidStack fluidStack) {
        return new AutoValue_DisplayComponent.Builder()
                .setComponent(FluidComponent.create(fluidStack))
                .setStackSize(fluidStack.amount)
                .setAdditionalTooltip(Tooltip.EMPTY_TOOLTIP);
    }

    public abstract Builder toBuilder();

    @AutoValue.Builder
    public abstract static class Builder {
        public abstract Builder setComponent(Component component);
        public abstract Builder setStackSize(Optional<Integer> stackSize);
        public abstract Builder setStackSize(int stackSize);
        public abstract Builder setAdditionalInfo(Optional<String> stackSize);
        public abstract Builder setAdditionalInfo(@Nullable String stackSize);
        public abstract Builder setAdditionalTooltip(Tooltip additionalTooltip);

        public Builder clearStackSize() {
            return setStackSize(Optional.empty());
        }

        public Builder clearAdditionalInfo() {
            return setAdditionalInfo(Optional.empty());
        }

        public abstract DisplayComponent build();
    }
}