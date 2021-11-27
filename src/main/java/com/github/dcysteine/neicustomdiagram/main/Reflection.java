package com.github.dcysteine.neicustomdiagram.main;

import com.google.auto.value.AutoValue;
import cpw.mods.fml.relauncher.ReflectionHelper;
import net.minecraft.client.gui.inventory.GuiContainer;

import java.lang.reflect.Field;

/** Class containing reflection accessors for private fields. */
public final class Reflection {
    public static final ReflectionField<GuiContainer, Integer> GUI_LEFT =
            ReflectionField.createInteger(GuiContainer.class, "guiLeft", "field_147003_i");
    public static final ReflectionField<GuiContainer, Integer> GUI_TOP =
            ReflectionField.createInteger(GuiContainer.class, "guiTop", "field_147009_r");
    public static final ReflectionField<GuiContainer, Integer> X_SIZE =
            ReflectionField.createInteger(GuiContainer.class, "xSize", "field_146999_f");
    public static final ReflectionField<GuiContainer, Integer> Y_SIZE =
            ReflectionField.createInteger(GuiContainer.class, "ySize", "field_147000_g");

    @AutoValue
    public abstract static class ReflectionField<T, U> {
        public static <T> ReflectionField<T, Integer> createInteger(
                Class<T> clazz, String... fieldNames) {
            Field field = ReflectionHelper.findField(clazz, fieldNames);
            return new AutoValue_Reflection_ReflectionField<>(field, field::getInt);
        }

        @FunctionalInterface
        protected interface FieldAccessorFunction<R, S> {
            S apply(R obj) throws IllegalAccessException;
        }

        public abstract Field field();
        public abstract FieldAccessorFunction<T, U> accessorFunction();

        public U get(T obj) {
            try {
                return accessorFunction().apply(obj);
            } catch (IllegalAccessException e) {
                throw new RuntimeException("Could not access reflection field!", e);
            }
        }
    }

    // Static class.
    private Reflection() {}
}
