package de.tum.cit.ase.maze;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import games.spooky.gdx.nativefilechooser.NativeFileChooserConfiguration;

public class FileChooserHelper {
    public static NativeFileChooserConfiguration mapChooserConfiguration() {
        NativeFileChooserConfiguration conf = new NativeFileChooserConfiguration();
        conf.directory = Gdx.files.absolute(System.getProperty("user.home"));
        conf.directory = Gdx.files.internal("maps");
        conf.nameFilter = (dir, name) -> name.endsWith(".properties");
        conf.title = "Choose Map";
        return conf;
    }

}
