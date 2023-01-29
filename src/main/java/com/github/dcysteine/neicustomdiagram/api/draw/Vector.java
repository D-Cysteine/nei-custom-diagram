package com.github.dcysteine.neicustomdiagram.api.draw;

import com.google.auto.value.AutoValue;

/**
 * Immutable class representing a distance and angle in 2D space.
 *
 * <p>While this class's contents are the same as those of {@link Point} and {@link Dimension}, for
 * type-safety, we treat this as a separate class.
 */
@AutoValue
public abstract class Vector {
    public static Vector create(int x, int y) {
        return new AutoValue_Vector(x, y);
    }

    public abstract int x();
    public abstract int y();

    /** Returns a new vector object; the original is not modified. */
    public Vector scale(int scale) {
        return Vector.create(scale * x(), scale * y());
    }

    /** Returns a new vector object; the original is not modified. */
    public Vector sum(Vector other) {
        return Vector.create(x() + other.x(), y() + other.y());
    }

    /** Returns a new vector object; the original is not modified. */
    public Vector difference(Vector other) {
        return Vector.create(x() - other.x(), y() - other.y());
    }
}
