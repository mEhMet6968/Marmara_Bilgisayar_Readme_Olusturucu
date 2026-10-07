package edu.marmara.readme.gui;

import edu.marmara.readme.engine.ReadmeGenerator;
import edu.marmara.readme.engine.config.KonfigurasyonJsonKontrol;
import edu.marmara.readme.engine.model.Konfigurasyon;

import java.nio.file.Path;
import java.util.prefs.Preferences;

/**
 * Uygulama genelindeki yol/ayar yönetimi. Python tarafındaki QSettings +
 * json_depo_bilgileri.txt ikilisinin karşılığı: JSON deposunun yolu kullanıcı
 * tercihlerinde (java.util.prefs, OS'e göre registry/plist/dosya) saklanır.
 */
public final class AppConfig {

    private static final String PREF_KEY = "json_depo_yolu";
    private static final Preferences PREFS = Preferences.userNodeForPackage(AppConfig.class);

    private AppConfig() {
    }

    public static Path jsonDeposu() {
        String kayitli = PREFS.get(PREF_KEY, null);
        if (kayitli != null) {
            return Path.of(kayitli);
        }
        return Path.of(".");
    }

    public static void jsonDeposunuAyarla(Path yol) {
        PREFS.put(PREF_KEY, yol.toAbsolutePath().toString());
    }

    public static Konfigurasyon konfigurasyonuYukle() {
        Path konfPath = jsonDeposu().resolve(ReadmeGenerator.KONFIGURASYON_JSON);
        return KonfigurasyonJsonKontrol.guncelle(konfPath, jsonDeposu());
    }

    public static Path dokumanlarRepoYolu() {
        Konfigurasyon k = konfigurasyonuYukle();
        return jsonDeposu().resolve(k.dokumanlar_repo_yolu).normalize();
    }
}
