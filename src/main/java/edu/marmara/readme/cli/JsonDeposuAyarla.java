package edu.marmara.readme.cli;

import edu.marmara.readme.gui.AppConfig;

import java.nio.file.Path;

/** Küçük yardımcı: GUI'nin varsayılan JSON deposu tercihini (java.util.prefs) komut satırından ayarlar. */
public final class JsonDeposuAyarla {

    private JsonDeposuAyarla() {
    }

    public static void main(String[] args) {
        if (args.length != 1) {
            System.err.println("Kullanım: JsonDeposuAyarla <yol>");
            System.exit(1);
        }
        AppConfig.jsonDeposunuAyarla(Path.of(args[0]));
        System.out.println("JSON deposu ayarlandı: " + Path.of(args[0]).toAbsolutePath());
    }
}
