package com.github.dcysteine.neicustomdiagram.mod;

import com.google.auto.value.AutoValue;
import cpw.mods.fml.relauncher.ReflectionHelper;
import net.minecraft.client.gui.inventory.GuiContainer;

import java.lang.reflect.Field;

/** Class containing reflection accessors for private fields. */
public final class Reflection {
    public static ReflectionField<GuiContainer, Integer> GUI_LEFT;
    public static ReflectionField<GuiContainer, Integer> GUI_TOP;
    public static ReflectionField<GuiContainer, Integer> X_SIZE;
    public static ReflectionField<GuiContainer, Integer> Y_SIZE;

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

    /** This method is only intended to be called during mod initialization. */
    public static void initialize() {
        GUI_LEFT = ReflectionField.createInteger(GuiContainer.class, "guiLeft", "field_147003_i");
        GUI_TOP = ReflectionField.createInteger(GuiContainer.class, "guiTop", "field_147009_r");
        X_SIZE = ReflectionField.createInteger(GuiContainer.class, "xSize", "field_146999_f");
        Y_SIZE = ReflectionField.createInteger(GuiContainer.class, "ySize", "field_147000_g");
    }
}
