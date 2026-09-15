package undomanager;

import undomanager.command.impl.FormatCommand;
import undomanager.command.impl.InsertTextCommand;
import undomanager.command.impl.ReplaceTextCommand;
import undomanager.document.Document;
import undomanager.document.SimpleDocument;
import undomanager.document.TextFormat;

/**
 * Standalone demo — run this to see the undo manager working without any
 * UI. Good for a live demo during grading, and as a template for unit tests.
 */
public class Main {
    public static void main(String[] args) {
        Document doc = new SimpleDocument();
        UndoManager undoManager = new UndoManager();

        undoManager.addListener((canUndo, canRedo, undoDesc, redoDesc) ->
                System.out.println("   [state] canUndo=" + canUndo + " canRedo=" + canRedo
                        + " | undo=" + undoDesc + " | redo=" + redoDesc));

        System.out.println("Typing 'Hello' then ' World' quickly (should merge into one step):");
        undoManager.executeCommand(new InsertTextCommand(doc, 0, "Hello"));
        undoManager.executeCommand(new InsertTextCommand(doc, 5, " World"));
        System.out.println("Text: \"" + doc.getText() + "\"\n");

        System.out.println("Bolding \"Hello\":");
        undoManager.executeCommand(new FormatCommand(doc, 0, 5, TextFormat.BOLD, true));
        System.out.println();

        System.out.println("Undo (should remove bold):");
        undoManager.undo();
        System.out.println("Text: \"" + doc.getText() + "\"\n");

        System.out.println("Undo again (should remove the merged typing):");
        undoManager.undo();
        System.out.println("Text: \"" + doc.getText() + "\"\n");

        System.out.println("Redo (should bring the typing back):");
        undoManager.redo();
        System.out.println("Text: \"" + doc.getText() + "\"\n");

        System.out.println("Replacing \"Hello\" with \"Hi\":");
        undoManager.executeCommand(new ReplaceTextCommand(doc, 0, 5, "Hi"));
        System.out.println("Text: \"" + doc.getText() + "\"\n");

        System.out.println("Undo the replace:");
        undoManager.undo();
        System.out.println("Text: \"" + doc.getText() + "\"");
    }
}
