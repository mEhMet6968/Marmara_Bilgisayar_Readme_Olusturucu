package edu.marmara.readme.gui;

import edu.marmara.readme.engine.Sabitler;
import edu.marmara.readme.engine.model.*;
import edu.marmara.readme.engine.util.HocaKisaltmaOlustur;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

/** Python DersEkleGuncelleWindow içindeki ders ekleme/düzenleme alt diyaloğunun Java/JavaFX portu. */
public class DersDuzenlemeDialog {

    private final Stage owner;
    private final Ders duzenlenecekDers; // null ise "ekle" modu
    private final DerslerBolumu derslerData;
    private final HocalarBolumu hocalarData;
    private final Runnable onSaved;
    private final Consumer<Ders> onDeleted;

    private TextField adAlani;
    private ComboBox<Integer> yilBox;
    private ComboBox<String> donemBox;
    private ComboBox<String> tipBox;
    private ComboBox<String> guncelBox;
    private ObservableList<String> secilenHocalar;
    private ListView<String> hocalarListView;
    private final List<CheckBox> mufredatKutulari = new ArrayList<>();
    private ObservableList<String> kaynaklar;
    private ListView<String> kaynaklarListView;
    private final List<String[]> oneriCiftleri = new ArrayList<>(); // [sahibi, oneri]
    private ObservableList<String> oneriGosterim;
    private ListView<String> oneriListView;

    public DersDuzenlemeDialog(Stage owner, Ders ders, DerslerBolumu derslerData, HocalarBolumu hocalarData,
                                 Runnable onSaved, Consumer<Ders> onDeleted) {
        this.owner = owner;
        this.duzenlenecekDers = ders;
        this.derslerData = derslerData;
        this.hocalarData = hocalarData != null ? hocalarData : new HocalarBolumu();
        this.onSaved = onSaved;
        this.onDeleted = onDeleted;
    }

    public void show() {
        Stage stage = new Stage();
        stage.initOwner(owner);
        stage.initModality(Modality.APPLICATION_MODAL);
        stage.setTitle(duzenlenecekDers != null ? "Ders Düzenle" : "Ders Ekle");

        adAlani = new TextField(duzenlenecekDers != null ? duzenlenecekDers.ad : "");

        yilBox = new ComboBox<>(FXCollections.observableArrayList(0, 1, 2, 3, 4));
        yilBox.setValue(duzenlenecekDers != null ? duzenlenecekDers.yil : 0);

        donemBox = new ComboBox<>(FXCollections.observableArrayList(Sabitler.DONEMLER_DIZISI_YOKLA_BERABER));
        String mevcutDonem = duzenlenecekDers != null && duzenlenecekDers.donem != null && !duzenlenecekDers.donem.isEmpty()
                ? duzenlenecekDers.donem : Sabitler.YOK;
        donemBox.setValue(mevcutDonem);

        tipBox = new ComboBox<>(FXCollections.observableArrayList(Sabitler.DERS_TIPLERI));
        tipBox.setEditable(true);
        tipBox.setValue(duzenlenecekDers != null ? duzenlenecekDers.tip : Sabitler.DERS_TIPLERI.get(0));

        guncelBox = new ComboBox<>(FXCollections.observableArrayList("Evet", "Hayır"));
        guncelBox.setValue(duzenlenecekDers == null || duzenlenecekDers.guncel_mi ? "Evet" : "Hayır");

        secilenHocalar = FXCollections.observableArrayList();
        if (duzenlenecekDers != null) {
            for (HocaKisaRef ref : duzenlenecekDers.dersi_veren_hocalar) {
                secilenHocalar.add(ref.ad);
            }
        }
        hocalarListView = new ListView<>(secilenHocalar);
        hocalarListView.setPrefHeight(90);
        Button hocaEkleBtn = new Button("Dersi Veren Hoca Ekle");
        hocaEkleBtn.setOnAction(e -> hocaEkle());
        Button hocaSilBtn = new Button("Seçili Hocayı Kaldır");
        hocaSilBtn.setOnAction(e -> {
            String secili = hocalarListView.getSelectionModel().getSelectedItem();
            if (secili != null) {
                secilenHocalar.remove(secili);
            }
        });

        VBox mufredatKutusu = new VBox(4);
        List<String> baslangicMufredat = duzenlenecekDers != null ? duzenlenecekDers.mufredatlar : List.of();
        for (String yil : Sabitler.BILINEN_MUFREDATLAR) {
            CheckBox cb = new CheckBox(yil);
            cb.setSelected(baslangicMufredat.contains(yil));
            mufredatKutulari.add(cb);
            mufredatKutusu.getChildren().add(cb);
        }
        Button yeniMufredatBtn = new Button("Yeni Müfredat Yılı Ekle");
        yeniMufredatBtn.setOnAction(e -> Dialogs.textInput("Müfredat Yılı", "Örn: 2029", "").ifPresent(yeni -> {
            if (!yeni.isBlank() && !Sabitler.BILINEN_MUFREDATLAR.contains(yeni)) {
                Sabitler.BILINEN_MUFREDATLAR.add(yeni);
                CheckBox cb = new CheckBox(yeni);
                cb.setSelected(true);
                mufredatKutulari.add(cb);
                mufredatKutusu.getChildren().add(cb);
            }
        }));

        kaynaklar = FXCollections.observableArrayList(
                duzenlenecekDers != null ? duzenlenecekDers.faydali_olabilecek_kaynaklar : List.of());
        kaynaklarListView = new ListView<>(kaynaklar);
        kaynaklarListView.setPrefHeight(90);
        Button kaynakEkleBtn = new Button("Kaynak Ekle");
        kaynakEkleBtn.setOnAction(e -> Dialogs.multilineInput("Kaynak Ekle",
                "Kaynak (markdown link içerebilir, örn: [Başlık](./dosya.pdf)):", "").ifPresent(yeni -> {
            if (!yeni.isBlank()) {
                kaynaklar.add(yeni);
            }
        }));
        Button kaynakSilBtn = new Button("Seçili Kaynağı Kaldır");
        kaynakSilBtn.setOnAction(e -> {
            String secili = kaynaklarListView.getSelectionModel().getSelectedItem();
            if (secili != null) {
                kaynaklar.remove(secili);
            }
        });

        if (duzenlenecekDers != null) {
            for (OneriGrubu grup : duzenlenecekDers.derse_dair_oneriler) {
                for (String oneri : grup.oneriler) {
                    oneriCiftleri.add(new String[]{grup.oneri_sahibi, oneri});
                }
            }
        }
        oneriGosterim = FXCollections.observableArrayList();
        oneriCiftleri.forEach(p -> oneriGosterim.add(p[0] + ": " + p[1]));
        oneriListView = new ListView<>(oneriGosterim);
        oneriListView.setPrefHeight(90);
        Button oneriEkleBtn = new Button("Öneri Ekle");
        oneriEkleBtn.setOnAction(e -> oneriEkle());
        Button oneriSilBtn = new Button("Seçili Öneriyi Kaldır");
        oneriSilBtn.setOnAction(e -> {
            int idx = oneriListView.getSelectionModel().getSelectedIndex();
            if (idx >= 0) {
                oneriCiftleri.remove(idx);
                oneriGosterim.remove(idx);
            }
        });

        Button kaydetBtn = new Button(duzenlenecekDers != null ? "Değişiklikleri Kaydet" : "Ekle");
        kaydetBtn.setOnAction(e -> kaydet(stage));

        HBox altButonlar = new HBox(8, kaydetBtn);
        if (duzenlenecekDers != null) {
            Button silBtn = new Button("Dersi Sil");
            silBtn.setOnAction(e -> sil(stage));
            altButonlar.getChildren().add(silBtn);
        }

        VBox root = new VBox(6,
                new Label("Ders Adı"), adAlani,
                new Label("Yıl"), yilBox,
                new Label("Dönem"), donemBox,
                new Label("Tip"), tipBox,
                new Label("Güncel Mi?"), guncelBox,
                new Label("Dersi Veren Hocalar"), hocalarListView, new HBox(8, hocaEkleBtn, hocaSilBtn),
                new Label("Müfredatlar (Ortak Ders rozeti için birden fazla seçin)"), mufredatKutusu, yeniMufredatBtn,
                new Label("Faydalı Olabilecek Kaynaklar"), kaynaklarListView, new HBox(8, kaynakEkleBtn, kaynakSilBtn),
                new Label("Derse Dair Öneriler"), oneriListView, new HBox(8, oneriEkleBtn, oneriSilBtn),
                altButonlar);
        root.setPadding(new Insets(12));

        String ilkAd = adAlani.getText();
        Integer ilkYil = yilBox.getValue();
        String ilkDonem = donemBox.getValue();
        String ilkTip = tipBox.getValue();
        String ilkGuncel = guncelBox.getValue();
        List<String> ilkHocalar = new ArrayList<>(secilenHocalar);
        List<String> ilkKaynaklar = new ArrayList<>(kaynaklar);
        List<String> ilkOneriler = new ArrayList<>(oneriGosterim);
        List<Boolean> ilkMufredat = mufredatKutulari.stream().map(CheckBox::isSelected).toList();
        CloseGuard.install(stage, () ->
                !adAlani.getText().equals(ilkAd) || !yilBox.getValue().equals(ilkYil)
                        || !donemBox.getValue().equals(ilkDonem) || !tipBox.getValue().equals(ilkTip)
                        || !guncelBox.getValue().equals(ilkGuncel) || !secilenHocalar.equals(ilkHocalar)
                        || !kaynaklar.equals(ilkKaynaklar) || !oneriGosterim.equals(ilkOneriler)
                        || !mufredatKutulari.stream().map(CheckBox::isSelected).toList().equals(ilkMufredat));

        ScrollPane scroll = new ScrollPane(root);
        scroll.setFitToWidth(true);
        stage.setScene(new Scene(scroll, 560, 780));
        stage.showAndWait();
    }

    private void hocaEkle() {
        List<String> adlar = hocalarData.hocalar.stream().map(h -> h.ad).toList();
        if (adlar.isEmpty()) {
            Dialogs.error("Hata", "Herhangi bir hoca bulunamadı. Lütfen önce hoca ekleyin.");
            return;
        }
        Dialogs.choice("Hoca Seç", "Hocayı seçin:", adlar, adlar.get(0)).ifPresent(secilen -> {
            if (!secilenHocalar.contains(secilen)) {
                secilenHocalar.add(secilen);
            }
        });
    }

    private void oneriEkle() {
        Dialogs.textInput("Öneri Sahibi", "Öneri sahibinin adı/nicki:", "Anonim").ifPresent(sahibi ->
                Dialogs.multilineInput("Öneri", "Dersle ilgili öneri:", "").ifPresent(metin -> {
                    if (!metin.isBlank()) {
                        oneriCiftleri.add(new String[]{sahibi.isBlank() ? "Anonim" : sahibi, metin});
                        oneriGosterim.add((sahibi.isBlank() ? "Anonim" : sahibi) + ": " + metin);
                    }
                }));
    }

    private void kaydet(Stage stage) {
        String ad = adAlani.getText().strip();
        if (ad.isEmpty()) {
            Dialogs.error("Hata", "Ders adı boş olamaz.");
            return;
        }
        boolean yeniDersMi = duzenlenecekDers == null;
        if (yeniDersMi && derslerData.dersler.stream().anyMatch(d -> d.ad.equalsIgnoreCase(ad))) {
            Dialogs.error("Hata", "Bu isimde bir ders zaten var!");
            return;
        }

        Ders ders = yeniDersMi ? new Ders() : duzenlenecekDers;
        ders.ad = ad;
        ders.yil = yilBox.getValue();
        ders.donem = Sabitler.YOK.equals(donemBox.getValue()) ? "" : donemBox.getValue();
        ders.tip = tipBox.getValue();
        ders.guncel_mi = "Evet".equals(guncelBox.getValue());

        Map<String, String> kisaltmalar = HocaKisaltmaOlustur.benzersizKisaltmalarUret(
                hocalarData.hocalar.stream().map(h -> h.ad).toList());
        ders.dersi_veren_hocalar = new ArrayList<>();
        for (String hocaAdi : secilenHocalar) {
            HocaKisaRef ref = new HocaKisaRef();
            ref.ad = hocaAdi;
            ref.kisaltma = kisaltmalar.getOrDefault(hocaAdi, HocaKisaltmaOlustur.hocaKisaltmaOlustur(hocaAdi));
            ders.dersi_veren_hocalar.add(ref);
        }

        List<String> secilenMufredatlar = new ArrayList<>();
        for (CheckBox cb : mufredatKutulari) {
            if (cb.isSelected()) {
                secilenMufredatlar.add(cb.getText());
            }
        }
        ders.mufredatlar = secilenMufredatlar;

        ders.faydali_olabilecek_kaynaklar = new ArrayList<>(kaynaklar);

        Map<String, OneriGrubu> gruplar = new LinkedHashMap<>();
        for (String[] cift : oneriCiftleri) {
            OneriGrubu grup = gruplar.computeIfAbsent(cift[0], sahibi -> {
                OneriGrubu g = new OneriGrubu();
                g.oneri_sahibi = sahibi;
                return g;
            });
            grup.oneriler.add(cift[1]);
        }
        ders.derse_dair_oneriler = new ArrayList<>(gruplar.values());

        if (yeniDersMi) {
            derslerData.dersler.add(ders);
        }

        onSaved.run();
        stage.close();
    }

    private void sil(Stage stage) {
        if (!Dialogs.confirm("Onay", "Dersi silmek istediğinden emin misin?")) {
            return;
        }
        onDeleted.accept(duzenlenecekDers);
        stage.close();
    }
}
