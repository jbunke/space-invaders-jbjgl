package com.jordanbunke.invaders.menus;

import com.jordanbunke.invaders.SpaceInvaders;
import com.jordanbunke.invaders.handlers.SIHandler;
import com.jordanbunke.invaders.handlers.SIRenderer;
import com.jordanbunke.invaders.io.Settings;
import com.jordanbunke.delta_time.image.GameImage;
import com.jordanbunke.delta_time.menus.Menu;
import com.jordanbunke.delta_time.menus.MenuBuilder;
import com.jordanbunke.delta_time.menus.MenuSelectionLogic;
import com.jordanbunke.delta_time.menus.menu_elements.MenuElement;
import com.jordanbunke.delta_time.menus.menu_elements.visual.StaticMenuElement;
import com.jordanbunke.delta_time.utility.Coord2D;

public final class Menus {
    private static final int MID_Y = SIRenderer.CANVAS_HEIGHT / 2, INC_Y = 16;

    private static final Menus INSTANCE = new Menus();

    private final Menu placeholder;
    private Menu gameOverMenu, beatGameMenu, pauseMenu;

    private Menus() {
        this.placeholder = new Menu();
        this.pauseMenu = generatePauseMenu();
        this.gameOverMenu = generateEndgameMenu(false);
        this.beatGameMenu = generateEndgameMenu(true);
    }

    public static Menu resolve() {
        final SIHandler.State state = SIHandler.get().getState();

        if (SIHandler.get().isPaused())
            return INSTANCE.pauseMenu;
        else if (state == SIHandler.State.GAME_OVER)
            return INSTANCE.gameOverMenu;
        else if (state == SIHandler.State.BEAT_THE_GAME)
            return INSTANCE.beatGameMenu;

        return INSTANCE.placeholder;
    }

    public static void regenerateEndgameMenus() {
        INSTANCE.beatGameMenu = generateEndgameMenu(true);
        INSTANCE.gameOverMenu = generateEndgameMenu(false);
    }

    private static Menu generatePauseMenu() {
        return generateMenu(4,
                new String[] {},
                new String[] {
                        "RESUME",
                        Settings.isFullscreen() ? "WINDOWED" : "FULLSCREEN",
                        "RESTART",
                        "QUIT"
                },
                new Runnable[] {
                        SIHandler.get()::pausePlay,
                        () -> {
                            Settings.toggleFullscreen();
                            SpaceInvaders.refreshWindow();
                            INSTANCE.pauseMenu = generatePauseMenu();
                        },
                        SIHandler.get()::restartGame,
                        SpaceInvaders::quit
                }
        );
    }

    private static Menu generateEndgameMenu(final boolean won) {
        final int score = SIHandler.get().getScore(), highScore = SIHandler.get().getHighScore();
        final boolean isHighScore = score == highScore;

        return generateMenu(2,
                new String[] {
                        won ? "YOU WON!" : "GAME OVER!",
                        (isHighScore ? "NEW HIGH" : "FINAL") + " SCORE: " + score,
                        isHighScore ? " " : "HIGH SCORE: " + highScore
                },
                new String[] {
                        "RESTART", "QUIT"
                },
                new Runnable[] {
                        SIHandler.get()::restartGame,
                        SpaceInvaders::quit
                }
        );
    }

    private static Menu generateMenu(final int backgroundDivisor,
            final String[] prepends, final String[] labels, final Runnable[] behaviours
    ) {
        if (labels.length != behaviours.length)
            return new Menu();

        final int n = prepends.length + labels.length, buffer = 4,
                topY = MID_Y - (((n / 2) * INC_Y) + (n % 2 == 0 ? 0 : (INC_Y / 2)));
        int y = topY;

        final MenuBuilder builder = new MenuBuilder();
        final GameImage background = new GameImage(SIRenderer.CANVAS_WIDTH / backgroundDivisor,
                (2 * (buffer + ((SIRenderer.CANVAS_HEIGHT / 2) - topY))));
        background.fillRectangle(RenderHelper.BLACK, 0, 0,
                background.getWidth(), background.getHeight());
        builder.add(new StaticMenuElement(
                new Coord2D(SIRenderer.CANVAS_WIDTH / 2, (SIRenderer.CANVAS_HEIGHT / 2) - 6),
                MenuElement.Anchor.CENTRAL, background.submit()
        ));

        for (String prepend : prepends) {
            builder.add(new StaticMenuElement(new Coord2D(SIRenderer.CANVAS_WIDTH / 2, y),
                    MenuElement.Anchor.CENTRAL, RenderHelper.drawText(prepend)));
            y += INC_Y;
        }

        for (int i = 0; i < labels.length; i++) {
            builder.add(SIButton.create(labels[i], behaviours[i], y));
            y += INC_Y;
        }

        return builder.build(MenuSelectionLogic.basic());
    }
}
