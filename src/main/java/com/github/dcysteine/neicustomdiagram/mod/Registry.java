package com.github.dcysteine.neicustomdiagram.mod;

import codechicken.nei.api.API;
import com.github.dcysteine.neicustomdiagram.api.diagram.DiagramGenerator;
import com.github.dcysteine.neicustomdiagram.api.diagram.DiagramGroup;
import com.github.dcysteine.neicustomdiagram.api.diagram.DiagramGroupInfo;
import com.github.dcysteine.neicustomdiagram.generators.debug.ruler.DebugRuler;
import com.github.dcysteine.neicustomdiagram.generators.enderstorage.chestoverview.EnderStorageChestOverview;
import com.github.dcysteine.neicustomdiagram.generators.enderstorage.tankoverview.EnderStorageTankOverview;
import com.github.dcysteine.neicustomdiagram.generators.forge.fluidcontainers.ForgeFluidContainers;
import com.github.dcysteine.neicustomdiagram.generators.forge.oredictionary.ForgeOreDictionary;
import com.github.dcysteine.neicustomdiagram.generators.gregtech5.materialparts.GregTechMaterialParts;
import com.github.dcysteine.neicustomdiagram.generators.gregtech5.materialtools.GregTechMaterialTools;
import com.github.dcysteine.neicustomdiagram.generators.gregtech5.oredictionary.GregTechOreDictionary;
import com.github.dcysteine.neicustomdiagram.generators.gregtech5.oreprefixes.GregTechOrePrefixes;
import com.github.dcysteine.neicustomdiagram.generators.gregtech5.oreprocessing.GregTechOreProcessing;
import com.github.dcysteine.neicustomdiagram.mod.config.ConfigOptions;
import com.github.dcysteine.neicustomdiagram.mod.config.DiagramGroupVisibility;
import com.google.auto.value.AutoValue;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import cpw.mods.fml.common.Loader;
import cpw.mods.fml.common.ModContainer;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

/** Registry of diagram generators. Add your diagram generator here! */
public final class Registry {
    /** This will be prepended to all group IDs, to ensure that they are globally unique. */
    public static final String GROUP_ID_PREFIX = "neicustomdiagram.diagramgroup.";

    private static final List<RegistryEntry> entries = new ArrayList<>();
    private static final List<DiagramGenerator> generators = new ArrayList<>();
    private static ImmutableList<DiagramGroupInfo> info = null;

    static {
        // Add your diagram generator here!
        addGenerator("debug.ruler", DebugRuler::new);
        addGenerator(
                "enderstorage.chestoverview", EnderStorageChestOverview::new,
                ModDependency.ENDER_STORAGE);
        addGenerator(
                "enderstorage.tankoverview", EnderStorageTankOverview::new,
                ModDependency.ENDER_STORAGE);
        addGenerator("forge.fluidcontainers", ForgeFluidContainers::new);
        addGenerator("forge.oredictionary", ForgeOreDictionary::new);
        addGenerator(
                "gregtech.materialparts", GregTechMaterialParts::new, ModDependency.GREGTECH_5);
        addGenerator(
                "gregtech.materialtools", GregTechMaterialTools::new, ModDependency.GREGTECH_5);
        addGenerator(
                "gregtech.oredictionary", GregTechOreDictionary::new, ModDependency.GREGTECH_5);
        addGenerator("gregtech.oreprefixes", GregTechOrePrefixes::new, ModDependency.GREGTECH_5);
        addGenerator(
                "gregtech.oreprocessing", GregTechOreProcessing::new, ModDependency.GREGTECH_5);
    }

    public enum ModDependency {
        // If you're adding a new mod dependency here, don't forget to also add it to the list of
        // dependencies in NeiCustomDiagram.java (if necessary).
        ENDER_STORAGE("EnderStorage"),

        // GregTech 5 shares a mod ID with GregTech 6, so we must also check the mod version.
        GREGTECH_5("gregtech") {
            @Override
            public boolean isLoaded() {
                if (super.isLoaded()) {
                    return !getVersion().startsWith("GT6");
                } else {
                    return false;
                }
            }
        },

        // GregTech5 add-ons
        BARTWORKS("bartworks"),
        DETRAV_SCANNER("detravscannermod"),

        // GregTech 6 shares a mod ID with GregTech 5, so we must also check the mod version.
        GREGTECH_6("gregtech") {
            @Override
            public boolean isLoaded() {
                if (super.isLoaded()) {
                    return getVersion().startsWith("GT6");
                } else {
                    return false;
                }
            }
        };

        public final String modId;

        ModDependency(String modId) {
            this.modId = modId;
        }

        public boolean isLoaded() {
            return Loader.isModLoaded(modId);
        }

        public String getVersion() {
            return getMod().getVersion();
        }

        public ModContainer getMod() {
            return Loader.instance().getIndexedModList().get(modId);
        }
    }

    @AutoValue
    protected abstract static class RegistryEntry {
        protected static RegistryEntry create(
                String groupIdSuffix, Function<String, DiagramGenerator> generatorConstructor,
                ModDependency... hardDependencies) {
            return new AutoValue_Registry_RegistryEntry(
                    GROUP_ID_PREFIX + groupIdSuffix, generatorConstructor,
                    ImmutableSet.copyOf(hardDependencies));
        }

        protected abstract String groupId();
        protected abstract Function<String, DiagramGenerator> generatorConstructor();
        protected abstract ImmutableSet<ModDependency> hardDependencies();

        protected DiagramGenerator get() {
            return generatorConstructor().apply(groupId());
        }

        protected List<ModDependency> missingDependencies() {
            return hardDependencies().stream()
                    .filter(modDependency -> !modDependency.isLoaded())
                    .collect(Collectors.toList());
        }
    }

    // Static class.
    private Registry() {};

    /** This method must be called before mod initialization. */
    public static void addGenerator(
            String groupIdSuffix, Function<String, DiagramGenerator> generatorConstructor,
            ModDependency... hardDependencies) {
        entries.add(RegistryEntry.create(groupIdSuffix, generatorConstructor, hardDependencies));
    }

    /** This method is only intended to be called during mod initialization. */
    public static void initialize() {
        Logger.MOD.info("Initializing diagram groups...");

        ImmutableList.Builder<DiagramGroupInfo> infoBuilder = ImmutableList.builder();
        for (RegistryEntry entry : entries) {
            List<ModDependency> missingDependencies = entry.missingDependencies();
            if (!missingDependencies.isEmpty()) {
                Logger.MOD.warn(
                        "Diagram group [{}] is missing dependencies: {}",
                        entry.groupId(), missingDependencies);
                continue;
            }

            DiagramGenerator generator = entry.get();
            infoBuilder.add(generator.info());
            generators.add(generator);
            Logger.MOD.info("Initialized diagram group [{}]!", entry.groupId());
        }
        info = infoBuilder.build();

        Logger.MOD.info("Initialization complete!");
    }

    public static ImmutableList<DiagramGroupInfo> info() {
        return info;
    }

    public static void generateDiagramGroups() {
        Logger.MOD.info("Generating diagram groups...");

        for (DiagramGenerator generator : generators) {
            DiagramGroupInfo info = generator.info();
            if (ConfigOptions.getDiagramGroupVisibility(info) == DiagramGroupVisibility.DISABLED) {
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

    /**
     * Call this after diagram generation to clear out static references so that objects can get
     * garbage-collected.
     *
     * <p>In particular, diagram generators can have quite heavy memory usage, and are no longer
     * used after diagram generation. This method will clear references to them so that they can be
     * garbage-collected.
     */
    public static void cleanUp() {
        entries.clear();
        generators.clear();
        // info() is not cleared because it is used asynchronously by NEIRegisterHandlerInfosEvent,
        // and that could potentially (though unlikely) happen after cleanUp() is called.
    }
}
