
public class EditCommand implements MergeableCommand {
    private final TextEditor editor;
    private final String description;
    private final boolean typing;
    private final String typedText;
    private final Runnable action;

    private EditorSnapshot before;
    private EditorSnapshot after;

    public EditCommand(TextEditor editor, String description, boolean typing,
                       String typedText, Runnable action) {
        this.editor = editor;
        this.description = description;
        this.typing = typing;
        this.typedText = typedText;
        this.action = action;
    }

    @Override
    public void execute() {
        before = editor.captureState();
        action.run();
        after = editor.captureState();
    }

    @Override
    public void undo() {
        editor.restoreState(before);
    }

    @Override
    public void redo() {
        editor.restoreState(after);
    }

    @Override
    public String getDescription() {
        return description;
    }

   

    @Override
    public boolean canMergeWith(Command other) {
        if (!(other instanceof EditCommand)) return false;
        EditCommand o = (EditCommand) other;
        return this.typing && o.typing
                && !" ".equals(o.typedText)                              // a space starts a new step
                && o.before.getSelection().isEmpty()                     // not replacing a selection
                && this.after.getCursor().equals(o.before.getCursor());  // typed right after previous text
    }

    @Override
    public void mergeWith(Command other) {
        this.after = ((EditCommand) other).after;
    }
}
