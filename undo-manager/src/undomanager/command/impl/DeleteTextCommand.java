package undomanager.command.impl;

import undomanager.command.Command;
import undomanager.document.Document;

/**
 * Deletes a range of text (Backspace, Delete, or cutting a selection).
 * The deleted text is captured at execute() time so undo() can put it back.
 */
public class DeleteTextCommand implements Command {

    private final Document document;
    private final int position;
    private final int length;
    private String deletedText;

    public DeleteTextCommand(Document document, int position, int length) {
        this.document = document;
        this.position = position;
        this.length = length;
    }

    @Override
    public void execute() {
        deletedText = document.getTextRange(position, length);
        document.deleteText(position, length);
    }

    @Override
    public void undo() {
        document.insertText(position, deletedText);
    }

    @Override
    public String getDescription() {
        return "Delete " + length + " character(s)";
    }
}
