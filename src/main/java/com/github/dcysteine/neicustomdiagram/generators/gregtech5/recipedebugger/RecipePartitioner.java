package com.github.dcysteine.neicustomdiagram.generators.gregtech5.recipedebugger;

import com.github.dcysteine.neicustomdiagram.api.diagram.component.Component;
import com.github.dcysteine.neicustomdiagram.api.diagram.component.FluidComponent;
import com.github.dcysteine.neicustomdiagram.api.diagram.component.ItemComponent;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.hash.BloomFilter;
import com.google.common.hash.Funnel;
import com.google.common.hash.PrimitiveSink;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Class that helps us handle huge lists of recipes by partitioning them with bloom filters.
 *
 * <p>Programmed circuits will be ignored for the bloom filter check. This is necessary for the
 * unnecessary circuits view check to work.
 */
class RecipePartitioner {
    private static final int MIN_SIZE_TO_PARTITION = 2048;

    /**
     * This seems like a "magic number" that works pretty well. Much larger numbers seem to perform
     * worse.
     */
    private static final int PARTITION_SIZE = 128;
    private static final int INSERTIONS_PER_RECIPE = 3;

    private enum ComponentFunnel implements Funnel<Component> {
        INSTANCE;

        @Override
        public void funnel(Component from, PrimitiveSink into) {
            switch (from.type()) {
                case ITEM:
                    ItemComponent itemComponent = (ItemComponent) from;
                    into.putInt(itemComponent.itemId())
                            .putInt(itemComponent.damage());
                    break;

                case FLUID:
                    FluidComponent fluidComponent = (FluidComponent) from;
                    into.putInt(fluidComponent.fluidId());
                    break;
            }
        }
    }

    private final ImmutableList<RecipeHandler.Recipe> recipeList;
    private final boolean isPartitioned;

    private Map<BloomFilter<Component>, List<RecipeHandler.Recipe>> partitions;

    RecipePartitioner(List<RecipeHandler.Recipe> recipeList) {
        this.recipeList = ImmutableList.copyOf(recipeList);
        this.isPartitioned = recipeList.size() >= MIN_SIZE_TO_PARTITION;
    }

    /** This method must be called before any other methods are called. */
    void initialize() {
        if (!isPartitioned) {
            return;
        }

        partitions = new HashMap<>();
        for (List<RecipeHandler.Recipe> partition : Lists.partition(recipeList, PARTITION_SIZE)) {
            BloomFilter<Component> bloomFilter =
                    BloomFilter.create(
                            ComponentFunnel.INSTANCE,
                            INSERTIONS_PER_RECIPE * partition.size());

            partition.stream()
                    .flatMap(
                            recipe -> RecipeHandler.filterCircuits(
                                    recipe.inputs().keySet()).stream())
                    .forEach(bloomFilter::put);

            partitions.put(bloomFilter, partition);
        }
    }

    int size() {
        return recipeList.size();
    }

    ImmutableList<RecipeHandler.Recipe> allRecipes() {
        return recipeList;
    }

    Iterable<RecipeHandler.Recipe> lookup(Set<Component> components) {
        if (!isPartitioned) {
            return recipeList;
        }

        List<List<RecipeHandler.Recipe>> matchingPartitions = new ArrayList<>();
        for (Map.Entry<BloomFilter<Component>, List<RecipeHandler.Recipe>> entry
                : partitions.entrySet()) {
            if (RecipeHandler.filterCircuits(components).stream()
                    .allMatch(entry.getKey()::mightContain)) {
                matchingPartitions.add(entry.getValue());
            }
        }

        return Iterables.concat(matchingPartitions);
    }
}
