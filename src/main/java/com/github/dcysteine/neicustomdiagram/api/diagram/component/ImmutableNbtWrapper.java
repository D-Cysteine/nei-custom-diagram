package com.github.dcysteine.neicustomdiagram.api.diagram.component;

import com.google.auto.value.AutoValue;
import net.minecraft.nbt.NBTTagCompound;

import java.util.Comparator;

/** Class wrapping the mutable {@link NBTTagCompound} to make it immutable. */
@AutoValue
public abstract class ImmutableNbtWrapper implements Comparable<ImmutableNbtWrapper> {
    /** This comparator is null-safe, for convenience of usage in component comparators. */
    public static final Comparator<ImmutableNbtWrapper> COMPARATOR =
            Comparator.nullsFirst(
                    Comparator.<ImmutableNbtWrapper, String>comparing(w -> w.nbt().toString()));

    public static ImmutableNbtWrapper create(NBTTagCompound nbt) {
        return new AutoValue_ImmutableNbtWrapper((NBTTagCompound) nbt.copy());
    }

    protected abstract NBTTagCompound nbt();

    public NBTTagCompound get() {
        return (NBTTagCompound) nbt().copy();
    }

    @Override
    public int compareTo(ImmutableNbtWrapper other) {
        if (other == null) {
            return 1;
        }

        return COMPARATOR.compare(this, other);
    }
}
