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

    private Music menuScreenMusic;
    private Music gameScreenMusic;

    public MazeRunnerGame(NativeFileChooser fileChooser) {
        super();
        this.fileChooser = fileChooser;
    }

    @Override
    public void create() {
        batch = new SpriteBatch();
        skin = new Skin(Gdx.files.internal("craft/craftacular-ui.json"));

        // play background music
        menuScreenMusic = Gdx.audio.newMusic(Gdx.files.internal("menuScreenMusic.mp3"));
        menuScreenMusic.setLooping(true);
        menuScreenMusic.setVolume(.1f);

        gameScreenMusic = Gdx.audio.newMusic(Gdx.files.internal("pixel_sprinter_loop.mp3"));
        gameScreenMusic.setLooping(true);
        gameScreenMusic.setVolume(.2f);

        // instantiate screen instances
        this.menuScreen = new MenuScreen(this);
        this.pauseScreen = new PauseScreen(this);
        this.gameOverScreen = new GameOverScreen(this);
        this.victoryScreen = new VictoryScreen(this);


        // got to game screen (directly for now)
        gotoMenu();
    }

    public void gotoMenu() {
        gameScreenMusic.stop();
        menuScreenMusic.play();
        this.setScreen(menuScreen);
    }

    public void goToPause() {
        gameScreenMusic.pause();
        this.pause();
        this.setScreen(pauseScreen);
    }

    public void goToGame() {
        menuScreenMusic.stop();
        if (this.gameState == null || this.gameState == GameState.NEW_GAME || this.gameState == GameState.GAME_OVER || this.gameState == GameState.VICTORY) {
            gameScreenMusic.stop();
            this.gameState = GameState.RUNNING;
            this.gameScreen = new GameScreen(this, fileHandle);
        }
        gameScreenMusic.play();
        this.setScreen(gameScreen);
    }

    public void goToGameOver() {
        menuScreenMusic.stop();
        this.setScreen(gameOverScreen);
    }

    public void goToVictory() {
        gameScreenMusic.pause();
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

    public GameOverScreen getGameOverScreen() {
        return gameOverScreen;
    }

    public void setGameOverScreen(GameOverScreen gameOverScreen) {
        this.gameOverScreen = gameOverScreen;
    }

    public VictoryScreen getVictoryScreen() {
        return victoryScreen;
    }

    public void setVictoryScreen(VictoryScreen victoryScreen) {
        this.victoryScreen = victoryScreen;
    }

    public Music getMenuScreenMusic() {
        return menuScreenMusic;
    }

    public void setMenuScreenMusic(Music menuScreenMusic) {
        this.menuScreenMusic = menuScreenMusic;
    }

    public Music getGameScreenMusic() {
        return gameScreenMusic;
    }

    public void setGameScreenMusic(Music gameScreenMusic) {
        this.gameScreenMusic = gameScreenMusic;
    }

    @Override
    public void dispose() {
        batch.dispose();
        skin.dispose();
    }
}