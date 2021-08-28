package com.github.dcysteine.neicustomdiagram.util;

import com.github.dcysteine.neicustomdiagram.api.diagram.component.Component;
import com.github.dcysteine.neicustomdiagram.api.diagram.component.DisplayComponent;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Class containing utility methods for comparing and converting between {@link Component} and
 * {@link DisplayComponent}.
 */
public final class ComponentTransformer {
    // Static class.
    private ComponentTransformer() {}

    /** Compares {@code displayComponent}'s component type to {@code component}. */
    public static boolean equals(DisplayComponent displayComponent, Component component) {
        return displayComponent.component().equals(component);
    }

    public static DisplayComponent transformToDisplay(Component component) {
        return DisplayComponent.builder(component).build();
    }

    public static Component transformFromDisplay(DisplayComponent displayComponent) {
        return displayComponent.component();
    }

    public static List<DisplayComponent> transformToDisplay(Collection<Component> components) {
        return components.stream()
                .map(ComponentTransformer::transformToDisplay)
                .collect(Collectors.toList());
    }

    /** The returned list will have duplicate {@link Component}s removed. */
    public static List<Component> transformFromDisplay(
            Collection<DisplayComponent> displayComponents) {
        return displayComponents.stream()
                .map(ComponentTransformer::transformFromDisplay)
                .distinct()
                .collect(Collectors.toList());
    }

    /**
     * Returns whether {@code displayComponents} contains any elements that, when transformed to
     * {@link Component}, are equal to {@code component}.
     */
    public static boolean containsComponent(
            Collection<DisplayComponent> displayComponents, Component component) {
        return displayComponents.stream()
                .anyMatch(displayComponent -> equals(displayComponent, component));
    }

    /**
     * Returns whether {@code components} contains any elements that are equal to
     * {@code displayComponent} transformed to {@link Component}.
     */
    public static boolean containsDisplayComponent(
            Collection<Component> components, DisplayComponent displayComponent) {
        return components.stream().anyMatch(component -> equals(displayComponent, component));
    }

    /**
     * Returns whether {@code displayComponents} contains elements that, when transformed to
     * {@link Component}, are equal to all elements of {@code components}.
     */
    public static boolean containsAllComponents(
            Collection<DisplayComponent> displayComponents, Iterable<Component> components) {
        for (Component component : components) {
            if (!containsComponent(displayComponents, component)) {
                return false;
            }
        }
        return true;
    }

    /**
     * Returns whether {@code components} contains elements that are equal to all elements of
     * {@code displayComponents} transformed to {@link Component}.
     */
    public static boolean containsAllDisplayComponents(
            Collection<Component> components, Iterable<DisplayComponent> displayComponents) {
        for (DisplayComponent displayComponent : displayComponents) {
            if (!containsDisplayComponent(components, displayComponent)) {
                return false;
            }
        }
        return true;
    }

    /**
     * Returns whether {@code displayComponents} contains elements that, when transformed to
     * {@link Component}, are equal to all elements of {@code components}.
     */
    public static boolean containsAllComponents(
            Collection<DisplayComponent> displayComponents, Component... components) {
        return containsAllComponents(displayComponents, Arrays.asList(components));
    }

    /**
     * Returns whether {@code components} contains elements that are equal to all elements of
     * {@code displayComponents} transformed to {@link Component}.
     */
    public static boolean containsAllDisplayComponents(
            Collection<Component> components, DisplayComponent... displayComponents) {
        return containsAllDisplayComponents(components, Arrays.asList(displayComponents));
    }

    /**
     * Removes all elements of {@code displayComponents} that, when transformed to
     * {@link Component}, are equal to {@code component}.
     *
     * @return whether any elements were removed
     */
    public static boolean removeComponent(
            Collection<DisplayComponent> displayComponents, Component component) {
        return displayComponents.removeIf(displayComponent -> equals(displayComponent, component));
    }

    /**
     * Removes all elements of {@code components} that are equal to {@code displayComponent}
     * transformed to {@link Component}.
     *
     * @return whether any elements were removed
     */
    public static boolean removeDisplayComponent(
            Collection<Component> components, DisplayComponent displayComponent) {
        return components.removeIf(component -> equals(displayComponent, component));
    }

    /**
     * Removes all elements of {@code displayComponents} that, when transformed to
     * {@link Component}, are equal to any element of {@code components}.
     *
     * @return whether any elements were removed
     */
    public static boolean removeAllComponents(
            Collection<DisplayComponent> displayComponents, Iterable<Component> components) {
        boolean anythingRemoved = false;
        for (Component component : components) {
            anythingRemoved |= removeComponent(displayComponents, component);
        }
        return anythingRemoved;
    }

    /**
     * Removes all elements of {@code components} that are equal to {@code displayComponent}
     * transformed to {@link Component}.
     *
     * @return whether any elements were removed
     */
    public static boolean removeAllDisplayComponents(
            Collection<Component> components, Iterable<DisplayComponent> displayComponents) {
        boolean anythingRemoved = false;
        for (DisplayComponent displayComponent : displayComponents) {
            anythingRemoved |= removeDisplayComponent(components, displayComponent);
        }
        return anythingRemoved;
    }

    /**
     * Removes all elements of {@code displayComponents} that, when transformed to
     * {@link Component}, are equal to any element of {@code components}.
     *
     * @return whether any elements were removed
     */
    public static boolean removeAllComponents(
            Collection<DisplayComponent> displayComponents, Component... components) {
        return removeAllComponents(displayComponents, Arrays.asList(components));
    }

    /**
     * Removes all elements of {@code components} that are equal to {@code displayComponent}
     * transformed to {@link Component}.
     *
     * @return whether any elements were removed
     */
    public static boolean removeAllDisplayComponents(
            Collection<Component> components, DisplayComponent... displayComponents) {
        return removeAllDisplayComponents(components, Arrays.asList(displayComponents));
    }
}
