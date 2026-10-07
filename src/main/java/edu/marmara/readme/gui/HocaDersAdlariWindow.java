package edu.marmara.readme.gui;

import edu.marmara.readme.engine.ReadmeGenerator;
import edu.marmara.readme.engine.model.DerslerBolumu;
import edu.marmara.readme.engine.model.HocalarBolumu;
import edu.marmara.readme.engine.util.JsonUtil;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.nio.file.Path;
import java.text.Normalizer;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

/** Python hoca_ve_ders_adlari_window.py'nin Java/JavaFX portu — Google Form'lara eklemek için hoca/ders adlarını kopyalar. */
public class HocaDersAdlariWindow {

    private final Path jsonDeposu;

    public HocaDersAdlariWindow(Path jsonDeposu) {
        this.jsonDeposu = jsonDeposu;
    }

    public void show() {
        Stage stage = new Stage();
        stage.setTitle("Hoca ve Dersler Listesi");
        stage.initModality(Modality.APPLICATION_MODAL);

        HocalarBolumu hocalar = JsonUtil.read(jsonDeposu.resolve(ReadmeGenerator.HOCALAR_JSON), HocalarBolumu.class);
        DerslerBolumu dersler = JsonUtil.read(jsonDeposu.resolve(ReadmeGenerator.DERSLER_JSON), DerslerBolumu.class);

        List<String> hocaAdlari = hocalar == null ? List.of() : hocalar.hocalar.stream()
                .map(h -> h.ad).sorted(turkceSirala()).collect(Collectors.toList());
        List<String> dersAdlari = dersler == null ? List.of() : dersler.dersler.stream()
                .map(d -> d.ad).sorted(turkceSirala()).collect(Collectors.toList());

        ListView<String> hocaListView = new ListView<>(javafx.collections.FXCollections.observableArrayList(hocaAdlari));
        ListView<String> dersListView = new ListView<>(javafx.collections.FXCollections.observableArrayList(dersAdlari));

        Button hocaKopyalaBtn = new Button("Hocaları Kopyala");
        hocaKopyalaBtn.setOnAction(e -> kopyala(hocaAdlari));
        Button dersKopyalaBtn = new Button("Dersleri Kopyala");
        dersKopyalaBtn.setOnAction(e -> kopyala(dersAdlari));

        VBox sol = new VBox(8, new Label("Hocalar (" + hocaAdlari.size() + ")"), hocaListView, hocaKopyalaBtn);
        VBox sag = new VBox(8, new Label("Dersler (" + dersAdlari.size() + ")"), dersListView, dersKopyalaBtn);
        VBox.setVgrow(hocaListView, Priority.ALWAYS);
        VBox.setVgrow(dersListView, Priority.ALWAYS);

        HBox root = new HBox(16, sol, sag);
        root.setPadding(new Insets(10));
        HBox.setHgrow(sol, Priority.ALWAYS);
        HBox.setHgrow(sag, Priority.ALWAYS);

        stage.setScene(new Scene(root, 700, 520));
        stage.showAndWait();
    }

    private Comparator<String> turkceSirala() {
        return Comparator.comparing(s -> Normalizer.normalize(s == null ? "" : s, Normalizer.Form.NFKD).toLowerCase(Locale.ROOT));
    }

    private void kopyala(List<String> adlar) {
        ClipboardContent content = new ClipboardContent();
        content.putString(String.join("\n", adlar));
        Clipboard.getSystemClipboard().setContent(content);
        Dialogs.info("Kopyalandı", adlar.size() + " ad panoya kopyalandı.");
    }
}
