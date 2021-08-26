package com.github.dcysteine.neicustomdiagram.api;

import com.google.auto.value.AutoValue;
import cpw.mods.fml.relauncher.ReflectionHelper;
import net.minecraft.client.gui.inventory.GuiContainer;

import java.lang.reflect.Field;

/** Class containing reflection accessors for private fields. */
public final class Reflection {
    public static IntegerField GUI_LEFT;
    public static IntegerField GUI_TOP;

    @AutoValue
    public abstract static class IntegerField {
        public static IntegerField create(Class<?> clazz, String... fieldNames) {
            Field field = ReflectionHelper.findField(clazz, fieldNames);
            return new AutoValue_Reflection_IntegerField(field);
        }

        public abstract Field field();

        public int getInt(Object obj) {
            try {
                return field().getInt(obj);
            } catch (IllegalAccessException e) {
                throw new IllegalStateException("Could not access reflection field!", e);
            }
        }
    }

    /** This method is only intended to be called during mod initialization. */
    public static void initialize() {
        GUI_LEFT = IntegerField.create(GuiContainer.class, "guiLeft", "field_147003_i");
        GUI_TOP = IntegerField.create(GuiContainer.class, "guiTop", "field_147009_r");
    }

}
