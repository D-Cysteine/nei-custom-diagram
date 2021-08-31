package com.github.dcysteine.neicustomdiagram.api.diagram;

import com.github.dcysteine.neicustomdiagram.api.diagram.interactable.Interactable;
import com.github.dcysteine.neicustomdiagram.api.diagram.layout.Layout;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Abstract extension of {@link Diagram} that supports showing and hiding sub-diagrams based on
 * custom diagram state.
 *
 * <p>To use this class, extend it and implement {@link #activeDiagrams(DiagramState)} to return a
 * collection of diagrams that should be shown based on the current diagram state. You'll probably
 * want to add a member variable that stores all diagrams, and then return a subset of that in
 * {@code activeDiagrams(DiagramState)}.
 */
public abstract class CompositeDiagram extends Diagram {
    /** The passed-in layout and interactables will always be shown. */
    public CompositeDiagram(Layout layout, ImmutableList<Interactable> interactables) {
        super(layout, interactables);
    }

    /** The passed-in diagram will always be shown. */
    public CompositeDiagram(Diagram diagram) {
        super(diagram.layout, diagram.interactables);
    }

    @Override
    public Iterable<Interactable> interactables(DiagramState diagramState) {
        List<Iterable<Interactable>> iterables = new ArrayList<>();

        iterables.add(super.interactables(diagramState));
        activeDiagrams(diagramState)
                .forEach(diagram -> iterables.add(diagram.interactables(diagramState)));

        return Iterables.concat(iterables);
    }

    @Override
    public void drawBackground(DiagramState diagramState) {
        super.drawBackground(diagramState);
        activeDiagrams(diagramState).forEach(diagram -> diagram.drawBackground(diagramState));
    }

    @Override
    public void drawForeground(DiagramState diagramState) {
        super.drawForeground(diagramState);
        activeDiagrams(diagramState).forEach(diagram -> diagram.drawForeground(diagramState));
    }

    /** Returns a list of diagrams that should be shown, based on the diagram state. */
    protected abstract Collection<Diagram> activeDiagrams(DiagramState diagramState);
}
