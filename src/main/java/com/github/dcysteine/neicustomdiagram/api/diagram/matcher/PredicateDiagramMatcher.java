package com.github.dcysteine.neicustomdiagram.api.diagram.matcher;

import com.github.dcysteine.neicustomdiagram.api.diagram.Diagram;
import com.github.dcysteine.neicustomdiagram.api.diagram.component.Component;
import com.github.dcysteine.neicustomdiagram.api.diagram.interactable.Interactable;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.ImmutableSetMultimap;

import java.util.Collection;
import java.util.Map;
import java.util.function.BiPredicate;
import java.util.stream.Collectors;

/**
 * Abstract implementation of {@link DiagramMatcher} that matches diagrams using predicates.
 *
 * <p>This implementation is the most flexible, but requires a greater amount of setup.
 * This implementation is a good choice if:
 * <ul>
 *     <li>There is a small number of diagrams, or diagrams re-use the same few predicates.
 *     <li>Diagrams require complex logic to determine what they match, or they match a large number
 *     of components (such as matching all armor items).
 * </ul>
 */
public class PredicateDiagramMatcher implements DiagramMatcher {
    protected final ImmutableSetMultimap<
            BiPredicate<Interactable.RecipeType, Component>, Diagram> matchData;

    public PredicateDiagramMatcher(
            ImmutableSetMultimap<BiPredicate<Interactable.RecipeType, Component>, Diagram> matchData) {
        this.matchData = matchData;
    }

    @Override
    public Collection<Diagram> all() {
        return ImmutableSet.copyOf(matchData.values());
    }

    @Override
    public Collection<Diagram> match(Interactable.RecipeType recipeType, Component component) {
        return matchData.entries().stream()
                .filter(entry -> entry.getKey().test(recipeType, component))
                .map(Map.Entry::getValue)
                .collect(Collectors.toSet());
    }

    public static Builder builder() {
        return new Builder();
    }

    public static final class Builder {
        private final ImmutableSetMultimap.Builder<
                BiPredicate<Interactable.RecipeType, Component>, Diagram> matchDataBuilder;

        public Builder() {
            matchDataBuilder = new ImmutableSetMultimap.Builder<>();
        }

        public Builder addDiagram(
                BiPredicate<Interactable.RecipeType, Component> predicate, Diagram diagram) {
            matchDataBuilder.put(predicate, diagram);
            return this;
        }

        /**
         * The diagram will match if any of the provided predicates matches.
         *
         * <p>Use this method to break up complex predicates into multiple smaller predicates that
         * can be re-used. Re-using predicates for diagrams will help improve match speed.
         */
        public Builder addDiagram(
                Iterable<BiPredicate<Interactable.RecipeType, Component>> predicates,
                Diagram diagram) {
            predicates.forEach(predicate -> addDiagram(predicate, diagram));
            return this;
        }

        public PredicateDiagramMatcher build() {
            return new PredicateDiagramMatcher(matchDataBuilder.build());
        }
    }
}