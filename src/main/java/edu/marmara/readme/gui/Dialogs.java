package edu.marmara.readme.gui;

import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ChoiceDialog;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextInputDialog;
import javafx.scene.control.Dialog;
import javafx.scene.layout.VBox;

import java.util.List;
import java.util.Optional;

/** Ortak Alert/Dialog yardımcıları — Python tarafındaki QMessageBox/QInputDialog kullanım kalıplarının karşılığı. */
public final class Dialogs {

    private Dialogs() {
    }

    public static boolean confirm(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION, message, ButtonType.YES, ButtonType.NO);
        alert.setTitle(title);
        alert.setHeaderText(null);
        Optional<ButtonType> sonuc = alert.showAndWait();
        return sonuc.isPresent() && sonuc.get() == ButtonType.YES;
    }

    public static void error(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR, message, ButtonType.OK);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.showAndWait();
    }

    public static void info(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION, message, ButtonType.OK);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.showAndWait();
    }

    public static Optional<String> textInput(String title, String label, String initial) {
        TextInputDialog dialog = new TextInputDialog(initial == null ? "" : initial);
        dialog.setTitle(title);
        dialog.setHeaderText(null);
        dialog.setContentText(label);
        return dialog.showAndWait();
    }

    /** Çok satırlı metin girişi; dönüşte metni ve OK/Cancel sonucunu taşır. */
    public static Optional<String> multilineInput(String title, String label, String initial) {
        Dialog<String> dialog = new Dialog<>();
        dialog.setTitle(title);
        dialog.setHeaderText(label);
        TextArea area = new TextArea(initial == null ? "" : initial);
        area.setWrapText(true);
        area.setPrefRowCount(10);
        area.setPrefColumnCount(50);
        VBox box = new VBox(8, area);
        dialog.getDialogPane().setContent(box);
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);
        dialog.setResultConverter(bt -> bt == ButtonType.OK ? area.getText() : null);
        return dialog.showAndWait();
    }

    public static Optional<String> choice(String title, String label, List<String> secenekler, String secili) {
        ChoiceDialog<String> dialog = new ChoiceDialog<>(secili, secenekler);
        dialog.setTitle(title);
        dialog.setHeaderText(null);
        dialog.setContentText(label);
        return dialog.showAndWait();
    }
}
