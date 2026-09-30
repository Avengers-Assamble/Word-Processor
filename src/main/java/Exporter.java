// bisakha's code
// EDITED: 'Paragraph' -> 'paragraph' (Aarohi's class name), UTF-8 + auto-closing file,
// and added a version that takes the TextEditor directly. Export logic is the same.
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;

public class Exporter {

    public void exportToTxt(document doc, String path) throws IOException {
        try (BufferedWriter fw = Files.newBufferedWriter(Paths.get(path), StandardCharsets.UTF_8)) {
            for (paragraph p : doc.getParagraphs()) {
                for (textrun r : p.getRuns()) {
                    fw.write(r.getText());
                }
                fw.write("\n");
            }
        }
    }

    /** Export what is currently in the editor. */
    public void exportToTxt(TextEditor editor, String path) throws IOException {
        exportToTxt(editor.getDocument(), path);
    }
}