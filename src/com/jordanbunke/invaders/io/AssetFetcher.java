package com.jordanbunke.invaders.io;

import com.jordanbunke.jbjgl.fonts.Font;
import com.jordanbunke.jbjgl.image.GameImage;
import com.jordanbunke.jbjgl.io.ResourceLoader;

import java.nio.file.Path;

public final class AssetFetcher {
    private static final Path FONT_FOLDER = Path.of("img", "fonts");

    public static final int FONT_OFFSET = 9;

    public static final Font GAME_FONT = Font.loadFromSource(FONT_FOLDER, true,
            "font-arcade", false, 0.3, 1, false);
    public static GameImage sprite(final String name) {
        return ResourceLoader.loadImageResource(Path.of("img", "sprite", name + ".png"));
    }
}
