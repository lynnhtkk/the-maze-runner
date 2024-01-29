package de.tum.cit.ase.maze;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.maps.tiled.TiledMapTileLayer;
import com.badlogic.gdx.maps.tiled.TiledMapTileSet;
import com.badlogic.gdx.maps.tiled.renderers.OrthogonalTiledMapRenderer;
import com.badlogic.gdx.maps.tiled.tiles.StaticTiledMapTile;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.viewport.ExtendViewport;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import com.badlogic.gdx.utils.Timer;

import java.awt.*;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.*;
import java.util.List;

import com.badlogic.gdx.scenes.scene2d.ui.Image;


/**
 * Core gameplay screen for Maze Runner Game.
 * <p>
 * This class represents the main game screen where all the playable actions take place.
 * It handles the rendering of the game world, including the map, player, mobs, collectables,
 * and HUD (Heads-Up Display). The class manages game logic such as player-mob interactions,
 * collecting items, and navigating through the game map. It is responsible for the overall
 * gameplay experience, updating game states, and transitioning to other screens based on
 * game events like victory or game over.
 * </p>
 */
public class GameScreen implements Screen {

    private MazeRunnerGame game;
    private OrthogonalTiledMapRenderer renderer;
    private OrthographicCamera camera;
    private ExtendViewport viewport;

    private Player player;
    private Key key;

    private SpeedBuff speedBuff;

    // location coordinates for hearts
    private List<int[]> heartsPositions;

    //list of hearts
    private List<CollectableHearts> collectableHearts;

    // initial coordinates for player's spawn point
    private float playerX;
    private float playerY;

    // location coordinates for mobs
    private List<int[]> mobsPositions;
    // list of dynamic mobs
    private List<Mob> mobs;

    // list of exits
    private Array<Rectangle> exits;

    // the width and height of the map
    private int mapWidth;
    private int mapHeight;

    // border tiles around the map
    private int borderTiles;

    // Tile Set containing tiles that's used in the map
    private TiledMapTileSet tileSet;

    // Map
    private TiledMap map;

    // dummy HUD
    private Stage stage;
    private Image heart1, heart2, heart3, keyImage;

    // music and sounds
    private Sound takeDamageSound;
    private Sound keyCollectedSound;
    private Sound victorySound;
    private Sound gameOverSound;

    /**
     * Constructs a GameScreen with a reference to the MazeRunnerGame instance and a map file.
     * <p>
     * Initializes the GameScreen with the provided game instance and loads the game map from the
     * specified file location. The constructor sets up essential game components including the player,
     * mobs, collectables, camera, viewport, and renderer. It also initializes the HUD and sound effects.
     * This setup is crucial for creating the interactive game environment and ensuring all game elements
     * are rendered and updated correctly.
     * </p>
     *
     * @param game         The MazeRunnerGame instance this screen is part of.
     * @param mapLocation  The file handle pointing to the map's file, used to load the game map.
     */
    public GameScreen(MazeRunnerGame game, FileHandle mapLocation) {
        borderTiles = 20;
        mobsPositions = new ArrayList<>();
        heartsPositions = new ArrayList<>();
        this.game = game;
        key = new Key(0f, 0f);
        speedBuff = new SpeedBuff(0f,0f);
        exits = new Array<>();
        map = loadMap(mapLocation);
        collectableHearts = spawnHearts(heartsPositions);
        mobs = spawnMobs(mobsPositions);
        player = new Player(playerX, playerY, (TiledMapTileLayer) map.getLayers().get(1));
        renderer = new OrthogonalTiledMapRenderer(map);
        camera = new OrthographicCamera();
        camera.zoom = .6f;
        viewport = new ExtendViewport(500, 500, camera);

        // Sound Effects
        setupSounds();

        // HUD display
        setupHUD();
    }

    @Override
    public void render(float delta) {
        // clear screen
        Gdx.gl.glClearColor(0, 0, 0, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        // press ESC to pause the game
        checkPause();

        // check if the player loses all his lives
        checkGameOver();

        // to win the game, the player must have the key and find the exit
        checkVictory();

        // update the camera position each frame to center the player
        camera.position.set(player.getPlayerX(), player.getPlayerY(), 0);
        camera.update();

        renderer.setView(camera);

        renderer.render();

        // begin the batch of renderer
        renderer.getBatch().begin();

        // render mobs
        renderMob(delta);

        // render the key
        renderKey(delta);

        // render collectable hearts
        renderCollectableHearts(delta);

        // render speed buff
        renderSpeedBuff(delta);

        // render player
        renderPlayer(delta);

        // end the batch from renderer
        renderer.getBatch().end();

        renderHUD();
    }

    @Override
    public void show() {

    }

    /**
     * Checks if the player has won the game.
     * <p>
     * This method checks the victory conditions for the game. The player wins if they have
     * collected the key and reached one of the exits. If the player meets these conditions,
     * the game state is set to VICTORY, a victory sound is played, and the game transitions
     * to the Victory screen.
     * </p>
     */
    private void checkVictory() {
        for (Rectangle exit : exits) {
            if (player.isHasKey() && player.getCollisionBox().intersects(exit)) {
                game.setGameState(GameState.VICTORY);
                victorySound.play();
                game.goToVictory();
            }
        }
    }

    /**
     * Checks if the game is over due to the player losing all lives.
     * <p>
     * This method determines if the player has lost the game by checking their remaining lives.
     * If the player's lives are reduced to zero or less, the game state is set to GAME_OVER,
     * the game screen music is stopped, a game-over sound is played, and the game transitions
     * to the Game Over screen.
     * </p>
     */
    private void checkGameOver() {
        if (player.getPlayerLives() <= 0) {
            game.setGameState(GameState.GAME_OVER);
            game.getGameScreenMusic().stop();
            gameOverSound.play();
            game.goToGameOver();
        }
    }

    /**
     * Checks if the game should be paused.
     * <p>
     * This method detects if the ESCAPE key is pressed to pause the game. If the ESCAPE key
     * is pressed, the game transitions to the Pause screen. This allows players to pause the
     * game at any point during gameplay.
     * </p>
     */
    private void checkPause() {
        if (Gdx.input.isKeyPressed(Input.Keys.ESCAPE)) {
            game.goToPause();
        }
    }

    /**
     * Renders the mobs and checks for interactions between the player and mobs.
     * <p>
     * This method is called within the game's render loop to draw each mob and update
     * their states. It also checks for collisions between the player and the mobs.
     * If a collision occurs with a Mob, the player might take damage, and if the mob is of type {@link DynamicMob},
     * the mob might be knocked back or removed if its lives are depleted. This method ensures that the interactions
     * between the player and the mobs are processed, and the game's logic is maintained.
     * </p>
     *
     * @param delta The time span between the current and last frame in seconds.
     */
    private void renderMob(float delta) {
        Iterator<Mob> iterator = mobs.iterator();
        while (iterator.hasNext()) {
            Mob mob = iterator.next();
            mob.update(delta);
            mob.draw(renderer.getBatch());

            // check for collision between mobs and player
            if (mob instanceof DynamicMob) {
                if (!player.isInvincible() && mob.getHitBox().intersects(player.getHitBox())) {
                    player.takeDamage();
                    takeDamageSound.play();
                    player.applyKnockBack(mob, .9f);
                }
                if (player.getAttackBox().intersects(mob.getHitBox())) {
                    ((DynamicMob) mob).takeDamage();
                    ((DynamicMob) mob).applyKnockBack(player, .9f);

                    // If the mob has lost all lives, remove it
                    if (((DynamicMob) mob).getLives() <= 0) {
                        iterator.remove();
                    }
                }
            } else if (mob instanceof StaticMob) {
                if (!player.isInvincible() && mob.getHitBox().intersects(player.getCollisionBox())) {
                    player.takeDamage();
                    takeDamageSound.play();
                    player.applyKnockBack(mob, .9f);
                }
            }
        }
    }

    /**
     * Renders the key and checks for player-key interactions.
     * <p>
     * This method is responsible for rendering the key on the screen and updating its state
     * each frame. It checks for collisions between the player and the key. If a collision is detected,
     * indicating the player has reached the key, the player's state is updated to reflect that they have collected the key,
     * and a sound effect is played to signify the collection. It also stops rendering the key as soon as the player collects it.
     * This method contributes to the game's logic by managing the key's visibility and interaction with the player.
     * </p>
     *
     * @param delta The time span between the current and last frame in seconds.
     */
    private void renderKey(float delta) {
        if (!player.isHasKey()) {
            key.update(delta);
            key.draw(renderer.getBatch());
            if (key.getHitBox().intersects(player.getCollisionBox())) {
                player.setHasKey(true);
                keyCollectedSound.play();
            }
        }
    }

    /**
     * Renders collectable hearts and checks for player-heart interactions.
     * <p>
     * This method iterates through all collectable hearts and performs two main functions:
     * updating and drawing each heart, and checking for collisions between the hearts and the player.
     * If a collision is detected and the player has less than the maximum number of lives, the player
     * gains a life, a heart collection sound is played, and the heart is removed from the game.
     * This method ensures that the collectable hearts are not only visually represented but also
     * interactively contribute to the player's health during gameplay.
     * </p>
     *
     * @param delta The time span between the current and last frame in seconds.
     */
    private void renderCollectableHearts(float delta) {
        Iterator<CollectableHearts> heartIterator = collectableHearts.iterator();
        while (heartIterator.hasNext()) {
            CollectableHearts heart = heartIterator.next();

            // Update and draw the hearts
            heart.update(delta);
            heart.draw(renderer.getBatch());

            //  Check if the player collides with the hearts
            if (player.getCollisionBox().intersects(heart.getHitBox()) && player.getPlayerLives() < 3) {

                keyCollectedSound.play();
                if (player.getPlayerLives() < 3) {
                    player.setPlayerLives(player.getPlayerLives() + 1);
                }
                // Update and draw the hearts
                heartIterator.remove();
            }
        }
    }

    /**
     * Renders the speed buff and checks for player interaction.
     * <p>
     * This method is responsible for handling the rendering and state update of the speed buff,
     * typically represented by an apple. If the speed buff is present, it updates its state and
     * draws it on the screen. The method checks for a collision between the player and the speed buff.
     * If a collision occurs, the speed buff is consumed, the player's movement speed is temporarily
     * increased, and a sound is played. A timer is set to return the player's speed to normal after
     * a duration of 5 seconds, ensuring the buff effect is temporary.
     * </p>
     *
     * @param delta The time span between the current and last frame in seconds.
     */
    private void renderSpeedBuff(float delta) {
        if (speedBuff != null) {
            speedBuff.update(delta);
            speedBuff.draw(renderer.getBatch());

            // Check if player collides with apple
            if (player.getCollisionBox().intersects(speedBuff.getHitBox())) {
                speedBuff = null;
                keyCollectedSound.play();
                // Increase player movement speed and set a timer to return to normal speed after 5 seconds
                player.increaseSpeed();
                Timer.instance().scheduleTask(new Timer.Task() {
                    @Override
                    public void run() {
                        //Return to normal speed
                        player.resetSpeed();
                    }
                }, 5);

            }
        }
    }

    /**
     * Updates and renders the player on the screen.
     * <p>
     * This method updates the player's state based on the time delta and the constraints
     * of the game map, including map dimensions and border tile positions. Following the update,
     * it draws the player using the game's rendering batch. This ensures that the player's
     * movements and animations are consistently updated and displayed each frame.
     * </p>
     *
     * @param delta The time span between the current and last frame in seconds.
     */
    private void renderPlayer(float delta) {
        player.update(delta, mapWidth, mapHeight, borderTiles);
        player.draw(renderer.getBatch());
    }

    /**
     * Renders the Heads-Up Display (HUD) on the screen.
     * <p>
     * This method updates and displays the HUD elements, including health indicators (hearts)
     * and key possession status. The visibility of the heart icons is updated based on the player's
     * current health, and the key icon visibility is based on whether the player has collected a key.
     * <br><br>
     * The HUD is crucial for providing the player with immediate and essential game information.
     * </p>
     */
    private void renderHUD() {
        int health = player.getPlayerLives();
        boolean hasKey = player.isHasKey();

        // Update the visibility of heart icons based on player's health
        heart1.setVisible(health >= 1);
        heart2.setVisible(health >= 2);
        heart3.setVisible(health >= 3);

        // Update the visibility of the key icon based on whether the player has a key
        keyImage.setVisible(hasKey);

        // draw the stage
        stage.act(Math.min(Gdx.graphics.getDeltaTime(), 1 / 30f));
        stage.draw();
    }

    /**
     * Initializes the sound effects used in the game.
     * <p>
     * This method loads and sets up various sound effects, such as taking damage, collecting keys,
     * achieving victory, and game over scenarios. These sound effects are crucial for enhancing
     * the player's immersive experience and providing audio feedback for in-game actions and events.
     * </p>
     */
    private void setupSounds() {
        takeDamageSound = Gdx.audio.newSound(Gdx.files.internal("take-damage.wav"));
        keyCollectedSound = Gdx.audio.newSound(Gdx.files.internal("key-collected.wav"));
        victorySound = Gdx.audio.newSound(Gdx.files.internal("victory.wav"));
        gameOverSound = Gdx.audio.newSound(Gdx.files.internal("game-over.wav"));
    }

    /**
     * Sets up the Heads-Up Display (HUD) for the game.
     * <p>
     * This method initializes and configures the HUD, which includes creating and positioning
     * health indicators (hearts) and a key icon. The HUD elements are arranged on a table layout,
     * ensuring a consistent and organized display. The HUD provides crucial in-game information
     * to the player, such as health status and key possession.
     * </p>
     */
    private void setupHUD() {
        stage = new Stage(new ScreenViewport(new OrthographicCamera()), game.getBatch());

        // Create a table that fills the entire stage
        Table table = new Table();
        table.setFillParent(true);
        stage.addActor(table);

        // Initialize heart icons
        heart1 = new Image(collectableHearts.get(0).animationFrames.first());
        heart2 = new Image(collectableHearts.get(0).animationFrames.first());
        heart3 = new Image(collectableHearts.get(0).animationFrames.first());
        heart1.setScale(4f);
        heart2.setScale(4f);
        heart3.setScale(4f);

        // Initialize key icon
        keyImage = new Image(key.getAnimationFrames().first());
        keyImage.setScale(4f);

        table.align(Align.topLeft);

        table.add(heart1).padRight(50).padTop(100);
        table.add(heart2).padRight(50).padTop(100);
        table.add(heart3).padRight(50).padTop(100);

        table.row();

        table.add(keyImage).colspan(1).padTop(25).padLeft(25);
    }

    /**
     * Loads the tile set used for creating the game's tile map.
     * <p>
     * This method initializes the tile set by loading textures from specified image files and
     * creating individual tiles for different map elements like walls, entry points, exits, and floors.
     * Each tile is created from a portion of the texture sheets and assigned a unique ID. The tiles
     * are then added to a tile set, which is used in constructing the game's map. The method ensures
     * that the necessary graphical elements are available for map creation.
     * </p>
     * <p>
     * Credits for tileset artwork: https://kenney-assets.itch.io/tiny-dungeon
     * </p>
     */
    private void loadTileSet() {
        Texture basicTilesSheet = new Texture(Gdx.files.internal("basictiles.png"));
        // credit: https://kenney-assets.itch.io/tiny-dungeon
        Texture tileMapSheet = new Texture(Gdx.files.internal("tilemap_packed.png"));

        int TILE_SIZE = 16;

        StaticTiledMapTile tile0 = new StaticTiledMapTile(new TextureRegion(basicTilesSheet, 0 * TILE_SIZE, 10 * TILE_SIZE, TILE_SIZE, TILE_SIZE));                 // wall tiles
        tile0.setId(0);
        StaticTiledMapTile tile1 = new StaticTiledMapTile(new TextureRegion(basicTilesSheet, 1 * TILE_SIZE, 7 * TILE_SIZE, TILE_SIZE, TILE_SIZE));      // entry point
        tile1.setId(1);
        StaticTiledMapTile tile2 = new StaticTiledMapTile(new TextureRegion(tileMapSheet, 9 * TILE_SIZE, 1 * TILE_SIZE, TILE_SIZE, TILE_SIZE));         // exit
        tile2.setId(2);
        StaticTiledMapTile tile6 = new StaticTiledMapTile(new TextureRegion(tileMapSheet, 4 * TILE_SIZE, 3 * TILE_SIZE, TILE_SIZE, TILE_SIZE));         // floor
        tile6.setId(6);

        tileSet = new TiledMapTileSet();
        tileSet.putTile(0, tile0);
        tileSet.putTile(1, tile1);
        tileSet.putTile(2, tile2);
        tileSet.putTile(6, tile6);
    }

    /**
     * Determines the size of the game map from a properties file.
     * <p>
     * This method calculates the width and height of the game map based on the contents of the
     * provided properties file. If the properties file explicitly specifies the map's 'Width' and 'Height',
     * these values are used. Otherwise, the method calculates the dimensions by finding the largest
     * X and Y coordinates from the keys in the properties file, which represent tile positions.
     * The calculated size is essential for various game logic, such as ensuring the player does not
     * move outside the bounds of the map.
     * </p>
     *
     * @param properties The properties file containing the map data.
     * @return A map with keys 'Width' and 'Height' and their corresponding integer values representing
     *         the size of the game map.
     */
    private Map<String, Integer> findMapSize(Properties properties) {
        Map<String, Integer> mapSize = new HashMap<>();
        if (properties.containsKey("Width") && properties.containsKey("Height")) {
            mapSize.put("Width", Integer.parseInt(properties.getProperty("Width")));
            mapSize.put("Height", Integer.parseInt(properties.getProperty("Height")));
        } else {
            int mapWidth = Integer.MIN_VALUE;
            int mapHeight = Integer.MIN_VALUE;
            for (String key : properties.stringPropertyNames()) {
                if (key.contains(",")) {
                    String[] coordinates = key.split(",");
                    int x = Integer.parseInt(coordinates[0]);
                    if (x > mapWidth) mapWidth = x;
                    int y = Integer.parseInt(coordinates[1]);
                    if (y > mapHeight) mapHeight = y;
                }
            }
            mapSize.put("Width", mapWidth + 1);
            mapSize.put("Height", mapHeight + 1);
        }
        return mapSize;
    }

    /**
     * Loads and constructs the game map from a properties file.
     * <p>
     * This method reads a properties file from the provided file handle and constructs a TiledMap
     * object representing the game map. It initializes the tile set, determines the map's size, and
     * creates layers for the map floor and objects like walls, keys, and mob spawn points. The method
     * also marks crucial in-game coordinates such as those for mobs, the player, exits, keys, and other
     * collectables. It plays a pivotal role in translating the static map data into a dynamic game environment.
     * </p>
     *
     * @param mapLocation The file handle pointing to the map's properties file.
     * @return A {@link TiledMap} object representing the constructed game map.
     * @throws IOException If there is an error reading the properties file.
     *
     * @see FileHandle
     * @see TiledMap
     * @see Properties
     */
    private TiledMap loadMap(FileHandle mapLocation) {
        // load the tile set that we are going to use to construct the maze
        this.loadTileSet();
        TiledMap tiledMap = new TiledMap();
        tiledMap.getTileSets().addTileSet(this.tileSet);

        // read the properties file
        try {
            Properties properties = new Properties();
            properties.load(new FileInputStream(String.valueOf(mapLocation)));

            // set the width and height of the maze according to the properties file
            // later, we have to check whether the width or height of the maze is given in the file
            // if not, max x = width, max y = height
            Map<String, Integer> mapSize = findMapSize(properties);
            this.mapWidth = mapSize.get("Width");
            this.mapHeight = mapSize.get("Height");

            // add floor layer
            // the concept is to increment the maze layer objects' coordinates in both x and y-axis by the number of border tiles
            TiledMapTileLayer floorLayer = new TiledMapTileLayer(this.mapWidth + this.borderTiles * 2, this.mapHeight + this.borderTiles * 2, 16, 16);
            for (int x = 0; x < floorLayer.getWidth(); x++) {
                for (int y = 0; y < floorLayer.getHeight(); y++) {
                    floorLayer.setCell(x, y, new TiledMapTileLayer.Cell().setTile(tileSet.getTile(6)));
                }
            }
            tiledMap.getLayers().add(floorLayer);

            // add object layer (walls, keys, etc)
            TiledMapTileLayer objectLayer = new TiledMapTileLayer(this.mapWidth + borderTiles, this.mapHeight + borderTiles, 16, 16);
            for (String key : properties.stringPropertyNames()) {
                if (key.contains(",")) {
                    String[] coordinates = key.split(",");
                    int x = Integer.parseInt(coordinates[0]) + borderTiles;
                    int y = Integer.parseInt(coordinates[1]) + borderTiles;
                    // mobs and player spawn points
                    if (properties.getProperty(key).equals("3")) {
                        mobsPositions.add(new int[]{3, x * 16, y * 16});
                    } else if (properties.getProperty(key).equals("4")) {
                        mobsPositions.add(new int[]{4, x * 16, y * 16});
                    } else if (properties.getProperty(key).equals("5")) {
                        this.key.setPosition(x * 16, y * 16);
                    } else if (properties.getProperty(key).equals("6")) {
                       heartsPositions.add(new int[]{x * 16,y * 16});
                    } else if (properties.getProperty(key).equals("7")) {
                        this.speedBuff.setPosition(x*16,y*16);
                    }else {
                        if (properties.getProperty(key).equals("1")) {
                            playerX = x * 16f;
                            playerY = y * 16f;
                        }
                        if (properties.getProperty(key).equals("2")) {
                            exits.add(new Rectangle(x * 16, y * 16, 16, 16));
                        }
                        int tileID = Integer.parseInt(properties.getProperty(key));

                        TiledMapTileLayer.Cell cell = new TiledMapTileLayer.Cell();
                        cell.setTile(tileSet.getTile(tileID));
                        objectLayer.setCell(x, y, cell);
                    }
                }
            }
            tiledMap.getLayers().add(objectLayer);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return tiledMap;
    }

    /**
     * Spawns mobs at specified positions on the game map.
     * <p>
     * This method creates Mob instances at the locations specified in the mobsPositions list.
     * It differentiates between different types of mobs based on identifiers in the index {@code 0} of the list
     * (e.g., '4' for DynamicMob, '3' for StaticMob) and instantiates the corresponding mob type.
     * The created mobs are added to a list which is then returned.
     * </p>
     *
     * @param mobsPositions A list of integer arrays, each containing the type identifier and
     *                      x, y coordinates for spawning a mob.
     * @return A list of {@link Mob} instances created and positioned according to the input list.
     */
    private List<Mob> spawnMobs(List<int[]> mobsPositions) {
        List<Mob> mobs = new ArrayList<>();
        for (int[] coordinates : mobsPositions) {
            if (coordinates[0] == 4) {
                mobs.add(new DynamicMob(coordinates[1], coordinates[2], (TiledMapTileLayer) map.getLayers().get(1)));
            } else if (coordinates[0] == 3) {
                mobs.add(new StaticMob(coordinates[1], coordinates[2]));
            }
        }
        return mobs;
    }

    /**
     * Spawns collectable hearts at specified positions on the game map.
     * <p>
     * This method creates CollectableHearts instances at the locations specified in the heartsPositions list.
     * The hearts are instantiated and added to a list, which is then returned. This method is crucial
     * for placing health collectables on the map, providing players with opportunities to regain health.
     * </p>
     *
     * @param heartsPositions A list of integer arrays, each containing the coordinates for spawning a heart.
     * @return A list of {@link CollectableHearts} instances created and positioned according to the input list.
     */
    private List<CollectableHearts> spawnHearts(List<int[]> heartsPositions) {
        List<CollectableHearts> hearts = new ArrayList<>();
        for (int[] coordinates : heartsPositions) {
            hearts.add(new CollectableHearts(coordinates[0], coordinates[1]));
        }
        return hearts;
    }

    /**
     * Updates the viewport dimensions when the window size changes.
     * <p>
     * This method is called when the screen size of the game window is changed. It adjusts the viewport
     * to match the new dimensions, ensuring that the game's rendering is correctly scaled to fit the
     * new window size. This is crucial for maintaining the correct aspect ratio and visual layout of
     * the game across different screen sizes.
     * </p>
     *
     * @param width  The new width of the window.
     * @param height The new height of the window.
     */
    @Override
    public void resize(int width, int height) {
        viewport.update(width, height);
    }

    @Override
    public void pause() {

    }

    @Override
    public void resume() {

    }

    @Override
    public void hide() {

    }

    public MazeRunnerGame getGame() {
        return game;
    }

    public void setGame(MazeRunnerGame game) {
        this.game = game;
    }

    public OrthogonalTiledMapRenderer getRenderer() {
        return renderer;
    }

    public void setRenderer(OrthogonalTiledMapRenderer renderer) {
        this.renderer = renderer;
    }

    public OrthographicCamera getCamera() {
        return camera;
    }

    public void setCamera(OrthographicCamera camera) {
        this.camera = camera;
    }

    public ExtendViewport getViewport() {
        return viewport;
    }

    public void setViewport(ExtendViewport viewport) {
        this.viewport = viewport;
    }

    public Player getPlayer() {
        return player;
    }

    public void setPlayer(Player player) {
        this.player = player;
    }

    public float getPlayerX() {
        return playerX;
    }

    public void setPlayerX(float playerX) {
        this.playerX = playerX;
    }

    public float getPlayerY() {
        return playerY;
    }

    public void setPlayerY(float playerY) {
        this.playerY = playerY;
    }

    public List<int[]> getMobsPositions() {
        return mobsPositions;
    }

    public void setMobsPositions(List<int[]> mobsPositions) {
        this.mobsPositions = mobsPositions;
    }

    public List<Mob> getMobs() {
        return mobs;
    }

    public void setMobs(List<Mob> mobs) {
        this.mobs = mobs;
    }

    public List<int[]> getHeartsPositions() {
        return heartsPositions;
    }

    public void setHeartsPositions(List<int[]> heartsPositions) {
        this.heartsPositions = heartsPositions;
    }

    public List<CollectableHearts> getCollectableHearts() {
        return collectableHearts;
    }

    public void setCollectableHearts(List<CollectableHearts> collectableHearts) {
        this.collectableHearts = collectableHearts;
    }

    public int getMapWidth() {
        return mapWidth;
    }

    public void setMapWidth(int mapWidth) {
        this.mapWidth = mapWidth;
    }

    public int getMapHeight() {
        return mapHeight;
    }

    public void setMapHeight(int mapHeight) {
        this.mapHeight = mapHeight;
    }

    public int getBorderTiles() {
        return borderTiles;
    }

    public void setBorderTiles(int borderTiles) {
        this.borderTiles = borderTiles;
    }

    public TiledMapTileSet getTileSet() {
        return tileSet;
    }

    public void setTileSet(TiledMapTileSet tileSet) {
        this.tileSet = tileSet;
    }

    public TiledMap getMap() {
        return map;
    }

    public void setMap(TiledMap map) {
        this.map = map;
    }

    /**
     * Releases resources when they are no longer needed.
     * <p>
     * This method is called when the game is closing or when this screen is being disposed of.
     * It is responsible for freeing up memory by disposing of resources such as the renderer
     * and other game assets. Proper disposal of resources is crucial to prevent memory leaks
     * and ensure efficient memory management.
     * </p>
     */
    @Override
    public void dispose() {
        renderer.dispose();
        game.dispose();
    }
}