package com.github.dcysteine.neicustomdiagram.util.bartworks;

import com.github.bartimaeusnek.bartworks.system.material.Werkstoff;
import com.github.bartimaeusnek.bartworks.system.material.WerkstoffLoader;
import com.github.dcysteine.neicustomdiagram.api.diagram.component.ItemComponent;
import gregtech.api.enums.OrePrefixes;
import net.minecraft.item.ItemStack;

import java.util.Optional;

public final class BartWorksOreDictUtil {
    // Static class.
    private BartWorksOreDictUtil() {}

    public static Optional<ItemComponent> getComponent(OrePrefixes prefix, Werkstoff werkstoff) {
        if (!werkstoff.hasGenerationFeature(prefix)) {
            return Optional.empty();
        }

        Optional<ItemStack> itemStackOptional =
                Optional.ofNullable(
                        WerkstoffLoader.getCorrespondingItemStackUnsafe(prefix, werkstoff, 1));
        return itemStackOptional.map(ItemComponent::create);
    }
}
