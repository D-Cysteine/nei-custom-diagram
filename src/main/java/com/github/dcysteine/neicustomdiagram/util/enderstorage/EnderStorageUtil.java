package com.github.dcysteine.neicustomdiagram.util.enderstorage;

import codechicken.enderstorage.EnderStorage;
import codechicken.enderstorage.api.EnderStorageManager;
import codechicken.enderstorage.storage.EnderItemStoragePlugin;
import codechicken.enderstorage.storage.item.EnderItemStorage;
import codechicken.enderstorage.storage.liquid.EnderLiquidStorage;
import com.github.dcysteine.neicustomdiagram.api.diagram.component.Component;
import com.github.dcysteine.neicustomdiagram.api.diagram.component.ItemComponent;
import net.minecraft.client.Minecraft;
import net.minecraft.item.Item;
import net.minecraftforge.fluids.FluidStack;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import java.util.stream.IntStream;

public final class EnderStorageUtil {
    public static final int MAX_FREQUENCY = 0xFFF;

    public enum Owner {
        GLOBAL,
        PERSONAL;

        public String stringParam() {
            switch (this) {
                case GLOBAL:
                    return "global";

                case PERSONAL:
                    return Minecraft.getMinecraft().thePlayer.getDisplayName();
            }

            throw new IllegalStateException("Unhandled Owner: " + this);
        }
    }

    public enum Type {
        CHEST("item"), TANK("liquid"), POUCH("item");

        public final String stringParam;

        Type(String stringParam) {
            this.stringParam = stringParam;
        }
    }

    // Static class.
    private EnderStorageUtil() {}

    public static ItemComponent getItem(Type type) {
        switch (type) {
            case CHEST:
                return ItemComponent.create(EnderStorage.blockEnderChest, 0).get();

            case TANK:
                return ItemComponent.create(EnderStorage.blockEnderChest, 1 << 12).get();

            case POUCH:
                return ItemComponent.create(EnderStorage.itemEnderPouch, 0);
        }

        throw new IllegalArgumentException("Unhandled Type: " + type);
    }

    /** This is the item that is used to lock an ender chest / tank to a personal frequency. */
    public static ItemComponent getPersonalItem() {
        return ItemComponent.create(EnderStorage.personalItem, 0);
    }

    public static Optional<Type> getType(Component component) {
        if (component.type() != Component.ComponentType.ITEM) {
            return Optional.empty();
        }
        ItemComponent itemComponent = (ItemComponent) component;

        if (itemComponent.item() == EnderStorage.itemEnderPouch) {
            return Optional.of(Type.POUCH);
        } else if (itemComponent.item() == Item.getItemFromBlock(EnderStorage.blockEnderChest)) {
            switch (itemComponent.damage() >> 12) {
                case 0:
                    return Optional.of(Type.CHEST);

                case 1:
                    return Optional.of(Type.TANK);

                default:
                    return Optional.empty();
            }
        }

        return Optional.empty();
    }

    /** Returns a map of frequency to ender chest inventory. */
    public static Map<EnderStorageFrequency, EnderItemStorage> getEnderChests(Owner owner) {
        String ownerParam = owner.stringParam();

        // Use LinkedHashMap so that the resulting map iterates through keys in insertion order.
        Map<EnderStorageFrequency, EnderItemStorage> map = new LinkedHashMap<>();
        IntStream.rangeClosed(0, MAX_FREQUENCY)
                .forEach(
                        freq -> map.put(
                                EnderStorageFrequency.create(freq),
                                (EnderItemStorage) getManager().getStorage(
                                        ownerParam, freq, Type.CHEST.stringParam)));

        return map;
    }

    /** Returns a map of frequency to ender tank inventory. */
    public static Map<EnderStorageFrequency, EnderLiquidStorage> getEnderTanks(Owner owner) {
        String ownerParam = owner.stringParam();

        // Use LinkedHashMap so that the resulting map iterates through keys in insertion order.
        Map<EnderStorageFrequency, EnderLiquidStorage> map = new LinkedHashMap<>();
        IntStream.rangeClosed(0, MAX_FREQUENCY)
                // Ender tanks seem to have their frequency colours in reverse order.
                .map(EnderStorageUtil::reverseInt)
                .forEach(
                        freq -> map.put(
                                EnderStorageFrequency.createReverse(freq),
                                (EnderLiquidStorage) getManager().getStorage(
                                        ownerParam, freq, Type.TANK.stringParam)));

        return map;
    }

    /**
     * Ender tanks seem to have their frequency colours in reverse order; this method helps us
     * compensate for that.
     */
    public static int reverseInt(int i) {
        int a = i & 0x00F;
        int b = i & 0x0F0;
        int c = i & 0xF00;

        return (a << 8) | b | (c >> 8);
    }

    public static int getChestSize() {
        return EnderItemStoragePlugin.sizes[EnderItemStoragePlugin.configSize];
    }

    public static boolean isEmpty(EnderItemStorage storage) {
        for (int i = 0; i < getChestSize(); i++) {
            if (storage.getStackInSlot(i) != null) {
                return false;
            }
        }
        return true;
    }

    public static boolean isEmpty(EnderLiquidStorage storage) {
        FluidStack fluidStack = storage.getFluid();
        return fluidStack == null || fluidStack.amount == 0;
    }

    /**
     * Try to return the server instance when possible (which is for the host of a
     * single-player or LAN world).
     *
     * <p>If we're playing on a server, the server instance won't work, so we'll return the client
     * instance, but it has access to limited data. As of the time of this writing
     * ({@code 2021-09}), the client instance will only refresh data for ender chests when they are
     * opened by the player, and data for ender tanks is missing entirely.
     *
     * @see <a href=https://github.com/GTNewHorizons/GT-New-Horizons-Modpack/issues/7860#issuecomment-925382503>
     *     GitHub issue</a>
     */
    private static EnderStorageManager getManager() {
        return EnderStorageManager.instance(!Minecraft.getMinecraft().isSingleplayer());
    }
}
