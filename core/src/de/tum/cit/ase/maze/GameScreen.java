package de.tum.cit.ase.maze;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.maps.tiled.TiledMapTileLayer;
import com.badlogic.gdx.maps.tiled.TiledMapTileSet;
import com.badlogic.gdx.maps.tiled.renderers.OrthogonalTiledMapRenderer;
import com.badlogic.gdx.maps.tiled.tiles.StaticTiledMapTile;
import com.badlogic.gdx.utils.viewport.ExtendViewport;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.*;

public class GameScreen implements Screen {

    private MazeRunnerGame game;
    private OrthogonalTiledMapRenderer renderer;
    private OrthographicCamera camera;
    private ExtendViewport viewport;

    private Player player;

    // initial coordinates for player's spawn point
    private float playerX;
    private float playerY;

    // location coordinates for mobs
    private List<int[]> mobsPositions;
    // list of dynamic mobs
    private List<Mob> mobs;

    // the width and height of the map
    private int mapWidth;
    private int mapHeight;

    // border tiles around the map
    private int borderTiles;

    // Tile Set containing tiles that's used in the map
    private TiledMapTileSet tileSet;

    // Map
    private TiledMap map;

    private ShapeRenderer shapeRenderer;

    public GameScreen(MazeRunnerGame game, FileHandle mapLocation) {
        borderTiles = 20;
        mobsPositions = new ArrayList<>();
        this.game = game;
        map = loadMap(mapLocation);
        mobs = spawnMobs(mobsPositions);
        this.player = new Player(playerX, playerY, (TiledMapTileLayer) map.getLayers().get(1));
        renderer = new OrthogonalTiledMapRenderer(map);
        camera = new OrthographicCamera();
        camera.zoom = .8f;
        viewport = new ExtendViewport(500, 500, camera);
        shapeRenderer = new ShapeRenderer();
    }

    @Override
    public void show() {

    }

    @Override
    public void render(float delta) {
        if (Gdx.input.isKeyPressed(Input.Keys.ESCAPE)) game.goToPause();

        Gdx.gl.glClearColor(0, 0, 0, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        camera.position.set(player.getPlayerX(), player.getPlayerY(), 0);
        camera.update();
        player.update(Gdx.graphics.getDeltaTime(), mapWidth, mapHeight, borderTiles);

        renderer.setView(camera);
        renderer.render();

        renderer.getBatch().begin();
        for (Mob mob : mobs) {
            mob.update(Gdx.graphics.getDeltaTime());
            mob.draw(renderer.getBatch());
        }
        player.draw(renderer.getBatch());
        renderer.getBatch().end();

        // render the HitBox of player for debugging purposes
        shapeRenderer.setProjectionMatrix(camera.combined);
        shapeRenderer.begin(ShapeRenderer.ShapeType.Line);
        shapeRenderer.rect(player.getHitBox().x, player.getHitBox().y, player.getHitBox().width, player.getHitBox().height);
        shapeRenderer.end();
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

    private List<Mob> spawnMobs(List<int[]> mobsPositions) {
        List<Mob> mobs = new ArrayList<>();
        for (int[] coordinates : mobsPositions) {
            mobs.add(new Mob(coordinates[0], coordinates[1]));
        }
        return mobs;
    }

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

    public ShapeRenderer getShapeRenderer() {
        return shapeRenderer;
    }

    public void setShapeRenderer(ShapeRenderer shapeRenderer) {
        this.shapeRenderer = shapeRenderer;
    }

    @Override
    public void dispose() {
        renderer.dispose();
        game.dispose();
        shapeRenderer.dispose();
    }
}