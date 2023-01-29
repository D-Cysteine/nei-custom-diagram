package com.github.dcysteine.neicustomdiagram.api.draw.scroll;

import com.github.dcysteine.neicustomdiagram.api.draw.Dimension;
import com.github.dcysteine.neicustomdiagram.api.draw.Point;
import com.github.dcysteine.neicustomdiagram.api.draw.Vector;

public enum ScrollOrientation {
    HORIZONTAL(1, 0), VERTICAL(0, 1);

    public final int xFactor;
    public final int yFactor;

    ScrollOrientation(int xFactor, int yFactor) {
        this.xFactor = xFactor;
        this.yFactor = yFactor;
    }

    public Vector scale(int scale) {
        return Vector.create(xFactor * scale, yFactor * scale);
    }

    public Vector transposeScale(int scale) {
        return Vector.create(yFactor * scale, xFactor * scale);
    }

    public int dotProduct(ScrollDirection direction) {
        return xFactor * direction.xFactor + yFactor * direction.yFactor;
    }

    public int dotProduct(Point point) {
        return xFactor * point.x() + yFactor * point.y();
    }

    public int dotProduct(Vector vector) {
        return xFactor * vector.x() + yFactor * vector.y();
    }

    public int dotProduct(Dimension dimension) {
        return xFactor * dimension.width() + yFactor * dimension.height();
    }

    public int transposeProduct(Point point) {
        return yFactor * point.x() + xFactor * point.y();
    }

    public int transposeProduct(Vector vector) {
        return yFactor * vector.x() + xFactor * vector.y();
    }

    public int transposeProduct(Dimension dimension) {
        return yFactor * dimension.width() + xFactor * dimension.height();
    }
}
