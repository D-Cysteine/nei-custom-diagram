package com.github.dcysteine.neicustomdiagram.api.draw;

/**
 * This class represents anything that can be drawn, and has a well-defined bounding box.
 *
 * <p>Here, by "bounding box", we mean hitbox, or area where a click should be interpreted as on
 * that object.
 */
public interface BoundedDrawable extends Drawable {
    /** The center of this bounded drawable. */
    Point position();

    /** The width and height of this bounded drawable. */
    Dimension dimension();

    @Override
    default Dimension maxDimension() {
        return Dimension.create(
                position().x() + dimension().width() / 2,
                position().y() + dimension().height() / 2);
    }
}
