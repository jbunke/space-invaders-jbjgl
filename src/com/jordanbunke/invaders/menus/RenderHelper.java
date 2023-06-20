package com.jordanbunke.invaders.menus;

import com.jordanbunke.invaders.io.AssetFetcher;
import com.jordanbunke.jbjgl.image.GameImage;
import com.jordanbunke.jbjgl.image.ImageProcessing;
import com.jordanbunke.jbjgl.text.Text;
import com.jordanbunke.jbjgl.text.TextBuilder;

import java.awt.*;

public final class RenderHelper {
    public static final Color
            WHITE = new Color(255, 255, 255),
            GREEN = new Color(0, 255, 0),
            BLACK = new Color(0, 0, 0);
    public static GameImage mask(final GameImage base) {
        final GameImage composed = new GameImage(base.getWidth(), base.getHeight());

        for (int x = 0; x < composed.getWidth(); x++) {
            for (int y = 0; y < composed.getHeight(); y++) {
                final Color c = ImageProcessing.colorAtPixel(base, x, y);

                if (c.equals(BLACK))
                    continue;

                composed.dot(c, x, y);
            }
        }

        return composed.submit();
    }

    public static GameImage composeOverNonTransparentPixels(final GameImage base, final GameImage overlay) {
        final GameImage composed = new GameImage(base);

        for (int x = 0; x < composed.getWidth(); x++) {
            for (int y = 0; y < composed.getHeight(); y++) {
                if (ImageProcessing.colorAtPixel(composed, x, y).getAlpha() == 255)
                    composed.dot(ImageProcessing.colorAtPixel(overlay, x, y), x, y);
            }
        }

        return composed.submit();
    }

    public static GameImage drawText(final String text) {
        return drawText(text, WHITE);
    }

    public static GameImage drawText(final String text, final Color color) {
        final TextBuilder builder = new TextBuilder(1.0, Text.Orientation.CENTER, color,
                AssetFetcher.GAME_FONT);
        final String[] lines = text.split("\n");

        for (int i = 0; i < lines.length; i++) {
            builder.addText(lines[i]);

            if (i + 1 < lines.length)
                builder.addLineBreak();
        }

        final GameImage around = builder.build().draw();
        if (lines.length > 1)
            return around;

        final GameImage trimmed = new GameImage(around.getWidth(), 13);
        trimmed.draw(around, 0, -13);
        return trimmed.submit();
    }
}