package com.github.dcysteine.neicustomdiagram.api.diagram.matcher;

import com.github.dcysteine.neicustomdiagram.api.diagram.Diagram;
import com.github.dcysteine.neicustomdiagram.api.diagram.component.Component;
import com.github.dcysteine.neicustomdiagram.api.diagram.interactable.Interactable;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSetMultimap;

import java.util.Collection;
import java.util.EnumMap;
import java.util.stream.Collectors;

/**
 * Implementation of {@link DiagramMatcher} that looks up diagrams by their components.
 *
 * <p>This implementation has fast lookup regardless of the number of diagrams, but is
 * memory-intensive. NBT is ignored by default. This implementation is a good choice if:
 * <ul>
 *     <li>There is a large number of diagrams.
 *     <li>Diagrams match a small number of components.
 * </ul>
 */
public class ComponentDiagramMatcher implements DiagramMatcher {
    protected final ImmutableMap<
            Interactable.RecipeType, ImmutableSetMultimap<Component, Diagram>> matchData;

    public ComponentDiagramMatcher(
            ImmutableMap<
                    Interactable.RecipeType, ImmutableSetMultimap<Component, Diagram>> matchData) {
        this.matchData = matchData;
    }

    @Override
    public Collection<Diagram> all() {
        return matchData.values().stream()
                .map(ImmutableSetMultimap::values)
                .flatMap(Collection::stream)
                .collect(Collectors.toSet());
    }

    @Override
    public Collection<Diagram> match(Interactable.RecipeType recipeType, Component component) {
        return matchData.get(recipeType).get(component);
    }

    public static Builder builder() {
        return new Builder();
    }

    public static final class Builder {
        private final EnumMap<Interactable.RecipeType,
                ImmutableSetMultimap.Builder<Component, Diagram>> matchDataBuilder;

        public Builder() {
            matchDataBuilder = new EnumMap<>(Interactable.RecipeType.class);

            for (Interactable.RecipeType recipeType : Interactable.RecipeType.VALID_TYPES) {
                matchDataBuilder.put(recipeType, ImmutableSetMultimap.builder());
            }
        }

        public DiagramSubBuilder addDiagram(Diagram diagram) {
            return new DiagramSubBuilder(diagram);
        }

        public ComponentDiagramMatcher build() {
            ImmutableMap.Builder<
                    Interactable.RecipeType, ImmutableSetMultimap<Component, Diagram>> builder =
                            ImmutableMap.builder();
            matchDataBuilder.forEach((key, value) -> builder.put(key, value.build()));

            return new ComponentDiagramMatcher(builder.build());
        }

        public final class DiagramSubBuilder {
            private final Diagram diagram;

            private DiagramSubBuilder(Diagram diagram) {
                this.diagram = diagram;
            }

            /**
             * If ignoring NBT, it is the caller's responsibility to remove NBT from
             * {@code component}.
             */
            public DiagramSubBuilder addComponent(Component component) {
                for (Interactable.RecipeType recipeType : Interactable.RecipeType.VALID_TYPES) {
                    this.addComponent(recipeType, component);
                }
                return this;
            }

            /**
             * If ignoring NBT, it is the caller's responsibility to remove NBT from
             * {@code components}.
             */
            public DiagramSubBuilder addAllComponents(Iterable<? extends Component> components) {
                for (Interactable.RecipeType recipeType : Interactable.RecipeType.VALID_TYPES) {
                    this.addAllComponents(recipeType, components);
                }
                return this;
            }

            /**
             * If ignoring NBT, it is the caller's responsibility to remove NBT from
             * {@code component}.
             */
            public DiagramSubBuilder addComponent(
                    Interactable.RecipeType recipeType, Component component) {
                matchDataBuilder.get(recipeType).put(component, diagram);
                return this;
            }

            /**
             * If ignoring NBT, it is the caller's responsibility to remove NBT from
             * {@code components}.
             */
            public DiagramSubBuilder addAllComponents(
                    Interactable.RecipeType recipeType, Iterable<? extends Component> components) {
                ImmutableSetMultimap.Builder<Component, Diagram> builder =
                        matchDataBuilder.get(recipeType);
                components.forEach(c -> builder.put(c, diagram));
                return this;
            }
        }
    }
}