import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.geometry.Bounds;
import javafx.geometry.Insets;
import javafx.geometry.Point2D;
import javafx.geometry.Pos;
import javafx.scene.control.ScrollPane;
import javafx.scene.effect.DropShadow;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.CornerRadii;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Path;
import javafx.scene.text.Font;
import javafx.scene.text.FontPosture;
import javafx.scene.text.FontWeight;
import javafx.scene.text.HitInfo;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;
import javafx.util.Duration;

import java.util.List;

/**
 * Draws the document (document -> paragraph -> textrun) on a white "page" and
 * turns keyboard / mouse input into calls on TextEditor and UndoManager.
 *
 * Each paragraph is a small VBox holding [selection highlight, TextFlow, caret],
 * so character index N in the TextFlow == offset N in the model.
 *
 * NOTE: we never import javafx.scene.Cursor, so "Cursor" here is the team's class.
 */
public class EditorView extends ScrollPane {

    private static final double PAGE_WIDTH = 794;      // A4 at 96 dpi
    private static final double PAGE_HEIGHT = 1123;
    private static final double PAGE_PADDING = 60;
    private static final double PX_PER_PT = 96.0 / 72.0;
    private static final String FONT_FAMILY = "Arial";
    private static final Color SELECTION_COLOR = Color.web("#b3d4fc");

    private TextEditor editor;
    private UndoManager undo;
    private Runnable onChange = () -> { };

    private final VBox page = new VBox();
    private final StackPane desk = new StackPane(page);
    private Path currentCaret;

    public EditorView() {
        // the white page
        page.setPadding(new Insets(PAGE_PADDING));
        page.setMinWidth(PAGE_WIDTH);
        page.setPrefWidth(PAGE_WIDTH);
        page.setMaxWidth(PAGE_WIDTH);
        page.setMinHeight(PAGE_HEIGHT);
        page.setMaxHeight(Region.USE_PREF_SIZE);
        page.setBackground(new Background(new BackgroundFill(Color.WHITE, CornerRadii.EMPTY, Insets.EMPTY)));
        page.setEffect(new DropShadow(10, Color.gray(0, 0.35)));

        // the grey desk around the page
        desk.setPadding(new Insets(24));
        desk.setAlignment(Pos.TOP_CENTER);
        desk.setBackground(new Background(new BackgroundFill(Color.web("#e4e4e4"), CornerRadii.EMPTY, Insets.EMPTY)));
        desk.setCursor(javafx.scene.Cursor.TEXT);

        setContent(desk);
        setFitToWidth(true);
        setFocusTraversable(true);

        // mouse (scene coordinates, so it works anywhere on the desk)
        desk.setOnMousePressed(e -> {
            if (!e.isPrimaryButtonDown()) return;
            requestFocus();
            placeCaret(e, e.isShiftDown());
        });
        desk.setOnMouseDragged(e -> {
            if (e.isPrimaryButtonDown()) placeCaret(e, true);
        });

        // keyboard: filters run BEFORE ScrollPane's own arrow-key scrolling
        addEventFilter(KeyEvent.KEY_PRESSED, this::onKeyPressed);
        addEventFilter(KeyEvent.KEY_TYPED, this::onKeyTyped);

        // blinking caret
        Timeline blink = new Timeline(new KeyFrame(Duration.millis(530), e -> {
            if (currentCaret != null) currentCaret.setVisible(!currentCaret.isVisible());
        }));
        blink.setCycleCount(Animation.INDEFINITE);
        blink.play();
    }

    // ------------------------------------------------------------------
    //  Public API
    // ------------------------------------------------------------------

    /** Point the view at an editor + its undo manager (call again after New / Open). */
    public void bind(TextEditor editor, UndoManager undo) {
        this.editor = editor;
        this.undo = undo;
        refresh();
    }

    /** Called after every redraw (used by the app to update the status bar). */
    public void setOnChange(Runnable onChange) {
        this.onChange = onChange;
    }

    /** Rebuild everything from the model. Call after ANY change to the editor. */
    public void refresh() {
        if (editor == null) return;

        page.getChildren().clear();
        currentCaret = null;

        List<paragraph> paras = editor.getDocument().getParagraphs();
        for (paragraph p : paras) {
            page.getChildren().add(buildParagraph(p));
        }

        // TextFlow can only report caret / selection shapes after it has been laid out
        page.applyCss();
        page.layout();

        for (int i = 0; i < paras.size(); i++) {
            decorate(i, (VBox) page.getChildren().get(i), lengthOf(paras.get(i)));
        }

        Platform.runLater(this::ensureCaretVisible);
        onChange.run();
    }

    // ------------------------------------------------------------------
    //  Rendering
    // ------------------------------------------------------------------

    private VBox buildParagraph(paragraph p) {
        List<textrun> runs = p.getRuns();
        TextFlow flow = new TextFlow();

        if (lengthOf(p) == 0) {
            // an empty TextFlow has no height, so give it an invisible space
            // (uses the paragraph's first run for the line height if there is one)
            textrun style = runs.isEmpty() ? new textrun(" ") : ClipboardManager.copyRun(runs.get(0), " ");
            flow.getChildren().add(makeText(style));
        } else {
            for (textrun r : runs) {
                if (!r.getText().isEmpty()) flow.getChildren().add(makeText(r));
            }
        }

        Path highlight = new Path();
        highlight.setFill(SELECTION_COLOR);
        highlight.setStroke(null);
        highlight.setManaged(false);
        highlight.setMouseTransparent(true);

        Path caret = new Path();
        caret.setFill(null);
        caret.setStroke(Color.BLACK);
        caret.setStrokeWidth(1.2);
        caret.setManaged(false);
        caret.setMouseTransparent(true);

        // order matters: highlight (behind), text, caret (in front)
        return new VBox(highlight, flow, caret);
    }

    private Text makeText(textrun r) {
        Text t = new Text(r.getText());
        t.setFont(Font.font(
                FONT_FAMILY,
                r.isBold() ? FontWeight.BOLD : FontWeight.NORMAL,
                r.isItalic() ? FontPosture.ITALIC : FontPosture.REGULAR,
                r.getFontsize() * PX_PER_PT));
        t.setUnderline(r.isUnderline());
        t.setFill(Color.BLACK);
        return t;
    }

    /** Draws the selection highlight and the caret for paragraph i. */
    private void decorate(int i, VBox box, int len) {
        Path highlight = (Path) box.getChildren().get(0);
        TextFlow flow = (TextFlow) box.getChildren().get(1);
        Path caret = (Path) box.getChildren().get(2);

        Selection sel = editor.getSelection();
        if (!sel.isEmpty()) {
            Cursor s = sel.getStart();
            Cursor e = sel.getEnd();
            if (i >= s.getParagraphIndex() && i <= e.getParagraphIndex()) {
                int from = (i == s.getParagraphIndex()) ? s.getOffset() : 0;
                int to = (i == e.getParagraphIndex()) ? e.getOffset() : len;
                if (len == 0) {
                    // empty line: highlight its placeholder only if the selection continues past it
                    from = 0;
                    to = (i < e.getParagraphIndex()) ? 1 : 0;
                }
                if (to > from) {
                    highlight.getElements().setAll(flow.rangeShape(from, to));
                }
            }
        }

        Cursor c = editor.getCursor();
        if (c.getParagraphIndex() == i) {
            int off = Math.min(c.getOffset(), len);
            caret.getElements().setAll(flow.caretShape(off, true));
            currentCaret = caret;
        }
    }

    private void ensureCaretVisible() {
        if (currentCaret == null || getScene() == null) return;
        if (currentCaret.getBoundsInLocal().isEmpty()) return;

        Bounds cb = desk.sceneToLocal(currentCaret.localToScene(currentCaret.getBoundsInLocal()));
        if (cb == null) return;

        double viewH = getViewportBounds().getHeight();
        double range = desk.getHeight() - viewH;
        if (range <= 0) return;

        double top = getVvalue() * range;
        if (cb.getMinY() < top) {
            setVvalue(Math.max(0, (cb.getMinY() - 20) / range));
        } else if (cb.getMaxY() > top + viewH) {
            setVvalue(Math.min(1, (cb.getMaxY() + 20 - viewH) / range));
        }
    }

    private static int lengthOf(paragraph p) {
        int n = 0;
        for (textrun r : p.getRuns()) n += r.getText().length();
        return n;
    }

    // ------------------------------------------------------------------
    //  Mouse
    // ------------------------------------------------------------------

    private void placeCaret(MouseEvent e, boolean extend) {
        if (editor == null) return;
        List<javafx.scene.Node> boxes = page.getChildren();
        for (int i = 0; i < boxes.size(); i++) {
            VBox box = (VBox) boxes.get(i);
            Point2D p = box.sceneToLocal(e.getSceneX(), e.getSceneY());
            boolean last = (i == boxes.size() - 1);
            if (p.getY() <= box.getHeight() || last) {
                TextFlow flow = (TextFlow) box.getChildren().get(1);
                HitInfo hit = flow.hitTest(new Point2D(p.getX(), Math.max(0, p.getY())));
                editor.setCursor(i, hit.getInsertionIndex(), extend);   // TextEditor clamps the offset
                refresh();
                return;
            }
        }
    }

    // ------------------------------------------------------------------
    //  Keyboard  (shortcuts like Ctrl+Z / Ctrl+C live in the menu bar)
    // ------------------------------------------------------------------

    private void onKeyPressed(KeyEvent e) {
        if (editor == null) return;
        boolean shift = e.isShiftDown();
        boolean ctrl = e.isShortcutDown();

        switch (e.getCode()) {
            // moving the caret does not change the document -> call the editor directly
            case LEFT  -> editor.moveLeft(shift);
            case RIGHT -> editor.moveRight(shift);
            case UP    -> editor.moveUp(shift);
            case DOWN  -> editor.moveDown(shift);
            case HOME  -> { if (ctrl) editor.moveToDocumentStart(shift); else editor.moveToLineStart(shift); }
            case END   -> { if (ctrl) editor.moveToDocumentEnd(shift); else editor.moveToLineEnd(shift); }
            // changing the document -> ALWAYS go through the UndoManager
            case BACK_SPACE -> undo.backspace();
            case DELETE     -> undo.deleteKey();
            case ENTER      -> undo.newLine();
            case TAB        -> undo.type("    ");
            default -> { return; }
        }
        e.consume();
        refresh();
    }

    private void onKeyTyped(KeyEvent e) {
        if (editor == null) return;
        // Ctrl+letter / Cmd+letter are shortcuts, not text (Ctrl+Alt = AltGr is fine)
        if ((e.isControlDown() && !e.isAltDown()) || e.isMetaDown()) return;

        String ch = e.getCharacter();
        if (ch == null || ch.isEmpty()) return;
        char c = ch.charAt(0);
        if (c < 32 || c == 127) return;          // Enter, Backspace, Tab, Esc ... handled above

        undo.type(ch);
        e.consume();
        refresh();
    }
}