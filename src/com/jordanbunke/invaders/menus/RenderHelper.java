package com.jordanbunke.invaders.menus;

import com.jordanbunke.invaders.io.AssetFetcher;
import com.jordanbunke.delta_time.image.GameImage;
import com.jordanbunke.delta_time.image.ImageProcessing;
import com.jordanbunke.delta_time.text.Text;
import com.jordanbunke.delta_time.text.TextBuilder;

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
        final TextBuilder builder = new TextBuilder(1.0, 0.5,
                Text.Orientation.CENTER, color, AssetFetcher.GAME_FONT);
        final String[] lines = text.split("\n");

        for (int i = 0; i < lines.length; i++) {
            builder.addText(lines[i]);

            if (i + 1 < lines.length)
                builder.addLineBreak();
        }

        return builder.build().draw();
    }

    public static GameImage drawTextForMenu(final String text, final Color color) {
        final GameImage around = drawText(text, color);

        final GameImage trimmed = new GameImage(around.getWidth(), 7);
        trimmed.draw(around, 0, -8);
        return trimmed.submit();
    }
}
