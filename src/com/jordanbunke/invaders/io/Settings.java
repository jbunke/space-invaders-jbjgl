package com.jordanbunke.invaders.io;

import com.jordanbunke.invaders.handlers.SIHandler;
import com.jordanbunke.jbjgl.io.FileIO;

import java.nio.file.Path;

public final class Settings {
    private static final Path DATA_FILE = Path.of("game_data", "data.txt");

    private static final String HIGH_SCORE_TAG = "hs", DEBUG_TAG = "debug",
            FULLSCREEN_TAG = "fullscreen", SEPARATOR = ":", NEW_LINE = "\n";

    private static boolean debug, fullscreen;
    private static int savedHighScore;

    static {
        debug = false;
        fullscreen = false;

        savedHighScore = 0;
    }

    public static void read() {
        final String contents = FileIO.readFile(DATA_FILE);
        if (contents == null)
            return;
        final String[] lines = contents.split("\n");

        for (String line : lines) {
            final String[] values = line.split(SEPARATOR);

            if (values.length != 2)
                continue;

            switch (values[0]) {
                case HIGH_SCORE_TAG -> savedHighScore = parseIntOrElse(values[1], savedHighScore);
                case DEBUG_TAG -> debug = parseBoolOrElse(values[1], debug);
                case FULLSCREEN_TAG -> fullscreen = parseBoolOrElse(values[1], fullscreen);
            }
        }
    }

    public static void write() {
        final String contents =
                HIGH_SCORE_TAG + SEPARATOR + SIHandler.get().getHighScore() + NEW_LINE +
                DEBUG_TAG + SEPARATOR + debug + NEW_LINE +
                FULLSCREEN_TAG + SEPARATOR + fullscreen + NEW_LINE;
        FileIO.writeFile(DATA_FILE, contents);
    }

    private static int parseIntOrElse(final String toParse, final int defaultValue) {
        try {
            return Integer.parseInt(toParse);
        } catch (final NumberFormatException e) {
            return defaultValue;
        }
    }

    private static boolean parseBoolOrElse(final String toParse, final boolean defaultValue) {
        try {
            return Boolean.parseBoolean(toParse);
        } catch (final NumberFormatException e) {
            return defaultValue;
        }
    }

    public static int getSavedHighScore() {
        return savedHighScore;
    }

    public static boolean isDebug() {
        return debug;
    }

    public static boolean isFullscreen() {
        return fullscreen;
    }

    public static void toggleFullscreen() {
        Settings.fullscreen = !fullscreen;
    }
}
