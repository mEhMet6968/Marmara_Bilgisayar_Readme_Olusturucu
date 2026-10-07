package edu.marmara.readme.gui;

import javafx.stage.Stage;

import java.util.function.BooleanSupplier;

/** Python close_event.py'nin (closeEventHandler) Java/JavaFX portu — kaydedilmemiş değişiklikle kapatmaya karşı uyarır. */
public final class CloseGuard {

    private CloseGuard() {
    }

    /** stage kapatılmak istendiğinde hasChanges true dönerse onay ister; "Hayır" denirse kapanmayı iptal eder. */
    public static void install(Stage stage, BooleanSupplier hasChanges) {
        stage.setOnCloseRequest(event -> {
            if (hasChanges.getAsBoolean()) {
                boolean devamEt = Dialogs.confirm("Kaydedilmemiş Değişiklikler",
                        "Değişiklikler kaydedilmedi. Çıkmak istediğinize emin misiniz?");
                if (!devamEt) {
                    event.consume();
                }
            }
        });
    }
}
