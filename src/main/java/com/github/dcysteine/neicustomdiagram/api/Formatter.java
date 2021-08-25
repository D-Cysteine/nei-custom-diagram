package com.github.dcysteine.neicustomdiagram.api;

public final class Formatter {
    public static final String INTEGER_FORMAT = "%,d";
    public static final String DOUBLE_FORMAT = "%.2f";

    // Static class.
    private Formatter() {}

    public static String formatInt(int i) {
        return String.format(INTEGER_FORMAT, i);
    }

    public static String formatDouble(double d) {
        return String.format(DOUBLE_FORMAT, d);
    }
}
