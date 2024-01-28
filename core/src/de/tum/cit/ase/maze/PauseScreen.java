package de.tum.cit.ase.maze;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import com.badlogic.gdx.utils.viewport.Viewport;
import games.spooky.gdx.nativefilechooser.NativeFileChooserCallback;
import games.spooky.gdx.nativefilechooser.NativeFileChooserConfiguration;

public class PauseScreen implements Screen {

    private Stage stage;

    private final Texture backgroundTexture;

    public PauseScreen(MazeRunnerGame game) {


        OrthographicCamera camera = new OrthographicCamera();
        camera.zoom = 1.5f;

        Viewport viewport = new ScreenViewport(new OrthographicCamera(Gdx.graphics.getWidth(), Gdx.graphics.getHeight()));
        stage = new Stage(viewport, game.getBatch());
        backgroundTexture = game.getPauseBackgroundTexture();


        Image background = new Image(new TextureRegion(backgroundTexture));
        background.setName("background");
        background.setSize(stage.getWidth(), stage.getHeight());
        stage.addActor(background);

        Table table = new Table();
        table.setFillParent(true);
        stage.addActor(table);


        //Set the background of the stage to Image
        stage.addActor(background);

        // add title
        table.add(new Label("Pause Screen", game.getSkin(), "title")).padBottom(80).row();

        table.defaults().padBottom(10);
        // create a button to continue the current game
        TextButton continueButton = new TextButton("Continue", game.getSkin());
        table.add(continueButton).width(300).row();
        continueButton.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                game.goToGame();
            }
        });

        // create a button to continue the current game
        TextButton restartLevelButton = new TextButton("Restart", game.getSkin());
        table.add(restartLevelButton).width(300).row();
        restartLevelButton.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                game.setGameState(GameState.NEW_GAME);
                game.goToGame();
            }
        });

        // create a button to initiate a new game
        TextButton newGameButton = new TextButton("New Game", game.getSkin());
        table.add(newGameButton).width(300).row();
        newGameButton.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                game.setGameState(GameState.NEW_GAME);
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

        // create a button to initiate a new game
        TextButton goToMenuButton = new TextButton("Go to Menu", game.getSkin());
        table.add(goToMenuButton).width(300).row();
        goToMenuButton.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                game.setGameState(GameState.NEW_GAME);
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
        stage.act(Math.min(Gdx.graphics.getDeltaTime(), 1 / 30f));
        stage.draw();
    }

    @Override
    public void resize(int width, int height) {
        stage.getViewport().update(width, height, true);
        Image background = (Image) stage.getRoot().findActor("background");
        if (background != null) {
            background.setSize(stage.getWidth(), stage.getHeight());
        }
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

    public Stage getStage() {
        return stage;
    }

    public void setStage(Stage stage) {
        this.stage = stage;
    }

    @Override
    public void dispose() {
        stage.dispose();
    }
}


