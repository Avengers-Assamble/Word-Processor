package undomanager.command.impl;

import undomanager.command.Command;
import undomanager.document.Document;
import undomanager.document.TextFormat;

/**
 * Applies or removes a single formatting attribute (bold, italic, etc.) over
 * a text range. Use one FormatCommand per attribute; combine several with
 * CompositeCommand if a single user action toggles multiple attributes.
 */
public class FormatCommand implements Command {

    private final Document document;
    private final int start;
    private final int end;
    private final TextFormat format;
    private final boolean applying;

    /**
     * @param applying true if this command turns the format ON, false if it turns it OFF.
     */
    public FormatCommand(Document document, int start, int end, TextFormat format, boolean applying) {
        this.document = document;
        this.start = start;
        this.end = end;
        this.format = format;
        this.applying = applying;
    }

    @Override
    public void execute() {
        if (applying) {
            document.applyFormat(start, end, format);
        } else {
            document.removeFormat(start, end, format);
        }
    }

    @Override
    public void undo() {
        if (applying) {
            document.removeFormat(start, end, format);
        } else {
            document.applyFormat(start, end, format);
        }
    }

    @Override
    public String getDescription() {
        return (applying ? "Apply " : "Remove ") + format;
    }
}
