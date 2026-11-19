# Project overview

**Local Document Search and Management System using Java**

A one-window JavaFX application for adding, viewing, updating, deleting and searching short documents. SQLite keeps the documents between runs. An in-memory inverted index supports word lookup.

## Six application classes

| Class | Responsibility |
| --- | --- |
| Main | Creates the window, handles button actions and starts background work |
| Document | Stores a document ID, title and content |
| SearchResult | Stores a matching document and its integer score |
| Tokenizer | Converts text into lowercase words and removes common stop words |
| SearchEngine | Builds the inverted index and sorts search results |
| DatabaseManager | Creates the table and performs JDBC CRUD operations |

The interface uses Java code and the default JavaFX controls. The project has one SQLite table and one small background worker for each load or change.

## Data and search flow

1. Read the documents from SQLite.
2. Tokenize each document's content.
3. Build `Map<String, Map<Integer, Integer>>`, containing word, document ID and frequency.
4. Normalize the query using the same tokenizer.
5. Add matching frequencies for each distinct query word.
6. Sort by descending score, then ascending document ID.

After a document changes, rebuild the small index from the saved collection. The worker creates the new index and `Platform.runLater` installs it and updates the window. Disabling controls during the operation prevents overlapping edits.

## Database

| Column | Type | Purpose |
| --- | --- | --- |
| id | INTEGER PRIMARY KEY AUTOINCREMENT | Stable document ID |
| title | TEXT NOT NULL | Display title |
| content | TEXT NOT NULL | Searchable text |

The database file is `documents.db`. Sample rows are inserted on first initialization. Each JDBC operation opens and closes its resources using try-with-resources.

## Java concepts

| Concept | Example in this project |
| --- | --- |
| OOP and inheritance | Document objects, constructors, private fields, Main extending Application |
| Polymorphism and exceptions | Overridden start method, Runnable worker and SQLException handling |
| Threads and events | Background work, button handlers and JavaFX-thread updates |
| JDBC and collections | SQLite CRUD, List, HashMap and HashSet |
| Generics and JavaFX | Typed collections, TextField, TextArea, Button and ListView |

The program uses library generic types and JavaFX events.

## Preparation order

Read Document and SearchResult first, then Tokenizer, SearchEngine, DatabaseManager and Main. Trace the two-document example in VIVA_GUIDE.md and practise the short demonstration twice.
