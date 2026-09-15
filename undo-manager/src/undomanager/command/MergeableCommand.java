package undomanager.command;

/**
 * Optional capability for commands that should be able to absorb the next
 * command instead of creating a brand-new undo step. This is what makes
 * "type 5 letters, hit Ctrl+Z once" undo the whole word instead of one
 * character at a time.
 */
public interface MergeableCommand extends Command {

    /** Whether `other` can be folded into this command instead of being pushed separately. */
    boolean canMergeWith(Command other);

    /** Folds `other` into this command. Called only when canMergeWith(other) is true. */
    void mergeWith(Command other);
}
