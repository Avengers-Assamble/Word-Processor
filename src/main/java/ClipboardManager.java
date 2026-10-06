import java.util.ArrayList;
import java.util.List;

public class ClipboardManager {
    private List<paragraph> content = new ArrayList<>();

    public void copy(List<paragraph> paragraphs) { content = deepCopy(paragraphs); }
    public List<paragraph> paste() { return deepCopy(content); }
    public boolean isEmpty() { return content.isEmpty(); }
    public void clear() { content = new ArrayList<>(); }

    public String getPlainText() {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < content.size(); i++) {
            if (i > 0) sb.append('\n');
            for (textrun r : content.get(i).getRuns()) sb.append(r.getText());
        }
        return sb.toString();
    }

    private static List<paragraph> deepCopy(List<paragraph> source) {
        List<paragraph> result = new ArrayList<>();
        for (paragraph p : source) {
            paragraph copy = new paragraph();
            for (textrun r : p.getRuns()) copy.addRun(copyRun(r, r.getText()));
            result.add(copy);
        }
        return result;
    }

  
    public static textrun copyRun(textrun style, String text) {
        textrun c = new textrun(text);
        c.setBold(style.isBold());
        c.setItalic(style.isItalic());
        c.setUnderline(style.isUnderline());
        c.setFontsize(style.getFontsize());
        return c;
    }
}
