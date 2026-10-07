package edu.marmara.readme.gui;

import edu.marmara.readme.engine.ReadmeGenerator;
import edu.marmara.readme.engine.Sabitler;
import edu.marmara.readme.engine.model.Donem;
import edu.marmara.readme.engine.model.DonemlerBolumu;
import edu.marmara.readme.engine.util.JsonUtil;
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
import java.util.ArrayList;
import java.util.List;

/** Python donem_ekle_guncelle_window.py'nin Java/JavaFX portu. */
public class DonemWindow {

    private final Path donemlerJsonYolu;
    private DonemlerBolumu data;
    private final ObservableList<Donem> listModel = FXCollections.observableArrayList();
    private final UndoManager<Donem> undoManager = new UndoManager<>();
    private Stage stage;
    private Label sayiLabel;
    private TextField aramaAlani;

    public DonemWindow(Path jsonDeposu) {
        this.donemlerJsonYolu = jsonDeposu.resolve(ReadmeGenerator.DONEMLER_JSON);
    }

    public void show() {
        data = JsonUtil.read(donemlerJsonYolu, DonemlerBolumu.class);
        if (data == null) {
            data = new DonemlerBolumu();
            jsonKaydet();
        }

        stage = new Stage();
        stage.setTitle("Dönem Ekle/Güncelle");
        stage.initModality(Modality.APPLICATION_MODAL);

        Button ekleBtn = new Button("➕ Dönem Ekle");
        ekleBtn.setOnAction(e -> duzenlemeAc(null));

        aramaAlani = new TextField();
        aramaAlani.setPromptText("Dönem ara...");
        aramaAlani.textProperty().addListener((obs, eski, yeni) -> uygulaFiltre(yeni));

        sayiLabel = new Label();
        ListView<Donem> listView = new ListView<>(listModel);
        listView.setCellFactory(lv -> new ListCell<>() {
            @Override
            protected void updateItem(Donem d, boolean empty) {
                super.updateItem(d, empty);
                setText(empty || d == null ? null : d.donem_adi);
            }
        });
        listView.setOnMouseClicked(e -> {
            if (e.getClickCount() == 2 && listView.getSelectionModel().getSelectedItem() != null) {
                duzenlemeAc(listView.getSelectionModel().getSelectedItem());
            }
        });

        VBox root = new VBox(8, ekleBtn, new HBox(8, aramaAlani), sayiLabel, listView);
        root.setPadding(new Insets(10));
        VBox.setVgrow(listView, Priority.ALWAYS);

        Scene scene = new Scene(root, 480, 480);
        scene.setOnKeyPressed(e -> {
            if (e.isControlDown() && e.getCode() == KeyCode.Z) {
                undoSil();
            }
        });
        stage.setScene(scene);

        yenile();
        stage.showAndWait();
    }

    private void yenile() {
        listModel.setAll(data.donemler);
        sayiLabel.setText("Toplam " + data.donemler.size() + " dönem");
        if (!aramaAlani.getText().isEmpty()) {
            uygulaFiltre(aramaAlani.getText());
        }
    }

    private void uygulaFiltre(String sorgu) {
        if (sorgu == null || sorgu.isEmpty()) {
            listModel.setAll(data.donemler);
            sayiLabel.setText("Toplam " + data.donemler.size() + " dönem");
            return;
        }
        String q = sorgu.replace("İ", "i").toLowerCase();
        List<Donem> filtreli = data.donemler.stream()
                .filter(d -> d.donem_adi.replace("İ", "i").toLowerCase().contains(q)).toList();
        listModel.setAll(filtreli);
        sayiLabel.setText(filtreli.size() + " dönem bulundu");
    }

    private void undoSil() {
        var silinen = undoManager.popDeleted();
        if (silinen == null) {
            return;
        }
        int index = Math.min(silinen.index(), data.donemler.size());
        data.donemler.add(index, silinen.item());
        jsonKaydet();
        yenile();
        Toast.show(stage, "Dönem geri alındı.", Toast.Tip.BASARI);
    }

    private void jsonKaydet() {
        JsonUtil.write(donemlerJsonYolu, data);
    }

    private void duzenlemeAc(Donem donem) {
        Stage dlg = new Stage();
        dlg.initOwner(stage);
        dlg.initModality(Modality.APPLICATION_MODAL);
        dlg.setTitle(donem != null ? "Dönem Düzenle" : "Dönem Ekle");

        TextField adAlani = new TextField(donem != null ? donem.donem_adi : "");
        ComboBox<Integer> yilBox = new ComboBox<>(FXCollections.observableArrayList(0, 1, 2, 3, 4));
        yilBox.setValue(donem != null ? donem.yil : 0);
        ComboBox<String> donemBox = new ComboBox<>(FXCollections.observableArrayList(Sabitler.DONEMLER_DIZISI_YOKLA_BERABER));
        donemBox.setValue(donem != null && donem.donem != null && !donem.donem.isEmpty() ? donem.donem : Sabitler.YOK);

        ObservableList<String> tavsiyeler = FXCollections.observableArrayList(
                donem != null ? donem.genel_tavsiyeler : List.of());
        ListView<String> tavsiyeListView = new ListView<>(tavsiyeler);
        tavsiyeListView.setPrefHeight(140);
        DragReorder.enable(tavsiyeListView, yeniSira -> {
        });
        Button tavsiyeEkleBtn = new Button("Genel Tavsiye Ekle");
        tavsiyeEkleBtn.setOnAction(e -> Dialogs.textInput("Tavsiye Ekle", "Tavsiye metni:", "").ifPresent(yeni -> {
            if (!yeni.isBlank()) {
                tavsiyeler.add(yeni);
            }
        }));
        Button tavsiyeSilBtn = new Button("Seçili Tavsiyeyi Sil");
        tavsiyeSilBtn.setOnAction(e -> {
            String secili = tavsiyeListView.getSelectionModel().getSelectedItem();
            if (secili != null) {
                tavsiyeler.remove(secili);
            }
        });

        Button kaydetBtn = new Button(donem != null ? "Değişiklikleri Kaydet" : "Ekle");
        kaydetBtn.setOnAction(e -> {
            String ad = adAlani.getText().strip();
            if (ad.isEmpty()) {
                Dialogs.error("Hata", "Dönem adı boş olamaz.");
                return;
            }
            Donem hedef = donem != null ? donem : new Donem();
            hedef.donem_adi = ad;
            hedef.yil = yilBox.getValue();
            hedef.donem = Sabitler.YOK.equals(donemBox.getValue()) ? "" : donemBox.getValue();
            hedef.genel_tavsiyeler = new ArrayList<>(tavsiyeler);
            if (donem == null) {
                data.donemler.add(hedef);
            }
            jsonKaydet();
            yenile();
            dlg.close();
            Toast.show(stage, "Dönem başarıyla kaydedildi.", Toast.Tip.BASARI);
        });

        HBox altButonlar = new HBox(8, kaydetBtn);
        if (donem != null) {
            Button silBtn = new Button("Dönemi Sil");
            silBtn.setOnAction(e -> {
                if (Dialogs.confirm("Onay", "Dönemi silmek istediğine emin misin?")) {
                    int index = data.donemler.indexOf(donem);
                    undoManager.pushDeleted(index, donem);
                    data.donemler.remove(donem);
                    jsonKaydet();
                    yenile();
                    dlg.close();
                    Toast.show(stage, "Dönem silindi. (Geri almak için Ctrl+Z)", Toast.Tip.BASARI);
                }
            });
            altButonlar.getChildren().add(silBtn);
        }

        VBox root = new VBox(6,
                new Label("Dönem Adı"), adAlani,
                new Label("Yıl"), yilBox,
                new Label("Dönem"), donemBox,
                new Label("Genel Tavsiyeler (sürükleyerek sıralayabilirsin)"), tavsiyeListView,
                new HBox(8, tavsiyeEkleBtn, tavsiyeSilBtn),
                altButonlar);
        root.setPadding(new Insets(12));
        dlg.setScene(new Scene(root, 440, 560));
        dlg.showAndWait();
    }
}
