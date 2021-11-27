package com.github.dcysteine.neicustomdiagram.generators.gregtech5.recipedebugger;

import com.github.dcysteine.neicustomdiagram.api.diagram.CustomDiagramGroup;
import com.github.dcysteine.neicustomdiagram.api.diagram.Diagram;
import com.github.dcysteine.neicustomdiagram.api.diagram.DiagramGenerator;
import com.github.dcysteine.neicustomdiagram.api.diagram.DiagramGroupInfo;
import com.github.dcysteine.neicustomdiagram.api.diagram.component.Component;
import com.github.dcysteine.neicustomdiagram.api.diagram.component.ItemComponent;
import com.github.dcysteine.neicustomdiagram.api.diagram.interactable.Interactable;
import com.github.dcysteine.neicustomdiagram.api.diagram.matcher.CustomDiagramMatcher;
import com.github.dcysteine.neicustomdiagram.main.Lang;
import com.github.dcysteine.neicustomdiagram.main.config.DiagramGroupVisibility;
import com.github.dcysteine.neicustomdiagram.util.gregtech5.GregTechOreDictUtil;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import gregtech.api.enums.ItemList;
import gregtech.api.util.GT_Utility;
import net.minecraft.init.Items;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.function.Supplier;

public final class GregTechRecipeDebugger implements DiagramGenerator {
    public static final ItemComponent ICON =
            ItemComponent.create(GT_Utility.getIntegratedCircuit(0));

    public enum View {
        PROGRAMMED_CIRCUITS(
                "-programmed-circuits",
                ItemComponent.create(GT_Utility.getIntegratedCircuit(24)),
                "programmedcircuitsbutton"),

        CONSUME_CIRCUIT_RECIPES(
                "-consume-circuit-recipes",
                ItemComponent.create(Items.blaze_powder, 0),
                "consumecircuitrecipesbutton"),

        UNNECESSARY_CIRCUIT_RECIPES(
                "-unnecessary-circuit-recipes",
                ItemComponent.create(Items.sugar, 0),
                "unnecessarycircuitrecipesbutton"),

        COLLIDING_RECIPES(
                "-colliding-recipes",
                ItemComponent.create(Items.slime_ball, 0),
                "collidingrecipesbutton"),

        VOIDING_RECIPES(
                "-voiding-recipes",
                ItemComponent.create(Items.magma_cream, 0),
                "voidingrecipesbutton"),

        UNEQUAL_CELL_RECIPES(
                "-unequal-cell-recipes",
                GregTechOreDictUtil.getComponent(ItemList.Cell_Empty),
                "unequalcellrecipesbutton");

        /** The suffix to append to the group ID, to get the custom behavior ID for this view. */
        public final String suffix;

        /** The icon for this view's button. */
        public final ItemComponent icon;

        /** The translation key for this view's button tooltip. */
        public final String tooltipKey;

        View(String suffix, ItemComponent icon, String tooltipKey) {
            this.suffix = suffix;
            this.icon = icon;
            this.tooltipKey = tooltipKey;
        }

        public String behaviorId(DiagramGroupInfo info) {
            return info.groupId() + suffix;
        }
    }

    private final DiagramGroupInfo info;

    private final LabelHandler labelHandler;
    private final LayoutFactory layoutFactory;
    private final RecipeHandler recipeHandler;
    private final DiagramHandler diagramHandler;

    public GregTechRecipeDebugger(String groupId) {
        this.info =
                DiagramGroupInfo.builder(
                                Lang.GREGTECH_5_RECIPE_DEBUGGER.trans("groupname"),
                                groupId, ICON, 1)
                        .setDefaultVisibility(DiagramGroupVisibility.DISABLED)
                        .setDescription(
                                "This diagram checks for recipe collisions and various other recipe"
                                        + " issues."
                                        + "\nThis diagram is quite heavy, so you probably shouldn't"
                                        + " enable it unless you need it.")
                        .build();

        this.labelHandler = new LabelHandler();
        this.layoutFactory = new LayoutFactory(this.info, this.labelHandler);
        this.recipeHandler = new RecipeHandler();
        this.diagramHandler = new DiagramHandler(this.info, this.layoutFactory, this.recipeHandler);
    }

    @Override
    public DiagramGroupInfo info() {
        return info;
    }

    @Override
    public CustomDiagramGroup generate() {
        labelHandler.initialize();
        layoutFactory.initialize();
        recipeHandler.initialize();
        diagramHandler.initialize();

        ImmutableMap.Builder<String, Supplier<Collection<Diagram>>> customBehaviorMapBuilder =
                ImmutableMap.builder();
        Arrays.stream(View.values())
                .forEach(view -> customBehaviorMapBuilder.put(
                        view.behaviorId(info), () -> diagramHandler.getDiagrams(view)));

        return new CustomDiagramGroup(
                info,
                new CustomDiagramMatcher(diagramHandler.getMenuDiagram(), this::getDiagram),
                customBehaviorMapBuilder.build());
    }

    /** Returns either a single-element list, or an empty list. */
    private List<Diagram> getDiagram(Interactable.RecipeType unused, Component component) {
        if (component.type() == Component.ComponentType.ITEM
                && ((ItemComponent) component).item() == RecipeHandler.PROGRAMMED_CIRCUIT) {
            return diagramHandler.getDiagrams(View.PROGRAMMED_CIRCUITS);
        }

        return Lists.newArrayList();
    }
}
