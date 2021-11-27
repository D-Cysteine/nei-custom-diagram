package com.github.dcysteine.neicustomdiagram.util.enderstorage;

import codechicken.enderstorage.api.EnderStorageManager;
import com.github.dcysteine.neicustomdiagram.api.diagram.component.DisplayComponent;
import com.github.dcysteine.neicustomdiagram.api.diagram.component.ItemComponent;
import com.google.auto.value.AutoValue;
import net.minecraft.init.Blocks;

/** Utility class for convenient handling of Ender Storage frequencies. */
@AutoValue
public abstract class EnderStorageFrequency {
    public static EnderStorageFrequency create(Colour colour1, Colour colour2, Colour colour3) {
        return new AutoValue_EnderStorageFrequency(colour1, colour2, colour3);
    }

    public static EnderStorageFrequency create(int frequency) {
        Colour[] values = Colour.values();
        int[] colours = EnderStorageManager.getColoursFromFreq(frequency);
        return create(values[colours[0]], values[colours[1]], values[colours[2]]);
    }

    /** Ender tanks seem to have their frequency colours in reverse order. */
    public static EnderStorageFrequency createReverse(int frequency) {
        Colour[] values = Colour.values();
        int[] colours = EnderStorageManager.getColoursFromFreq(frequency);
        return create(values[colours[2]], values[colours[1]], values[colours[0]]);
    }

    /** Ender Storage uses the same colour-integer mapping as wool blocks. */
    public enum Colour {
        WHITE, ORANGE, MAGENTA, LIGHT_BLUE, YELLOW, LIME, PINK, GRAY,
        LIGHT_GRAY, CYAN, PURPLE, BLUE, BROWN, GREEN, RED, BLACK;

        public DisplayComponent icon() {
            return DisplayComponent.builder(ItemComponent.create(Blocks.wool, this.ordinal()).get())
                    .build();
        }
    }

    public abstract Colour colour1();
    public abstract Colour colour2();
    public abstract Colour colour3();

    public int frequency() {
        return EnderStorageManager.getFreqFromColours(
                colour1().ordinal(), colour2().ordinal(), colour3().ordinal());
    }
}
