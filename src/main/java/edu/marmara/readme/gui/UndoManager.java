package edu.marmara.readme.gui;

import java.util.ArrayDeque;
import java.util.Deque;

/** Python undo_manager.py'nin Java portu — son 10 silinen öğeyi tutar, Ctrl+Z ile geri alınır. */
public class UndoManager<T> {

    public record SilinenOge<T>(int index, T item) {
    }

    private final int maxUndoCount;
    private final Deque<SilinenOge<T>> yigin = new ArrayDeque<>();

    public UndoManager() {
        this(10);
    }

    public UndoManager(int maxUndoCount) {
        this.maxUndoCount = maxUndoCount;
    }

    public void pushDeleted(int index, T item) {
        if (yigin.size() >= maxUndoCount) {
            yigin.removeLast();
        }
        yigin.push(new SilinenOge<>(index, item));
    }

    public SilinenOge<T> popDeleted() {
        return yigin.isEmpty() ? null : yigin.pop();
    }

    public boolean canUndo() {
        return !yigin.isEmpty();
    }

    public void clear() {
        yigin.clear();
    }

    public int undoCount() {
        return yigin.size();
    }
}
