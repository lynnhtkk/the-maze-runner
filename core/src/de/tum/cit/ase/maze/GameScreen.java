package de.tum.cit.ase.maze;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.maps.tiled.renderers.OrthogonalTiledMapRenderer;
import com.badlogic.gdx.utils.viewport.ExtendViewport;

public class GameScreen implements Screen {

    private MazeRunnerGame game;
    private OrthogonalTiledMapRenderer renderer;
    private OrthographicCamera camera;
    private ExtendViewport viewport;

    public GameScreen(MazeRunnerGame game) {
        this.game = game;
        renderer = new OrthogonalTiledMapRenderer(game.getMap());
        camera = new OrthographicCamera();
        camera.zoom = .8f;
        viewport = new ExtendViewport(500, 500, camera);
    }

    @Override
    public void show() {

    }

    @Override
    public void render(float delta) {
        Gdx.gl.glClearColor(0, 0, 0, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        camera.update();
        camera.position.set(game.getPlayerX(), game.getPlayerY(), 0);

        renderer.setView(camera);
        renderer.render();
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
    }
}