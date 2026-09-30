<img width="750" height="375" alt="images" src="https://github.com/user-attachments/assets/19bb422c-6314-436f-b10d-368c3f2d8f38" />

# Word Processor (Java + JavaFX)

A lightweight word processor built in Java with a JavaFX graphical interface. It is a team project that keeps the core logic (document model, editing, undo/redo, file I/O) separate from the GUI, so each part could be developed and understood on its own.

## Team

| Member | Responsibility | Files |
|---|---|---|
| Himangshu Keot | GUI and integration: window, menu bar, toolbar, status bar, page view, keyboard and mouse handling | `Launcher`, `EditorApp`, `EditorView` |
| Aarohi Verma | Document model | `document`, `paragraph`, `textrun` |
| Afrida Doulla | Editing logic: typing, deleting, cursor, selection, clipboard | `TextEditor`, `Cursor`, `Selection`, `ClipboardManager` |
| Ankita Dutta | Undo/redo (Command pattern) | `UndoManager`, `Command`, `MergeableCommand`, `EditCommand`, `EditorSnapshot`, `UndoRedoListener` |
| Bisakha Das | File handling | `filemanager`, `Exporter` |

## Features

- Rich text editing on an A4-style page: typing, Backspace, Delete, Enter, Tab
- Caret movement with arrow keys, Home/End and Ctrl+Home/End; click and drag with the mouse to place the caret and select text
- Bold, italic, underline and font size applied to the selected text
- Cut, copy, paste and select all. Copying inside the editor keeps formatting, and text is also shared with other programs through the system clipboard (as plain text)
- Undo and redo through the Command pattern. Typing is merged, so typing a word is one undo step
- New, Open, Save, Save As and Export to `.txt` (UTF-8), with a prompt before unsaved changes are discarded
- Status bar showing line/column, word count and character count

## Keyboard shortcuts

| Action | Shortcut |
|---|---|
| New / Open / Save | Ctrl+N / Ctrl+O / Ctrl+S |
| Undo / Redo | Ctrl+Z / Ctrl+Y |
| Cut / Copy / Paste | Ctrl+X / Ctrl+C / Ctrl+V |
| Select all | Ctrl+A |
| Bold / Italic / Underline | Ctrl+B / Ctrl+I / Ctrl+U |
| Start / end of line | Home / End |
| Start / end of document | Ctrl+Home / Ctrl+End |
| Extend selection | Hold Shift while moving or clicking |

(On macOS, use Cmd instead of Ctrl.)

## Tech Stack

- **Language:** Java 25
- **GUI:** JavaFX 25 (`javafx-controls`), the only external dependency
- **Build tool:** Maven (with the Maven Wrapper, so Maven does not need to be installed)

JavaFX is used because a word processor needs a graphical interface and rich-text layout. Everything else is plain Java from the JDK.

## Architecture

The GUI never edits the document directly. Data flows in one direction:

```
EditorApp / EditorView  (GUI)
        |
        |  edits                      |  caret moves, selection, copy
        v                             v
   UndoManager  ----------------->  TextEditor  --->  document -> paragraph -> textrun
   (wraps each change in an          (cursor, selection, formatting,
    EditCommand snapshot)             clipboard)

filemanager / Exporter  <-- used by EditorApp for Open, Save and Export
```

- **Model:** a `document` holds `paragraph`s, and a `paragraph` holds `textrun`s. Each run stores its text and its formatting (bold, italic, underline, size).
- **Editing:** `TextEditor` owns the document, the `Cursor`, the `Selection` and the `ClipboardManager`, and implements all editing operations.
- **Undo/redo:** `UndoManager` runs every change as a `Command`. `EditCommand` stores an `EditorSnapshot` before and after the action, and typing commands can merge (`MergeableCommand`). `UndoRedoListener` tells the GUI when to enable or disable Undo and Redo.
- **GUI:** `EditorView` draws each paragraph as a `TextFlow`, so character index N in the `TextFlow` equals offset N in the model. It also draws the caret and selection highlight.

Design patterns used: Command (undo/redo), Memento-style snapshots (`EditorSnapshot`), Observer (`UndoRedoListener`).

## Project Structure

```
.
├── pom.xml
├── mvnw, mvnw.cmd, .mvn/        # Maven Wrapper
├── docs/                        # UML diagrams (class, use-case, sequence)
└── src/main/java/
    ├── Launcher.java            # entry point
    ├── EditorApp.java           # main window, menus, toolbar, status bar, file actions
    ├── EditorView.java          # page view: rendering, caret, keyboard and mouse
    ├── TextEditor.java          # editing logic
    ├── Cursor.java, Selection.java, ClipboardManager.java
    ├── document.java, paragraph.java, textrun.java        # document model
    ├── UndoManager.java, Command.java, MergeableCommand.java,
    │   EditCommand.java, EditorSnapshot.java, UndoRedoListener.java
    └── filemanager.java, Exporter.java                    # file I/O
```

All classes are in the default package.

## Getting Started

### Prerequisites

- **JDK 25** (the version set in `pom.xml`)
- An internet connection the first time you build, so Maven can download JavaFX. No separate JavaFX install is needed.

### Run

From the project folder:

```bash
# Windows
mvnw.cmd javafx:run

# macOS / Linux
./mvnw javafx:run
```

### Run from IntelliJ IDEA

Open the project folder (IntelliJ imports `pom.xml`), then run the `Launcher` class. Run `Launcher`, not `EditorApp`: starting a JavaFX `Application` class directly from the classpath fails with "JavaFX runtime components are missing".

Some harmless JavaFX warnings about "restricted methods" may appear in the console on newer JDKs.

## Testing

Manual checks used to verify the program (see `docs/` for the full test plan):

- Type text, press Ctrl+Z, and check that one undo removes one typing step; Ctrl+Y restores it
- Select text across several paragraphs, then apply bold, italic, underline and a font size; undo each one
- Cut, copy and paste inside the editor (formatting kept) and from another program (plain text)
- Save, close, reopen: the text must be identical
- Try edge cases: Backspace at the very start, Delete at the very end, Enter in the middle of a word, empty document

## Known Limitations

- Save and Export write plain `.txt`, so formatting (bold, italic, underline, size) is not stored in the file
- Up and Down arrows move by paragraph, not by wrapped line
- Formatting applies only to selected text; it cannot be switched on before typing
- An empty paragraph does not remember its formatting
- Toolbar buttons do not show the formatting at the caret


## Status

Completed as a two-week team project. Core editing, undo/redo, file handling and the GUI are integrated and working.
