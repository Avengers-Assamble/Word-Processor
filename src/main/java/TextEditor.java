
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Predicate;

public class TextEditor {
    private static final int END = Integer.MAX_VALUE;

    private final document doc;
    private final ClipboardManager clipboard;
    private Cursor cursor;
    private Selection selection;

    public TextEditor(document doc) {
        this.doc = doc;
        this.clipboard = new ClipboardManager();
        if (doc.getParagraphs().isEmpty()) {
            doc.insertParagraphs(new paragraph());
        }
        this.cursor = new Cursor(0, 0);
        this.selection = new Selection(cursor);
    }


    public document getDocument() { return doc; }
    public Cursor getCursor() { return cursor; }
    public Selection getSelection() { return selection; }
    public ClipboardManager getClipboard() { return clipboard; }

    public String getText() {
        StringBuilder sb = new StringBuilder();
        List<paragraph> paras = doc.getParagraphs();
        for (int i = 0; i < paras.size(); i++) {
            if (i > 0) sb.append('\n');
            sb.append(textOf(paras.get(i).getRuns()));
        }
        return sb.toString();
    }

    public String getSelectedText() {
        if (selection.isEmpty()) return "";
        Cursor s = selection.getStart();
        Cursor e = selection.getEnd();
        StringBuilder sb = new StringBuilder();
        for (int i = s.getParagraphIndex(); i <= e.getParagraphIndex(); i++) {
            if (i > s.getParagraphIndex()) sb.append('\n');
            sb.append(textOf(slice(para(i), fromOf(i, s), toOf(i, e))));
        }
        return sb.toString();
    }

    public EditorSnapshot captureState() {
        return new EditorSnapshot(doc.getParagraphs(), cursor, selection);
    }

    public void restoreState(EditorSnapshot s) {
        doc.setParagraphs(s.getParagraphs());
        cursor = s.getCursor();
        selection = s.getSelection();
    }

   
    public void insertText(String text) {
        if (text == null || text.isEmpty()) return;
        text = text.replace("\r", "");
        deleteSelection();
        String[] lines = text.split("\n", -1);
        for (int i = 0; i < lines.length; i++) {
            if (i > 0) splitParagraph();
            if (!lines[i].isEmpty()) insertInline(lines[i]);
        }
    }

    public void insertNewLine() {
        deleteSelection();
        splitParagraph();
    }

    public void deleteBackward() {
        if (deleteSelection()) return;
        int p = cursor.getParagraphIndex();
        int off = cursor.getOffset();
        if (off > 0) {
            deleteRange(new Cursor(p, off - 1), cursor);
        } else if (p > 0) {
            deleteRange(new Cursor(p - 1, length(para(p - 1))), cursor);
        }
    }

    public void deleteForward() {
        if (deleteSelection()) return;
        int p = cursor.getParagraphIndex();
        int off = cursor.getOffset();
        if (off < length(para(p))) {
            deleteRange(cursor, new Cursor(p, off + 1));
        } else if (p < paragraphCount() - 1) {
            deleteRange(cursor, new Cursor(p + 1, 0));
        }
    }

    
    public void moveLeft(boolean extend) {
        if (!extend && !selection.isEmpty()) {
            moveTo(selection.getStart(), false);
            return;
        }
        int p = cursor.getParagraphIndex();
        int off = cursor.getOffset();
        Cursor target = cursor;
        if (off > 0) target = new Cursor(p, off - 1);
        else if (p > 0) target = new Cursor(p - 1, length(para(p - 1)));
        moveTo(target, extend);
    }

    public void moveRight(boolean extend) {
        if (!extend && !selection.isEmpty()) {
            moveTo(selection.getEnd(), false);
            return;
        }
        int p = cursor.getParagraphIndex();
        int off = cursor.getOffset();
        Cursor target = cursor;
        if (off < length(para(p))) target = new Cursor(p, off + 1);
        else if (p < paragraphCount() - 1) target = new Cursor(p + 1, 0);
        moveTo(target, extend);
    }

    public void moveUp(boolean extend) {
        int p = cursor.getParagraphIndex();
        Cursor target = (p == 0)
                ? new Cursor(0, 0)
                : new Cursor(p - 1, Math.min(cursor.getOffset(), length(para(p - 1))));
        moveTo(target, extend);
    }

    public void moveDown(boolean extend) {
        int p = cursor.getParagraphIndex();
        Cursor target = (p == paragraphCount() - 1)
                ? new Cursor(p, length(para(p)))
                : new Cursor(p + 1, Math.min(cursor.getOffset(), length(para(p + 1))));
        moveTo(target, extend);
    }

    public void moveToLineStart(boolean extend) {
        moveTo(new Cursor(cursor.getParagraphIndex(), 0), extend);
    }

    public void moveToLineEnd(boolean extend) {
        int p = cursor.getParagraphIndex();
        moveTo(new Cursor(p, length(para(p))), extend);
    }

    public void moveToDocumentStart(boolean extend) {
        moveTo(new Cursor(0, 0), extend);
    }

    public void moveToDocumentEnd(boolean extend) {
        int last = paragraphCount() - 1;
        moveTo(new Cursor(last, length(para(last))), extend);
    }

    public void setCursor(int paragraphIndex, int offset, boolean extend) {
        int p = Math.max(0, Math.min(paragraphIndex, paragraphCount() - 1));
        int o = Math.max(0, Math.min(offset, length(para(p))));
        moveTo(new Cursor(p, o), extend);
    }

    public void selectAll() {
        int last = paragraphCount() - 1;
        Cursor end = new Cursor(last, length(para(last)));
        selection = new Selection(new Cursor(0, 0), end);
        cursor = end;
    }

    
    public void setBold(boolean value) { applyStyle(r -> r.setBold(value)); }
    public void setItalic(boolean value) { applyStyle(r -> r.setItalic(value)); }
    public void setUnderline(boolean value) { applyStyle(r -> r.setUnderline(value)); }
    public void setFontSize(int size) { applyStyle(r -> r.setFontsize(size)); }

    public void toggleBold() { setBold(!allSelected(textrun::isBold)); }
    public void toggleItalic() { setItalic(!allSelected(textrun::isItalic)); }
    public void toggleUnderline() { setUnderline(!allSelected(textrun::isUnderline)); }

    
    public void copy() {
        if (selection.isEmpty()) return;
        Cursor s = selection.getStart();
        Cursor e = selection.getEnd();
        List<paragraph> copied = new ArrayList<>();
        for (int i = s.getParagraphIndex(); i <= e.getParagraphIndex(); i++) {
            copied.add(build(slice(para(i), fromOf(i, s), toOf(i, e))));
        }
        clipboard.copy(copied);
    }

    public void cut() {
        if (selection.isEmpty()) return;
        copy();
        deleteSelection();
    }

    public void paste() {
        if (clipboard.isEmpty()) return;
        List<paragraph> items = clipboard.paste();
        deleteSelection();
        for (int k = 0; k < items.size(); k++) {
            if (k > 0) splitParagraph();
            insertRuns(items.get(k).getRuns());
        }
    }

    
    private paragraph para(int index) { return doc.getParagraphs().get(index); }
    private int paragraphCount() { return doc.getParagraphs().size(); }

    private int length(paragraph p) {
        int n = 0;
        for (textrun r : p.getRuns()) n += r.getText().length();
        return n;
    }

    private String textOf(List<textrun> runs) {
        StringBuilder sb = new StringBuilder();
        for (textrun r : runs) sb.append(r.getText());
        return sb.toString();
    }

    private int fromOf(int paragraphIndex, Cursor start) {
        return paragraphIndex == start.getParagraphIndex() ? start.getOffset() : 0;
    }

    private int toOf(int paragraphIndex, Cursor end) {
        return paragraphIndex == end.getParagraphIndex() ? end.getOffset() : END;
    }

    /** Copies of the runs (cut to size) covering characters [from, to) of a paragraph. */
    private List<textrun> slice(paragraph p, int from, int to) {
        List<textrun> out = new ArrayList<>();
        int pos = 0;
        for (textrun r : p.getRuns()) {
            String t = r.getText();
            int start = pos;
            int end = pos + t.length();
            pos = end;
            int s = Math.max(from, start);
            int e = Math.min(to, end);
            if (s < e) {
                out.add(ClipboardManager.copyRun(r, t.substring(s - start, e - start)));
            }
        }
        return out;
    }

    private paragraph build(List<textrun> runs) {
        paragraph p = new paragraph();
        for (textrun r : runs) p.addRun(r);
        return p;
    }

    private textrun styleAt(paragraph p, int offset) {
        textrun found = null;
        int pos = 0;
        for (textrun r : p.getRuns()) {
            int end = pos + r.getText().length();
            if (found == null) found = r;
            if (offset > pos && offset <= end) return r;
            pos = end;
        }
        return found != null ? found : new textrun("");
    }

    private void setCaret(int p, int o) {
        cursor = new Cursor(p, o);
        selection = new Selection(cursor);
    }

    private void moveTo(Cursor target, boolean extend) {
        selection = extend
                ? new Selection(selection.getAnchor(), target)
                : new Selection(target);
        cursor = target;
    }

    private void insertInline(String s) {
        int p = cursor.getParagraphIndex();
        int off = cursor.getOffset();
        paragraph old = para(p);
        textrun style = styleAt(old, off);

        List<textrun> runs = slice(old, 0, off);
        runs.add(ClipboardManager.copyRun(style, s));
        runs.addAll(slice(old, off, END));

        doc.setParagraph(p, build(runs));
        setCaret(p, off + s.length());
    }

    private void insertRuns(List<textrun> pasted) {
        int p = cursor.getParagraphIndex();
        int off = cursor.getOffset();
        paragraph old = para(p);

        List<textrun> runs = slice(old, 0, off);
        int added = 0;
        for (textrun r : pasted) {
            if (r.getText().isEmpty()) continue;
            runs.add(ClipboardManager.copyRun(r, r.getText()));
            added += r.getText().length();
        }
        runs.addAll(slice(old, off, END));

        doc.setParagraph(p, build(runs));
        setCaret(p, off + added);
    }

    private void splitParagraph() {
        int p = cursor.getParagraphIndex();
        int off = cursor.getOffset();
        paragraph old = para(p);

        doc.setParagraph(p, build(slice(old, 0, off)));
        doc.insertParagraphAt(p + 1, build(slice(old, off, END)));
        setCaret(p + 1, 0);
    }

    private boolean deleteSelection() {
        if (selection.isEmpty()) return false;
        deleteRange(selection.getStart(), selection.getEnd());
        return true;
    }

    private void deleteRange(Cursor start, Cursor end) {
        paragraph first = para(start.getParagraphIndex());
        paragraph last = para(end.getParagraphIndex());

        List<textrun> runs = slice(first, 0, start.getOffset());
        runs.addAll(slice(last, end.getOffset(), END));

        doc.setParagraph(start.getParagraphIndex(), build(runs));
        for (int i = end.getParagraphIndex(); i > start.getParagraphIndex(); i--) {
            doc.removeParagraph(i);
        }
        setCaret(start.getParagraphIndex(), start.getOffset());
    }

    private boolean allSelected(Predicate<textrun> check) {
        if (selection.isEmpty()) return false;
        Cursor s = selection.getStart();
        Cursor e = selection.getEnd();
        for (int i = s.getParagraphIndex(); i <= e.getParagraphIndex(); i++) {
            for (textrun r : slice(para(i), fromOf(i, s), toOf(i, e))) {
                if (!check.test(r)) return false;
            }
        }
        return true;
    }

    private void applyStyle(Consumer<textrun> change) {
        if (selection.isEmpty()) return;
        Cursor s = selection.getStart();
        Cursor e = selection.getEnd();
        for (int i = s.getParagraphIndex(); i <= e.getParagraphIndex(); i++) {
            paragraph old = para(i);
            int from = fromOf(i, s);
            int to = toOf(i, e);

            List<textrun> runs = slice(old, 0, from);
            for (textrun r : slice(old, from, to)) {
                change.accept(r);
                runs.add(r);
            }
            runs.addAll(slice(old, to, END));
            doc.setParagraph(i, build(runs));
        }
    }
}
