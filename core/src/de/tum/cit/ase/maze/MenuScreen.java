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

public class MenuScreen implements Screen {

    private final Stage stage;

    public MenuScreen(MazeRunnerGame game) {
        OrthographicCamera camera = new OrthographicCamera();
        camera.zoom = 1.5f;

        Viewport viewport = new ScreenViewport(camera);
        stage = new Stage(viewport, game.getBatch());           // Create a stage for UI elements

        Table table = new Table();      // create a table for layout
        table.setFillParent(true);      // make the table fill the entire stage
        stage.addActor(table);          // add the table to the stage

        // add a label as a title
        table.add(new Label("Welcome to the Maze Runner", game.getSkin(), "title")).padBottom(80).row();

        table.defaults().padBottom(10);

        // create and add a button to go to the game screen
        TextButton goToGameButton = new TextButton("Play Game", game.getSkin());
        table.add(goToGameButton).width(300).row();
        goToGameButton.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                NativeFileChooserConfiguration conf = mapChooserConfiguration();
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

        // create and add a button to go to the game screen
        TextButton exitButton = new TextButton("Exit Game", game.getSkin());
        table.add(exitButton).width(300).row();

        exitButton.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                Gdx.app.exit();
            }
        });
    }

    @Override
    public void show() {
        // set the input processor to the stage so it can receive input events
        Gdx.input.setInputProcessor(stage);
    }

    @Override
    public void render(float delta) {
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
        stage.act(Math.min(Gdx.graphics.getDeltaTime(), 1 / 30f));
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

    public NativeFileChooserConfiguration mapChooserConfiguration() {
        NativeFileChooserConfiguration conf = new NativeFileChooserConfiguration();
        conf.directory = Gdx.files.absolute(System.getProperty("user.dir"));
        conf.nameFilter = (dir, name) -> name.endsWith(".properties");
        conf.title = "Choose Map";
        return conf;
    }

    public Stage getStage() {
        return stage;
    }

    @Override
    public void dispose() {
        stage.dispose();
    }
}