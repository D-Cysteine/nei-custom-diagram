package com.github.dcysteine.neicustomdiagram.api.diagram;

public interface DiagramGenerator {
    DiagramGroupInfo info();
    DiagramGroup generate();
}
