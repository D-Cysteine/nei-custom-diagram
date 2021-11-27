package com.github.dcysteine.neicustomdiagram.main.config;

import com.github.dcysteine.neicustomdiagram.main.NeiCustomDiagram;
import cpw.mods.fml.client.config.GuiConfig;
import cpw.mods.fml.client.config.IConfigElement;
import net.minecraft.client.gui.GuiScreen;
import net.minecraftforge.common.config.ConfigElement;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public final class ConfigGui extends GuiConfig {
    public ConfigGui(GuiScreen parent) {
        super(
                parent,
                getConfigOptions(),
                NeiCustomDiagram.MOD_ID,
                false,
                false,
                GuiConfig.getAbridgedConfigPath(Config.getConfigFilePath()),
                NeiCustomDiagram.MOD_NAME);
    }

    @Override
    public void onGuiClosed() {
        super.onGuiClosed();
        Config.saveConfig();
    }

    @SuppressWarnings("rawtypes")
    private static List<IConfigElement> getConfigOptions() {
        return Arrays.stream(ConfigOptions.Category.values())
                .map(
                        category -> new ConfigElement(
                                Config.CONFIG.getCategory(category.toString())))
                .collect(Collectors.toList());
    }
}
