package com.github.dcysteine.neicustomdiagram.generators.gregtech5.oreprocessing;

import com.github.bartimaeusnek.bartworks.system.material.Werkstoff;
import com.github.dcysteine.neicustomdiagram.api.diagram.DiagramGenerator;
import com.github.dcysteine.neicustomdiagram.api.diagram.DiagramGroup;
import com.github.dcysteine.neicustomdiagram.api.diagram.DiagramGroupInfo;
import com.github.dcysteine.neicustomdiagram.api.diagram.component.ItemComponent;
import com.github.dcysteine.neicustomdiagram.api.diagram.matcher.ComponentDiagramMatcher;
import com.github.dcysteine.neicustomdiagram.main.Lang;
import com.github.dcysteine.neicustomdiagram.main.Logger;
import com.github.dcysteine.neicustomdiagram.main.Registry;
import com.github.dcysteine.neicustomdiagram.util.DiagramUtil;
import com.github.dcysteine.neicustomdiagram.util.bartworks.BartWorksOreDictUtil;
import com.github.dcysteine.neicustomdiagram.util.gregtech5.GregTechOreDictUtil;
import com.google.common.collect.ImmutableList;
import gregtech.api.enums.Materials;
import gregtech.api.enums.OrePrefixes;
import gregtech.common.blocks.GT_Block_Ores_Abstract;
import gtPlusPlus.core.block.base.BlockBaseOre;
import gtPlusPlus.core.material.Material;
import net.minecraft.block.Block;
import net.minecraft.item.ItemStack;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/** Generates ore processing diagrams for GregTech ores. */
public final class GregTechOreProcessing implements DiagramGenerator {
    public static final ItemComponent ICON =
            GregTechOreDictUtil.getAllComponents(OrePrefixes.ore, Materials.Aluminium).stream()
                    .filter(GregTechOreProcessing::isGregTechOreBlock)
                    .findFirst().get();

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
                DiagramGroupInfo.builder(
                                Lang.GREGTECH_5_ORE_PROCESSING.trans("groupname"),
                                groupId, ICON, 1)
                        // We'll always insert the ore block itself, so require at least 2
                        // components to be inserted to be non-empty.
                        .setEmptyDiagramPredicate(DiagramUtil.buildEmptyDiagramPredicate(2))
                        .setDescription("This diagram displays GregTech ore processing products.")
                        .build();

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
                    prefix -> rawOres.addAll(
                            GregTechOreDictUtil.getAllComponents(prefix, material)));

            buildDiagram(matcherBuilder, rawOres);
        }

        if (Registry.ModDependency.BARTWORKS.isLoaded()) {
            for (Werkstoff werkstoff : Werkstoff.werkstoffHashSet) {
                Optional<ItemComponent> rawOre =
                        BartWorksOreDictUtil.getComponent(OrePrefixes.ore, werkstoff);
                if (!rawOre.isPresent()) {
                    continue;
                }

                List<ItemComponent> rawOres = new ArrayList<>();
                rawOres.add(rawOre.get());

                OTHER_ORE_PREFIXES.forEach(
                        prefix -> BartWorksOreDictUtil.getComponent(prefix, werkstoff)
                                .ifPresent(rawOres::add));

                buildDiagram(matcherBuilder, rawOres);
            }
        }

        if (Registry.ModDependency.GT_PLUS_PLUS.isLoaded()) {
            for (Material material : Material.mMaterialMap) {
                ItemStack ore = material.getOre(1);
                if (ore == null
                        || !(Block.getBlockFromItem(ore.getItem()) instanceof BlockBaseOre)) {
                    // Skip non-GT++ ore blocks to avoid duplicate diagrams.
                    continue;
                }

                buildDiagram(matcherBuilder, ImmutableList.of(ItemComponent.create(ore)));
            }
        }

        return new DiagramGroup(info, matcherBuilder.build());
    }

    private void buildDiagram(
            ComponentDiagramMatcher.Builder matcherBuilder, List<ItemComponent> rawOres) {
        DiagramBuilder diagramBuilder =
                new DiagramBuilder(layoutHandler, labelHandler, recipeHandler, rawOres);
        diagramBuilder.buildDiagram(matcherBuilder);

        Logger.GREGTECH_5_ORE_PROCESSING.debug("Generated diagram [{}]", rawOres.get(0));
    }

    static boolean isGregTechOreBlock(ItemComponent itemComponent) {
        Block block = Block.getBlockFromItem(itemComponent.item());
        return block instanceof GT_Block_Ores_Abstract;
    }
}
