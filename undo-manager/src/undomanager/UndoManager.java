package undomanager;

import undomanager.command.Command;
import undomanager.command.MergeableCommand;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;

/**
 * Executes commands and maintains undo/redo history.
 */
public class UndoManager {

    private static final int DEFAULT_MAX_HISTORY_SIZE = 200;

    private final Deque<Command> undoStack = new ArrayDeque<>();
    private final Deque<Command> redoStack = new ArrayDeque<>();
    private final List<UndoRedoListener> listeners = new ArrayList<>();

    private int maxHistorySize = DEFAULT_MAX_HISTORY_SIZE;

    public void executeCommand(Command command) {
        if (command == null) {
            throw new IllegalArgumentException("command must not be null");
        }

        if (!undoStack.isEmpty() && undoStack.peek() instanceof MergeableCommand) {
            MergeableCommand previous = (MergeableCommand) undoStack.peek();
            if (previous.canMergeWith(command)) {
                command.execute();
                previous.mergeWith(command);
                redoStack.clear();
                notifyListeners();
                return;
            }
        }

        command.execute();
        undoStack.push(command);
        redoStack.clear();
        trimHistory();
        notifyListeners();
    }

    public boolean canUndo() {
        return !undoStack.isEmpty();
    }

    public boolean canRedo() {
        return !redoStack.isEmpty();
    }

    public void undo() {
        if (!canUndo()) {
            return;
        }

        Command command = undoStack.pop();
        command.undo();
        redoStack.push(command);
        notifyListeners();
    }

    public void redo() {
        if (!canRedo()) {
            return;
        }

        Command command = redoStack.pop();
        command.redo();
        undoStack.push(command);
        notifyListeners();
    }

    public void clearHistory() {
        undoStack.clear();
        redoStack.clear();
        notifyListeners();
    }

    public void setMaxHistorySize(int maxHistorySize) {
        this.maxHistorySize = maxHistorySize;
        trimHistory();
        notifyListeners();
    }

    public int getMaxHistorySize() {
        return maxHistorySize;
    }

    public void addListener(UndoRedoListener listener) {
        if (listener == null) {
            return;
        }
        listeners.add(listener);
        listener.onStateChanged(canUndo(), canRedo(), getUndoDescription(), getRedoDescription());
    }

    public void removeListener(UndoRedoListener listener) {
        listeners.remove(listener);
    }

    private String getUndoDescription() {
        return canUndo() ? undoStack.peek().getDescription() : null;
    }

    private String getRedoDescription() {
        return canRedo() ? redoStack.peek().getDescription() : null;
    }

    private void notifyListeners() {
        boolean canUndo = canUndo();
        boolean canRedo = canRedo();
        String undoDescription = getUndoDescription();
        String redoDescription = getRedoDescription();

        for (UndoRedoListener listener : listeners) {
            listener.onStateChanged(canUndo, canRedo, undoDescription, redoDescription);
        }
    }

    private void trimHistory() {
        if (maxHistorySize <= 0) {
            return;
        }

        while (undoStack.size() > maxHistorySize) {
            undoStack.removeLast();
        }
    }
}
