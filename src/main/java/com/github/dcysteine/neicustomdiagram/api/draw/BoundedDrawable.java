package com.github.dcysteine.neicustomdiagram.api.draw;

/** This class represents anything that can be drawn, and has a well-defined bounding box. */
public interface BoundedDrawable extends Drawable {
    Point position();
    int width();
    int height();
}
