package edu.marmara.readme.gui;

/**
 * JavaFX başlatıcısı. JDK11+'ta, main sınıfı doğrudan {@code Application}'ı extend ediyorsa
 * modül yolu olmadan "JavaFX runtime components are missing" hatası veriyor; bu ayrı,
 * Application'ı extend ETMEYEN sınıf classpath modunda çalıştırmayı mümkün kılıyor.
 */
public final class Launcher {

    private Launcher() {
    }

    public static void main(String[] args) {
        MainApp.main(args);
    }
}
