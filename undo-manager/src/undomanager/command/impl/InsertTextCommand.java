package undomanager.command.impl;

import undomanager.command.Command;
import undomanager.command.MergeableCommand;
import undomanager.document.Document;

/**
 * Inserts text at a position. Implements MergeableCommand so that typing
 * "h", "e", "l", "l", "o" in quick succession collapses into a single undo
 * step ("Hello") instead of five.
 */
public class InsertTextCommand implements MergeableCommand {

    private static final long MERGE_TIME_WINDOW_MS = 1000;

    private final Document document;
    private final int position;
    private final StringBuilder text;
    private final long timestamp;

    public InsertTextCommand(Document document, int position, String text) {
        this.document = document;
        this.position = position;
        this.text = new StringBuilder(text);
        this.timestamp = System.currentTimeMillis();
    }

    @Override
    public void execute() {
        document.insertText(position, text.toString());
    }

    @Override
    public void undo() {
        document.deleteText(position, text.length());
    }

    @Override
    public boolean canMergeWith(Command other) {
        if (!(other instanceof InsertTextCommand)) {
            return false;
        }
        InsertTextCommand next = (InsertTextCommand) other;
        boolean contiguous = next.position == this.position + this.text.length();
        boolean withinTimeWindow = (next.timestamp - this.timestamp) <= MERGE_TIME_WINDOW_MS;
        boolean thisEndsAWord = text.length() > 0 && Character.isWhitespace(text.charAt(text.length() - 1));
        return contiguous && withinTimeWindow && !thisEndsAWord;
    }

    @Override
    public void mergeWith(Command other) {
        InsertTextCommand next = (InsertTextCommand) other;
        this.text.append(next.text);
    }

    @Override
    public String getDescription() {
        String t = text.toString();
        return "Typing \"" + (t.length() <= 12 ? t : t.substring(0, 12) + "...") + "\"";
    }
}
