package edu.marmara.readme.gui;

import edu.marmara.readme.engine.Sabitler;
import edu.marmara.readme.engine.model.Ders;
import edu.marmara.readme.engine.model.DerslerBolumu;
import edu.marmara.readme.engine.model.Hoca;
import edu.marmara.readme.engine.model.HocaKisaRef;
import edu.marmara.readme.engine.model.HocalarBolumu;
import edu.marmara.readme.engine.util.HocaKisaltmaOlustur;
import edu.marmara.readme.engine.util.JsonUtil;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

/**
 * Python HocaDuzenlemeWindow'un Java/JavaFX portu — tek bir hocayı ekler/düzenler
 * ve dersler.json'daki dersi_veren_hocalar referanslarını senkronize eder.
 */
public class HocaDuzenlemeDialog {

    private final Stage owner;
    private final Hoca duzenlenecekHoca; // null ise "ekle" modu
    private final HocalarBolumu hocalarData;
    private final DerslerBolumu derslerData;
    private final Path derslerJsonYolu;
    private final Runnable onSaved;
    private final Consumer<Hoca> onDeleted;

    private ComboBox<String> unvanBox;
    private TextField adAlani;
    private TextField ofisAlani;
    private TextField linkAlani;
    private ComboBox<String> aktifBox;
    private ComboBox<String> erkekBox;
    private ObservableList<String> secilenDersler;
    private ListView<String> derslerListView;

    public HocaDuzenlemeDialog(Stage owner, Hoca hoca, HocalarBolumu hocalarData, DerslerBolumu derslerData,
                                 Path derslerJsonYolu, Runnable onSaved, Consumer<Hoca> onDeleted) {
        this.owner = owner;
        this.duzenlenecekHoca = hoca;
        this.hocalarData = hocalarData;
        this.derslerData = derslerData != null ? derslerData : new DerslerBolumu();
        this.derslerJsonYolu = derslerJsonYolu;
        this.onSaved = onSaved;
        this.onDeleted = onDeleted;
    }

    public void show() {
        Stage stage = new Stage();
        stage.initOwner(owner);
        stage.initModality(Modality.APPLICATION_MODAL);
        stage.setTitle(duzenlenecekHoca != null ? "Hoca Düzenle" : "Hoca Ekle");

        String mevcutAd = duzenlenecekHoca != null ? duzenlenecekHoca.ad : "";
        String[] ayiklanmis = ayiklaUnvan(mevcutAd);

        unvanBox = new ComboBox<>(FXCollections.observableArrayList(Sabitler.UNVANLAR));
        unvanBox.setValue(Sabitler.UNVANLAR.contains(ayiklanmis[1]) ? ayiklanmis[1] : Sabitler.UNVANLAR.get(0));

        adAlani = new TextField(ayiklanmis[0]);
        ofisAlani = new TextField(duzenlenecekHoca != null ? duzenlenecekHoca.ofis : "");
        linkAlani = new TextField(duzenlenecekHoca != null ? duzenlenecekHoca.link : "");

        aktifBox = new ComboBox<>(FXCollections.observableArrayList("Evet", "Hayır"));
        aktifBox.setValue(duzenlenecekHoca == null || duzenlenecekHoca.hoca_aktif_gorevde_mi ? "Evet" : "Hayır");

        erkekBox = new ComboBox<>(FXCollections.observableArrayList("Evet", "Hayır"));
        erkekBox.setValue(duzenlenecekHoca == null || duzenlenecekHoca.erkek_mi ? "Evet" : "Hayır");

        secilenDersler = FXCollections.observableArrayList(
                duzenlenecekHoca != null ? duzenlenecekHoca.dersler : List.of());
        derslerListView = new ListView<>(secilenDersler);
        derslerListView.setPrefHeight(140);

        Button dersEkleBtn = new Button("Hocanın Verdiği Ders Ekle");
        dersEkleBtn.setOnAction(e -> dersEkle());
        Button dersSilBtn = new Button("Seçili Dersi Kaldır");
        dersSilBtn.setOnAction(e -> {
            String secili = derslerListView.getSelectionModel().getSelectedItem();
            if (secili != null) {
                secilenDersler.remove(secili);
            }
        });

        Button kaydetBtn = new Button(duzenlenecekHoca != null ? "Değişiklikleri Kaydet" : "Ekle");
        kaydetBtn.setOnAction(e -> kaydet(stage));

        HBox altButonlar = new HBox(8, kaydetBtn);
        if (duzenlenecekHoca != null) {
            Button silBtn = new Button("Hocayı Sil");
            silBtn.setOnAction(e -> sil(stage));
            altButonlar.getChildren().add(silBtn);
        }

        VBox root = new VBox(6,
                new Label("Ünvan"), unvanBox,
                new Label("Ad"), adAlani,
                new Label("Ofis"), ofisAlani,
                new Label("Link"), linkAlani,
                new Label("Aktif görevde mi?"), aktifBox,
                new Label("Erkek mi?"), erkekBox,
                new Label("Hocanın Verdiği Dersler"), derslerListView,
                new HBox(8, dersEkleBtn, dersSilBtn),
                altButonlar);
        root.setPadding(new Insets(12));

        String ilkUnvan = unvanBox.getValue();
        String ilkAd = adAlani.getText();
        String ilkOfis = ofisAlani.getText();
        String ilkLink = linkAlani.getText();
        String ilkAktif = aktifBox.getValue();
        String ilkErkek = erkekBox.getValue();
        List<String> ilkDersler = new ArrayList<>(secilenDersler);
        CloseGuard.install(stage, () ->
                !unvanBox.getValue().equals(ilkUnvan) || !adAlani.getText().equals(ilkAd)
                        || !ofisAlani.getText().equals(ilkOfis) || !linkAlani.getText().equals(ilkLink)
                        || !aktifBox.getValue().equals(ilkAktif) || !erkekBox.getValue().equals(ilkErkek)
                        || !secilenDersler.equals(ilkDersler));

        stage.setScene(new Scene(root, 480, 640));
        stage.showAndWait();
    }

    private void dersEkle() {
        List<String> dersAdlari = new ArrayList<>();
        for (Ders d : derslerData.dersler) {
            dersAdlari.add(d.ad);
        }
        if (dersAdlari.isEmpty()) {
            Dialogs.error("Hata", "Herhangi bir ders bulunamadı. Lütfen önce ders ekleyin.");
            return;
        }
        Dialogs.choice("Ders Seç", "Dersi seçin:", dersAdlari, dersAdlari.get(0)).ifPresent(secilen -> {
            if (!secilenDersler.contains(secilen)) {
                secilenDersler.add(secilen);
            }
        });
    }

    private String[] ayiklaUnvan(String ad) {
        for (String unvan : Sabitler.UNVANLAR) {
            if (ad.startsWith(unvan)) {
                return new String[]{ad.substring(unvan.length() + 1), unvan};
            }
        }
        return new String[]{ad, ""};
    }

    private void kaydet(Stage stage) {
        String adTamam = unvanBox.getValue() + " " + adAlani.getText().strip();
        if (adAlani.getText().isBlank()) {
            Dialogs.error("Hata", "Ad alanı boş olamaz.");
            return;
        }
        boolean yeniHocaMi = duzenlenecekHoca == null;
        if (yeniHocaMi && hocalarData.hocalar.stream()
                .anyMatch(h -> h.ad.equalsIgnoreCase(adTamam))) {
            Dialogs.error("Hata", "Bu isimde bir hoca zaten var!");
            return;
        }

        Hoca hoca = yeniHocaMi ? new Hoca() : duzenlenecekHoca;
        hoca.ad = adTamam;
        hoca.ofis = ofisAlani.getText();
        hoca.link = linkAlani.getText();
        hoca.hoca_aktif_gorevde_mi = "Evet".equals(aktifBox.getValue());
        hoca.erkek_mi = "Evet".equals(erkekBox.getValue());
        hoca.dersler = new ArrayList<>(secilenDersler);

        if (yeniHocaMi) {
            hocalarData.hocalar.add(hoca);
        }

        dersiVerenHocalariSenkronizeEt(adTamam);

        onSaved.run();
        stage.close();
    }

    /** Python derslereHocayiEkle: seçilen derslere bu hocanın kısaltmasını ekler, seçilmeyenlerden çıkarır. */
    private void dersiVerenHocalariSenkronizeEt(String hocaAdi) {
        List<String> tumHocaAdlari = hocalarData.hocalar.stream().map(h -> h.ad).toList();
        Map<String, String> kisaltmalar = HocaKisaltmaOlustur.benzersizKisaltmalarUret(tumHocaAdlari);
        String kisaltma = kisaltmalar.get(hocaAdi);

        DerslerBolumu guncelDersler = JsonUtil.read(derslerJsonYolu, DerslerBolumu.class);
        if (guncelDersler == null) {
            return;
        }
        for (Ders ders : guncelDersler.dersler) {
            boolean secili = secilenDersler.contains(ders.ad);
            boolean mevcut = ders.dersi_veren_hocalar.stream().anyMatch(h -> h.ad.equals(hocaAdi));
            if (secili && !mevcut) {
                HocaKisaRef ref = new HocaKisaRef();
                ref.ad = hocaAdi;
                ref.kisaltma = kisaltma;
                ders.dersi_veren_hocalar.add(ref);
            } else if (secili && mevcut) {
                ders.dersi_veren_hocalar.stream().filter(h -> h.ad.equals(hocaAdi))
                        .forEach(h -> h.kisaltma = kisaltma);
            } else if (!secili && mevcut) {
                ders.dersi_veren_hocalar.removeIf(h -> h.ad.equals(hocaAdi));
            }
        }
        JsonUtil.write(derslerJsonYolu, guncelDersler);
    }

    private void sil(Stage stage) {
        if (!Dialogs.confirm("Onay", "Hocayı silmek istediğinden emin misin?")) {
            return;
        }
        onDeleted.accept(duzenlenecekHoca);
        stage.close();
    }
}
