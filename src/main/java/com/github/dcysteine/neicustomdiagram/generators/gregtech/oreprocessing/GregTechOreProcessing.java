package com.github.dcysteine.neicustomdiagram.generators.gregtech.oreprocessing;

import com.github.bartimaeusnek.bartworks.system.material.Werkstoff;
import com.github.dcysteine.neicustomdiagram.api.Lang;
import com.github.dcysteine.neicustomdiagram.api.Logger;
import com.github.dcysteine.neicustomdiagram.api.Registry;
import com.github.dcysteine.neicustomdiagram.api.diagram.DiagramGenerator;
import com.github.dcysteine.neicustomdiagram.api.diagram.DiagramGroup;
import com.github.dcysteine.neicustomdiagram.api.diagram.DiagramGroupInfo;
import com.github.dcysteine.neicustomdiagram.api.diagram.component.ItemComponent;
import com.github.dcysteine.neicustomdiagram.api.diagram.matcher.ComponentDiagramMatcher;
import com.github.dcysteine.neicustomdiagram.util.bartworks.BartWorksOreDictUtil;
import com.github.dcysteine.neicustomdiagram.util.gregtech.GregTechOreDictUtil;
import com.google.common.collect.ImmutableList;
import gregtech.api.enums.Materials;
import gregtech.api.enums.OrePrefixes;
import net.minecraft.init.Blocks;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/** Generates ore processing diagrams for GregTech ores. */
public final class GregTechOreProcessing implements DiagramGenerator {
    public static final ItemComponent ICON =
            GregTechOreDictUtil.getComponent(OrePrefixes.ore, Materials.Beryllium)
                    .orElse(ItemComponent.create(Blocks.iron_ore, 0).get());

    private static final ImmutableList<OrePrefixes> OTHER_ORE_PREFIXES = ImmutableList.of(
            OrePrefixes.oreBlackgranite, OrePrefixes.oreRedgranite, OrePrefixes.oreMarble,
            OrePrefixes.oreBasalt, OrePrefixes.oreNetherrack, OrePrefixes.oreNether,
            OrePrefixes.oreDense, OrePrefixes.oreRich, OrePrefixes.oreNormal, OrePrefixes.oreSmall,
            OrePrefixes.orePoor, OrePrefixes.oreEndstone, OrePrefixes.oreEnd);

    private final DiagramGroupInfo info;
    private final LabelHandler labelHandler;
    private final LayoutHandler layoutHandler;
    private final RecipeHandler recipeHandler;

    public GregTechOreProcessing(String groupId) {
        this.info =
                DiagramGroupInfo.create(
                        Lang.GREGTECH_ORE_PROCESSING.trans("groupname"),
                        groupId, ICON, 1);

        this.labelHandler = new LabelHandler();
        this.layoutHandler = new LayoutHandler(this.info, this.labelHandler);
        this.recipeHandler = new RecipeHandler();
    }

    @Override
    public DiagramGroupInfo info() {
        return info;
    }

    @Override
    public DiagramGroup generate() {
        labelHandler.initialize();
        layoutHandler.initialize();
        recipeHandler.initialize();

        ComponentDiagramMatcher.Builder matcherBuilder = ComponentDiagramMatcher.builder();

        for (Materials material : Materials.getAll()) {
            if ((material.mTypes & 8) == 0) {
                // Bit 4 is the flag controlling whether ores get generated.
                // So if it's off, skip this material.
                continue;
            }

            List<ItemComponent> rawOres =
                    GregTechOreDictUtil.getAllComponents(OrePrefixes.ore, material);
            if (rawOres.isEmpty()) {
                continue;
            }

            OTHER_ORE_PREFIXES.forEach(
                    prefix ->
                            rawOres.addAll(
                                    GregTechOreDictUtil.getAllComponents(prefix, material)));

            buildDiagram(matcherBuilder, rawOres);
        }

        if (Registry.ModIds.isModLoaded(Registry.ModIds.BARTWORKS)) {
            for (Werkstoff werkstoff : Werkstoff.werkstoffHashSet) {
                Optional<ItemComponent> rawOre =
                        BartWorksOreDictUtil.getComponent(OrePrefixes.ore, werkstoff);
                if (!rawOre.isPresent()) {
                    continue;
                }

                List<ItemComponent> rawOres = new ArrayList<>();
                rawOres.add(rawOre.get());

                OTHER_ORE_PREFIXES.forEach(
                        prefix ->
                                BartWorksOreDictUtil.getComponent(prefix, werkstoff)
                                        .ifPresent(rawOres::add));

                buildDiagram(matcherBuilder, rawOres);
            }
        }

        // TODO maybe add GT++ ores?

        return new DiagramGroup(info, matcherBuilder.build());
    }

    private void buildDiagram(
            ComponentDiagramMatcher.Builder matcherBuilder, List<ItemComponent> rawOres) {
        DiagramBuilder diagramBuilder =
                new DiagramBuilder(layoutHandler, labelHandler, recipeHandler, rawOres);
        diagramBuilder.buildDiagram(matcherBuilder);

        Logger.GREGTECH_ORE_PROCESSING.debug("Generated diagram [{}]", rawOres.get(0));
    }
}
