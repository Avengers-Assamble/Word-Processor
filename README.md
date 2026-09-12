# Word Processor (Java + JavaFX)

A lightweight, modular word processor built in Java, with a JavaFX-based GUI. Built as a team project focused on clean core logic (document model, editing, undo/redo, file I/O) paired with a polished, modern desktop interface.

## Team

| Member | Module |
|---|---|
| Himangshu Keot | GUI: EditorWindow, Toolbar, MenuBar, StatusBar, Font/Style UI |
| Aarohi Verma | Document, Paragraph, TextRun (core model) |
| Afrida Doulla | TextEditor logic, Cursor/Selection, ClipboardManager |
| Ankita Dutta | UndoManager + Command classes |
| Bisakha Das | FileManager, Exporter(s) |

*(Module assignments are from initial planning and may shift as work progresses.)*

## Features

- Rich text editing with cursor and selection support
- Cut / copy / paste via a dedicated clipboard manager
- Undo / redo via the Command pattern
- Open / save documents, with export support
- Font and style controls (bold, italic, size, etc.)
- Live status bar (word count, cursor position)

## Tech Stack

- **Language:** Java
- **GUI:** JavaFX (CSS-styled for a modern look)
- **Build tool:** *(fill in — e.g. Maven / Gradle)*
- **Architecture:** Core document/editing logic is fully decoupled from the GUI — the GUI calls into core classes (`Document`, `TextEditor`, `UndoManager`, `FileManager`) through defined interfaces, so UI and logic can be built in parallel.

## Project Structure

```
src/
├── model/       # Document, Paragraph, TextRun
├── editor/      # TextEditor, Cursor/Selection, ClipboardManager
├── undo/        # UndoManager, Command classes
├── io/          # FileManager, Exporter(s)
└── gui/         # EditorWindow, Toolbar, MenuBar, StatusBar, Font/Style UI
```

## Getting Started

### Prerequisites
- JDK *(specify version)*
- JavaFX SDK *(if not bundled with your JDK)*

### Build & Run
```bash
# fill in your actual build/run commands, e.g.:
mvn clean install
mvn javafx:run
```

## Scope & Design Philosophy

This project intentionally favors a small set of well-implemented, clearly explainable features over a longer list of partially-working ones. Each module exposes a simple, stable interface so other modules (especially the GUI) can be developed against stubs before the underlying logic is complete.

## Status

🚧 In active development — built over a 2-week team sprint.
