package com.github.dcysteine.neicustomdiagram.api.draw;

import com.google.auto.value.AutoValue;

/**
 * Immutable class representing a width and height.
 *
 * <p>While this class's contents are the same as those of {@link Point}, for type-safety, we treat
 * this as a separate class.
 */
@AutoValue
public abstract class Dimension {
    public static Dimension create(int width, int height) {
        return new AutoValue_Dimension(width, height);
    }

    public static Dimension create(int width) {
        return new AutoValue_Dimension(width, width);
    }

    public abstract int width();
    public abstract int height();

    /** Takes the larger width and larger height. */
    public static Dimension max(Dimension a, Dimension b) {
        return Dimension.create(Math.max(a.width(), b.width()), Math.max(a.height(), b.height()));
    }
}
