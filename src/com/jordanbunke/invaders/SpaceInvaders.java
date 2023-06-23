package com.jordanbunke.invaders;

import com.jordanbunke.invaders.handlers.SIHandler;
import com.jordanbunke.invaders.handlers.SIRenderer;
import com.jordanbunke.invaders.io.AssetFetcher;
import com.jordanbunke.invaders.io.Settings;
import com.jordanbunke.invaders.logic.GameConstants;
import com.jordanbunke.jbjgl.OnStartup;
import com.jordanbunke.jbjgl.contexts.ProgramContext;
import com.jordanbunke.jbjgl.debug.GameDebugger;
import com.jordanbunke.jbjgl.game.Game;
import com.jordanbunke.jbjgl.game.GameEngine;
import com.jordanbunke.jbjgl.game.GameManager;
import com.jordanbunke.jbjgl.image.GameImage;
import com.jordanbunke.jbjgl.io.InputEventLogger;
import com.jordanbunke.jbjgl.window.GameWindow;

public final class SpaceInvaders implements ProgramContext {
    private static final SpaceInvaders INSTANCE;

    static {
        INSTANCE = new SpaceInvaders();
    }

    public static void main(String[] args) {
        OnStartup.run();
        Settings.read();

        final GameWindow window = createWindow();
        final GameManager gameManager = new GameManager(0, INSTANCE);
        final GameEngine gameEngine = new GameEngine(window, gameManager,
                GameConstants.UPDATE_HZ, GameConstants.TARGET_FPS);
        gameEngine.setCanvasSize(SIRenderer.CANVAS_WIDTH, SIRenderer.CANVAS_HEIGHT);

        new Game(GameConstants.TITLE, gameManager, gameEngine);
    }

    private static GameWindow createWindow() {
        return Settings.isFullscreen()
                ? new GameWindow(GameConstants.TITLE, AssetFetcher.sprite("ufo"))
                : new GameWindow(GameConstants.TITLE,
                SIRenderer.CANVAS_WIDTH * SIRenderer.WINDOW_SCALE_UP,
                SIRenderer.CANVAS_HEIGHT * SIRenderer.WINDOW_SCALE_UP,
                AssetFetcher.sprite("ufo"), false);
    }

    @Override
    public void process(final InputEventLogger eventLogger) {
        SIHandler.get().process(eventLogger);
    }

    @Override
    public void update(final double deltaTime) {
        SIHandler.get().update(deltaTime);
        SIRenderer.get().update();
    }

    @Override
    public void render(final GameImage canvas) {
        SIRenderer.get().render(canvas);
    }

    @Override
    public void debugRender(final GameImage canvas, final GameDebugger debugger) {
        if (Settings.isDebug())
            SIRenderer.get().debugRender(canvas, debugger);
    }

    public static void quit() {
        Settings.write();
        System.exit(0);
    }
}
