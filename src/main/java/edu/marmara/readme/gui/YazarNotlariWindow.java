package edu.marmara.readme.gui;

import edu.marmara.readme.engine.ReadmeGenerator;
import edu.marmara.readme.engine.Sabitler;
import edu.marmara.readme.engine.model.YazarNotlariBolumu;
import edu.marmara.readme.engine.util.JsonUtil;
import edu.marmara.readme.engine.util.MetinIslemleri;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.nio.file.Path;
import java.util.List;

/** Python yazarin_notlari_duzenle_window.py'nin Java/JavaFX portu. */
public class YazarNotlariWindow {

    private final Path yolu;
    private YazarNotlariBolumu data;
    private final ObservableList<String> model = FXCollections.observableArrayList();
    private final UndoManager<String> undoManager = new UndoManager<>();
    private Stage stage;
    private TextField aramaAlani;
    private Label sayiLabel;

    public YazarNotlariWindow(Path jsonDeposu) {
        this.yolu = jsonDeposu.resolve(ReadmeGenerator.YAZARIN_NOTLARI_JSON);
    }

    public void show() {
        data = JsonUtil.read(yolu, YazarNotlariBolumu.class);
        if (data == null) {
            data = new YazarNotlariBolumu();
            data.baslik = Sabitler.VARSAYILAN_YAZARIN_NOTLARI_BOLUM_ADI;
            jsonKaydet();
        }
        model.setAll(data.aciklamalar);

        stage = new Stage();
        stage.setTitle("Yazarın Notları Ekle/Güncelle");
        stage.initModality(Modality.APPLICATION_MODAL);

        Button baslikBtn = new Button();
        baslikBtn.setMaxWidth(Double.MAX_VALUE);
        baslikBtn.setText(MetinIslemleri.kisaltMetin(data.baslik));
        baslikBtn.setOnAction(e -> Dialogs.textInput("Başlık", "Yazarın notları başlığı:", data.baslik).ifPresent(yeni -> {
            data.baslik = yeni;
            jsonKaydet();
            baslikBtn.setText(MetinIslemleri.kisaltMetin(data.baslik));
            Toast.show(stage, "Başlık güncellendi.", Toast.Tip.BASARI);
        }));

        ListView<String> listView = new ListView<>(model);
        listView.setOnMouseClicked(e -> {
            if (e.getClickCount() == 2) {
                String secili = listView.getSelectionModel().getSelectedItem();
                if (secili != null) {
                    Dialogs.multilineInput("Not Düzenle", "Not içeriği:", secili).ifPresent(yeni -> {
                        int idx = model.indexOf(secili);
                        model.set(idx, yeni);
                        data.aciklamalar.set(idx, yeni);
                        jsonKaydet();
                        Toast.show(stage, "Not güncellendi.", Toast.Tip.BASARI);
                    });
                }
            }
        });

        aramaAlani = new TextField();
        aramaAlani.setPromptText("Not ara...");
        aramaAlani.textProperty().addListener((obs, eski, yeni) -> uygulaFiltre(yeni));
        sayiLabel = new Label();

        Button ekleBtn = new Button("➕ Not Ekle");
        ekleBtn.setOnAction(e -> Dialogs.multilineInput("Not Ekle", "Not içeriği:", "").ifPresent(yeni -> {
            if (!yeni.isBlank()) {
                model.add(yeni);
                data.aciklamalar.add(yeni);
                jsonKaydet();
                guncelleSayi();
                Toast.show(stage, "Not eklendi.", Toast.Tip.BASARI);
            }
        }));
        Button silBtn = new Button("Seçili Notu Sil");
        silBtn.setOnAction(e -> {
            String secili = listView.getSelectionModel().getSelectedItem();
            if (secili != null && Dialogs.confirm("Onay", "Bu notu silmek istiyor musun?")) {
                int idx = data.aciklamalar.indexOf(secili);
                undoManager.pushDeleted(idx, secili);
                model.remove(secili);
                data.aciklamalar.remove(secili);
                jsonKaydet();
                guncelleSayi();
                Toast.show(stage, "Not silindi. (Geri almak için Ctrl+Z)", Toast.Tip.BASARI);
            }
        });

        VBox root = new VBox(8, baslikBtn, new HBox(8, ekleBtn, silBtn, aramaAlani), sayiLabel,
                new Label("Notlar (çift tıkla: düzenle)"), listView);
        root.setPadding(new Insets(10));
        VBox.setVgrow(listView, Priority.ALWAYS);

        Scene scene = new Scene(root, 560, 540);
        scene.setOnKeyPressed(e -> {
            if (e.isControlDown() && e.getCode() == KeyCode.Z) {
                undoSil();
            }
        });
        stage.setScene(scene);
        guncelleSayi();
        stage.showAndWait();
    }

    private void guncelleSayi() {
        sayiLabel.setText("Toplam " + data.aciklamalar.size() + " not");
    }

    private void uygulaFiltre(String sorgu) {
        if (sorgu == null || sorgu.isEmpty()) {
            model.setAll(data.aciklamalar);
            guncelleSayi();
            return;
        }
        String q = sorgu.replace("İ", "i").toLowerCase();
        List<String> filtreli = data.aciklamalar.stream()
                .filter(s -> s.replace("İ", "i").toLowerCase().contains(q)).toList();
        model.setAll(filtreli);
        sayiLabel.setText(filtreli.size() + " not bulundu");
    }

    private void undoSil() {
        var silinen = undoManager.popDeleted();
        if (silinen == null) {
            return;
        }
        int index = Math.min(silinen.index(), data.aciklamalar.size());
        data.aciklamalar.add(index, silinen.item());
        model.setAll(data.aciklamalar);
        jsonKaydet();
        guncelleSayi();
        Toast.show(stage, "Not geri alındı.", Toast.Tip.BASARI);
    }

    private void jsonKaydet() {
        JsonUtil.write(yolu, data);
    }
}
