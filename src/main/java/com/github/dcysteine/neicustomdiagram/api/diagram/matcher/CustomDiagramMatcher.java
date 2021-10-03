package com.github.dcysteine.neicustomdiagram.api.diagram.matcher;

import com.github.dcysteine.neicustomdiagram.api.diagram.Diagram;
import com.github.dcysteine.neicustomdiagram.api.diagram.component.Component;
import com.github.dcysteine.neicustomdiagram.api.diagram.interactable.Interactable;
import com.google.common.collect.ImmutableList;

import java.util.Collection;
import java.util.function.BiFunction;

/**
 * Implementation of {@link DiagramMatcher} that makes it more convenient to construct custom
 * instances.
 */
public class CustomDiagramMatcher implements DiagramMatcher {
    protected final ImmutableList<Diagram> allDiagrams;
    protected final BiFunction<
            Interactable.RecipeType, Component, Collection<Diagram>> diagramFunction;

    public CustomDiagramMatcher(
            Iterable<? extends Diagram> allDiagrams,
            BiFunction<Interactable.RecipeType, Component, Collection<Diagram>> diagramFunction) {
        this.allDiagrams = ImmutableList.copyOf(allDiagrams);
        this.diagramFunction = diagramFunction;
    }

    /**
     * This constructor is intended for use with diagram generators that generate their diagrams
     * dynamically, and therefore does not support {@link #all()}.
     *
     * <p>Dynamic diagram generation is a good choice if:
     * <ul>
     *     <li>The diagram should be displayed for a very large number of components (e.g. some
     *     debug information that should be shown for most items).
     *     <li>It is infeasible to compute the complete set of all diagrams that will be needed
     *     (e.g. it's difficult or impossible to find all components that should have a diagram).
     * </ul>
     */
    public CustomDiagramMatcher(
            BiFunction<Interactable.RecipeType, Component, Collection<Diagram>> diagramFunction) {
        this(ImmutableList.of(), diagramFunction);
    }

    @Override
    public Collection<Diagram> all() {
        return allDiagrams;
    }

    @Override
    public Collection<Diagram> match(Interactable.RecipeType recipeType, Component component) {
        return diagramFunction.apply(recipeType, component);
    }
}