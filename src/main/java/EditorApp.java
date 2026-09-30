import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuBar;
import javafx.scene.control.MenuItem;
import javafx.scene.control.Separator;
import javafx.scene.control.SeparatorMenuItem;
import javafx.scene.control.ToolBar;
import javafx.scene.control.Tooltip;
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.KeyCombination;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.scene.text.FontPosture;
import javafx.scene.text.FontWeight;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;
import java.io.IOException;

/**
 * Main window: menu bar, toolbar, EditorView, status bar.
 * Wires the team's classes together:
 *   filemanager / Exporter  -> File menu
 *   TextEditor              -> cursor, selection, copy
 *   UndoManager             -> every change to the document
 *
 * Run it through Launcher (not directly).
 */
public class EditorApp extends Application {

    private final filemanager fileManager = new filemanager();
    private final Exporter exporter = new Exporter();

    private Stage stage;
    private EditorView view;
    private TextEditor editor;
    private UndoManager undo;
    private File currentFile;
    private boolean dirty;

    private MenuItem undoItem;
    private MenuItem redoItem;
    private Button undoBtn;
    private Button redoBtn;
    private ComboBox<Integer> sizeBox;
    private Label posLabel;
    private Label statsLabel;

    @Override
    public void start(Stage stage) {
        this.stage = stage;
        view = new EditorView();
        view.setOnChange(this::updateStatus);

        BorderPane root = new BorderPane();
        root.setTop(new VBox(buildMenuBar(), buildToolBar()));
        root.setCenter(view);
        root.setBottom(buildStatusBar());

        attach(new TextEditor(new document()));      // start with an empty document

        stage.setScene(new Scene(root, 1000, 700));
        stage.setOnCloseRequest(e -> {
            if (!confirmDiscard()) e.consume();
        });
        stage.show();
        Platform.runLater(() -> {                    // redraw once the window has a real size
            view.refresh();
            view.requestFocus();
        });
    }

    /** Connect a (new or opened) TextEditor to a fresh UndoManager and the view. */
    private void attach(TextEditor newEditor) {
        editor = newEditor;
        undo = new UndoManager(editor);
        dirty = false;

        undo.addListener((canUndo, canRedo, undoDesc, redoDesc) -> {
            undoItem.setDisable(!canUndo);
            redoItem.setDisable(!canRedo);
            undoBtn.setDisable(!canUndo);
            redoBtn.setDisable(!canRedo);
            undoItem.setText(undoDesc == null ? "Undo" : "Undo " + undoDesc);
            redoItem.setText(redoDesc == null ? "Redo" : "Redo " + redoDesc);
            dirty = true;
            updateTitle();
        });

        undoItem.setDisable(true);
        redoItem.setDisable(true);
        undoBtn.setDisable(true);
        redoBtn.setDisable(true);
        undoItem.setText("Undo");
        redoItem.setText("Redo");

        view.bind(editor, undo);
        updateTitle();
    }

    // ------------------------------------------------------------------
    //  Building the UI
    // ------------------------------------------------------------------

    private MenuBar buildMenuBar() {
        Menu file = new Menu("File");
        file.getItems().addAll(
                item("New", KeyCode.N, this::doNew),
                item("Open…", KeyCode.O, this::doOpen),
                new SeparatorMenuItem(),
                item("Save", KeyCode.S, this::doSave),
                item("Save As…", null, this::doSaveAs),
                item("Export as .txt…", null, this::doExport),
                new SeparatorMenuItem(),
                item("Exit", null, () -> { if (confirmDiscard()) stage.close(); }));

        undoItem = item("Undo", KeyCode.Z, () -> edit(() -> undo.undo()));
        redoItem = item("Redo", KeyCode.Y, () -> edit(() -> undo.redo()));

        Menu editMenu = new Menu("Edit");
        editMenu.getItems().addAll(
                undoItem,
                redoItem,
                new SeparatorMenuItem(),
                item("Cut", KeyCode.X, () -> edit(this::doCut)),
                item("Copy", KeyCode.C, () -> edit(this::doCopy)),
                item("Paste", KeyCode.V, () -> edit(this::doPaste)),
                new SeparatorMenuItem(),
                item("Select All", KeyCode.A, () -> edit(() -> editor.selectAll())));

        Menu format = new Menu("Format");
        format.getItems().addAll(
                item("Bold", KeyCode.B, () -> edit(() -> undo.toggleBold())),
                item("Italic", KeyCode.I, () -> edit(() -> undo.toggleItalic())),
                item("Underline", KeyCode.U, () -> edit(() -> undo.toggleUnderline())));

        return new MenuBar(file, editMenu, format);
    }

    private ToolBar buildToolBar() {
        undoBtn = tool("Undo", "Undo (Ctrl+Z)", () -> edit(() -> undo.undo()));
        redoBtn = tool("Redo", "Redo (Ctrl+Y)", () -> edit(() -> undo.redo()));

        Button bold = tool("B", "Bold (Ctrl+B)", () -> edit(() -> undo.toggleBold()));
        bold.setFont(Font.font("Arial", FontWeight.BOLD, 13));

        Button italic = tool("I", "Italic (Ctrl+I)", () -> edit(() -> undo.toggleItalic()));
        italic.setFont(Font.font("Arial", FontPosture.ITALIC, 13));

        Button underline = tool("U", "Underline (Ctrl+U)", () -> edit(() -> undo.toggleUnderline()));
        underline.setStyle("-fx-underline: true;");

        sizeBox = new ComboBox<>();
        sizeBox.getItems().addAll(8, 10, 11, 12, 14, 16, 18, 20, 24, 28, 32, 36, 48, 72);
        sizeBox.setValue(12);
        sizeBox.setFocusTraversable(false);
        sizeBox.setTooltip(new Tooltip("Font size of the selected text"));
        sizeBox.setOnAction(e -> {
            Integer size = sizeBox.getValue();
            if (size != null) edit(() -> undo.setFontSize(size));
        });

        return new ToolBar(undoBtn, redoBtn, new Separator(), bold, italic, underline, new Separator(), sizeBox);
    }

    private HBox buildStatusBar() {
        posLabel = new Label();
        statsLabel = new Label();
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        HBox bar = new HBox(10, posLabel, spacer, statsLabel);
        bar.setPadding(new Insets(4, 10, 4, 10));
        bar.setStyle("-fx-background-color: #f0f0f0; -fx-border-color: #cfcfcf transparent transparent transparent;");
        return bar;
    }

    private MenuItem item(String text, KeyCode key, Runnable action) {
        MenuItem mi = new MenuItem(text);
        if (key != null) {
            mi.setAccelerator(new KeyCodeCombination(key, KeyCombination.SHORTCUT_DOWN));
        }
        mi.setOnAction(e -> action.run());
        return mi;
    }

    private Button tool(String text, String tip, Runnable action) {
        Button b = new Button(text);
        b.setTooltip(new Tooltip(tip));
        b.setFocusTraversable(false);            // so clicking it doesn't steal the keyboard from the editor
        b.setOnAction(e -> action.run());
        return b;
    }

    /** Run an action, redraw, and give the keyboard back to the editor. */
    private void edit(Runnable action) {
        action.run();
        view.refresh();
        view.requestFocus();
    }

    private void updateStatus() {
        if (editor == null) return;
        Cursor c = editor.getCursor();
        posLabel.setText("Ln " + (c.getParagraphIndex() + 1) + ", Col " + (c.getOffset() + 1));

        String text = editor.getText();
        String trimmed = text.trim();
        int words = trimmed.isEmpty() ? 0 : trimmed.split("\\s+").length;
        int chars = text.replace("\n", "").length();
        statsLabel.setText(words + " words  |  " + chars + " characters");
    }

    private void updateTitle() {
        String name = (currentFile == null) ? "Untitled" : currentFile.getName();
        stage.setTitle((dirty ? "*" : "") + name + " - Word Processor");
    }

    // ------------------------------------------------------------------
    //  Clipboard (internal clipboard keeps formatting; we also mirror plain text
    //  to the system clipboard so copy/paste works with other programs)
    // ------------------------------------------------------------------

    private void doCopy() {
        if (editor.getSelection().isEmpty()) return;
        editor.copy();
        putSystemClipboard(editor.getSelectedText());
    }

    private void doCut() {
        if (editor.getSelection().isEmpty()) return;
        String text = editor.getSelectedText();
        undo.cut();
        putSystemClipboard(text);
    }

    private void doPaste() {
        String system = normalize(Clipboard.getSystemClipboard().hasString()
                ? Clipboard.getSystemClipboard().getString() : null);
        ClipboardManager internal = editor.getClipboard();

        boolean fromOutside = !system.isEmpty()
                && (internal.isEmpty() || !system.equals(normalize(internal.getPlainText())));
        if (fromOutside) {
            undo.type(system);                   // text copied from another program: plain text
        } else {
            undo.paste();                        // our own copy: keeps bold / italic / size
        }
    }

    private void putSystemClipboard(String text) {
        ClipboardContent content = new ClipboardContent();
        content.putString(text);
        Clipboard.getSystemClipboard().setContent(content);
    }

    private static String normalize(String s) {
        return s == null ? "" : s.replace("\r\n", "\n").replace("\r", "\n");
    }

    // ------------------------------------------------------------------
    //  Files
    // ------------------------------------------------------------------

    private void doNew() {
        if (!confirmDiscard()) return;
        currentFile = null;
        attach(new TextEditor(new document()));
        view.requestFocus();
    }

    private void doOpen() {
        if (!confirmDiscard()) return;
        File f = chooser("Open").showOpenDialog(stage);
        if (f == null) return;
        try {
            TextEditor loaded = fileManager.openInEditor(f.getPath());
            currentFile = f;
            attach(loaded);
            view.requestFocus();
        } catch (IOException ex) {
            error("Could not open the file", ex);
        }
    }

    private boolean doSave() {
        return (currentFile == null) ? doSaveAs() : saveTo(currentFile);
    }

    private boolean doSaveAs() {
        File f = chooser("Save As").showSaveDialog(stage);
        if (f == null) return false;
        if (!f.getName().contains(".")) f = new File(f.getPath() + ".txt");
        if (!saveTo(f)) return false;
        currentFile = f;
        updateTitle();
        return true;
    }

    private boolean saveTo(File f) {
        try {
            fileManager.save(editor, f.getPath());
            dirty = false;
            updateTitle();
            return true;
        } catch (IOException ex) {
            error("Could not save the file", ex);
            return false;
        }
    }

    private void doExport() {
        File f = chooser("Export").showSaveDialog(stage);
        if (f == null) return;
        try {
            exporter.exportToTxt(editor, f.getPath());
        } catch (IOException ex) {
            error("Could not export the file", ex);
        }
    }

    /** true = safe to continue (nothing unsaved, or saved / discarded); false = user cancelled. */
    private boolean confirmDiscard() {
        if (!dirty) return true;
        String name = (currentFile == null) ? "Untitled" : currentFile.getName();
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION,
                "Save changes to " + name + "?", ButtonType.YES, ButtonType.NO, ButtonType.CANCEL);
        alert.setHeaderText(null);
        alert.initOwner(stage);
        ButtonType answer = alert.showAndWait().orElse(ButtonType.CANCEL);
        if (answer == ButtonType.YES) return doSave();
        return answer == ButtonType.NO;
    }

    private FileChooser chooser(String title) {
        FileChooser fc = new FileChooser();
        fc.setTitle(title);
        fc.getExtensionFilters().add(new FileChooser.ExtensionFilter("Text files (*.txt)", "*.txt"));
        return fc;
    }

    private void error(String message, Exception ex) {
        Alert alert = new Alert(Alert.AlertType.ERROR, message + ":\n" + ex.getMessage());
        alert.initOwner(stage);
        alert.showAndWait();
    }

    public static void main(String[] args) {
        launch(args);
    }
}