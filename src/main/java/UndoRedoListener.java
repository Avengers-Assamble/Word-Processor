//afrida's code (package line removed so it links with the editor classes)

public interface UndoRedoListener {
    void onStateChanged(boolean canUndo, boolean canRedo, String undoDescription, String redoDescription);
}