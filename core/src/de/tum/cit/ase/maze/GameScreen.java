package de.tum.cit.ase.maze;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.maps.tiled.TiledMapTileLayer;
import com.badlogic.gdx.maps.tiled.renderers.OrthogonalTiledMapRenderer;
import com.badlogic.gdx.utils.viewport.ExtendViewport;

public class GameScreen implements Screen {

    private MazeRunnerGame game;
    private OrthogonalTiledMapRenderer renderer;
    private OrthographicCamera camera;
    private ExtendViewport viewport;

    private Player player;

    private ShapeRenderer shapeRenderer;

    public GameScreen(MazeRunnerGame game) {
        this.game = game;
        this.player = new Player(game.getPlayerX(), game.getPlayerY(), (TiledMapTileLayer) game.getMap().getLayers().get(1));
        renderer = new OrthogonalTiledMapRenderer(game.getMap());
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
        Gdx.gl.glClearColor(0, 0, 0, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        camera.position.set(player.getPlayerX(), player.getPlayerY(), 0);
        camera.update();
        player.update(Gdx.graphics.getDeltaTime(), game.getMapWidth(), game.getMapHeight(), game.getBorderTiles());

        renderer.setView(camera);
        renderer.render();

        renderer.getBatch().begin();
        player.draw(renderer.getBatch());
        renderer.getBatch().end();

        shapeRenderer.setProjectionMatrix(camera.combined);
        shapeRenderer.begin(ShapeRenderer.ShapeType.Line);
        shapeRenderer.rect(player.getHitBox().x, player.getHitBox().y, player.getHitBox().width, player.getHitBox().height);
        shapeRenderer.end();
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

    @Override
    public void dispose() {
        renderer.dispose();
        game.dispose();
        shapeRenderer.dispose();
    }
}