// bisakha's code
// EDITED: 'Paragraph' -> 'paragraph' (Aarohi's class name), UTF-8 + auto-closing files,
// and added versions that take the TextEditor directly. Save/open logic is the same.
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;

public class filemanager {

    public void save(document doc, String path) throws IOException {
        try (BufferedWriter fw = Files.newBufferedWriter(Paths.get(path), StandardCharsets.UTF_8)) {
            for (paragraph p : doc.getParagraphs()) {
                for (textrun r : p.getRuns()) {
                    fw.write(r.getText());
                }
                fw.write("\n");
            }
        }
    }

    /** Save what is currently in the editor. */
    public void save(TextEditor editor, String path) throws IOException {
        save(editor.getDocument(), path);
    }

    public document open(String path) throws IOException {
        document doc = new document();
        try (BufferedReader br = Files.newBufferedReader(Paths.get(path), StandardCharsets.UTF_8)) {
            String line;
            while ((line = br.readLine()) != null) {
                paragraph p = new paragraph();
                p.addRun(new textrun(line));
                doc.insertParagraphs(p);
            }
        }
        return doc;
    }

    /**
     * Open a file and get a ready-to-use editor for it.
     * (An empty file gives an editor with one empty paragraph.)
     * Then create the undo manager with: new UndoManager(editor)
     */
    public TextEditor openInEditor(String path) throws IOException {
        return new TextEditor(open(path));
    }
}