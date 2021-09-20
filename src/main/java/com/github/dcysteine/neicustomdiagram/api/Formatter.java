package com.github.dcysteine.neicustomdiagram.api;

public final class Formatter {
    public static final String INTEGER_FORMAT = "%,d";
    public static final String FLOAT_FORMAT = "%.2f";

    // Static class.
    private Formatter() {}

    public static String formatInteger(long i) {
        return String.format(INTEGER_FORMAT, i);
    }

    /**
     * Shortens the string by using metric suffixes if {@code i} has too many digits.
     *
     * <p>Useful for printing fluid stack sizes that can be very large, or any other number that
     * needs to fit in a slot. Only handles positive values, though - negative values will be
     * handled the same way as {@link #formatInteger(long)} would handle them.
     */
    public static String smartFormatInteger(long i) {
        if (i >= 10_000_000) {
            return String.format(INTEGER_FORMAT, Math.round(i / 1_000_000f)) + "M";
        } else if (i >= 100_000) {
            return String.format(INTEGER_FORMAT, Math.round(i / 1_000f)) + "K";
        } else {
            return String.format(INTEGER_FORMAT, i);
        }
    }

    public static String formatFloat(double d) {
        return String.format(FLOAT_FORMAT, d);
    }
}
