// Command used by UndoManager (afrida's design, default package)
// If Afrida already has her own Command.java, keep hers and delete this one.

        public interface Command {
    void execute();

    void undo();

    void redo();

    String getDescription();
}