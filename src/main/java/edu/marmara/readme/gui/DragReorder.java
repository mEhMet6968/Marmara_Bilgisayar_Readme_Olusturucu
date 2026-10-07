package edu.marmara.readme.gui;

import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.Dragboard;
import javafx.scene.input.TransferMode;

import java.util.List;
import java.util.function.Consumer;

/**
 * Python helpers/surukleme_listesi.py'nin (SuruklemeListe) basitleştirilmiş Java/JavaFX portu —
 * bir ListView&lt;String&gt; içinde sürükle-bırak ile yeniden sıralama sağlar.
 */
public final class DragReorder {

    private DragReorder() {
    }

    /** listView üzerinde sürükle-bırak sıralamayı etkinleştirir; her başarılı sıralamadan sonra onReordered çağrılır. */
    public static void enable(ListView<String> listView, Consumer<List<String>> onReordered) {
        listView.setCellFactory(lv -> {
            ListCell<String> cell = new ListCell<>() {
                @Override
                protected void updateItem(String item, boolean empty) {
                    super.updateItem(item, empty);
                    setText(empty || item == null ? null : item);
                }
            };

            cell.setOnDragDetected(event -> {
                if (cell.getItem() == null) {
                    return;
                }
                Dragboard db = cell.startDragAndDrop(TransferMode.MOVE);
                ClipboardContent cc = new ClipboardContent();
                cc.putString(String.valueOf(cell.getIndex()));
                db.setContent(cc);
                event.consume();
            });

            cell.setOnDragOver(event -> {
                if (event.getGestureSource() != cell && event.getDragboard().hasString()) {
                    event.acceptTransferModes(TransferMode.MOVE);
                }
                event.consume();
            });

            cell.setOnDragDropped(event -> {
                if (cell.getItem() == null) {
                    event.setDropCompleted(false);
                    event.consume();
                    return;
                }
                Dragboard db = event.getDragboard();
                boolean success = false;
                if (db.hasString()) {
                    int draggedIdx = Integer.parseInt(db.getString());
                    int thisIdx = cell.getIndex();
                    if (draggedIdx != thisIdx) {
                        String draggedItem = listView.getItems().remove(draggedIdx);
                        listView.getItems().add(thisIdx, draggedItem);
                        onReordered.accept(List.copyOf(listView.getItems()));
                    }
                    success = true;
                }
                event.setDropCompleted(success);
                event.consume();
            });

            return cell;
        });
    }
}
