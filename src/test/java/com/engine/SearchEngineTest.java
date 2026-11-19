package com.engine;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

/** Small repeatable checks for frequency ranking and actual SQLite CRUD. */
public class SearchEngineTest {
    private static int checks;

    public static void main(String[] args) throws Exception {
        checkSearch();
        checkDatabase();
        System.out.println("PASS: " + checks + " checks for search, SQLite CRUD and restart persistence.");
    }

    private static void checkSearch() {
        Tokenizer tokenizer = new Tokenizer();
        check(tokenizer.tokenize("The JAVA, java! database AND 17.")
                .equals(List.of("java", "java", "database", "17")), "Normalization and stop words");

        Document first = new Document(1, "First", "Java Java collections.");
        Document second = new Document(2, "Java headline", "Java database.");
        SearchEngine engine = new SearchEngine();
        engine.buildIndex(List.of(second, first));

        List<SearchResult> results = engine.search("java");
        check(results.size() == 2, "Both documents match java");
        check(results.get(0).getDocument().getId() == 1 && results.get(0).getScore() == 2,
                "First document scores two");
        check(results.get(1).getScore() == 1, "Second document scores one");
        check(engine.search("java java").get(0).getScore() == 2, "Repeated query words count once");

        results = engine.search("JAVA, database!");
        check(results.get(0).getScore() == 2 && results.get(1).getScore() == 2, "Multi-word frequency sum");
        check(results.get(0).getDocument().getId() == 1, "Equal scores use ascending document ID");
        check(engine.search("headline").isEmpty(), "Titles do not alter content scores");
        check(engine.search("database").size() == 1, "Single matching document");
        check(engine.search("the and").isEmpty(), "Stop-word-only query");
        check(engine.search("").isEmpty(), "Empty query");
        check(engine.search("missing").isEmpty(), "Unknown word");
        check(engine.getVocabularySize() == 3, "Distinct indexed words");

        engine.getDocuments().clear();
        check(engine.getDocuments().size() == 2, "Document list returned as a copy");

        Document edited = new Document(1, "Edited", "Database database.");
        engine.buildIndex(List.of(edited, second));
        check(engine.search("java").size() == 1, "Rebuild removes old word frequencies after an edit");
        check(engine.search("database").get(0).getDocument().getId() == 1, "Edited content changes ranking");
        engine.buildIndex(List.of(edited));
        check(engine.search("java").isEmpty(), "Rebuild excludes deleted document");
        engine.buildIndex(List.of());
        check(engine.getVocabularySize() == 0 && engine.search("database").isEmpty(), "Empty collection");
    }

    private static void checkDatabase() throws Exception {
        Path folder = Files.createTempDirectory("document-search-test-");
        Path file = folder.resolve("documents.db");
        String url = "jdbc:sqlite:" + file;

        try {
            DatabaseManager database = new DatabaseManager(url);
            database.initialize();
            check(database.getAllDocuments().size() == 5, "New table gets five samples");
            database.initialize();
            check(database.getAllDocuments().size() == 5, "Repeated initialization does not duplicate samples");

            DatabaseManager reopened = new DatabaseManager(url);
            reopened.initialize();
            check(reopened.getAllDocuments().size() == 5, "Documents persist across reopening");
            for (Document document : reopened.getAllDocuments()) {
                reopened.deleteDocument(document.getId());
            }
            new DatabaseManager(url).initialize();
            check(reopened.getAllDocuments().isEmpty(), "An intentionally empty table stays empty after restart");

            expectInvalid(() -> reopened.insertDocument(" ", "Text"));
            expectInvalid(() -> reopened.insertDocument("Title", " "));
            expectInvalid(() -> reopened.insertDocument(null, "Text"));
            check(reopened.getAllDocuments().isEmpty(), "Invalid fields do not insert rows");

            reopened.insertDocument("  O'Brien's notes  ", "Java database. It's readable.");
            List<Document> documents = reopened.getAllDocuments();
            check(documents.size() == 1, "Insert stores one document");
            Document saved = documents.get(0);
            check(saved.getTitle().equals("O'Brien's notes"), "PreparedStatement preserves apostrophes and trims title");
            check(saved.getContent().equals("Java database. It's readable."), "Read returns saved content");
            expectInvalid(() -> reopened.updateDocument(saved.getId(), "Title", ""));

            SearchEngine engine = new SearchEngine();
            engine.buildIndex(documents);
            check(engine.search("java").get(0).getScore() == 1, "Stored content can be indexed");
            reopened.updateDocument(saved.getId(), "Updated notes", "Java Java collections.");
            documents = new DatabaseManager(url).getAllDocuments();
            check(documents.get(0).getId() == saved.getId(), "Update preserves document ID");
            check(documents.get(0).getTitle().equals("Updated notes"), "Update persists");
            engine.buildIndex(documents);
            check(engine.search("java").get(0).getScore() == 2, "Index reflects database update");
            check(engine.search("database").isEmpty(), "Update removes obsolete matches");

            reopened.deleteDocument(saved.getId());
            engine.buildIndex(reopened.getAllDocuments());
            check(engine.search("java").isEmpty(), "Delete removes indexed document after rebuilding");
            DatabaseManager emptyRestart = new DatabaseManager(url);
            emptyRestart.initialize();
            check(emptyRestart.getAllDocuments().isEmpty(), "Deleted final document does not trigger reseeding");
        } finally {
            Files.deleteIfExists(file);
            Files.deleteIfExists(folder);
        }
    }

    private static void check(boolean condition, String message) {
        checks++;
        if (!condition) {
            throw new AssertionError(message);
        }
    }

    private static void expectInvalid(DatabaseAction action) throws Exception {
        try {
            action.run();
            throw new AssertionError("Blank or missing fields should be rejected");
        } catch (IllegalArgumentException expected) {
            checks++;
        }
    }

    private interface DatabaseAction {
        void run() throws Exception;
    }
}

