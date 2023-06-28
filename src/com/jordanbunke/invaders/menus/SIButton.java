package com.jordanbunke.invaders.menus;

import com.jordanbunke.invaders.handlers.SIRenderer;
import com.jordanbunke.delta_time.image.GameImage;
import com.jordanbunke.delta_time.menus.menu_elements.button.SimpleMenuButton;
import com.jordanbunke.delta_time.utility.Coord2D;

public final class SIButton extends SimpleMenuButton {
    private SIButton(
            final Coord2D position,
            final Coord2D dimensions,
            final Runnable chosenBehaviour,
            final GameImage nonHighlightedImage,
            final GameImage highlightedImage
    ) {
        super(position, dimensions, Anchor.CENTRAL, true,
                chosenBehaviour, nonHighlightedImage, highlightedImage);
    }

    public static SIButton create(final String text, final Runnable chosenBehaviour, final int y) {
        final Coord2D position = new Coord2D(SIRenderer.CANVAS_WIDTH / 2, y);

        final GameImage nh = RenderHelper.drawTextForMenu(text, RenderHelper.WHITE),
                h = RenderHelper.drawTextForMenu(text, RenderHelper.GREEN);

        final Coord2D dimensions = new Coord2D(nh.getWidth(), nh.getHeight());

        return new SIButton(position, dimensions, chosenBehaviour, nh, h);
    }
}
