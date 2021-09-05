package com.github.dcysteine.neicustomdiagram.api.diagram.component;

import com.google.auto.value.AutoValue;
import net.minecraft.nbt.NBTTagCompound;

/** Class wrapping the mutable {@link NBTTagCompound} to make it immutable. */
@AutoValue
public abstract class ImmutableNbtWrapper {
    public static ImmutableNbtWrapper create(NBTTagCompound nbt) {
        return new AutoValue_ImmutableNbtWrapper((NBTTagCompound) nbt.copy());
    }

    protected abstract NBTTagCompound nbt();

    public NBTTagCompound get() {
        return (NBTTagCompound) nbt().copy();
    }
}
