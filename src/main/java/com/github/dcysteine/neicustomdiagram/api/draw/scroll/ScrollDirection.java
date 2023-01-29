package com.github.dcysteine.neicustomdiagram.api.draw.scroll;

public enum ScrollDirection {
    UP(0, -1),
    DOWN(0, 1),
    LEFT(-1, 0),
    RIGHT(1, 0),
    ;

    public final int xFactor;
    public final int yFactor;

    ScrollDirection(int xFactor, int yFactor) {
        this.xFactor = xFactor;
        this.yFactor = yFactor;
    }
}
