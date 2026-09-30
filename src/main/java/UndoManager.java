// afrida's code
// EDITED: package/imports removed, and linked with TextEditor (so it works on
// aarohi's document/paragraph/textrun through the editor). Her original undo/redo
// logic (stacks, merging, listeners, history limit) is unchanged.

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;

public class UndoManager {

    private final Deque<Command> undoStack = new ArrayDeque<>();
    private final Deque<Command> redoStack = new ArrayDeque<>();
    private final List<UndoRedoListener> listeners = new ArrayList<>();

    private int maxHistorySize = 200;

    // ---- link to the editor (new) ----
    private final TextEditor editor;

    public UndoManager(TextEditor editor) {
        this.editor = editor;
    }

    public TextEditor getEditor() {
        return editor;
    }

    // =====================================================
    //  Undoable editing (new): use these instead of calling
    //  the TextEditor directly, so every change can be undone.
    //  Moving the caret, selecting and copying don't change the
    //  document, so call those on the TextEditor itself.
    // =====================================================

    public void type(String text) {
        if (text == null || text.isEmpty()) return;
        boolean plainTyping = text.indexOf('\n') < 0;
        executeCommand(new EditCommand(editor, plainTyping ? "Typing" : "Type text",
                plainTyping, text, () -> editor.insertText(text)));
    }

    public void newLine() {
        executeCommand(new EditCommand(editor, "New line", false, null, editor::insertNewLine));
    }

    public void backspace() {
        boolean atStart = editor.getSelection().isEmpty()
                && editor.getCursor().equals(new Cursor(0, 0));
        if (atStart) return; // nothing to delete
        executeCommand(new EditCommand(editor, "Delete", false, null, editor::deleteBackward));
    }

    public void deleteKey() {
        if (editor.getSelection().isEmpty() && isCaretAtDocumentEnd()) return;
        executeCommand(new EditCommand(editor, "Delete", false, null, editor::deleteForward));
    }

    public void toggleBold() {
        runOnSelection("Bold", editor::toggleBold);
    }

    public void toggleItalic() {
        runOnSelection("Italic", editor::toggleItalic);
    }

    public void toggleUnderline() {
        runOnSelection("Underline", editor::toggleUnderline);
    }

    public void setFontSize(int size) {
        runOnSelection("Font size", () -> editor.setFontSize(size));
    }

    public void cut() {
        runOnSelection("Cut", editor::cut);
    }

    public void paste() {
        if (editor.getClipboard().isEmpty()) return;
        executeCommand(new EditCommand(editor, "Paste", false, null, editor::paste));
    }

    private void runOnSelection(String description, Runnable action) {
        if (editor.getSelection().isEmpty()) return; // nothing selected -> nothing to do
        executeCommand(new EditCommand(editor, description, false, null, action));
    }

    private boolean isCaretAtDocumentEnd() {
        int last = editor.getDocument().getParagraphs().size() - 1;
        String text = editor.getText();
        int lastLen = text.length() - (text.lastIndexOf('\n') + 1);
        return editor.getCursor().equals(new Cursor(last, lastLen));
    }

    // =====================================================
    //  Afrida's original code (unchanged)
    // =====================================================

    public void setMaxHistorySize(int max) {
        this.maxHistorySize = max;
        trimHistory();
    }

    public void addListener(UndoRedoListener listener) {
        listeners.add(listener);
    }

    public void removeListener(UndoRedoListener listener) {
        listeners.remove(listener);
    }

    public void executeCommand(Command command) {
        command.execute();
        redoStack.clear();

        Command top = undoStack.peek();
        if (canMerge(top, command)) {
            ((MergeableCommand) top).mergeWith(command);
        } else {
            undoStack.push(command);
            trimHistory();
        }
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

    public String getUndoDescription() {
        return canUndo() ? undoStack.peek().getDescription() : null;
    }

    public String getRedoDescription() {
        return canRedo() ? redoStack.peek().getDescription() : null;
    }

    public void clear() {
        undoStack.clear();
        redoStack.clear();
        notifyListeners();
    }

    private boolean canMerge(Command top, Command incoming) {
        return top instanceof MergeableCommand
                && top.getClass().equals(incoming.getClass())
                && ((MergeableCommand) top).canMergeWith(incoming);
    }

    private void trimHistory() {
        if (maxHistorySize > 0) {
            while (undoStack.size() > maxHistorySize) {
                undoStack.removeLast();
            }
        }
    }

    private void notifyListeners() {
        boolean undo = canUndo();
        boolean redo = canRedo();
        String undoDesc = getUndoDescription();
        String redoDesc = getRedoDescription();
        for (UndoRedoListener l : listeners) {
            l.onStateChanged(undo, redo, undoDesc, redoDesc);
        }
    }
}
