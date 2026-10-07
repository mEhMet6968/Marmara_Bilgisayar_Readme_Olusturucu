package edu.marmara.readme.cli;

import edu.marmara.readme.engine.ReadmeGenerator;

import java.nio.file.Path;

/**
 * Başsız (GUI'siz) çalıştırma: {@code readme_olustur.py main()} karşılığı.
 * Kullanım: java -cp ... edu.marmara.readme.cli.ReadmeOlusturCli <json-deposu-yolu>
 */
public final class ReadmeOlusturCli {

    private ReadmeOlusturCli() {
    }

    public static void main(String[] args) {
        Path jsonDeposu = args.length > 0 ? Path.of(args[0]) : Path.of(".");
        ReadmeGenerator generator = new ReadmeGenerator(jsonDeposu, System.out::println);
        generator.generateAll();
    }
}
