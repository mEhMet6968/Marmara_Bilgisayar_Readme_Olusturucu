package edu.marmara.readme.gui;

import edu.marmara.readme.engine.ReadmeGenerator;
import edu.marmara.readme.engine.Sabitler;
import edu.marmara.readme.engine.model.IletisimBilgisi;
import edu.marmara.readme.engine.model.KatkidaBulunan;
import edu.marmara.readme.engine.model.KatkidaBulunanlarBolumu;
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
import java.util.ArrayList;
import java.util.List;

/** Python katkida_bulunanlari_duzenle_window.py + katkida_bulunan_ekle_window.py'nin Java/JavaFX portu. */
public class KatkidaBulunanWindow {

    private final Path yolu;
    private KatkidaBulunanlarBolumu data;
    private final ObservableList<KatkidaBulunan> model = FXCollections.observableArrayList();
    private final UndoManager<KatkidaBulunan> undoManager = new UndoManager<>();
    private Stage stage;
    private TextField aramaAlani;
    private Label sayiLabel;

    public KatkidaBulunanWindow(Path jsonDeposu) {
        this.yolu = jsonDeposu.resolve(ReadmeGenerator.KATKIDA_BULUNANLAR_JSON);
    }

    public void show() {
        data = JsonUtil.read(yolu, KatkidaBulunanlarBolumu.class);
        if (data == null) {
            data = new KatkidaBulunanlarBolumu();
            data.bolum_adi = Sabitler.VARSAYILAN_KATKIDA_BULUNANLAR_BOLUM_ADI;
            data.bolum_aciklamasi = Sabitler.VARSAYILAN_KATKIDA_BULUNANLAR_BOLUM_ACIKLAMASI;
            jsonKaydet();
        }
        model.setAll(data.katkida_bulunanlar);

        stage = new Stage();
        stage.setTitle("Katkıda Bulunan Ekle/Güncelle");
        stage.initModality(Modality.APPLICATION_MODAL);

        Button baslikBtn = new Button();
        baslikBtn.setMaxWidth(Double.MAX_VALUE);
        baslikBtn.setText(MetinIslemleri.kisaltMetin(data.bolum_adi));
        baslikBtn.setOnAction(e -> Dialogs.textInput("Bölüm Adı", "Bölüm adı:", data.bolum_adi).ifPresent(yeni -> {
            data.bolum_adi = yeni;
            jsonKaydet();
            baslikBtn.setText(MetinIslemleri.kisaltMetin(data.bolum_adi));
            Toast.show(stage, "Bölüm adı güncellendi.", Toast.Tip.BASARI);
        }));

        Button aciklamaBtn = new Button();
        aciklamaBtn.setMaxWidth(Double.MAX_VALUE);
        aciklamaBtn.setText(MetinIslemleri.kisaltMetin(data.bolum_aciklamasi));
        aciklamaBtn.setOnAction(e -> Dialogs.multilineInput("Bölüm Açıklaması", "Açıklama:", data.bolum_aciklamasi).ifPresent(yeni -> {
            data.bolum_aciklamasi = yeni;
            jsonKaydet();
            aciklamaBtn.setText(MetinIslemleri.kisaltMetin(data.bolum_aciklamasi));
            Toast.show(stage, "Açıklama güncellendi.", Toast.Tip.BASARI);
        }));

        ListView<KatkidaBulunan> listView = new ListView<>(model);
        listView.setCellFactory(lv -> new ListCell<>() {
            @Override
            protected void updateItem(KatkidaBulunan k, boolean empty) {
                super.updateItem(k, empty);
                setText(empty || k == null ? null : k.ad);
            }
        });
        listView.setOnMouseClicked(e -> {
            if (e.getClickCount() == 2 && listView.getSelectionModel().getSelectedItem() != null) {
                duzenlemeAc(listView.getSelectionModel().getSelectedItem());
            }
        });

        Button ekleBtn = new Button("➕ Katkıda Bulunan Ekle");
        ekleBtn.setOnAction(e -> duzenlemeAc(null));

        Button linkKontrolBtn = new Button("🔗 Kırık Bağlantı Tetkiki");
        linkKontrolBtn.setOnAction(e -> {
            List<LinkKontrolWindow.SahipLink> linkler = data.katkida_bulunanlar.stream()
                    .flatMap(k -> k.iletisim_bilgileri.stream()
                            .filter(b -> b.link != null && !b.link.isBlank())
                            .map(b -> new LinkKontrolWindow.SahipLink(k.ad + " (" + b.baslik + ")", b.link)))
                    .toList();
            new LinkKontrolWindow(linkler, "Katkıda Bulunan Linkleri Kontrolü").show();
        });

        aramaAlani = new TextField();
        aramaAlani.setPromptText("Katkıda bulunan ara...");
        aramaAlani.textProperty().addListener((obs, eski, yeni) -> uygulaFiltre(yeni));
        sayiLabel = new Label();

        VBox root = new VBox(8, baslikBtn, aciklamaBtn, new HBox(8, ekleBtn, linkKontrolBtn, aramaAlani),
                sayiLabel, listView);
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
        sayiLabel.setText("Toplam " + data.katkida_bulunanlar.size() + " kişi");
    }

    private void uygulaFiltre(String sorgu) {
        if (sorgu == null || sorgu.isEmpty()) {
            model.setAll(data.katkida_bulunanlar);
            guncelleSayi();
            return;
        }
        String q = sorgu.replace("İ", "i").toLowerCase();
        List<KatkidaBulunan> filtreli = data.katkida_bulunanlar.stream()
                .filter(k -> k.ad.replace("İ", "i").toLowerCase().contains(q)).toList();
        model.setAll(filtreli);
        sayiLabel.setText(filtreli.size() + " kişi bulundu");
    }

    private void undoSil() {
        var silinen = undoManager.popDeleted();
        if (silinen == null) {
            return;
        }
        int index = Math.min(silinen.index(), data.katkida_bulunanlar.size());
        data.katkida_bulunanlar.add(index, silinen.item());
        jsonKaydet();
        yenile();
        Toast.show(stage, "Katkıda bulunan geri alındı.", Toast.Tip.BASARI);
    }

    private void jsonKaydet() {
        JsonUtil.write(yolu, data);
    }

    private void yenile() {
        model.setAll(data.katkida_bulunanlar);
        guncelleSayi();
    }

    private void duzenlemeAc(KatkidaBulunan kisi) {
        Stage dlg = new Stage();
        dlg.initOwner(stage);
        dlg.initModality(Modality.APPLICATION_MODAL);
        dlg.setTitle(kisi != null ? "Katkıda Bulunanı Düzenle" : "Katkıda Bulunan Ekle");

        TextField adAlani = new TextField(kisi != null ? kisi.ad : "");
        TextField githubAlani = new TextField(kisi != null ? kisi.github_link : "");
        ComboBox<String> oranBox = new ComboBox<>(FXCollections.observableArrayList(Sabitler.KATKIDA_BULUNMA_ORANI_DIZI));
        oranBox.setValue(kisi != null ? kisi.katkida_bulunma_orani : Sabitler.KATKIDA_BULUNMA_ORANI_DIZI.get(
                Sabitler.KATKIDA_BULUNMA_ORANI_DIZI.size() - 1));

        ObservableList<String> iletisimGosterim = FXCollections.observableArrayList();
        List<IletisimBilgisi> iletisimler = new ArrayList<>();
        if (kisi != null) {
            iletisimler.addAll(kisi.iletisim_bilgileri);
            iletisimler.forEach(b -> iletisimGosterim.add(b.baslik + " -> " + b.link));
        }
        ListView<String> iletisimListView = new ListView<>(iletisimGosterim);
        iletisimListView.setPrefHeight(100);
        Button iletisimEkleBtn = new Button("İletişim Bilgisi Ekle");
        iletisimEkleBtn.setOnAction(e -> Dialogs.textInput("Başlık", "Örn: LinkedIn, E-posta...", "").ifPresent(baslik ->
                Dialogs.textInput("Link", "URL veya mailto: linki:", "").ifPresent(link -> {
                    IletisimBilgisi b = new IletisimBilgisi();
                    b.baslik = baslik;
                    b.link = link;
                    iletisimler.add(b);
                    iletisimGosterim.add(baslik + " -> " + link);
                })));
        Button iletisimSilBtn = new Button("Seçili İletişimi Kaldır");
        iletisimSilBtn.setOnAction(e -> {
            int idx = iletisimListView.getSelectionModel().getSelectedIndex();
            if (idx >= 0) {
                iletisimler.remove(idx);
                iletisimGosterim.remove(idx);
            }
        });

        Button kaydetBtn = new Button(kisi != null ? "Değişiklikleri Kaydet" : "Ekle");
        kaydetBtn.setOnAction(e -> {
            if (adAlani.getText().isBlank()) {
                Dialogs.error("Hata", "Ad boş olamaz.");
                return;
            }
            KatkidaBulunan hedef = kisi != null ? kisi : new KatkidaBulunan();
            hedef.ad = adAlani.getText().strip();
            hedef.github_link = githubAlani.getText().strip();
            hedef.katkida_bulunma_orani = oranBox.getValue();
            hedef.iletisim_bilgileri = iletisimler;
            if (kisi == null) {
                data.katkida_bulunanlar.add(hedef);
            }
            jsonKaydet();
            yenile();
            dlg.close();
            Toast.show(stage, "Katkıda bulunan kaydedildi.", Toast.Tip.BASARI);
        });

        HBox altButonlar = new HBox(8, kaydetBtn);
        if (kisi != null) {
            Button silBtn = new Button("Sil");
            silBtn.setOnAction(e -> {
                if (Dialogs.confirm("Onay", "Silmek istediğine emin misin?")) {
                    int index = data.katkida_bulunanlar.indexOf(kisi);
                    undoManager.pushDeleted(index, kisi);
                    data.katkida_bulunanlar.remove(kisi);
                    jsonKaydet();
                    yenile();
                    dlg.close();
                    Toast.show(stage, "Silindi. (Geri almak için Ctrl+Z)", Toast.Tip.BASARI);
                }
            });
            altButonlar.getChildren().add(silBtn);
        }

        VBox root = new VBox(6,
                new Label("Ad"), adAlani,
                new Label("GitHub Linki"), githubAlani,
                new Label("Katkı Oranı"), oranBox,
                new Label("İletişim Bilgileri"), iletisimListView, new HBox(8, iletisimEkleBtn, iletisimSilBtn),
                altButonlar);
        root.setPadding(new Insets(12));
        dlg.setScene(new Scene(root, 440, 540));
        dlg.showAndWait();
    }
}
