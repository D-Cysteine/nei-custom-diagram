package com.github.dcysteine.neicustomdiagram;

import codechicken.nei.event.NEIRegisterHandlerInfosEvent;
import com.github.dcysteine.neicustomdiagram.api.Config;
import com.github.dcysteine.neicustomdiagram.api.Logger;
import com.github.dcysteine.neicustomdiagram.api.Reflection;
import com.github.dcysteine.neicustomdiagram.api.Registry;
import com.github.dcysteine.neicustomdiagram.api.diagram.DiagramGroupInfo;
import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.Mod.EventHandler;
import cpw.mods.fml.common.Mod.Instance;
import cpw.mods.fml.common.event.FMLInitializationEvent;
import cpw.mods.fml.common.event.FMLLoadCompleteEvent;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.relauncher.Side;
import net.minecraftforge.common.MinecraftForge;

/** Main entry point for NEI Custom Diagram. */
@Mod(
        modid = NeiCustomDiagram.MOD_ID,
        name = NeiCustomDiagram.MOD_NAME,
        version = NeiCustomDiagram.MOD_VERSION,
        dependencies = NeiCustomDiagram.MOD_DEPENDENCIES)
public final class NeiCustomDiagram {
    public static final String MOD_ID = "neicustomdiagram";
    public static final String MOD_VERSION = "@version@";
    public static final String MOD_NAME = "NEI Custom Diagram";
    public static final String MOD_DEPENDENCIES =
            "required-after:NotEnoughItems;"
                + "after:gregtech;"
                + "after:bartworks;";

    @Instance(MOD_ID)
    public static NeiCustomDiagram instance;

    @EventHandler
    public void onInitialization(FMLInitializationEvent event) {
        if (event.getSide() != Side.CLIENT) {
            return;
        }
        Logger.MOD.info("Mod initialization starting...");

        Reflection.initialize();
        Registry.initialize();
        Config.initialize();
        Config.saveConfig();
        MinecraftForge.EVENT_BUS.register(NeiCustomDiagram.this);

        Logger.MOD.info("Mod initialization complete!");
    }

    @EventHandler
    public void onLoadComplete(FMLLoadCompleteEvent event) {
        if (event.getSide() != Side.CLIENT) {
            return;
        }
        Logger.MOD.info("Mod post-load starting...");

        // TODO might be nice to hook into BetterLoadingScreen.
        //  Though presently we load so fast that it really doesn't matter.
        Registry.generateDiagramGroups();
        Registry.cleanUp();

        Logger.MOD.info("Mod post-load complete!");
    }

    @SubscribeEvent
    public void registerHandlers(NEIRegisterHandlerInfosEvent event) {
        Logger.MOD.info("Registering handlers for diagram groups...");

        for (DiagramGroupInfo info : Registry.info()) {
            if (Config.getDiagramEnabled(info)) {
                event.registerHandlerInfo(info.groupId(), MOD_NAME, MOD_ID, info::buildHandlerInfo);
                Logger.MOD.info("Registered handler for diagram group [{}]!", info.groupId());
            }
        }

        Logger.MOD.info("Registration complete!");
    }
}