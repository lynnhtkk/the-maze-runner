package de.tum.cit.ase.maze;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.files.FileHandle;
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
    private GameOverScreen gameOverScreen;
    private VictoryScreen victoryScreen;

    // GameState to determine the current state of the game
    private GameState gameState;

    private NativeFileChooser fileChooser;
    private FileHandle fileHandle;

    public MazeRunnerGame(NativeFileChooser fileChooser) {
        super();
        this.fileChooser = fileChooser;
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
        this.gameOverScreen = new GameOverScreen(this);
        this.victoryScreen = new VictoryScreen(this);

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
        if (this.gameState == null || this.gameState == GameState.NEW_GAME || this.gameState == GameState.GAME_OVER || this.gameState == GameState.VICTORY) {
            this.gameState = GameState.RUNNING;
            this.gameScreen = new GameScreen(this, fileHandle);
        }
        this.setScreen(gameScreen);
    }

    public void goToGameOver() {
        this.setScreen(gameOverScreen);
    }

    public void goToVictory() {
        this.setScreen(victoryScreen);
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

    public NativeFileChooser getFileChooser() {
        return fileChooser;
    }

    public void setFileChooser(NativeFileChooser fileChooser) {
        this.fileChooser = fileChooser;
    }

    public FileHandle getFileHandle() {
        return fileHandle;
    }

    public void setFileHandle(FileHandle fileHandle) {
        this.fileHandle = fileHandle;
    }

    @Override
    public void dispose() {
        batch.dispose();
        skin.dispose();
    }
}