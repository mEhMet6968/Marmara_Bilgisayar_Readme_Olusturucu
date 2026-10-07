package edu.marmara.readme.gui;

import javafx.animation.FadeTransition;
import javafx.animation.PauseTransition;
import javafx.animation.SequentialTransition;
import javafx.geometry.Insets;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;
import javafx.stage.Popup;
import javafx.stage.Window;
import javafx.util.Duration;

/** Python toast_notification.py'nin Java/JavaFX portu — sahibin üstünde beliren, kendiliğinden kapanan bildirim. */
public final class Toast {

    public enum Tip { BASARI, HATA, UYARI, BILGI }

    private Toast() {
    }

    public static void show(Window owner, String message, Tip tip) {
        show(owner, message, tip, 2500);
    }

    public static void show(Window owner, String message, Tip tip, int durationMs) {
        Label label = new Label(message);
        label.setStyle("-fx-background-color: " + renk(tip) + "; -fx-text-fill: white; "
                + "-fx-padding: 10 18 10 18; -fx-background-radius: 6; -fx-font-size: 13px;");
        StackPane pane = new StackPane(label);
        pane.setPadding(new Insets(4));

        Popup popup = new Popup();
        popup.setAutoFix(true);
        popup.getContent().add(pane);
        popup.setOpacity(0);

        double x = owner.getX() + owner.getWidth() / 2.0;
        double y = owner.getY() + 60;
        popup.show(owner, x - 120, y);

        FadeTransition fadeIn = new FadeTransition(Duration.millis(200), pane);
        fadeIn.setFromValue(0);
        fadeIn.setToValue(1);
        popup.setOpacity(1);

        PauseTransition bekle = new PauseTransition(Duration.millis(durationMs));

        FadeTransition fadeOut = new FadeTransition(Duration.millis(400), pane);
        fadeOut.setFromValue(1);
        fadeOut.setToValue(0);
        fadeOut.setOnFinished(e -> popup.hide());

        new SequentialTransition(fadeIn, bekle, fadeOut).play();
    }

    private static String renk(Tip tip) {
        return switch (tip) {
            case BASARI -> "#27AE60";
            case HATA -> "#C0392B";
            case UYARI -> "#F39C12";
            case BILGI -> "#2980B9";
        };
    }
}
