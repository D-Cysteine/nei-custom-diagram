package com.github.dcysteine.neicustomdiagram.generators.gregtech5.lenses;

import com.github.dcysteine.neicustomdiagram.api.diagram.DiagramGenerator;
import com.github.dcysteine.neicustomdiagram.api.diagram.DiagramGroup;
import com.github.dcysteine.neicustomdiagram.api.diagram.DiagramGroupInfo;
import com.github.dcysteine.neicustomdiagram.api.diagram.component.ItemComponent;
import com.github.dcysteine.neicustomdiagram.api.diagram.matcher.ComponentDiagramMatcher;
import com.github.dcysteine.neicustomdiagram.mod.Lang;
import com.github.dcysteine.neicustomdiagram.util.gregtech5.GregTechOreDictUtil;
import gregtech.api.enums.Materials;
import gregtech.api.enums.OrePrefixes;

public final class GregTechLenses implements DiagramGenerator {
    public static final ItemComponent ICON =
            GregTechOreDictUtil.getComponent(OrePrefixes.lens, Materials.Emerald).get();

    private final DiagramGroupInfo info;

    private final LayoutHandler layoutHandler;
    private final RecipeHandler recipeHandler;
    private final DiagramFactory diagramFactory;

    public GregTechLenses(String groupId) {
        this.info =
                DiagramGroupInfo.builder(
                                Lang.GREGTECH_5_LENSES.trans("groupname"),
                                groupId, ICON, 1)
                        .setDescription("This diagram displays GregTech lens colors and recipes.")
                        .build();

        this.layoutHandler = new LayoutHandler(this.info);
        this.recipeHandler = new RecipeHandler();
        this.diagramFactory = new DiagramFactory(this.layoutHandler, this.recipeHandler);
    }

    @Override
    public DiagramGroupInfo info() {
        return info;
    }

    @Override
    public DiagramGroup generate() {
        layoutHandler.initialize();
        recipeHandler.initialize();

        ComponentDiagramMatcher.Builder matcherBuilder = ComponentDiagramMatcher.builder();
        recipeHandler.allLenses().forEach(
                lens -> diagramFactory.buildDiagrams(lens, matcherBuilder));

        return new DiagramGroup(info, matcherBuilder.build());
    }
}
