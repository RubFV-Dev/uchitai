package io.github.rubfv.uchitai;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import games.spooky.gdx.nativefilechooser.NativeFileChooser;
import games.spooky.gdx.nativefilechooser.NativeFileChooserCallback;
import games.spooky.gdx.nativefilechooser.NativeFileChooserConfiguration;

import java.util.function.Consumer;

public class GestorArchivos {

    private NativeFileChooser nfc;

    public GestorArchivos(NativeFileChooser nfc) {
        this.nfc = nfc;
    }

    public void PedirArchivo(String filtro, Consumer<FileHandle> alTerminar){
        NativeFileChooserConfiguration conf = new NativeFileChooserConfiguration();
        conf.directory = Gdx.files.external(".");
        conf.mimeFilter = filtro;

        nfc.chooseFile(conf, new NativeFileChooserCallback() {
            @Override
            public void onFileChosen(FileHandle fileHandle) {
                Gdx.app.postRunnable(() -> {
                    alTerminar.accept(fileHandle);
                });
            }

            @Override
            public void onCancellation() {
                System.out.println("Selección de archivo cancelada");
            }

            @Override
            public void onError(Exception e) {
                System.out.println("Error -> " + e.getMessage());
            }
        });
    }
}


