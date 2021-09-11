package com.github.dcysteine.neicustomdiagram.util.enderstorage;

import codechicken.enderstorage.api.EnderStorageManager;
import com.github.dcysteine.neicustomdiagram.api.diagram.component.DisplayComponent;
import com.github.dcysteine.neicustomdiagram.api.diagram.component.ItemComponent;
import com.google.auto.value.AutoValue;
import net.minecraft.init.Blocks;

/** Utility class for convenient handling of Ender Storage frequencies. */
@AutoValue
public abstract class EnderStorageFrequency {
    public static EnderStorageFrequency create(Color color1, Color color2, Color color3) {
        return new AutoValue_EnderStorageFrequency(color1, color2, color3);
    }

    public static EnderStorageFrequency create(int frequency) {
        Color[] values = Color.values();
        int[] colors = EnderStorageManager.getColoursFromFreq(frequency);
        return create(values[colors[0]], values[colors[1]], values[colors[2]]);
    }

    /** Ender tanks seem to have their frequency colors in reverse order. */
    public static EnderStorageFrequency createReverse(int frequency) {
        Color[] values = Color.values();
        int[] colors = EnderStorageManager.getColoursFromFreq(frequency);
        return create(values[colors[2]], values[colors[1]], values[colors[0]]);
    }

    /** Ender Storage uses the same color-integer mapping as wool blocks. */
    public enum Color {
        WHITE, ORANGE, MAGENTA, LIGHT_BLUE, YELLOW, LIME, PINK, GRAY,
        LIGHT_GRAY, CYAN, PURPLE, BLUE, BROWN, GREEN, RED, BLACK;

        public DisplayComponent icon() {
            return DisplayComponent.builder(ItemComponent.create(Blocks.wool, this.ordinal()).get())
                    .build();
        }
    }

    public abstract Color color1();
    public abstract Color color2();
    public abstract Color color3();

    public int frequency() {
        return EnderStorageManager.getFreqFromColours(
                color1().ordinal(), color2().ordinal(), color3().ordinal());
    }
}
