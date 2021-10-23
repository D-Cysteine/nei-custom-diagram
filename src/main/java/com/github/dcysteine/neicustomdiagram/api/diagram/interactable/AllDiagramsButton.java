package com.github.dcysteine.neicustomdiagram.api.diagram.interactable;

import com.github.dcysteine.neicustomdiagram.api.diagram.DiagramGroupInfo;
import com.github.dcysteine.neicustomdiagram.api.diagram.layout.ComponentLabel;
import com.github.dcysteine.neicustomdiagram.api.diagram.tooltip.Tooltip;
import com.github.dcysteine.neicustomdiagram.api.draw.Draw;
import com.github.dcysteine.neicustomdiagram.api.draw.Point;
import com.github.dcysteine.neicustomdiagram.mod.Lang;

/** A handy way to build a button that will load all diagrams in a diagram group. */
public class AllDiagramsButton extends CustomInteractable {
    public AllDiagramsButton(DiagramGroupInfo info, Point pos, String tooltip) {
        super(
                ComponentLabel.create(info.icon(), pos),
                Tooltip.create(tooltip, Tooltip.SPECIAL_FORMATTING),
                CustomInteractable.buildInteractionLambda(info.groupId()),
                Draw::drawRaisedSlot,
                position -> {},
                position -> Draw.drawOverlay(position, Draw.Color.OVERLAY_BLUE));
    }

    public AllDiagramsButton(DiagramGroupInfo info, Point pos) {
        this(info, pos, Lang.API.trans("showalldiagrams"));
    }
}
