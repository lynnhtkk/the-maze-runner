package de.tum.cit.ase.maze;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.maps.tiled.TiledMapTileLayer;
import com.badlogic.gdx.maps.tiled.TiledMapTileSet;
import com.badlogic.gdx.maps.tiled.tiles.StaticTiledMapTile;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import games.spooky.gdx.nativefilechooser.NativeFileChooser;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.*;

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

    // initial coordinates for player's spawn point
    private float playerX;
    private float playerY;

    // location coordinates for dynamic mobs
    private List<int[]> mobsPositions;

    // width and height of the maze
    private int mapWidth;
    private int mapHeight;

    // number of tiles around the maze
    private int borderTiles;

    private TiledMapTileSet tileSet;
    private TiledMap map;

    public MazeRunnerGame(NativeFileChooser fileChooser) {
        super();
    }

    @Override
    public void create() {
        batch = new SpriteBatch();
        skin = new Skin(Gdx.files.internal("craft/craftacular-ui.json"));

        // number of tiles around the maze
        // the idea is to increase the maze layer in both x and y-axis
        borderTiles = 20;

        this.mobsPositions = new ArrayList<>();

        // load the map
        map = this.loadMap();

        // play background music
        Music backgroundMusic = Gdx.audio.newMusic(Gdx.files.internal("background.mp3"));
        backgroundMusic.setLooping(true);
        backgroundMusic.setVolume(0.2f);
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

    private void loadTileSet() {
        Texture basicTilesSheet = new Texture(Gdx.files.internal("basictiles.png"));
        Texture tileMapSheet = new Texture(Gdx.files.internal("tilemap_packed.png"));
        Texture mobSheet = new Texture(Gdx.files.internal("mobs.png"));

        int TILE_SIZE = 16;

        StaticTiledMapTile tile0 = new StaticTiledMapTile(new TextureRegion(basicTilesSheet, 0, 10 * TILE_SIZE, TILE_SIZE, TILE_SIZE));                 // wall tiles
        tile0.setId(0);
        StaticTiledMapTile tile1 = new StaticTiledMapTile(new TextureRegion(basicTilesSheet, 1 * TILE_SIZE, 7 * TILE_SIZE, TILE_SIZE, TILE_SIZE));      // entry point
        tile1.setId(1);
        StaticTiledMapTile tile2 = new StaticTiledMapTile(new TextureRegion(tileMapSheet, 9 * TILE_SIZE, 1 * TILE_SIZE, TILE_SIZE, TILE_SIZE));         // exit
        tile2.setId(2);
        StaticTiledMapTile tile3 = new StaticTiledMapTile(new TextureRegion(tileMapSheet, 7 * TILE_SIZE, 1 * TILE_SIZE, TILE_SIZE, TILE_SIZE));         // static traps
        tile3.setId(3);
        StaticTiledMapTile tile4 = new StaticTiledMapTile(new TextureRegion(mobSheet, 2 * TILE_SIZE, 4 * TILE_SIZE, TILE_SIZE, TILE_SIZE));             // dynamic mobs
        tile4.setId(4);
        StaticTiledMapTile tile5 = new StaticTiledMapTile(new TextureRegion(tileMapSheet, 11 * TILE_SIZE, 8 * TILE_SIZE, TILE_SIZE, TILE_SIZE));         // key
        tile5.setId(5);
        StaticTiledMapTile tile6 = new StaticTiledMapTile(new TextureRegion(tileMapSheet, 4 * TILE_SIZE, 3 * TILE_SIZE, TILE_SIZE, TILE_SIZE));         // floor
        tile6.setId(6);

        tileSet = new TiledMapTileSet();
        tileSet.putTile(0, tile0);
        tileSet.putTile(1, tile1);
        tileSet.putTile(2, tile2);
        tileSet.putTile(3, tile3);
        tileSet.putTile(4, tile4);
        tileSet.putTile(5, tile5);
        tileSet.putTile(6, tile6);
    }

    private Map<String, Integer> findMapSize(Properties properties) {
        Map<String, Integer> mapSize = new HashMap<>();
        if (properties.containsKey("Width") && properties.containsKey("Height")) {
            mapSize.put("Width", Integer.parseInt(properties.getProperty("Width")));
            mapSize.put("Height", Integer.parseInt(properties.getProperty("Height")));
            return mapSize;
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
            return mapSize;
        }
    }

    private TiledMap loadMap() {
        // load the tile set that we are going to use to construct the maze
        this.loadTileSet();
        TiledMap tiledMap = new TiledMap();
        tiledMap.getTileSets().addTileSet(this.tileSet);

        // read the properties file
        try {
            Properties properties = new Properties();
            properties.load(new FileInputStream("/Users/linnhtet/IdeaProjects/fophn2324infun2324projectworkx-g34/maps/level-5.properties"));

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
                    // player spawn point
                    if (properties.getProperty(key).equals("4")) {
                        mobsPositions.add(new int[]{x * 16, y * 16});
                    } else {
                        if (properties.getProperty(key).equals("1")) {
                            playerX = x * 16f;
                            playerY = y * 16f;
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

    public SpriteBatch getBatch() {
        return batch;
    }

    public void setBatch(SpriteBatch batch) {
        this.batch = batch;
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

    public List<int[]> getMobsPositions() {
        return mobsPositions;
    }

    public void setMobsPositions(List<int[]> mobsPositions) {
        this.mobsPositions = mobsPositions;
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