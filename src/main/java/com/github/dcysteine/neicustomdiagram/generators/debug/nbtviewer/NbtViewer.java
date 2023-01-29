package com.github.dcysteine.neicustomdiagram.generators.debug.nbtviewer;

import com.github.dcysteine.neicustomdiagram.api.diagram.Diagram;
import com.github.dcysteine.neicustomdiagram.api.diagram.DiagramGenerator;
import com.github.dcysteine.neicustomdiagram.api.diagram.DiagramGroup;
import com.github.dcysteine.neicustomdiagram.api.diagram.DiagramGroupInfo;
import com.github.dcysteine.neicustomdiagram.api.diagram.component.Component;
import com.github.dcysteine.neicustomdiagram.api.diagram.component.DisplayComponent;
import com.github.dcysteine.neicustomdiagram.api.diagram.component.ItemComponent;
import com.github.dcysteine.neicustomdiagram.api.diagram.interactable.Interactable;
import com.github.dcysteine.neicustomdiagram.api.diagram.layout.Grid;
import com.github.dcysteine.neicustomdiagram.api.diagram.layout.Layout;
import com.github.dcysteine.neicustomdiagram.api.diagram.layout.Slot;
import com.github.dcysteine.neicustomdiagram.api.diagram.layout.Text;
import com.github.dcysteine.neicustomdiagram.api.diagram.matcher.CustomDiagramMatcher;
import com.github.dcysteine.neicustomdiagram.main.Lang;
import com.github.dcysteine.neicustomdiagram.main.config.ConfigOptions;
import com.github.dcysteine.neicustomdiagram.main.config.DiagramGroupVisibility;
import com.github.dcysteine.neicustomdiagram.util.NbtUtil;
import com.google.common.base.Splitter;
import com.google.common.collect.Lists;
import net.minecraft.init.Items;
import net.minecraft.nbt.NBTTagCompound;

import java.util.Collection;

/** Pretty-prints a component's NBT, for convenient viewing. */
public final class NbtViewer implements DiagramGenerator {
    public static final ItemComponent ICON = ItemComponent.create(Items.sign, 0);

    private static final String SLOT_KEY = "slot";

    private final DiagramGroupInfo info;

    public NbtViewer(String groupId) {
        this.info =
                DiagramGroupInfo.builder(
                                Lang.NBT_VIEWER.trans("groupname"),
                                groupId, ICON, 1)
                        .setIgnoreNbt(false)
                        .setDefaultVisibility(DiagramGroupVisibility.DISABLED)
                        .setDescription("This diagram pretty-prints NBT, for convenient viewing.")
                        .build();
    }

    @Override
    public DiagramGroupInfo info() {
        return info;
    }

    @Override
    public DiagramGroup generate() {
        return new DiagramGroup(
                info, new CustomDiagramMatcher(this::generateDiagrams));
    }

    private Collection<Diagram> generateDiagrams(
            Interactable.RecipeType recipeType, Component component) {
        if (!component.nbt().isPresent()) {
            return Lists.newArrayList();
        }

        return Lists.newArrayList(
                Diagram.builder()
                        .addLayout(buildLayout(component.nbt().get()))
                        .insertIntoSlot(
                                Layout.SlotKey.create(SLOT_KEY),
                                DisplayComponent.builder(component).build())
                        .build());
    }

    private static Layout buildLayout(NBTTagCompound nbt) {
        Layout.Builder layoutBuilder = Layout.builder();

        layoutBuilder.putSlot(
                Layout.SlotKey.create(SLOT_KEY),
                Slot.builder(Grid.GRID.grid(0, 0)).build());

        String nbtText = NbtUtil.prettyPrintNbt(nbt.toString());
        Iterable<String> lines = Splitter.on('\n').split(nbtText);
        layoutBuilder.addAllLabels(
                Text.multiLineBuilder(Grid.GRID.grid(-1, 1), Grid.Direction.SE)
                        .setSmall(ConfigOptions.NBT_VIEWER_SMALL_TEXT.get())
                        .addAllLines(lines)
                        .build());

        return layoutBuilder.build();
    }
}
