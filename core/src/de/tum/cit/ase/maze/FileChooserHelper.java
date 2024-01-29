package de.tum.cit.ase.maze;

import com.badlogic.gdx.Gdx;
import games.spooky.gdx.nativefilechooser.NativeFileChooserConfiguration;

/**
 * Utility class to assist with file choosing operations, particularly for selecting map files.
 * <p>
 * This class provides static methods to configure file chooser dialogs used in the game,
 * ensuring that players can select map files efficiently and correctly. The configurations
 * are tailored to the specific needs of the game, such as filtering for specific file types
 * and setting appropriate default directories.
 * </p>
 */
public class FileChooserHelper {

    /**
     * Creates and returns a configuration for a native file chooser specific to selecting map files.
     * <p>
     * This method sets up a {@link NativeFileChooserConfiguration} object for choosing map files.
     * It specifies the directory to start in, sets a name filter to only display files with a
     * '.properties' extension, and sets the title of the file chooser dialog. This configuration
     * is intended to facilitate the user in selecting the correct map file for the game.
     * </p>
     *
     * @return A pre-configured {@link NativeFileChooserConfiguration} object for selecting map files.
     */
    public static NativeFileChooserConfiguration mapChooserConfiguration() {
        NativeFileChooserConfiguration conf = new NativeFileChooserConfiguration();
        conf.directory = Gdx.files.internal("maps");
        conf.nameFilter = (dir, name) -> name.endsWith(".properties");
        conf.title = "Choose Map";
        return conf;
    }
}
