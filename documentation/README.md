# Documentation

Design documents for the Text Editor / Word Processor project. All diagrams are hand-drawn and scanned from notebook.

## Contents

1. [Use Case Diagram](#1-use-case-diagram)
2. [Class Diagram - Word Processor](#2-class-diagram---word-processor)
3. [Class Diagram - Undo/Redo](#3-class-diagram---undoredo)
4. [Sequence Diagram - Undo](#4-sequence-diagram---undo)
5. [Sequence Diagram - Save and Open](#5-sequence-diagram---save-and-open)

---

## 1. Use Case Diagram

<img width="764" height="890" alt="WhatsApp Image 2026-09-30 at 11 09 21 PM" src="https://github.com/user-attachments/assets/69223bee-d8dd-460f-ab84-337df716e3d3" />

Shows what the user (the only actor) can do with the text editor system:

- Type text
- Delete text
- Move cursor and select
- Format text
- Cut, copy and paste
- Undo and redo
- Save document
- Open document
- Export to text file

## 2. Class Diagram - Word Processor

<img width="879" height="1129" alt="WhatsApp Image 2026-09-30 at 11 08 35 PM" src="https://github.com/user-attachments/assets/4149f4ef-5213-4b3e-af2b-13683065d2c6" />

Main structure of the word processor.

- **TextEditor** is the central class: insertText(), deleteBackward(), moveLeft(), moveRight(), toggleBold(), copy(), cut(), paste()
- **FileManager** (save, open, openInEditor) and **Exporter** (exportToText) depend on TextEditor
- **TextEditor** is associated with ClipboardManager, Selection, Cursor and Document
- **ClipboardManager**: copy(), paste(), deepCopy()
- **Selection**: anchor, active, getStart()/getEnd()
- **Cursor**: paragraphIndex, offset
- **Document** has many Paragraphs (insertParagraph(), getParagraph())
- **Paragraph** has many TextRuns (addRun(), getRuns())
- **TextRun**: text, bold, italic, underline, fontSize

Legend used in the diagram: association, dependency, many (*), inheritance, implements interface.

## 3. Class Diagram - Undo/Redo

<img width="899" height="1199" alt="WhatsApp Image 2026-09-30 at 11 08 41 PM" src="https://github.com/user-attachments/assets/6cd192ef-6966-4d72-9721-69b0d43ccdab" />

Undo/redo is built with the Command pattern plus snapshots.

- **UndoManager**: undoStack, redoStack, executeCommand(), undo(), redo(), type(), toggleBold(), addListener()
- **UndoRedoListener** (interface): onStateChanged(), so the UI gets notified when undo/redo state changes
- **Command** (interface): execute(), undo(), redo(), getDescription(). UndoManager holds many commands
- **MergeableCommand** (interface): canMergeWith(), mergeWith(), used to merge small edits (like typing letters) into one undo step
- **EditCommand** implements MergeableCommand. It stores before/after snapshots
- **TextEditor**: captureState(), restoreState()
- **EditorSnapshot**: paragraphs, cursor, selection

## 4. Sequence Diagram - Undo

<img width="899" height="902" alt="WhatsApp Image 2026-09-30 at 11 09 13 PM" src="https://github.com/user-attachments/assets/44729890-fe0a-40e4-bf75-772df0d469b3" />

Objects: User, UndoManager, EditCommand, TextEditor, Document.

Typing:
1. User calls type("Hi") on UndoManager
2. UndoManager calls execute() on EditCommand
3. EditCommand calls captureState() on TextEditor (before snapshot)
4. EditCommand calls insertText("Hi"), which calls setParagraph() on Document
5. EditCommand calls captureState() again (after snapshot)
6. UndoManager pushes the command to the stack

Undo:
1. User calls undo() on UndoManager
2. UndoManager calls undo() on EditCommand
3. EditCommand calls restoreState() on TextEditor
4. TextEditor calls removeParagraph() and insertParagraph() on Document

## 5. Sequence Diagram - Save and Open

<img width="899" height="842" alt="WhatsApp Image 2026-09-30 at 11 08 58 PM" src="https://github.com/user-attachments/assets/0f53eea0-fd0d-4d2c-847e-2600aa000abb" />

Objects: User, FileManager, TextEditor, Document, TextFile.

Save:
1. User calls save(editor, path) on FileManager
2. FileManager calls getDocument() on TextEditor
3. FileManager calls getParagraphs() on Document
4. FileManager writes the lines to the TextFile

Open:
1. User calls openInEditor(path) on FileManager
2. FileManager calls readLine() on TextFile
3. FileManager calls insertParagraph() on Document for each line
4. FileManager creates new TextEditor(doc)
5. The editor is returned to the User

## Folder structure

```
documentation/
  README.md
  images/
    01-use-case-diagram.jpeg
    02-class-diagram-word-processor.jpeg
    03-class-diagram-undo-redo.jpeg
    04-sequence-diagram-undo.jpeg
    05-sequence-diagram-save-open.jpeg
```
