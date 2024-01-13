package de.tum.cit.ase.maze;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import com.badlogic.gdx.utils.viewport.Viewport;
import games.spooky.gdx.nativefilechooser.NativeFileChooserCallback;
import games.spooky.gdx.nativefilechooser.NativeFileChooserConfiguration;

public class GameOverScreen implements Screen {

    private Stage stage;

    public GameOverScreen(MazeRunnerGame game) {
        OrthographicCamera camera = new OrthographicCamera();
        camera.zoom = 1.5f;

        Viewport viewport = new ScreenViewport(camera);
        stage = new Stage(viewport, game.getBatch());

        Table table = new Table();
        table.setFillParent(true);
        stage.addActor(table);

        // add a label
        table.add(new Label("Game Over!", game.getSkin(), "title")).row();

        // create and add Play Again Button
        TextButton playAgainButton = new TextButton("Play Again", game.getSkin());
        table.add(playAgainButton).width(400).row();
        playAgainButton.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                game.goToGame();
            }
        });

        // create and add Choose New Map Button
        TextButton chooseNewMapButton = new TextButton("Choose Different Map", game.getSkin());
        table.add(chooseNewMapButton).width(400).row();
        chooseNewMapButton.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                NativeFileChooserConfiguration conf = FileChooserHelper.mapChooserConfiguration();
                game.getFileChooser().chooseFile(conf, new NativeFileChooserCallback() {
                    @Override
                    public void onFileChosen(FileHandle file) {
                        game.setFileHandle(file);
                        game.goToGame();
                    }

                    @Override
                    public void onCancellation() {

                    }

                    @Override
                    public void onError(Exception exception) {

                    }
                });
            }
        });

        // create and add Back To Menu Button
        TextButton backToMenuButton = new TextButton("Back to Main Menu", game.getSkin());
        table.add(backToMenuButton).width(400).row();
        backToMenuButton.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                game.gotoMenu();
            }
        });
    }

    @Override
    public void show() {
        Gdx.input.setInputProcessor(stage);
    }

    @Override
    public void render(float delta) {
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
        stage.act(Math.min(Gdx.graphics.getDeltaTime(), 1/ 30f));
        stage.draw();
    }

    @Override
    public void resize(int width, int height) {
        stage.getViewport().update(width, height, true);
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

    }
}

