# Undo Manager + Command Classes

This is the Undo/Redo subsystem for the team's word processor, built with the
**Command design pattern**. Every editing action (typing, deleting, replacing,
formatting) is wrapped in a `Command` object that knows how to do itself and
undo itself. The `UndoManager` just keeps two stacks of these objects — it
never needs to know what a "bold" or an "insert" actually does.

## Folder layout

```
src/undomanager/
├── Main.java                      demo / manual test, run this first
├── UndoManager.java                the undo/redo engine
├── UndoRedoListener.java           callback interface for UI buttons/menus
├── command/
│   ├── Command.java                base interface every action implements
│   ├── MergeableCommand.java       optional: lets actions coalesce (see below)
│   └── impl/
│       ├── InsertTextCommand.java   typing / pasting text
│       ├── DeleteTextCommand.java   backspace / delete
│       ├── ReplaceTextCommand.java  find & replace, autocorrect, typing over a selection
│       ├── FormatCommand.java       bold / italic / underline / strikethrough
│       └── CompositeCommand.java    bundles several commands into one undo step
└── document/
    ├── Document.java               interface the commands act on
    ├── SimpleDocument.java         throwaway StringBuilder-based impl, for testing only
    └── TextFormat.java             BOLD / ITALIC / UNDERLINE / STRIKETHROUGH
```

## How to run the demo

```
cd src
javac undomanager/*.java undomanager/command/*.java undomanager/command/impl/*.java undomanager/document/*.java
java undomanager.Main
```

It types text, formats it, undoes/redoes a few times, and prints the document
state after each step, so you can see the stacks working without any UI.

## How this plugs into the rest of the app

The **only thing the rest of the team needs to do** is make the real
document/text-buffer class implement the `Document` interface (or write a
small adapter class that wraps it and implements `Document`). Once that's
done, every command in this package works against the real document
unchanged — `SimpleDocument` was only a stand-in for testing before that
class existed.

From the editor/UI code, every edit should go through the manager instead of
touching the document directly:

```java
UndoManager undoManager = new UndoManager();

// user types a character
undoManager.executeCommand(new InsertTextCommand(document, caretPosition, "a"));

// user hits Ctrl+Z / clicks Undo
undoManager.undo();

// user hits Ctrl+Y (or Ctrl+Shift+Z) / clicks Redo
undoManager.redo();
```

Wire the toolbar/menu Undo and Redo buttons to `undoManager.undo()` /
`undoManager.redo()`, and enable/disable them (and set their labels) using
an `UndoRedoListener`:

```java
undoManager.addListener((canUndo, canRedo, undoDesc, redoDesc) -> {
    undoButton.setEnabled(canUndo);
    redoButton.setEnabled(canRedo);
    undoMenuItem.setText("Undo " + (undoDesc != null ? undoDesc : ""));
    redoMenuItem.setText("Redo " + (redoDesc != null ? redoDesc : ""));
});
```

## Design notes

- **Every command is symmetric**: `execute()` does the edit, `undo()` exactly
  reverses it. `DeleteTextCommand` and `ReplaceTextCommand` capture the text
  they're about to remove *inside* `execute()`, right before removing it, so
  undo always has what it needs — nothing has to be looked up later.
- **Command merging (`MergeableCommand`)**: without this, undoing after
  typing a sentence would take one Ctrl+Z per character, which is exactly
  the annoying behavior real word processors avoid. `InsertTextCommand`
  merges with the next insert if it's contiguous and happens within ~1
  second, but stops merging after a space (so undo removes a word at a
  time, not the whole paragraph in one shot). `UndoManager.executeCommand()`
  checks for this automatically — no special calls needed from the caller.
- **`CompositeCommand`** is for actions that are really several edits at
  once but should undo as a single step — e.g. "Replace All" across a
  document, or a toolbar button that sets bold *and* underline together.
- **History cap**: `UndoManager` defaults to keeping the last 200 commands
  (`setMaxHistorySize`) so long editing sessions don't leak memory. Set it
  to 0 or negative for unlimited history.
- **Formatting commands are logged, not rendered**, in `SimpleDocument` —
  whoever owns the real text-rendering/styling code just needs their
  `applyFormat`/`removeFormat` implementation to actually change how the
  range is displayed; the command logic itself doesn't change.

## Possible extensions if there's time

- Undo/redo grouped by "session" so a whole paste-and-autoformat action is
  one step (use `CompositeCommand`).
- Persisting the undo stack description list into a visible "history panel".
- A `SelectionAwareCommand` variant that also restores cursor/selection
  position on undo, if the UI team wants that polish.
