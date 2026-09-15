package undomanager.command;

/**
 * Every user action that changes the document (typing, deleting, formatting,
 * pasting, etc.) is wrapped in a Command. The UndoManager only ever talks to
 * this interface, so it never needs to know what the action actually did.
 */
public interface Command {

    /** Performs the action (or re-performs it, for redo, unless redo() is overridden). */
    void execute();

    /** Reverses exactly what execute() did. */
    void undo();

    /**
     * Re-applies the action after an undo. Defaults to execute(), which is
     * correct for most commands. Override only if redo needs different logic
     * than the original execute (rare).
     */
    default void redo() {
        execute();
    }

    /** Short human-readable label, e.g. for "Undo Typing" / "Undo Formatting" menu items. */
    default String getDescription() {
        return this.getClass().getSimpleName();
    }
}
