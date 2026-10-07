package edu.marmara.readme.gui;

import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

/** Python link_kontrol_window.py'nin Java/JavaFX portu — hoca/katkıda bulunan linklerinin kırık olup olmadığını kontrol eder. */
public class LinkKontrolWindow {

    public record SahipLink(String sahipAdi, String url) {
    }

    private final List<SahipLink> linkler;
    private final String baslik;
    private volatile boolean durduruldu = false;

    public LinkKontrolWindow(List<SahipLink> linkler, String baslik) {
        this.linkler = linkler;
        this.baslik = baslik;
    }

    public void show() {
        Stage stage = new Stage();
        stage.setTitle(baslik);
        stage.initModality(Modality.APPLICATION_MODAL);

        ProgressBar progressBar = new ProgressBar(0);
        progressBar.setPrefWidth(Double.MAX_VALUE);
        Label durumLabel = new Label("Hazır. " + linkler.size() + " link kontrol edilecek.");

        ListView<String> sonucListView = new ListView<>();
        sonucListView.setOnMouseClicked(e -> {
            if (e.getClickCount() == 2) {
                String secili = sonucListView.getSelectionModel().getSelectedItem();
                if (secili != null) {
                    copyToClipboard(secili);
                }
            }
        });

        Button baslatBtn = new Button("🔍 Kontrolü Başlat");
        Button durdurBtn = new Button("Durdur");
        durdurBtn.setDisable(true);
        Button kopyalaTumBtn = new Button("Kırık Linkleri Kopyala");
        kopyalaTumBtn.setOnAction(e -> {
            StringBuilder sb = new StringBuilder();
            for (String s : sonucListView.getItems()) {
                if (s.startsWith("❌")) {
                    sb.append(s).append("\n");
                }
            }
            copyToClipboard(sb.toString());
            Dialogs.info("Kopyalandı", "Kırık linkler panoya kopyalandı.");
        });

        baslatBtn.setOnAction(e -> {
            baslatBtn.setDisable(true);
            durdurBtn.setDisable(false);
            durduruldu = false;
            sonucListView.getItems().clear();
            AtomicInteger tamamlanan = new AtomicInteger(0);

            Thread thread = new Thread(() -> {
                HttpClient client = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(8)).build();
                for (SahipLink sl : linkler) {
                    if (durduruldu) {
                        break;
                    }
                    String sonucSatiri = kontrolEt(client, sl);
                    int tamam = tamamlanan.incrementAndGet();
                    Platform.runLater(() -> {
                        sonucListView.getItems().add(sonucSatiri);
                        progressBar.setProgress((double) tamam / linkler.size());
                        durumLabel.setText(tamam + " / " + linkler.size() + " kontrol edildi.");
                    });
                }
                Platform.runLater(() -> {
                    baslatBtn.setDisable(false);
                    durdurBtn.setDisable(true);
                    durumLabel.setText("Tamamlandı: " + tamamlanan.get() + " / " + linkler.size());
                });
            }, "link-kontrol");
            thread.setDaemon(true);
            thread.start();
        });

        durdurBtn.setOnAction(e -> durduruldu = true);

        VBox root = new VBox(8, durumLabel, progressBar, new HBox(8, baslatBtn, durdurBtn, kopyalaTumBtn),
                new Label("Sonuçlar (çift tıkla: kopyala):"), sonucListView);
        root.setPadding(new Insets(10));
        VBox.setVgrow(sonucListView, Priority.ALWAYS);

        stage.setScene(new Scene(root, 620, 520));
        stage.showAndWait();
    }

    private String kontrolEt(HttpClient client, SahipLink sl) {
        if (sl.url() == null || sl.url().isBlank()) {
            return "⚠️ " + sl.sahipAdi() + " -> (link boş)";
        }
        try {
            HttpRequest request = HttpRequest.newBuilder(URI.create(sl.url()))
                    .method("HEAD", HttpRequest.BodyPublishers.noBody())
                    .timeout(Duration.ofSeconds(8))
                    .build();
            HttpResponse<Void> response = client.send(request, HttpResponse.BodyHandlers.discarding());
            int kod = response.statusCode();
            if (kod >= 200 && kod < 400) {
                return "✅ " + sl.sahipAdi() + " -> " + sl.url() + " (HTTP " + kod + ")";
            }
            return "❌ " + sl.sahipAdi() + " -> " + sl.url() + " (HTTP " + kod + ")";
        } catch (Exception e) {
            return "❌ " + sl.sahipAdi() + " -> " + sl.url() + " (Hata: " + e.getClass().getSimpleName() + ")";
        }
    }

    private void copyToClipboard(String text) {
        ClipboardContent content = new ClipboardContent();
        content.putString(text);
        Clipboard.getSystemClipboard().setContent(content);
    }
}
