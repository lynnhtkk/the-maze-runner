package de.tum.cit.ase.maze;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import games.spooky.gdx.nativefilechooser.NativeFileChooser;

public class MazeRunnerGame extends Game {
    // SpriteBatch to render
    private SpriteBatch batch;

    // UI Skin
    private Skin skin;

    // Screens for each stage of the game
    private MenuScreen menuScreen;
    private GameScreen gameScreen;
    private PauseScreen pauseScreen;

    // GameState to determine the current state of the game
    private GameState gameState;

    public MazeRunnerGame(NativeFileChooser fileChooser) {
        super();
    }

    @Override
    public void create() {
        batch = new SpriteBatch();
        skin = new Skin(Gdx.files.internal("craft/craftacular-ui.json"));

        // play background music
        Music backgroundMusic = Gdx.audio.newMusic(Gdx.files.internal("background.mp3"));
        backgroundMusic.setLooping(true);
        backgroundMusic.setVolume(0.1f);
        backgroundMusic.play();

        // instantiate screen instances
        this.menuScreen = new MenuScreen(this);
        this.pauseScreen = new PauseScreen(this);
        this.gameScreen = new GameScreen(this);

        // got to game screen (directly for now)
        gotoMenu();
    }

    public void gotoMenu() {
        this.setScreen(menuScreen);
    }

    public void goToPause() {
        this.pause();
        this.setScreen(pauseScreen);
    }

    public void goToGame() {
        if (this.gameState == null || this.gameState == GameState.NEW_GAME) {
            this.gameState = GameState.RUNNING;
            this.gameScreen = new GameScreen(this);
        }
        this.setScreen(gameScreen);
    }

    public SpriteBatch getBatch() {
        return batch;
    }

    public void setBatch(SpriteBatch batch) {
        this.batch = batch;
    }

    public Skin getSkin() {
        return skin;
    }

    public void setSkin(Skin skin) {
        this.skin = skin;
    }

    public MenuScreen getMenuScreen() {
        return menuScreen;
    }

    public void setMenuScreen(MenuScreen menuScreen) {
        this.menuScreen = menuScreen;
    }

    public GameScreen getGameScreen() {
        return gameScreen;
    }

    public void setGameScreen(GameScreen gameScreen) {
        this.gameScreen = gameScreen;
    }

    public PauseScreen getPauseScreen() {
        return pauseScreen;
    }

    public void setPauseScreen(PauseScreen pauseScreen) {
        this.pauseScreen = pauseScreen;
    }

    public GameState getGameState() {
        return gameState;
    }

    public void setGameState(GameState gameState) {
        this.gameState = gameState;
    }

    @Override
    public void dispose() {
        batch.dispose();
        skin.dispose();
    }
}