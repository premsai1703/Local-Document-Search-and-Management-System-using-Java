# Short viva guide

## Opening explanation

"My project is a local document search and management system. It uses JavaFX for the window and SQLite to save documents. I build an inverted index with HashMap and rank search results by matching word frequencies."

## Read the code in this order

1. **Document and SearchResult** hold the data using fields, constructors and getters.
2. **Tokenizer** lowercases text, splits punctuation and removes common words.
3. **SearchEngine** counts words, looks up query words and sorts matches.
4. **DatabaseManager** saves and retrieves document rows using JDBC.
5. **Main** connects the controls to those operations and manages background work.

## Work through one example

Use two documents:

- Document 1 contains `Java Java collections`.
- Document 2 contains `Java database`.

The index contains:

```text
java        -> {1=2, 2=1}
collections -> {1=1}
database    -> {2=1}
```

Searching `java` gives scores 2 and 1. Searching `java database` gives both documents score 2. The lower document ID comes first when scores tie. Searching `java java` gives the same result as `java` because the query words are put into a HashSet.

## Six questions to understand

**What is an inverted index?**

A map from a word to the documents containing it. The inner map also stores how many times the word occurs.

**Why use HashMap and HashSet?**

HashMap lets the program look up a word's document frequencies. HashSet removes duplicate query words.

**How is a result's score calculated?**

Add the occurrence counts of its matching query words. Count the document content and sort higher scores first.

**What does JDBC do here?**

It connects Java to SQLite. PreparedStatement binds values for add, read, update and delete operations. Try-with-resources closes the JDBC objects.

**What happens when a button is clicked?**

Its event handler reads the input and calls the corresponding operation. For example, Search passes the query to SearchEngine and displays the returned results.

**Why use a background thread and Platform.runLater?**

Database work and rebuilding the index run in a worker so the window can respond. Platform.runLater sends the final control updates to the JavaFX application thread. Controls are disabled while a change is running.

## Five-minute demonstration

1. Open the application and show the sample document list.
2. Add Mango note one with the content `mango mango fruit`, then Mango note two with `mango tree`.
3. Search `mango` and explain the scores 2 and 1. This word keeps the demo separate from the seeded sample documents.
4. Change the second document to `mango mango mango tree`, choose Update and show its new score of 3.
5. Delete the second document and confirm the deletion.
6. Close and reopen the program to show that Mango note one was saved.

## Two-day preparation

**Day 1:** Run the application, read the small data/tokenizer/search classes and trace the worked example. Then read the four database operations.

**Day 2:** Read the event handlers and worker code in Main. Practise the demonstration twice and capture the report screenshots.

Know the two limits: exact words are matched, and frequency ranking can give longer documents higher scores.
