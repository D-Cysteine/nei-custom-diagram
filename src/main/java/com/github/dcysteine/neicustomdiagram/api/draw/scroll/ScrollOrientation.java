package com.github.dcysteine.neicustomdiagram.api.draw.scroll;

public enum ScrollOrientation {
    HORIZONTAL(1, 0), VERTICAL(0, 1);

    public final int xFactor;
    public final int yFactor;

    ScrollOrientation(int xFactor, int yFactor) {
        this.xFactor = xFactor;
        this.yFactor = yFactor;
    }

    public int scrollMultiplier(ScrollDirection direction) {
        return xFactor * direction.xFactor + yFactor * direction.yFactor;
    }
}
