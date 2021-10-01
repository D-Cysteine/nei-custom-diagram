package com.github.dcysteine.neicustomdiagram.util.gregtech5;

import com.github.dcysteine.neicustomdiagram.api.Formatter;
import com.github.dcysteine.neicustomdiagram.api.diagram.component.Component;
import com.github.dcysteine.neicustomdiagram.api.diagram.component.DisplayComponent;
import com.github.dcysteine.neicustomdiagram.api.diagram.tooltip.Tooltip;
import com.github.dcysteine.neicustomdiagram.mod.Lang;
import com.google.common.base.Joiner;
import com.google.common.collect.ImmutableList;
import gregtech.api.util.GT_OreDictUnificator;
import gregtech.api.util.GT_Recipe;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fluids.FluidStack;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

public final class GregTechRecipeUtil {
    private static final Joiner STRING_JOINER = Joiner.on(' ');

    // Static class.
    private GregTechRecipeUtil() {}

    /** Compares ignoring stack size. */
    public static List<GT_Recipe> findRecipeByInput(
            GT_Recipe.GT_Recipe_Map recipes, Component... inputs) {
        return findRecipe(recipes, Arrays.asList(inputs), ImmutableList.of());
    }

    /** Compares ignoring stack size. */
    public static List<GT_Recipe> findRecipeByOutput(
            GT_Recipe.GT_Recipe_Map recipes, Component... outputs) {
        return findRecipe(recipes, ImmutableList.of(), Arrays.asList(outputs));
    }

    public static List<GT_Recipe> findRecipe(
            GT_Recipe.GT_Recipe_Map recipes, Component input, Component output) {
        return findRecipe(recipes, ImmutableList.of(input), ImmutableList.of(output));
    }

    public static List<GT_Recipe> findRecipe(
            GT_Recipe.GT_Recipe_Map recipes,
            Collection<? extends Component> inputs, Collection<? extends Component> outputs) {
        List<FluidStack> inputFluids = new ArrayList<>();
        List<ItemStack> inputItems = new ArrayList<>();
        for (Component component : inputs) {
            switch (component.type()) {
                case ITEM:
                    inputItems.add(GT_OreDictUnificator.get_nocopy((ItemStack) component.stack()));
                    break;

                case FLUID:
                    inputFluids.add((FluidStack) component.stack());
                    break;
            }
        }

        List<FluidStack> outputFluids = new ArrayList<>();
        List<ItemStack> outputItems = new ArrayList<>();
        for (Component component : outputs) {
            switch (component.type()) {
                case ITEM:
                    outputItems.add(GT_OreDictUnificator.get_nocopy((ItemStack) component.stack()));
                    break;

                case FLUID:
                    outputFluids.add((FluidStack) component.stack());
                    break;
            }
        }

        List<GT_Recipe> foundRecipes = new ArrayList<>();
        for (GT_Recipe recipe : recipes.mRecipeList) {
            if (recipeContainsItems(recipe.mInputs, inputItems)
                    && recipeContainsFluids(recipe.mFluidInputs, inputFluids)
                    && recipeContainsItems(recipe.mOutputs, outputItems)
                    && recipeContainsFluids(recipe.mFluidOutputs, outputFluids)) {
                foundRecipes.add(recipe);
            }
        }

        return foundRecipes;
    }

    private static boolean recipeContainsFluids(
            FluidStack[] recipeFluidStacks, Collection<FluidStack> fluidStacks) {
        for (FluidStack fluidStack : fluidStacks) {
            if (Arrays.stream(recipeFluidStacks).noneMatch(fluidStack::isFluidEqual)) {
                return false;
            }
        }
        return true;
    }

    private static boolean recipeContainsItems(
            ItemStack[] recipeItemStacks, Collection<ItemStack> itemStacks) {
        for (ItemStack itemStack : itemStacks) {
            if (Arrays.stream(recipeItemStacks)
                    .noneMatch(s -> GT_OreDictUnificator.isInputStackEqual(s, itemStack))) {
                return false;
            }
        }
        return true;
    }

    public static List<DisplayComponent> buildComponents(ItemStack[] itemStacks) {
        return Arrays.stream(itemStacks)
                .filter(Objects::nonNull)
                .map(itemStack -> DisplayComponent.builder(itemStack).build())
                .collect(Collectors.toList());
    }

    public static List<DisplayComponent> buildComponents(FluidStack[] fluidStacks) {
        return Arrays.stream(fluidStacks)
                .filter(Objects::nonNull)
                .map(fluidStack -> DisplayComponent.builder(fluidStack).build())
                .collect(Collectors.toList());
    }

    public static List<DisplayComponent> buildComponentsFromInputs(GT_Recipe recipe) {
        List<DisplayComponent> components = new ArrayList<>();
        components.addAll(buildComponents(recipe.mInputs));
        components.addAll(buildComponents(recipe.mFluidInputs));
        return components;
    }

    public static List<DisplayComponent> buildComponentsFromOutputs(GT_Recipe recipe) {
        List<DisplayComponent> components = new ArrayList<>();
        components.addAll(buildComponentsFromItemOutputs(recipe));
        components.addAll(buildComponentsFromFluidOutputs(recipe));
        return components;
    }

    public static List<DisplayComponent> buildComponentsFromItemOutputs(GT_Recipe recipe) {
        List<DisplayComponent> results = new ArrayList<>();

        for (int i = 0; i < recipe.mOutputs.length; i++) {
            if (recipe.mOutputs[i] == null) {
                continue;
            }

            DisplayComponent.Builder builder = DisplayComponent.builder(recipe.mOutputs[i]);
            List<String> additionalInfoStrings = new ArrayList<>();
            List<Tooltip> tooltips = new ArrayList<>();

            int chance = recipe.getOutputChance(i);
            if (chance < 100_00) {
                double normalizedChance = chance / 100d;
                // Truncate the decimal portion where possible.
                String formattedChance =
                        chance % 100 == 0
                                ? Integer.toString(chance / 100)
                                : Formatter.formatFloat(normalizedChance);

                tooltips.add(
                        Tooltip.create(
                                Lang.GREGTECH_5_UTIL.transf("outputchance", normalizedChance),
                                Tooltip.INFO_FORMATTING));
                additionalInfoStrings.add(formattedChance + "%");
            }

            Optional<Tooltip> specialConditionsTooltipOptional =
                    buildSpecialConditionsTooltip(recipe);
            if (specialConditionsTooltipOptional.isPresent()) {
                additionalInfoStrings.add("*");
                tooltips.add(specialConditionsTooltipOptional.get());
            }

            if (!additionalInfoStrings.isEmpty()) {
                builder.setAdditionalInfo(STRING_JOINER.join(additionalInfoStrings));
            }
            if (!tooltips.isEmpty()) {
                builder.setAdditionalTooltip(Tooltip.concat(tooltips));
            }

            results.add(builder.build());
        }

        return results;
    }

    public static List<DisplayComponent> buildComponentsFromFluidOutputs(GT_Recipe recipe) {
        List<DisplayComponent> results = new ArrayList<>();

        for (int i = 0; i < recipe.mFluidOutputs.length; i++) {
            if (recipe.mFluidOutputs[i] == null) {
                continue;
            }

            DisplayComponent.Builder builder = DisplayComponent.builder(recipe.mFluidOutputs[i]);
            Optional<Tooltip> specialConditionsTooltipOptional =
                    buildSpecialConditionsTooltip(recipe);
            if (specialConditionsTooltipOptional.isPresent()) {
                builder.setAdditionalInfo("*");
                builder.setAdditionalTooltip(specialConditionsTooltipOptional.get());
            }

            results.add(builder.build());
        }

        return results;
    }

    private static Optional<Tooltip> buildSpecialConditionsTooltip(GT_Recipe recipe) {
        // TODO these special values only apply for certain recipe types.
        //  Do we ever run into cases where they don't apply?
        boolean requiresCleanroom =
                recipe.mSpecialValue == -100 || recipe.mSpecialValue == -300;
        boolean requiresLowGravity =
                recipe.mSpecialValue == -200 || recipe.mSpecialValue == -300;
        if (requiresCleanroom || requiresLowGravity) {
            Tooltip.Builder tooltipBuilder =
                    Tooltip.builder().setFormatting(Tooltip.INFO_FORMATTING);
            if (requiresCleanroom) {
                tooltipBuilder.addTextLine(Lang.GREGTECH_5_UTIL.trans("recipecleanroom"));
            }
            if (requiresLowGravity) {
                tooltipBuilder.addTextLine(Lang.GREGTECH_5_UTIL.trans("recipelowgravity"));
            }
            return Optional.of(tooltipBuilder.build());
        } else {
            return Optional.empty();
        }
    }
}
