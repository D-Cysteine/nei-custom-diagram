package com.github.dcysteine.neicustomdiagram.api;

import codechicken.nei.api.API;
import com.github.dcysteine.neicustomdiagram.api.diagram.DiagramGenerator;
import com.github.dcysteine.neicustomdiagram.api.diagram.DiagramGroup;
import com.github.dcysteine.neicustomdiagram.api.diagram.DiagramGroupInfo;
import com.github.dcysteine.neicustomdiagram.impl.forge.fluidcontainers.ForgeFluidContainers;
import com.github.dcysteine.neicustomdiagram.impl.forge.oredictionary.ForgeOreDictionary;
import com.github.dcysteine.neicustomdiagram.impl.gregtech.oredictionary.GregTechOreDictionary;
import com.github.dcysteine.neicustomdiagram.impl.gregtech.oreprocessing.GregTechOreProcessing;
import com.google.common.collect.ImmutableList;

/** Registry of diagram generators. Add your diagram generator here! */
public final class Registry {
    public static final ImmutableList<DiagramGenerator> GENERATORS;

    static {
        GENERATORS = ImmutableList.<DiagramGenerator>builder()
                // Add your diagram generator here!
                .add(new ForgeFluidContainers())
                .add(new ForgeOreDictionary())
                .add(new GregTechOreDictionary())
                .add(new GregTechOreProcessing())
                .build();
    }

    // Static class.
    private Registry() {};

    public static void generateDiagramGroups() {
        Logger.MOD.info("Generating diagram groups...");

        for (DiagramGenerator generator : GENERATORS) {
            DiagramGroupInfo info = generator.info();
            if (!Config.getDiagramEnabled(info)) {
                Logger.MOD.info("Diagram group [{}] disabled by config.", info.groupId());
                continue;
            }
            Logger.MOD.info("Generating diagram group [{}]...", info.groupId());

            DiagramGroup diagramGroup = generator.generate();
            API.registerRecipeHandler(diagramGroup);
            API.registerUsageHandler(diagramGroup);

            Logger.MOD.info("Generated diagram group [{}]!", info.groupId());
        }

        Logger.MOD.info("Generation complete!");
    }
}
