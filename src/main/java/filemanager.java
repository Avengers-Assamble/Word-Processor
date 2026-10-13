
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

    
    public TextEditor openInEditor(String path) throws IOException {
        return new TextEditor(open(path));
    }
}
