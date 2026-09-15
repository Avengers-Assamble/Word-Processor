package undomanager.document;

/**
 * Minimal contract Commands need from the actual document/text model.
 *
 * IMPORTANT for the team: whoever owns the real document class (text buffer,
 * rope, styled-text model, etc.) should implement this interface, or wrap
 * their class in an adapter that implements it. As long as that's done,
 * every Command in this package works against it unchanged.
 */
public interface Document {

    void insertText(int position, String text);

    void deleteText(int position, int length);

    /** Returns the substring [position, position+length) — needed so delete/replace can be undone. */
    String getTextRange(int position, int length);

    String getText();

    int length();

    void applyFormat(int start, int end, TextFormat format);

    void removeFormat(int start, int end, TextFormat format);
}
