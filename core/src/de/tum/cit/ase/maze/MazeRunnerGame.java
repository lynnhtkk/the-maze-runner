package de.tum.cit.ase.maze;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.maps.tiled.TiledMapTile;
import com.badlogic.gdx.maps.tiled.TiledMapTileLayer;
import com.badlogic.gdx.maps.tiled.TiledMapTileSet;
import com.badlogic.gdx.maps.tiled.tiles.StaticTiledMapTile;
import games.spooky.gdx.nativefilechooser.NativeFileChooser;
import org.w3c.dom.Text;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

public class MazeRunnerGame extends Game {

    // SpriteBatch to render
    private SpriteBatch batch;

    // initial coordinates for player's spawn point
    private float playerX;
    private float playerY;

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

        // number of tiles around the maze
        // the idea is to increase the maze layer in both x and y-axis
        borderTiles = 20;

        // load the map
        map = this.loadMap();

        // play background music
        Music backgroundMusic = Gdx.audio.newMusic(Gdx.files.internal("background.mp3"));
        backgroundMusic.setLooping(true);
        backgroundMusic.setVolume(0.2f);
        backgroundMusic.play();

        // got to game screen (directly for now)
        goToGame();
    }

    private void goToGame() {
        this.setScreen(new GameScreen(this));
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

    private TiledMap loadMap() {
        // load the tile set that we are going to use to construct the maze
        this.loadTileSet();
        TiledMap tiledMap = new TiledMap();
        tiledMap.getTileSets().addTileSet(this.tileSet);

        // read the properties file
        try {
            Properties properties = new Properties();
            properties.load(new FileInputStream("/Users/linnhtet/IdeaProjects/fophn2324infun2324projectworkx-g34/maps/level-2.properties"));

            // set the width and height of the maze according to the properties file
            // later, we have to check whether the width or height of the maze is given in the file
            // if not, max x = width, max y = height
            this.mapWidth = Integer.parseInt(properties.getProperty("Width"));
            this.mapHeight = Integer.parseInt(properties.getProperty("Height"));

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

    @Override
    public void dispose() {
        batch.dispose();
    }
}