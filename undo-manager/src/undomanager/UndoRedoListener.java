package undomanager;

/**
 * Implement this to keep UI in sync with the undo/redo stacks — e.g. graying
 * out the Undo/Redo buttons, or setting menu text to "Undo Typing 'hello'".
 */
public interface UndoRedoListener {
    void onStateChanged(boolean canUndo, boolean canRedo, String undoDescription, String redoDescription);
}
