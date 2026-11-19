# Project report pointers

Use the title **Local Document Search and Management System using Java**.

**Authors** T. Prem Sai, M. Suhaas Choudary, Santosh Bangaru, A. Yaswanth

## 1. Cover page and abstract

- Include the project title, author names listed above and report date.
- Summarize saving, managing and searching short documents.
- Mention JavaFX, SQLite/JDBC, collections, event handling and a background thread.

## 2. Problem and objectives

- Describe finding documents in a small saved collection.
- List add, view, update, delete, search and persistence across restarts.
- State the single-user desktop scope.

## 3. Requirements

- State the actual JDK, JavaFX and SQLite JDBC versions included with the submission.
- List Windows and the build/run scripts.
- Explain that the window is constructed in Java.

## 4. System design

- Draw a block diagram connecting the JavaFX window, DatabaseManager, SearchEngine and SQLite.
- Draw a small class diagram for the six classes listed in PROJECT_PLAN.md.
- Draw a search flowchart: query -> tokenization -> index lookup -> score addition -> sorting -> results.
- Explain that documents are stored in SQLite and the index is rebuilt in memory.

## 5. Database design

- Show `documents(id, title, content)` and explain the primary key.
- Describe one example each of INSERT, SELECT, UPDATE and DELETE.
- Explain PreparedStatement parameters and try-with-resources.
- Explain first-run sample insertion and reloading saved rows on restart.

## 6. Search algorithm

- Lowercase words, split punctuation and remove a short stop-word set.
- Show the nested map using `java -> {1=2, 2=1}`.
- Explain that a HashSet prevents repeated query words from inflating scores.
- Add frequencies of matching query words, then sort by score and document ID.
- Describe matching any query word and counting content words only.

| Document content | Score for java | Score for java database |
| --- | --- | --- |
| Java Java collections | 2 | 2 |
| Java database | 1 | 2 |

The score is an occurrence count. Explain that the index trades extra memory for faster word lookup.

## 7. Java concepts

| Topic | Example to explain |
| --- | --- |
| Classes and objects | Document and SearchResult fields, constructors and getters |
| Inheritance and overriding | Main extends Application and overrides start |
| Collections and generics | List, HashMap and HashSet with typed elements |
| Exceptions | Invalid input and SQLException handling |
| JDBC | Four document CRUD actions |
| Events | Search and document-management button handlers |
| Multithreading | One worker builds the index; Platform.runLater updates controls |

Describe the concepts present in the code. JavaFX events and built-in generic collections are the examples used here.

## 8. Screenshots and checks

Capture the main window, adding a document, ranked results, an update, a deletion and saved documents after restart.

Use a small table with input, expected result, actual result and pass/fail:

- Mixed case and punctuation normalize consistently.
- Unknown, empty and stop-word-only queries give no matches.
- Repeated query words do not inflate scores.
- Updating or deleting a document changes later results.
- Blank fields show a validation message.
- Restarting preserves the saved collection.
- A database error is reported and Reload permits recovery.

Include the actual `run-test.bat` output and your own GUI observations.

## 9. Limitations, conclusion and references

- Exact words match; `run` and `running` differ.
- Frequency scoring may favor longer documents.
- The entire small collection and index fit in memory.
- State what you learned about Java OOP, JDBC, collections, events and threads.
- Cite the Java, JavaFX and JDBC documentation used.
- Possible later improvements include stemming and improved ranking.
