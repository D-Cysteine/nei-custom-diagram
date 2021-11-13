package com.github.dcysteine.neicustomdiagram.util.dreamcraft;

import com.dreammaster.gthandler.CustomItemList;
import com.github.dcysteine.neicustomdiagram.api.diagram.component.ItemComponent;

public class DreamcraftUtil {
    // Static class.
    private DreamcraftUtil() {}

    public static ItemComponent getComponent(CustomItemList item) {
        return ItemComponent.create(item.get(1));
    }
}
