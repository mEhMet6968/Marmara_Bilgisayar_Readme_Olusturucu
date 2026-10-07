package edu.marmara.readme.gui;

import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.util.ArrayList;
import java.util.List;

/** Python link_kontrol_window.py içindeki DiffWindow'un Java/JavaFX portu — satır bazlı, renkli, iki sütunlu fark görünümü. */
public class DiffWindow {

    private final String baslik;
    private final String eskiMetin;
    private final String yeniMetin;

    public DiffWindow(String baslik, String eskiMetin, String yeniMetin) {
        this.baslik = baslik;
        this.eskiMetin = eskiMetin == null ? "" : eskiMetin;
        this.yeniMetin = yeniMetin == null ? "" : yeniMetin;
    }

    public void show() {
        Stage stage = new Stage();
        stage.setTitle("Fark Penceresi — " + baslik);
        stage.initModality(Modality.APPLICATION_MODAL);

        String[] eskiSatirlar = eskiMetin.split("\n", -1);
        String[] yeniSatirlar = yeniMetin.split("\n", -1);
        boolean[][] lcs = ortakAltDizi(eskiSatirlar, yeniSatirlar);

        VBox solKolon = new VBox(1);
        VBox sagKolon = new VBox(1);
        yuruVeDoldur(eskiSatirlar, yeniSatirlar, lcs, solKolon, sagKolon);

        ScrollPane solScroll = new ScrollPane(solKolon);
        solScroll.setFitToWidth(true);
        ScrollPane sagScroll = new ScrollPane(sagKolon);
        sagScroll.setFitToWidth(true);

        VBox solKutu = new VBox(4, new Label("Orjinal Hali"), solScroll);
        VBox sagKutu = new VBox(4, new Label("Değişmiş Hali"), sagScroll);
        VBox.setVgrow(solScroll, Priority.ALWAYS);
        VBox.setVgrow(sagScroll, Priority.ALWAYS);

        HBox root = new HBox(10, solKutu, sagKutu);
        HBox.setHgrow(solKutu, Priority.ALWAYS);
        HBox.setHgrow(sagKutu, Priority.ALWAYS);
        root.setPadding(new Insets(10));

        stage.setScene(new Scene(root, 900, 600));
        stage.showAndWait();
    }

    /** Standart LCS tablosu (satır bazlı). */
    private boolean[][] ortakAltDizi(String[] a, String[] b) {
        int[][] dp = new int[a.length + 1][b.length + 1];
        for (int i = a.length - 1; i >= 0; i--) {
            for (int j = b.length - 1; j >= 0; j--) {
                dp[i][j] = a[i].equals(b[j]) ? dp[i + 1][j + 1] + 1 : Math.max(dp[i + 1][j], dp[i][j + 1]);
            }
        }
        List<int[]> eslesmeler = new ArrayList<>();
        int i = 0, j = 0;
        while (i < a.length && j < b.length) {
            if (a[i].equals(b[j])) {
                eslesmeler.add(new int[]{i, j});
                i++;
                j++;
            } else if (dp[i + 1][j] >= dp[i][j + 1]) {
                i++;
            } else {
                j++;
            }
        }
        boolean[][] matchSet = new boolean[a.length][b.length];
        for (int[] m : eslesmeler) {
            matchSet[m[0]][m[1]] = true;
        }
        return matchSet;
    }

    private void yuruVeDoldur(String[] a, String[] b, boolean[][] match, VBox sol, VBox sag) {
        int i = 0, j = 0;
        while (i < a.length || j < b.length) {
            boolean esleserMi = i < a.length && j < b.length && match[i][j];
            if (esleserMi) {
                sol.getChildren().add(satir(a[i], null));
                sag.getChildren().add(satir(b[j], null));
                i++;
                j++;
            } else {
                boolean solunEslesmesiVar = false;
                boolean sagInEslesmesiVar = false;
                if (i < a.length) {
                    for (int jj = j; jj < b.length; jj++) {
                        if (match[i][jj]) {
                            solunEslesmesiVar = true;
                            break;
                        }
                    }
                }
                if (j < b.length) {
                    for (int ii = i; ii < a.length; ii++) {
                        if (match[ii][j]) {
                            sagInEslesmesiVar = true;
                            break;
                        }
                    }
                }
                if (i < a.length && !solunEslesmesiVar) {
                    sol.getChildren().add(satir(a[i], "#FFCCCC"));
                    i++;
                } else if (j < b.length && !sagInEslesmesiVar) {
                    sag.getChildren().add(satir(b[j], "#CCFFCC"));
                    j++;
                } else if (i < a.length) {
                    sol.getChildren().add(satir(a[i], "#FFCCCC"));
                    i++;
                } else if (j < b.length) {
                    sag.getChildren().add(satir(b[j], "#CCFFCC"));
                    j++;
                }
            }
        }
    }

    private Label satir(String icerik, String renk) {
        Label label = new Label(icerik.isEmpty() ? " " : icerik);
        label.setMaxWidth(Double.MAX_VALUE);
        label.setWrapText(true);
        String arkaPlan = renk != null ? "-fx-background-color: " + renk + ";" : "";
        label.setStyle("-fx-font-family: monospace; " + arkaPlan);
        return label;
    }
}
