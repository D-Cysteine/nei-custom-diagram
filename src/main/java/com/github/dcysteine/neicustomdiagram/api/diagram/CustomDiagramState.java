package com.github.dcysteine.neicustomdiagram.api.diagram;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

/**
 * Extension of {@link DiagramState} that provides an outline for handling state, both per-diagram
 * and global (to the diagram group).
 *
 * <p>Rather than using this class directly, you probably want to write a subclass that provides a
 * no-parameter constructor (for passing to {@link DiagramGroup}'s constructor) and provides values
 * for {@code T} and {@code U}.
 */
public class CustomDiagramState<T, U> extends DiagramState {
    protected final Map<Diagram, T> stateMap;
    protected final Function<Diagram, T> stateFunction;
    protected final U globalState;

    public CustomDiagramState(Function<Diagram, T> stateFunction, U globalState) {
        this.stateMap = new HashMap<>();
        this.stateFunction = stateFunction;
        this.globalState = globalState;
    }

    public T state(Diagram diagram) {
        return stateMap.computeIfAbsent(diagram, stateFunction);
    }

    public U globalState() {
        return globalState;
    }
}
