package undomanager.command.impl;

import undomanager.command.Command;
import undomanager.document.Document;

/**
 * Replaces a range of text with new text in one step (find-and-replace,
 * autocorrect, or typing while a selection is active).
 */
public class ReplaceTextCommand implements Command {

    private final Document document;
    private final int position;
    private final int oldLength;
    private final String newText;
    private String oldText;

    public ReplaceTextCommand(Document document, int position, int oldLength, String newText) {
        this.document = document;
        this.position = position;
        this.oldLength = oldLength;
        this.newText = newText;
    }

    @Override
    public void execute() {
        oldText = document.getTextRange(position, oldLength);
        document.deleteText(position, oldLength);
        document.insertText(position, newText);
    }

    @Override
    public void undo() {
        document.deleteText(position, newText.length());
        document.insertText(position, oldText);
    }

    @Override
    public String getDescription() {
        return "Replace text";
    }
}
