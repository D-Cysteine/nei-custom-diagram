package com.github.dcysteine.neicustomdiagram.api.diagram.matcher;

import com.github.dcysteine.neicustomdiagram.api.diagram.Diagram;
import com.github.dcysteine.neicustomdiagram.api.diagram.component.Component;
import com.github.dcysteine.neicustomdiagram.api.diagram.interactable.Interactable;
import com.google.common.collect.ImmutableList;

import java.util.Collection;
import java.util.function.BiFunction;

/**
 * Abstract implementation of {@link DiagramMatcher} that dynamically generates diagrams.
 *
 * <p>This implementation is probably the least generally useful, and does not support
 * {@link #all()}. This implementation is a good choice if:
 * <ul>
 *     <li>The diagram should be displayed for a very large number of components (e.g. some debug
 *     information that should be shown for most items).
 *     <li>The diagram is used to display the result of some dynamic computation that needs to be
 *     done during gameplay. In this case, it may make sense to override {@link #all()} to generate
 *     diagrams for some fixed set of components.
 *     <li>It is prohibitively expensive to pre-construct the diagram group.
 * </ul>
 */
public class DynamicDiagramMatcher implements DiagramMatcher {
    protected final BiFunction<
            Interactable.RecipeType, Component, Collection<Diagram>> diagramGenerator;

    public DynamicDiagramMatcher(
            BiFunction<Interactable.RecipeType, Component, Collection<Diagram>> diagramGenerator) {
        this.diagramGenerator = diagramGenerator;
    }

    @Override
    public Collection<Diagram> all() {
        return ImmutableList.of();
    }

    @Override
    public Collection<Diagram> match(Interactable.RecipeType recipeType, Component component) {
        return diagramGenerator.apply(recipeType, component);
    }
}