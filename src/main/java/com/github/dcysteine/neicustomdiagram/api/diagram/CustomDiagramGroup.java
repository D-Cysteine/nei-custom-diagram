package com.github.dcysteine.neicustomdiagram.api.diagram;

import com.github.dcysteine.neicustomdiagram.api.diagram.interactable.CustomInteractable;
import com.github.dcysteine.neicustomdiagram.api.diagram.interactable.Interactable;
import com.github.dcysteine.neicustomdiagram.api.diagram.matcher.DiagramMatcher;
import com.google.common.collect.ImmutableMap;

import java.util.Map;
import java.util.function.Supplier;

/**
 * Extension of {@link DiagramGroup} that provides a way to specify custom behavior for on special
 * IDs passed to NEI.
 *
 * <p>This is useful for doing things like having a custom button that shows a different set of
 * diagrams. You will need to add in a custom button or some other way of invoking NEI with the
 * custom ID.
 *
 * <p>To avoid collisions with any other NEI handlers, it is strongly recommended that the
 * diagram group's group ID be a prefix of all custom behavior IDs.
 *
 * @see CustomInteractable.Builder#setInteract(String)
 */
public class CustomDiagramGroup extends DiagramGroup {
    /**
     * Map of custom behavior ID to resulting diagrams.
     *
     * <p>The custom diagram method will be called when NEI is invoked with the custom behavior ID.
     *
     * <p>To avoid collisions with any other NEI handlers, it is strongly recommended that the
     * diagram group's group ID be a prefix of all custom behavior IDs.
     */
    private final ImmutableMap<String, Supplier<Iterable<Diagram>>> customBehaviorMap;

    public CustomDiagramGroup(
            DiagramGroupInfo info, DiagramMatcher matcher,
            Supplier<DiagramState> diagramStateSupplier,
            Map<String, Supplier<Iterable<Diagram>>> customBehaviorMap) {
        super(info, matcher, diagramStateSupplier);

        this.customBehaviorMap = ImmutableMap.copyOf(customBehaviorMap);
    }

    public CustomDiagramGroup(
            DiagramGroupInfo info, DiagramMatcher matcher,
            Map<String, Supplier<Iterable<Diagram>>> customBehaviorMap) {
        super(info, matcher);

        this.customBehaviorMap = ImmutableMap.copyOf(customBehaviorMap);
    }

    public CustomDiagramGroup(CustomDiagramGroup parent, Iterable<? extends Diagram> diagrams) {
        super(parent, diagrams);

        this.customBehaviorMap = parent.customBehaviorMap;
    }

    @Override
    public CustomDiagramGroup newInstance(Iterable<? extends Diagram> diagrams) {
        return new CustomDiagramGroup(this, diagrams);
    }

    @Override
    public DiagramGroup loadDiagrams(
            String id, Interactable.RecipeType recipeType, Object... stacks) {
        if (customBehaviorMap.containsKey(id)) {
            return newInstance(customBehaviorMap.get(id).get());
        }

        return super.loadDiagrams(id, recipeType, stacks);
    }
}
