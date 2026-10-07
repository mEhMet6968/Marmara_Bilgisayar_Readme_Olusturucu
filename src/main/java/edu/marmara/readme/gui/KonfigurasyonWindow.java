package edu.marmara.readme.gui;

import edu.marmara.readme.engine.ReadmeGenerator;
import edu.marmara.readme.engine.model.Konfigurasyon;
import edu.marmara.readme.engine.util.JsonUtil;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.stage.DirectoryChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.nio.file.Path;

/** Python konfigurasyon_window.py'nin basitleştirilmiş Java/JavaFX portu. */
public class KonfigurasyonWindow {

    private final Path jsonDeposu;

    public KonfigurasyonWindow(Path jsonDeposu) {
        this.jsonDeposu = jsonDeposu;
    }

    public void show() {
        Path konfYolu = jsonDeposu.resolve(ReadmeGenerator.KONFIGURASYON_JSON);
        Konfigurasyon k = edu.marmara.readme.engine.config.KonfigurasyonJsonKontrol.guncelle(konfYolu, jsonDeposu);

        Stage stage = new Stage();
        stage.setTitle("Konfigürasyon Düzenle");
        stage.initModality(Modality.APPLICATION_MODAL);

        GridPane grid = new GridPane();
        grid.setHgap(8);
        grid.setVgap(8);
        grid.setPadding(new Insets(10));

        TextField jsonDepoAlani = new TextField(jsonDeposu.toAbsolutePath().toString());
        Button jsonDepoSecBtn = new Button("Klasör Seç");
        jsonDepoSecBtn.setOnAction(e -> {
            DirectoryChooser chooser = new DirectoryChooser();
            java.io.File secilen = chooser.showDialog(stage);
            if (secilen != null) {
                jsonDepoAlani.setText(secilen.getAbsolutePath());
                AppConfig.jsonDeposunuAyarla(secilen.toPath());
            }
        });

        TextField githubUrl = new TextField(k.github_url);
        TextField hocaYorumlama = new TextField(k.hoca_yorumlama);
        TextField hocaOylama = new TextField(k.hoca_oylama);
        TextField dersYorumlama = new TextField(k.ders_yorumlama);
        TextField dersOylama = new TextField(k.ders_oylama);
        TextField dersOylamaCsv = new TextField(k.ders_oylama_csv);
        TextField dersYorumlamaCsv = new TextField(k.ders_yorumlama_csv);
        TextField hocaOylamaCsv = new TextField(k.hoca_oylama_csv);
        TextField hocaYorumlamaCsv = new TextField(k.hoca_yorumlama_csv);
        TextField dokumanlarRepoYolu = new TextField(k.dokumanlar_repo_yolu);
        TextField cikmislar = new TextField(k.cikmislar);

        int row = 0;
        grid.addRow(row++, new Label("JSON Deposu"), jsonDepoAlani, jsonDepoSecBtn);
        grid.addRow(row++, new Label("GitHub URL"), githubUrl);
        grid.addRow(row++, new Label("Hoca Yorumlama Formu"), hocaYorumlama);
        grid.addRow(row++, new Label("Hoca Oylama Formu"), hocaOylama);
        grid.addRow(row++, new Label("Ders Yorumlama Formu"), dersYorumlama);
        grid.addRow(row++, new Label("Ders Oylama Formu"), dersOylama);
        grid.addRow(row++, new Label("Ders Oylama CSV"), dersOylamaCsv);
        grid.addRow(row++, new Label("Ders Yorumlama CSV"), dersYorumlamaCsv);
        grid.addRow(row++, new Label("Hoca Oylama CSV"), hocaOylamaCsv);
        grid.addRow(row++, new Label("Hoca Yorumlama CSV"), hocaYorumlamaCsv);
        grid.addRow(row++, new Label("Dökümanlar Repo Yolu"), dokumanlarRepoYolu);
        grid.addRow(row++, new Label("Çıkmışlar Linki"), cikmislar);

        Button kaydetBtn = new Button("Kaydet");
        kaydetBtn.setOnAction(e -> {
            k.github_url = githubUrl.getText();
            k.hoca_yorumlama = hocaYorumlama.getText();
            k.hoca_oylama = hocaOylama.getText();
            k.ders_yorumlama = dersYorumlama.getText();
            k.ders_oylama = dersOylama.getText();
            k.ders_oylama_csv = dersOylamaCsv.getText();
            k.ders_yorumlama_csv = dersYorumlamaCsv.getText();
            k.hoca_oylama_csv = hocaOylamaCsv.getText();
            k.hoca_yorumlama_csv = hocaYorumlamaCsv.getText();
            k.dokumanlar_repo_yolu = dokumanlarRepoYolu.getText();
            k.cikmislar = cikmislar.getText();
            JsonUtil.write(konfYolu, k);
            Dialogs.info("Kaydedildi", "Konfigürasyon güncellendi.");
            stage.close();
        });

        VBox root = new VBox(10, grid, kaydetBtn);
        root.setPadding(new Insets(10));
        ScrollPane scroll = new ScrollPane(root);
        scroll.setFitToWidth(true);
        stage.setScene(new Scene(scroll, 620, 560));
        stage.showAndWait();
    }
}
