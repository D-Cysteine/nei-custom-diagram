package com.github.dcysteine.neicustomdiagram.generators.gregtech5.circuits;

import com.github.dcysteine.neicustomdiagram.api.diagram.CustomDiagramGroup;
import com.github.dcysteine.neicustomdiagram.api.diagram.Diagram;
import com.github.dcysteine.neicustomdiagram.api.diagram.DiagramGenerator;
import com.github.dcysteine.neicustomdiagram.api.diagram.DiagramGroup;
import com.github.dcysteine.neicustomdiagram.api.diagram.DiagramGroupInfo;
import com.github.dcysteine.neicustomdiagram.api.diagram.component.DisplayComponent;
import com.github.dcysteine.neicustomdiagram.api.diagram.component.ItemComponent;
import com.github.dcysteine.neicustomdiagram.api.diagram.matcher.ComponentDiagramMatcher;
import com.github.dcysteine.neicustomdiagram.api.diagram.tooltip.Tooltip;
import com.github.dcysteine.neicustomdiagram.main.Lang;
import com.github.dcysteine.neicustomdiagram.util.gregtech5.GregTechOreDictUtil;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import gregtech.api.enums.GT_Values;
import gregtech.api.enums.ItemList;

public final class GregTechCircuits implements DiagramGenerator {
    public static final ItemComponent ICON =
            GregTechOreDictUtil.getComponent(ItemList.Circuit_Good);

    private final DiagramGroupInfo info;

    private final CircuitLineHandler circuitLineHandler;
    private final LabelHandler labelHandler;
    private final LayoutHandler layoutHandler;
    private final RecipeHandler recipeHandler;
    private final DiagramFactory diagramFactory;

    public GregTechCircuits(String groupId) {
        this.info =
                DiagramGroupInfo.builder(
                                Lang.GREGTECH_5_CIRCUITS.trans("groupname"),
                                groupId, ICON, 1)
                        .setDescription("This diagram displays GregTech circuit lines and recipes.")
                        .build();

        this.circuitLineHandler = new CircuitLineHandler();
        this.labelHandler = new LabelHandler();
        this.layoutHandler = new LayoutHandler(this.info, this.circuitLineHandler);
        this.recipeHandler = new RecipeHandler(this.circuitLineHandler);
        this.diagramFactory =
                new DiagramFactory(
                        this.circuitLineHandler, this.labelHandler, this.layoutHandler,
                        this.recipeHandler);
    }

    @Override
    public DiagramGroupInfo info() {
        return info;
    }

    @Override
    public DiagramGroup generate() {
        circuitLineHandler.initialize();
        labelHandler.initialize();
        layoutHandler.initialize();
        recipeHandler.initialize();

        ImmutableList<Diagram> overviewDiagram =
                ImmutableList.of(diagramFactory.buildOverviewDiagram());

        ComponentDiagramMatcher.Builder matcherBuilder = ComponentDiagramMatcher.builder();
        circuitLineHandler.allCircuits().forEach(
                circuit -> diagramFactory.buildDiagrams(circuit, matcherBuilder));

        return new CustomDiagramGroup(
                info, matcherBuilder.build(),
                ImmutableMap.of(info.groupId(), () -> overviewDiagram));
    }

    static DisplayComponent buildCircuitDisplayComponent(ItemComponent circuit, int tier) {
        return DisplayComponent.builder(circuit)
                .setAdditionalTooltip(
                        Tooltip.create(
                                Lang.GREGTECH_5_CIRCUITS.transf("tierlabel", GT_Values.VN[tier]),
                                Tooltip.INFO_FORMATTING))
                .build();
    }
}
