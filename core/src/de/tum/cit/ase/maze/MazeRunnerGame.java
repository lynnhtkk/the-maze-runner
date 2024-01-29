package de.tum.cit.ase.maze;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import games.spooky.gdx.nativefilechooser.NativeFileChooser;

/**
 * Main game class for Maze Runner Game.
 * <p>
 * This class extends the {@link Game} class from libGDX and serves as the entry point and central controller
 * for the Maze Runner game. It manages the game's lifecycle, screens,assets, and game states.
 * The class handles the transitions between different screens such as the main menu, game screen, pause screen,
 * game over screen, and victory screen. It also manages the music and the rendering batch for the game.
 * </p>
 */
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

    /**
     * Constructs a MazeRunnerGame instance with a file chooser.
     * <p>
     * Initializes the game with a NativeFileChooser which is used for selecting map files
     * within the game. This is essential for functionalities like loading new game maps.
     * </p>
     *
     * @param fileChooser The file chooser used for map selection.
     *
     * @see Game
     * @see NativeFileChooser
     */

    public MazeRunnerGame(NativeFileChooser fileChooser) {
        super();
        this.fileChooser = fileChooser;
    }

    /**
     * Initializes the game components.
     * <p>
     * This method sets up the game by initializing the rendering batch, skin for UI elements,
     * music assets for various screens ({@link MenuScreen}, {@link PauseScreen}, {@link GameOverScreen}, {@link VictoryScreen}).
     * It also sets the current screen for the game to the menu screen at startup.
     * This method is called once when the game application is created.
     * </p>
     */
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

    /**
     * Transitions the game to the menu screen.
     * <p>
     * This method handles the transition from any game state to the menu screen. It stops the
     * game music, starts the menu music, and sets the current screen to the menu screen.
     * </p>
     */
    public void gotoMenu() {
        gameScreenMusic.stop();
        menuScreenMusic.play();
        this.setScreen(menuScreen);
    }

    /**
     * Transitions the game to the pause screen.
     * <p>
     * This method handles the transition from any game state to the pause screen. It pauses the
     * game music and sets the current screen to the pause screen.
     * </p>
     */
    public void goToPause() {
        gameScreenMusic.pause();
        this.pause();
        this.setScreen(pauseScreen);
    }

    /**
     * Transitions the game to the main gameplay screen.
     * <p>
     * This method is responsible for transitioning from any current game state to the active
     * gameplay screen. It stops the menu screen music and starts playing the game screen music.
     * If the current game state is either null, NEW_GAME, GAME_OVER, or VICTORY, it resets
     * the game state to RUNNING and initializes a new GameScreen with the selected map file.
     * This ensures that the game starts or restarts under the correct conditions. The method
     * then sets the current screen to the game screen, where the actual gameplay takes place.
     * </p>
     */
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

    /**
     * Transitions the game to the Game Over screen.
     * <p>
     * This method is called when the player loses the game. It stops the menu screen music and sets the current screen
     * to the Game Over screen. This transition typically occurs under specific condition, when the player's character
     * runs out of lives. The Game Over screen displays the end-of-game information and options for the player.
     * </p>
     */
    public void goToGameOver() {
        menuScreenMusic.stop();
        this.setScreen(gameOverScreen);
    }

    /**
     * Transitions the game to the Victory screen.
     * <p>
     * This method is invoked when the player wins the game. It pauses the game screen music
     * and switches the current screen to the Victory screen. The transition to the Victory screen is typically triggered
     * by the completion of a game level. The Victory screen presents the winning outcome and offers options for
     * next steps or replaying.
     * </p>
     */
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


    /**
     * Disposes of the game's resources.
     * <p>
     * This method releases all the resources used by the game, such as the rendering batch and
     * the UI skin, to prevent memory leaks. It should be called when the game is shutting down.
     * </p>
     */
    @Override
    public void dispose() {
        batch.dispose();
        skin.dispose();
    }
}