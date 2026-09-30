// MergeableCommand used by UndoManager (afrida's design, default package)
// If Afrida already has her own MergeableCommand.java, keep hers and delete this one.

        public interface MergeableCommand extends Command {
    boolean canMergeWith(Command other);

    void mergeWith(Command other);
}