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
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.maps.tiled.TiledMapTileLayer;
import com.badlogic.gdx.maps.tiled.TiledMapTileSet;
import com.badlogic.gdx.maps.tiled.renderers.OrthogonalTiledMapRenderer;
import com.badlogic.gdx.maps.tiled.tiles.StaticTiledMapTile;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.viewport.ExtendViewport;
import com.badlogic.gdx.utils.viewport.ScreenViewport;

import java.awt.*;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.*;
import java.util.List;

public class GameScreen implements Screen {

    private MazeRunnerGame game;
    private OrthogonalTiledMapRenderer renderer;
    private OrthographicCamera camera;
    private ExtendViewport viewport;

    private Player player;
    private Key key;

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

    private ShapeRenderer shapeRenderer;

    // dummy HUD
    private Stage stage;
    private Label playerLivesLabel;
    private Label hasKeyIndicator;
    private Rectangle spriteBox;

    // music and sounds
    private Sound takeDamageSound;
    private Sound keyCollectedSound;
    private Sound victorySound;
    private Sound gameOverSound;

    public GameScreen(MazeRunnerGame game, FileHandle mapLocation) {
        borderTiles = 20;
        mobsPositions = new ArrayList<>();
        this.game = game;
        key = new Key(0f, 0f);
        exits = new Array<>();
        map = loadMap(mapLocation);
        mobs = spawnMobs(mobsPositions);
        this.player = new Player(playerX, playerY, (TiledMapTileLayer) map.getLayers().get(1));
        renderer = new OrthogonalTiledMapRenderer(map);
        camera = new OrthographicCamera();
        camera.zoom = .6f;
        viewport = new ExtendViewport(500, 500, camera);
        shapeRenderer = new ShapeRenderer();

        // for debugging
        spriteBox = new Rectangle((int) playerX, (int) playerY, 16, 32);

        takeDamageSound = Gdx.audio.newSound(Gdx.files.internal("take-damage.wav"));
        keyCollectedSound = Gdx.audio.newSound(Gdx.files.internal("key-collected.wav"));
        victorySound = Gdx.audio.newSound(Gdx.files.internal("victory.wav"));
        gameOverSound = Gdx.audio.newSound(Gdx.files.internal("game-over.wav"));

        stage = new Stage(new ScreenViewport(new OrthographicCamera()), game.getBatch());

        Table table = new Table();
        table.setFillParent(true);
        stage.addActor(table);

        table.top().left();
        playerLivesLabel = new Label("", game.getSkin());
        playerLivesLabel.setAlignment(Align.left);
        table.add(playerLivesLabel).padLeft(20).left().row();
        hasKeyIndicator = new Label("Has Key: false", game.getSkin());
        hasKeyIndicator.setAlignment(Align.left);
        table.add(hasKeyIndicator).padLeft(20).left().row();
    }

    @Override
    public void show() {

    }

    @Override
    public void render(float delta) {
        // press ESC to pause the game
        if (Gdx.input.isKeyPressed(Input.Keys.ESCAPE)) {
            game.goToPause();
        }

        // check if the player loses all his lives
        if (player.getPlayerLives() <= 0) {
            game.setGameState(GameState.GAME_OVER);
            game.getGameScreenMusic().stop();
            gameOverSound.play();
            game.goToGameOver();
        }

        // to win the game, the player must have the key and find the exit
        for (Rectangle exit : exits) {
            if (player.isHasKey() && player.getCollisionBox().intersects(exit)) {
                game.setGameState(GameState.VICTORY);
                victorySound.play();
                game.goToVictory();
            }
        }

        Gdx.gl.glClearColor(0, 0, 0, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        camera.position.set(player.getPlayerX(), player.getPlayerY(), 0);
        camera.update();

        renderer.setView(camera);
        renderer.render();

        // to render the HitBoxes of player and mobs for debugging purposes
        // start of shapeRenderer
        shapeRenderer.setProjectionMatrix(camera.combined);
        shapeRenderer.begin(ShapeRenderer.ShapeType.Line);

        renderer.getBatch().begin();

        Iterator<Mob> iterator = mobs.iterator();
        while (iterator.hasNext()) {
            Mob mob = iterator.next();
            mob.update(Gdx.graphics.getDeltaTime());
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


        // check if the player has obtained the key
        if (!player.isHasKey()) {
            key.update(Gdx.graphics.getDeltaTime());
            key.draw(renderer.getBatch());
            if (key.getHitBox().intersects(player.getCollisionBox())) {
                player.setHasKey(true);
                keyCollectedSound.play();
                hasKeyIndicator.setText("Has Key: " + player.isHasKey());
            }
        }

        player.update(Gdx.graphics.getDeltaTime(), mapWidth, mapHeight, borderTiles);
        player.draw(renderer.getBatch());

        renderer.getBatch().end();

        /*// attack box left
        shapeRenderer.rect(player.getPlayerX() - 6, player.getPlayerY() + 4, 7, 16);
        // attack box right
        shapeRenderer.rect(player.getPlayerX() + 14, player.getPlayerY() + 4, 7, 16);
        // attack box up
        shapeRenderer.rect(player.getPlayerX(), player.getPlayerY() + 18, 16, 7);
        // attack box down
        shapeRenderer.rect(player.getPlayerX(), player.getPlayerY() + 1, 16, 7);

        shapeRenderer.rect(player.getAttackBox().x, player.getAttackBox().y, player.getAttackBox().width, player.getAttackBox().height);*/

        /*shapeRenderer.rect(player.getCollisionBox().x, player.getCollisionBox().y, player.getCollisionBox().width, player.getCollisionBox().height);
        shapeRenderer.rect(player.getHitBox().x, player.getHitBox().y, player.getHitBox().width, player.getHitBox().height);
        shapeRenderer.rect(key.getHitBox().x, key.getHitBox().y, key.getHitBox().width, key.getHitBox().height);
        shapeRenderer.setColor(Color.WHITE);
        shapeRenderer.setColor(Color.RED);
        shapeRenderer.setColor(Color.GREEN);
        shapeRenderer.rect(player.getPlayerX(), player.getPlayerY(), 16, 32);*/
        shapeRenderer.end();
        // end of shapeRenderer

        playerLivesLabel.setText("Player's Health: " + player.getPlayerLives());
        stage.act(Math.min(Gdx.graphics.getDeltaTime(), 1 / 30f));
        stage.draw();
    }

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
                    // mobs and player spawn points
                    if (properties.getProperty(key).equals("3")) {
                        mobsPositions.add(new int[]{3, x * 16, y * 16});
                    } else if (properties.getProperty(key).equals("4")) {
                        mobsPositions.add(new int[]{4, x * 16, y * 16});
                    } else if (properties.getProperty(key).equals("5")) {
                        this.key.setPosition(x * 16, y * 16);
                    } else {
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

    private List<Mob> spawnMobs(List<int[]> mobsPositions) {
        List<Mob> mobs = new ArrayList<>();
        for (int[] coordinates : mobsPositions) {
            if (coordinates[0] == 4) {
                mobs.add(new DynamicMob(coordinates[1], coordinates[2]));
            } else if (coordinates[0] == 3) {
                mobs.add(new StaticMob(coordinates[1], coordinates[2]));
            }
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