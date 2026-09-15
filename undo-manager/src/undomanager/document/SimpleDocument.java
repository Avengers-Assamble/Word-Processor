package undomanager.document;

/**
 * Bare-bones Document backed by a StringBuilder. Good enough to unit-test
 * the undo manager and command classes in isolation, before the real
 * document/editor component from the rest of the team is wired in.
 */
public class SimpleDocument implements Document {

    private final StringBuilder buffer = new StringBuilder();

    @Override
    public void insertText(int position, String text) {
        buffer.insert(position, text);
    }

    @Override
    public void deleteText(int position, int length) {
        buffer.delete(position, position + length);
    }

    @Override
    public String getTextRange(int position, int length) {
        return buffer.substring(position, position + length);
    }

    @Override
    public String getText() {
        return buffer.toString();
    }

    @Override
    public int length() {
        return buffer.length();
    }

    @Override
    public void applyFormat(int start, int end, TextFormat format) {
        System.out.println("APPLY " + format + " to [" + start + ", " + end + ")");
    }

    @Override
    public void removeFormat(int start, int end, TextFormat format) {
        System.out.println("REMOVE " + format + " from [" + start + ", " + end + ")");
    }
}
