# Local Document Search and Management System

**Authors** T. Prem Sai, M. Suhaas Choudary, Santosh Bangaru, A. Yaswanth

The application stores short documents in SQLite and searches their contents using an inverted index. Its JavaFX window is created directly in Java.

## Run

Use a 64-bit JDK 17 or later on Windows x64. From the project folder:

```powershell
.\run.bat
```

The script compiles the six Java classes and opens the application. The bundled libraries are JavaFX 17.0.18 for Windows x64, SQLite JDBC 3.45.1.0, and SLF4J API/NOP 1.7.36. The script supplies the JavaFX module path and library classpath.

On JDK 26, JavaFX may print a `sun.misc.Unsafe` deprecation warning during startup. It did not prevent the application from running in the verified session.

## Use

1. Select a document to read or edit its title and content.
2. Choose New document to clear the editor, then enter a title and content and choose Add.
3. Select a saved document and choose Update to save edits or Delete to remove it after confirmation.
4. Enter words in the search field and choose Search to see ranked results.
5. Close and reopen the application to see the saved collection.

Five sample documents are inserted when the database is first initialized. Your collection is stored in `documents.db`. Deleting every document leaves an empty collection on the next launch.

Loading, saving and rebuilding the index run on one background worker at a time. The controls are disabled while it works. If a database operation fails, the application displays an error and Reload lets you try loading the saved collection again.

## Search rule

Words are converted to lowercase, split at punctuation and filtered using a small stop-word set. A `HashMap` stores each word's occurrence count in each document. The score is the total frequency of the distinct query words in that document. Equal scores are ordered by document ID.

For documents containing `Java Java collections` and `Java database`, the query `java` produces scores 2 and 1. Search matches document content. Titles are display names.

## Check the program

```powershell
.\run-test.bat
```

This checks search scoring and SQLite operations using a temporary database. It leaves your saved collection separate from the test data.

The final automated run passed 37 checks covering search normalization, scores, updates, deletion, validation and SQLite restart persistence. Manual GUI checks confirmed startup, seeded documents, search, adding and updating a document, its text preview, and persistence after reopening. Deletion and input validation were verified through the automated backend checks.

## Source files

`src/main/java/com/engine` contains the six application classes. The search and database checks are in `src/test/java/com/engine/SearchEngineTest.java`.

Search uses exact normalized words, so `run` and `running` differ. Frequency scoring can favor longer documents. The program is intended for a small local collection and one user.
