package com.github.dcysteine.neicustomdiagram.api.diagram.layout;

import com.github.dcysteine.neicustomdiagram.api.diagram.DiagramState;
import com.github.dcysteine.neicustomdiagram.api.diagram.component.Component;
import com.github.dcysteine.neicustomdiagram.api.draw.BoundedDrawable;
import com.github.dcysteine.neicustomdiagram.api.draw.Draw;
import com.github.dcysteine.neicustomdiagram.api.draw.Point;
import com.google.auto.value.AutoValue;

/** A label that looks like a component. */
@AutoValue
public abstract class ComponentLabel implements BoundedDrawable {
    public static ComponentLabel create(Component component, Point pos) {
        return new AutoValue_ComponentLabel(component, pos);
    }

    public abstract Component component();

    @Override
    public abstract Point position();

    @Override
    public int width() {
        return Draw.ICON_WIDTH;
    }

    @Override
    public int height() {
        return Draw.ICON_WIDTH;
    }

    @Override
    public void draw(DiagramState diagramState) {
        component().draw(position());
    }
}
