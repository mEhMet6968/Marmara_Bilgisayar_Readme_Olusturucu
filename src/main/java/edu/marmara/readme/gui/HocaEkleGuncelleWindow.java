package edu.marmara.readme.gui;

import edu.marmara.readme.engine.ReadmeGenerator;
import edu.marmara.readme.engine.Sabitler;
import edu.marmara.readme.engine.model.DerslerBolumu;
import edu.marmara.readme.engine.model.Hoca;
import edu.marmara.readme.engine.model.HocalarBolumu;
import edu.marmara.readme.engine.util.JsonUtil;
import edu.marmara.readme.engine.writer.Yardimci;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.nio.file.Path;
import java.util.List;

/**
 * Python hoca_ekle_guncelle_window.py'nin (HocaEkleGuncelleWindow) Java/JavaFX portu.
 * Hocaları listeler, arama/filtre, ekleme/düzenleme/silme ve basit "son silineni geri al" sağlar.
 */
public class HocaEkleGuncelleWindow {

    private final Path jsonDeposu;
    private final Path hocalarJsonYolu;
    private final Path derslerJsonYolu;

    private HocalarBolumu data;
    private final ObservableList<Hoca> hocalarListModel = FXCollections.observableArrayList();
    private final UndoManager<Hoca> undoManager = new UndoManager<>();

    private ListView<Hoca> listView;
    private Label sayiLabel;
    private TextField aramaAlani;
    private Stage stage;

    public HocaEkleGuncelleWindow(Path jsonDeposu) {
        this.jsonDeposu = jsonDeposu;
        this.hocalarJsonYolu = jsonDeposu.resolve(ReadmeGenerator.HOCALAR_JSON);
        this.derslerJsonYolu = jsonDeposu.resolve(ReadmeGenerator.DERSLER_JSON);
    }

    public void show() {
        data = JsonUtil.read(hocalarJsonYolu, HocalarBolumu.class);
        if (data == null) {
            data = new HocalarBolumu();
            data.bolum_adi = Sabitler.VARSAYILAN_HOCA_BOLUM_ADI;
            data.bolum_aciklamasi = Sabitler.VARSAYILAN_HOCA_BOLUM_ACIKLAMASI;
            jsonKaydet();
        }

        stage = new Stage();
        stage.setTitle("Hocaları Ekle/Güncelle");
        stage.initModality(Modality.APPLICATION_MODAL);

        VBox root = new VBox(8);
        root.setPadding(new Insets(10));

        Button bolumAdiBtn = new Button();
        bolumAdiBtn.setMaxWidth(Double.MAX_VALUE);
        bolumAdiBtn.setOnAction(e -> {
            Dialogs.textInput("Bölüm Adı", "Bölüm adı:", data.bolum_adi).ifPresent(yeni -> {
                if (!yeni.equals(data.bolum_adi)) {
                    data.bolum_adi = yeni;
                    jsonKaydet();
                }
            });
            bolumAdiBtn.setText(data.bolum_adi);
        });
        bolumAdiBtn.setText(data.bolum_adi);

        Button aciklamaBtn = new Button();
        aciklamaBtn.setMaxWidth(Double.MAX_VALUE);
        aciklamaBtn.setOnAction(e -> {
            Dialogs.multilineInput("Bölüm Açıklaması", "Bölüm açıklaması:", data.bolum_aciklamasi).ifPresent(yeni -> {
                if (!yeni.equals(data.bolum_aciklamasi)) {
                    data.bolum_aciklamasi = yeni;
                    jsonKaydet();
                }
                aciklamaBtn.setText(kisalt(data.bolum_aciklamasi));
            });
        });
        aciklamaBtn.setText(kisalt(data.bolum_aciklamasi));

        Button ekleBtn = new Button("➕ Hoca Ekle");
        ekleBtn.setOnAction(e -> hocaDuzenlemeAc(null));

        Button linkKontrolBtn = new Button("🔗 Kırık Bağlantı Tetkiki");
        linkKontrolBtn.setOnAction(e -> {
            List<LinkKontrolWindow.SahipLink> linkler = data.hocalar.stream()
                    .filter(h -> h.link != null && !h.link.isBlank())
                    .map(h -> new LinkKontrolWindow.SahipLink(h.ad, h.link))
                    .toList();
            new LinkKontrolWindow(linkler, "Hoca Linkleri Kontrolü").show();
        });

        aramaAlani = new TextField();
        aramaAlani.setPromptText("Hoca ara...");
        aramaAlani.textProperty().addListener((obs, eski, yeni) -> uygulaFiltre(yeni));

        sayiLabel = new Label();

        listView = new ListView<>(hocalarListModel);
        listView.setCellFactory(lv -> new ListCell<>() {
            @Override
            protected void updateItem(Hoca hoca, boolean empty) {
                super.updateItem(hoca, empty);
                setText(empty || hoca == null ? null : hoca.ad);
            }
        });
        listView.setOnMouseClicked(e -> {
            if (e.getClickCount() == 2 && listView.getSelectionModel().getSelectedItem() != null) {
                hocaDuzenlemeAc(listView.getSelectionModel().getSelectedItem());
            }
        });

        HBox ustSatir = new HBox(8, ekleBtn, linkKontrolBtn, aramaAlani);
        root.getChildren().addAll(bolumAdiBtn, aciklamaBtn, ustSatir, sayiLabel, listView);
        VBox.setVgrow(listView, javafx.scene.layout.Priority.ALWAYS);

        Scene scene = new Scene(root, 640, 600);
        scene.setOnKeyPressed(e -> {
            if (e.isControlDown() && e.getCode() == KeyCode.Z) {
                undoSil();
            }
        });
        stage.setScene(scene);

        hocalariYenile();
        stage.showAndWait();
    }

    private String kisalt(String s) {
        return edu.marmara.readme.engine.util.MetinIslemleri.kisaltMetin(s == null ? "" : s);
    }

    private void jsonKaydet() {
        JsonUtil.write(hocalarJsonYolu, data);
    }

    private void hocalariYenile() {
        List<Hoca> sirali = data.hocalar.stream().filter(h -> h.ad != null && !h.ad.isEmpty())
                .sorted(Yardimci.HOCA_SIRALAMA).toList();
        hocalarListModel.setAll(sirali);
        sayiLabel.setText("Toplam " + sirali.size() + " hoca");
        if (!aramaAlani.getText().isEmpty()) {
            uygulaFiltre(aramaAlani.getText());
        }
    }

    private void uygulaFiltre(String sorgu) {
        List<Hoca> tumHocalar = data.hocalar.stream().filter(h -> h.ad != null && !h.ad.isEmpty())
                .sorted(Yardimci.HOCA_SIRALAMA).toList();
        if (sorgu == null || sorgu.isEmpty()) {
            hocalarListModel.setAll(tumHocalar);
            sayiLabel.setText("Toplam " + tumHocalar.size() + " hoca");
            return;
        }
        String q = sorgu.replace("İ", "i").toLowerCase();
        List<Hoca> filtreli = tumHocalar.stream()
                .filter(h -> h.ad.replace("İ", "i").toLowerCase().contains(q)).toList();
        hocalarListModel.setAll(filtreli);
        sayiLabel.setText(filtreli.size() + " hoca bulundu");
    }

    private void undoSil() {
        var silinen = undoManager.popDeleted();
        if (silinen == null) {
            return;
        }
        int index = Math.min(silinen.index(), data.hocalar.size());
        data.hocalar.add(index, silinen.item());
        jsonKaydet();
        hocalariYenile();
        Toast.show(stage, "Hoca geri alındı.", Toast.Tip.BASARI);
    }

    private void hocaDuzenlemeAc(Hoca hoca) {
        DerslerBolumu dersler = JsonUtil.read(derslerJsonYolu, DerslerBolumu.class);
        HocaDuzenlemeDialog dialog = new HocaDuzenlemeDialog(stage, hoca, data, dersler, derslerJsonYolu,
                () -> {
                    jsonKaydet();
                    hocalariYenile();
                    Toast.show(stage, "Hoca başarıyla kaydedildi.", Toast.Tip.BASARI);
                },
                silinenHoca -> {
                    int index = data.hocalar.indexOf(silinenHoca);
                    undoManager.pushDeleted(index, silinenHoca);
                    data.hocalar.remove(silinenHoca);
                    jsonKaydet();
                    hocalariYenile();
                    Toast.show(stage, "Hoca silindi. (Geri almak için Ctrl+Z)", Toast.Tip.BASARI);
                });
        dialog.show();
    }
}
