package com.github.dcysteine.neicustomdiagram.generators.gregtech5.materialparts;

import com.github.dcysteine.neicustomdiagram.api.diagram.component.DisplayComponent;
import com.github.dcysteine.neicustomdiagram.api.diagram.component.ItemComponent;
import com.github.dcysteine.neicustomdiagram.api.diagram.tooltip.Tooltip;
import com.github.dcysteine.neicustomdiagram.mod.Lang;
import com.github.dcysteine.neicustomdiagram.mod.Logger;
import com.github.dcysteine.neicustomdiagram.util.gregtech5.GregTechFormatting;
import com.github.dcysteine.neicustomdiagram.util.gregtech5.GregTechOreDictUtil;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.ImmutableSetMultimap;
import com.google.common.collect.MultimapBuilder;
import com.google.common.collect.SetMultimap;
import gregtech.api.enums.Materials;
import gregtech.api.enums.OrePrefixes;
import net.minecraft.init.Items;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

class RelatedMaterialsHandler {
    /** When building representative display components, we'll try these prefixes in order. */
    private static final ImmutableList<OrePrefixes> REPRESENTATION_PREFIXES =
            ImmutableList.of(
                    OrePrefixes.ingot, OrePrefixes.dust, OrePrefixes.bucket, OrePrefixes.stone);

    /**
     * There are some materials that have related materials set, but don't actually have any ore
     * prefixes registered or are dummy materials.
     */
    private static final ImmutableSet<Materials> EXCLUDED_MATERIALS =
            ImmutableSet.of(Materials.AnyCopper, Materials.AnyIron, Materials.Peanutwood);

    /**
     * Set multimap of material to complete set of related materials.
     *
     * <p>Only materials that are related to at least one other material will be present in the map.
     */
    private ImmutableSetMultimap<Materials, Materials> relatedMaterials;

    /**
     * Map of material to item representation of that material.
     *
     * <p>Only materials that are related to at least one other material will be present in the map.
     */
    private ImmutableMap<Materials, DisplayComponent> materialRepresentations;

    /** This method must be called before any other methods are called. */
    void initialize() {
        SetMultimap<Materials, Materials> relatedMaterials =
                MultimapBuilder.hashKeys().hashSetValues().build();
        Map<Materials, DisplayComponent> materialRepresentations = new HashMap<>();

        for (Materials material : Materials.getAll()) {
            if (EXCLUDED_MATERIALS.contains(material)) {
                continue;
            }

            relatedMaterials.putAll(material, getRelatedMaterials(material));
        }

        // We want to form connected components ("component" as in connected graph component), so we
        // need to also reverse the unidirectional links added above, as well as propagate
        // transitive relations.
        //
        // In practice, reversal + 1 propagation step is sufficient to fully connect the components.
        // See: Materials.setSmeltingInto(), Materials.setMaceratingInto()
        SetMultimap<Materials, Materials> reversed =
                MultimapBuilder.hashKeys().hashSetValues().build();
        for (Materials key : relatedMaterials.keys()) {
            relatedMaterials.get(key)
                    .forEach(value -> reversed.put(value, key));
        }
        relatedMaterials.putAll(reversed);

        SetMultimap<Materials, Materials> propagated =
                MultimapBuilder.hashKeys().hashSetValues().build();
        for (Materials key : relatedMaterials.keys()) {
            relatedMaterials.get(key).forEach(
                    value -> propagated.putAll(key, relatedMaterials.get(value)));
            propagated.remove(key, key);

            // At this stage, no new keys will be introduced, so it's safe to build our
            // representations map now.
            materialRepresentations.put(key, getRepresentation(key));
        }
        relatedMaterials.putAll(propagated);

        this.relatedMaterials = ImmutableSetMultimap.copyOf(relatedMaterials);
        this.materialRepresentations = ImmutableMap.copyOf(materialRepresentations);
    }

    List<DisplayComponent> getRelatedMaterialRepresentations(Materials material) {
        return relatedMaterials.get(material).stream()
                .map(materialRepresentations::get)
                .collect(Collectors.toList());
    }

    private static Set<Materials> getRelatedMaterials(Materials material) {
        Set<Materials> relatedMaterials = new HashSet<>();
        relatedMaterials.add(material.mSmeltInto);
        relatedMaterials.add(material.mMacerateInto);
        relatedMaterials.add(material.mArcSmeltInto);
        relatedMaterials.remove(material);
        relatedMaterials.remove(null);
        return relatedMaterials;
    }

    private static DisplayComponent getRepresentation(Materials material) {
        Optional<ItemComponent> representation =
                REPRESENTATION_PREFIXES.stream()
                        .map(prefix -> GregTechOreDictUtil.getComponent(prefix, material))
                        .filter(Optional::isPresent)
                        .map(Optional::get)
                        .findFirst();
        if (!representation.isPresent()) {
            Logger.GREGTECH_5_MATERIAL_PARTS.warn(
                    "Could not find representation for material [{}]. Checked prefixes [{}].",
                    material, REPRESENTATION_PREFIXES);
            representation = Optional.of(ItemComponent.create(Items.iron_ingot, 0));
        }

        return DisplayComponent.builder(representation.get())
                .setAdditionalTooltip(
                        Tooltip.create(
                                Lang.GREGTECH_5_MATERIAL_PARTS.transf(
                                        "materiallabel",
                                        GregTechFormatting.getMaterialDescription(material)),
                                Tooltip.INFO_FORMATTING))
                .build();
    }
}