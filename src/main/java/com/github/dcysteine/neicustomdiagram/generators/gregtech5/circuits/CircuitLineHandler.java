package com.github.dcysteine.neicustomdiagram.generators.gregtech5.circuits;

import com.dreammaster.gthandler.CustomItemList;
import com.github.bartimaeusnek.bartworks.system.material.CircuitGeneration.BW_Meta_Items;
import com.github.dcysteine.neicustomdiagram.api.diagram.component.DisplayComponent;
import com.github.dcysteine.neicustomdiagram.api.diagram.component.ItemComponent;
import com.github.dcysteine.neicustomdiagram.main.Registry;
import com.github.dcysteine.neicustomdiagram.util.dreamcraft.DreamcraftUtil;
import com.github.dcysteine.neicustomdiagram.util.gregtech5.GregTechOreDictUtil;
import com.google.auto.value.AutoValue;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableListMultimap;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Iterables;
import com.google.common.collect.ListMultimap;
import com.google.common.collect.MultimapBuilder;
import com.google.common.collect.Sets;
import gregtech.api.enums.ItemList;
import gregtech.api.util.GT_ModHandler;

import javax.annotation.Nullable;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

class CircuitLineHandler {
    @AutoValue
    abstract static class CircuitLineCircuits {
        private static final CircuitLineCircuits EMPTY =
                new AutoValue_CircuitLineHandler_CircuitLineCircuits(
                        Optional.empty(), Optional.empty(), Optional.empty());

        /** The previous circuit in the circuit line, if it exists. */
        abstract Optional<DisplayComponent> previousCircuit();

        /**
         * The current circuit in the circuit line, or empty optional if this circuit is not part of
         * a circuit line.
         */
        abstract Optional<DisplayComponent> currentCircuit();

        /** The next circuit in the circuit line, if it exists. */
        abstract Optional<DisplayComponent> nextCircuit();

        private static Builder builder(ItemComponent currentCircuit, int tier) {
            return new Builder(currentCircuit, tier);
        }

        private static final class Builder {
            @Nullable private ItemComponent previousCircuit;
            private final ItemComponent currentCircuit;
            @Nullable private ItemComponent nextCircuit;
            private final int tier;

            private Builder(ItemComponent currentCircuit, int tier) {
                this.tier = tier;
                this.previousCircuit = null;
                this.currentCircuit = currentCircuit;
                this.nextCircuit = null;
            }

            Builder setPreviousCircuit(@Nullable ItemComponent previousCircuit) {
                this.previousCircuit = previousCircuit;
                return this;
            }

            Builder setNextCircuit(@Nullable ItemComponent nextCircuit) {
                this.nextCircuit = nextCircuit;
                return this;
            }

            CircuitLineCircuits build() {
                if (previousCircuit == null && nextCircuit == null) {
                    // Not part of a circuit line.
                    return EMPTY;
                }

                return new AutoValue_CircuitLineHandler_CircuitLineCircuits(
                        buildCircuitDisplayComponent(previousCircuit, tier - 1),
                        buildCircuitDisplayComponent(currentCircuit, tier),
                        buildCircuitDisplayComponent(nextCircuit, tier + 1));
            }

            private Optional<DisplayComponent> buildCircuitDisplayComponent(
                    @Nullable ItemComponent circuit, int tier) {
                return Optional.ofNullable(circuit)
                        .map(c -> GregTechCircuits.buildCircuitDisplayComponent(circuit, tier));
            }
        }
    }

    private ImmutableList<CircuitLine> circuitLines;

    /** These are circuit "lines" that only contain a single circuit. */
    private ImmutableList<CircuitLine> individualCircuits;

    /** Additional items that we want to generate circuit assembling machine diagrams for. */
    private ImmutableList<ItemComponent> additionalDiagramItems;

    /**
     * Circuit parts that have tiers go into their own sub-list; single circuit parts go into a
     * one-element sub-list.
     */
    private ImmutableList<ImmutableList<ItemComponent>> circuitParts;

    /**
     * Map of circuit to adjacent circuits in the circuit line.
     *
     * <p>To make it easier for callers, all circuits will map to an instance of
     * {@link CircuitLineCircuits}, but those instances will be empty for individual circuits.
     */
    private ImmutableMap<ItemComponent, CircuitLineCircuits> circuitLineCircuits;

    /**
     * Map of circuit to list of circuits of the same tier.
     *
     * <p>The returned list will always include at least the key circuit.
     */
    private ImmutableListMultimap<ItemComponent, DisplayComponent> tierCircuits;

    /** This method must be called before any other methods are called. */
    void initialize() {
        ImmutableList.Builder<CircuitLine> circuitLinesBuilder = ImmutableList.builder();
        if (Registry.ModDependency.BARTWORKS.isLoaded()) {
            CircuitLine.Builder circuitLineBuilder =
                    CircuitLine.builder()
                            .addBoard(
                                    ItemComponent.create(
                                            BW_Meta_Items.getNEWCIRCUITS().getStack(3)))
                            .setStartTier(0);

            IntStream.rangeClosed(4, 14)
                    .mapToObj(i -> ItemComponent.create(BW_Meta_Items.getNEWCIRCUITS().getStack(i)))
                    .forEach(circuitLineBuilder::addCircuit);

            circuitLinesBuilder.add(circuitLineBuilder.build());
        }
        circuitLinesBuilder.add(
                CircuitLine.builder()
                        .addBoards(
                                GregTechOreDictUtil.getComponent(
                                        ItemList.Circuit_Board_Coated_Basic),
                                GregTechOreDictUtil.getComponent(
                                        ItemList.Circuit_Board_Phenolic_Good))
                        .setStartTier(0)
                        .addCircuits(
                                GregTechOreDictUtil.getComponent(ItemList.Circuit_Primitive),
                                ItemComponent.create(
                                        GT_ModHandler.getIC2Item("electronicCircuit", 1L)),
                                GregTechOreDictUtil.getComponent(ItemList.Circuit_Good))
                        .build());
        circuitLinesBuilder.add(
                CircuitLine.builder()
                        .addBoards(
                                GregTechOreDictUtil.getComponent(
                                        ItemList.Circuit_Board_Coated_Basic),
                                GregTechOreDictUtil.getComponent(
                                        ItemList.Circuit_Board_Phenolic_Good))
                        .setStartTier(1)
                        .addCircuits(
                                GregTechOreDictUtil.getComponent(ItemList.Circuit_Basic),
                                GregTechOreDictUtil.getComponent(ItemList.Circuit_Integrated_Good),
                                ItemComponent.create(
                                        GT_ModHandler.getIC2Item("advancedCircuit", 1L)))
                .build());
        circuitLinesBuilder.add(
                CircuitLine.builder()
                        .addBoard(
                                GregTechOreDictUtil.getComponent(
                                        ItemList.Circuit_Board_Plastic_Advanced))
                        .setStartTier(2)
                        .addCircuits(
                                GregTechOreDictUtil.getComponent(ItemList.Circuit_Processor),
                                GregTechOreDictUtil.getComponent(ItemList.Circuit_Advanced),
                                GregTechOreDictUtil.getComponent(ItemList.Circuit_Data),
                                GregTechOreDictUtil.getComponent(ItemList.Circuit_Elite))
                        .build());
        circuitLinesBuilder.add(
                CircuitLine.builder()
                        .addBoard(
                                GregTechOreDictUtil.getComponent(
                                        ItemList.Circuit_Board_Epoxy_Advanced))
                        .setStartTier(3)
                        .addCircuits(
                                GregTechOreDictUtil.getComponent(ItemList.Circuit_Nanoprocessor),
                                GregTechOreDictUtil.getComponent(ItemList.Circuit_Nanocomputer),
                                GregTechOreDictUtil.getComponent(
                                        ItemList.Circuit_Elitenanocomputer),
                                GregTechOreDictUtil.getComponent(ItemList.Circuit_Master))
                        .build());
        circuitLinesBuilder.add(
                CircuitLine.builder()
                        .addBoard(
                                GregTechOreDictUtil.getComponent(
                                        ItemList.Circuit_Board_Fiberglass_Advanced))
                        .setStartTier(4)
                        .addCircuits(
                                GregTechOreDictUtil.getComponent(ItemList.Circuit_Quantumprocessor),
                                GregTechOreDictUtil.getComponent(ItemList.Circuit_Quantumcomputer),
                                GregTechOreDictUtil.getComponent(
                                        ItemList.Circuit_Masterquantumcomputer),
                                GregTechOreDictUtil.getComponent(ItemList.Circuit_Quantummainframe))
                        .build());
        circuitLinesBuilder.add(
                CircuitLine.builder()
                        .addBoard(
                                GregTechOreDictUtil.getComponent(
                                        ItemList.Circuit_Board_Multifiberglass_Elite))
                        .setStartTier(5)
                        .addCircuits(
                                GregTechOreDictUtil.getComponent(ItemList.Circuit_Crystalprocessor),
                                GregTechOreDictUtil.getComponent(ItemList.Circuit_Crystalcomputer),
                                GregTechOreDictUtil.getComponent(
                                        ItemList.Circuit_Ultimatecrystalcomputer),
                                GregTechOreDictUtil.getComponent(ItemList.Circuit_Crystalmainframe))
                        .build());
        circuitLinesBuilder.add(
                CircuitLine.builder()
                        .addBoard(
                                GregTechOreDictUtil.getComponent(
                                        ItemList.Circuit_Board_Wetware_Extreme))
                        .setStartTier(6)
                        .addCircuits(
                                GregTechOreDictUtil.getComponent(ItemList.Circuit_Neuroprocessor),
                                GregTechOreDictUtil.getComponent(ItemList.Circuit_Wetwarecomputer),
                                GregTechOreDictUtil.getComponent(
                                        ItemList.Circuit_Wetwaresupercomputer),
                                GregTechOreDictUtil.getComponent(ItemList.Circuit_Wetwaremainframe))
                        .build());
        circuitLinesBuilder.add(
                CircuitLine.builder()
                        .addBoard(
                                GregTechOreDictUtil.getComponent(ItemList.Circuit_Board_Bio_Ultra))
                        .setStartTier(7)
                        .addCircuits(
                                GregTechOreDictUtil.getComponent(ItemList.Circuit_Bioprocessor),
                                GregTechOreDictUtil.getComponent(ItemList.Circuit_Biowarecomputer),
                                GregTechOreDictUtil.getComponent(
                                        ItemList.Circuit_Biowaresupercomputer),
                                GregTechOreDictUtil.getComponent(ItemList.Circuit_Biomainframe))
                        .build());
        // If we need to add any more circuit lines, we'll probably just want to add an entire
        // second page of circuit lines.
        circuitLines = circuitLinesBuilder.build();

        ImmutableList.Builder<CircuitLine> individualCircuitsBuilder = ImmutableList.builder();
        if (Registry.ModDependency.GTNH_CORE_MOD.isLoaded()) {
            individualCircuitsBuilder.add(
                    CircuitLine.builder()
                            .addBoard(DreamcraftUtil.getComponent(CustomItemList.NandChipBoard))
                            .setStartTier(0)
                            .addCircuit(GregTechOreDictUtil.getComponent(ItemList.NandChip))
                            .build());
        } else {
            individualCircuitsBuilder.add(
                    CircuitLine.builder()
                            .addBoard(
                                    GregTechOreDictUtil.getComponent(
                                            ItemList.Circuit_Board_Phenolic_Good))
                            .setStartTier(0)
                            .addCircuit(GregTechOreDictUtil.getComponent(ItemList.NandChip))
                            .build());
        }
        individualCircuitsBuilder.add(
                CircuitLine.builder()
                        .addBoard(
                                GregTechOreDictUtil.getComponent(
                                        ItemList.Circuit_Board_Plastic_Advanced))
                        .setStartTier(1)
                        .addCircuit(GregTechOreDictUtil.getComponent(
                                ItemList.Circuit_Microprocessor))
                        .build());
        individualCircuits = individualCircuitsBuilder.build();

        ImmutableList.Builder<ItemComponent> additionalDiagramItemsBuilder =
                ImmutableList.builder();
        if (Registry.ModDependency.GTNH_CORE_MOD.isLoaded()) {
            additionalDiagramItemsBuilder.add(
                    DreamcraftUtil.getComponent(CustomItemList.NandChipBoard));
        }
        additionalDiagramItems = additionalDiagramItemsBuilder.build();

        ImmutableList.Builder<ImmutableList<ItemComponent>> circuitPartsBuilder =
                ImmutableList.builder();
        circuitPartsBuilder.add(
                ImmutableList.of(
                        GregTechOreDictUtil.getComponent(ItemList.Circuit_Parts_Resistor),
                        GregTechOreDictUtil.getComponent(ItemList.Circuit_Parts_ResistorSMD),
                        GregTechOreDictUtil.getComponent(ItemList.Circuit_Parts_ResistorASMD),
                        GregTechOreDictUtil.getComponent(ItemList.Circuit_Parts_ResistorXSMD)));
        circuitPartsBuilder.add(
                ImmutableList.of(
                        GregTechOreDictUtil.getComponent(ItemList.Circuit_Parts_Diode),
                        GregTechOreDictUtil.getComponent(ItemList.Circuit_Parts_DiodeSMD),
                        GregTechOreDictUtil.getComponent(ItemList.Circuit_Parts_DiodeASMD),
                        GregTechOreDictUtil.getComponent(ItemList.Circuit_Parts_DiodeXSMD)));
        circuitPartsBuilder.add(
                ImmutableList.of(
                        GregTechOreDictUtil.getComponent(ItemList.Circuit_Parts_Transistor),
                        GregTechOreDictUtil.getComponent(ItemList.Circuit_Parts_TransistorSMD),
                        GregTechOreDictUtil.getComponent(ItemList.Circuit_Parts_TransistorASMD),
                        GregTechOreDictUtil.getComponent(ItemList.Circuit_Parts_TransistorXSMD)));
        circuitPartsBuilder.add(
                ImmutableList.of(
                        GregTechOreDictUtil.getComponent(ItemList.Circuit_Parts_Capacitor),
                        GregTechOreDictUtil.getComponent(ItemList.Circuit_Parts_CapacitorSMD),
                        GregTechOreDictUtil.getComponent(ItemList.Circuit_Parts_CapacitorASMD),
                        GregTechOreDictUtil.getComponent(ItemList.Circuit_Parts_CapacitorXSMD)));
        circuitPartsBuilder.add(
                ImmutableList.of(
                        GregTechOreDictUtil.getComponent(ItemList.Circuit_Parts_Coil),
                        GregTechOreDictUtil.getComponent(ItemList.Circuit_Parts_InductorSMD),
                        GregTechOreDictUtil.getComponent(ItemList.Circuit_Parts_InductorASMD),
                        GregTechOreDictUtil.getComponent(ItemList.Circuit_Parts_InductorXSMD)));
        circuitParts = circuitPartsBuilder.build();

        ImmutableMap.Builder<ItemComponent, CircuitLineCircuits> circuitLineCircuitsBuilder =
                ImmutableMap.builder();
        ListMultimap<Integer, ItemComponent> tierCircuitsMap =
                MultimapBuilder.hashKeys().arrayListValues().build();
        for (CircuitLine circuitLine : circuitLines) {
            ItemComponent previousCircuit = null;
            ImmutableList<ItemComponent> circuits = circuitLine.circuits();
            for (int i = 0; i < circuits.size(); i++) {
                ItemComponent currentCircuit = circuits.get(i);
                int tier = circuitLine.startTier() + i;

                circuitLineCircuitsBuilder.put(
                        currentCircuit,
                        CircuitLineCircuits.builder(currentCircuit, tier)
                                .setPreviousCircuit(previousCircuit)
                                .setNextCircuit(
                                        i < circuits.size() - 1 ? circuits.get(i + 1) : null)
                                .build());
                previousCircuit = currentCircuit;

                tierCircuitsMap.put(tier, currentCircuit);
            }
        }
        for (CircuitLine circuitLine : individualCircuits) {
            ItemComponent circuit = Iterables.getOnlyElement(circuitLine.circuits());
            circuitLineCircuitsBuilder.put(circuit, CircuitLineCircuits.EMPTY);
            tierCircuitsMap.put(circuitLine.startTier(), circuit);
        }
        circuitLineCircuits = circuitLineCircuitsBuilder.build();

        ImmutableListMultimap.Builder<ItemComponent, DisplayComponent> tierCircuitsBuilder =
                ImmutableListMultimap.builder();
        for (Map.Entry<Integer, Collection<ItemComponent>> entry
                : tierCircuitsMap.asMap().entrySet()) {
            Collection<ItemComponent> circuits = entry.getValue();
            List<DisplayComponent> displayCircuits =
                    circuits.stream()
                            .map(circuit -> GregTechCircuits.buildCircuitDisplayComponent(
                                    circuit, entry.getKey()))
                            .collect(Collectors.toList());

            circuits.forEach(circuit -> tierCircuitsBuilder.putAll(circuit, displayCircuits));
        }
        tierCircuits = tierCircuitsBuilder.build();
    }

    Set<ItemComponent> allCircuits() {
        Set<ItemComponent> allCircuits = Sets.newHashSet(additionalDiagramItems);
        for (CircuitLine circuitLine : Iterables.concat(circuitLines, individualCircuits)) {
            allCircuits.addAll(circuitLine.circuits());
        }
        return allCircuits;
    }

    int circuitLinesSize() {
        return circuitLines.size();
    }

    int individualCircuitsSize() {
        return individualCircuits.size();
    }

    int circuitPartsSize() {
        return circuitParts.size();
    }

    int circuitPartsSubListMaxSize() {
        return circuitParts.stream().mapToInt(List::size).max().getAsInt();
    }

    ImmutableList<CircuitLine> circuitLines() {
        return circuitLines;
    }

    ImmutableList<CircuitLine> individualCircuits() {
        return individualCircuits;
    }

    ImmutableList<ImmutableList<ItemComponent>> circuitParts() {
        return circuitParts;
    }

    CircuitLineCircuits circuitLineCircuits(ItemComponent circuit) {
        return circuitLineCircuits.getOrDefault(circuit, CircuitLineCircuits.EMPTY);
    }

    ImmutableList<DisplayComponent> tierCircuits(ItemComponent circuit) {
        return tierCircuits.get(circuit);
    }
}