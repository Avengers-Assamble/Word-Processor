// EditorSnapshot.java
import java.util.ArrayList;
import java.util.List;

public class EditorSnapshot {
    private final List<paragraph> paragraphs;
    private final Cursor cursor;
    private final Selection selection;

    public EditorSnapshot(List<paragraph> source, Cursor cursor, Selection selection) {
        this.paragraphs = deepCopy(source);
        this.cursor = cursor;
        this.selection = selection;
    }

    public List<paragraph> getParagraphs() { return deepCopy(paragraphs); }
    public Cursor getCursor() { return cursor; }
    public Selection getSelection() { return selection; }

    private static List<paragraph> deepCopy(List<paragraph> src) {
        List<paragraph> out = new ArrayList<>();
        for (paragraph p : src) {
            paragraph c = new paragraph();
            for (textrun r : p.getRuns()) c.addRun(ClipboardManager.copyRun(r, r.getText()));
            out.add(c);
        }
        return out;
    }
}