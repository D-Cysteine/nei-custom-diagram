package com.github.dcysteine.neicustomdiagram.api.diagram.interactable;

import com.github.dcysteine.neicustomdiagram.api.Lang;
import com.github.dcysteine.neicustomdiagram.api.diagram.DiagramGroupInfo;
import com.github.dcysteine.neicustomdiagram.api.diagram.layout.ComponentLabel;
import com.github.dcysteine.neicustomdiagram.api.diagram.tooltip.Tooltip;
import com.github.dcysteine.neicustomdiagram.api.draw.Draw;
import com.github.dcysteine.neicustomdiagram.api.draw.Point;

/** A handy way to build a button that will load all diagrams in a diagram group. */
public class AllDiagramsButton extends CustomInteractable {
    public AllDiagramsButton(DiagramGroupInfo info, Point pos) {
        super(
                ComponentLabel.create(info.icon(), pos),
                Tooltip.create(Lang.API.trans("showalldiagrams"), Tooltip.SPECIAL_FORMATTING),
                CustomInteractable.buildInteractionLambda(info.groupId()),
                Draw::drawRaisedSlot,
                p -> Draw.drawOverlay(p, Draw.Color.OVERLAY_BLUE));
    }
}
