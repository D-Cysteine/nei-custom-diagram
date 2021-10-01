package com.github.dcysteine.neicustomdiagram.api.draw;

import com.github.dcysteine.neicustomdiagram.api.diagram.DiagramState;

import java.util.stream.StreamSupport;

public interface Drawable {
    /**
     * The maximum x-coordinate and y-coordinate that this object takes up in the GUI.
     *
     * <p>Needed for computing layout and diagram width.
     */
    Dimension maxDimension();

    void draw(DiagramState diagramState);

    static Dimension computeMaxDimension(Iterable<? extends Drawable> drawables) {
        return StreamSupport.stream(drawables.spliterator(), false)
                .map(Drawable::maxDimension)
                .reduce(Dimension.create(0, 0), Dimension::max);
    }
}
