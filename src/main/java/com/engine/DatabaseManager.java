package com.engine;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

/** SQLite stores the documents. Each operation closes its JDBC resources. */
public class DatabaseManager {
    private final String jdbcUrl;

    public DatabaseManager() {
        this("jdbc:sqlite:documents.db");
    }

    public DatabaseManager(String jdbcUrl) {
        this.jdbcUrl = jdbcUrl;
    }

    public void initialize() throws SQLException {
        try (Connection connection = DriverManager.getConnection(jdbcUrl)) {
            connection.setAutoCommit(false);
            try {
                boolean tableExists;
                try (Statement statement = connection.createStatement();
                     ResultSet rows = statement.executeQuery(
                             "SELECT name FROM sqlite_master WHERE type='table' AND name='documents'")) {
                    tableExists = rows.next();
                }
                try (Statement statement = connection.createStatement()) {
                    statement.executeUpdate("CREATE TABLE IF NOT EXISTS documents ("
                            + "id INTEGER PRIMARY KEY AUTOINCREMENT, "
                            + "title TEXT NOT NULL, content TEXT NOT NULL)");
                }
                // Check table creation, not row count. Deleting every document stays permanent.
                if (!tableExists) {
                    seedDocuments(connection);
                }
                connection.commit();
            } catch (SQLException e) {
                connection.rollback();
                throw e;
            }
        }
    }

    private void seedDocuments(Connection connection) throws SQLException {
        String[][] samples = {
                {"Java", "Java is a programming language. Java supports classes and objects."},
                {"Databases", "A database stores information in tables. JDBC connects Java to a database."},
                {"Collections", "Java collections include ArrayList, HashMap and HashSet. HashMap stores key value pairs."},
                {"Files", "Java can read and write files. Exception handling reports errors when files cannot be read."},
                {"Search", "A search engine finds documents using query words. An inverted index maps words to documents."}
        };
        try (PreparedStatement statement = connection.prepareStatement(
                "INSERT INTO documents(title, content) VALUES(?, ?)")) {
            for (String[] sample : samples) {
                statement.setString(1, sample[0]);
                statement.setString(2, sample[1]);
                statement.executeUpdate();
            }
        }
    }

    public void insertDocument(String title, String content) throws SQLException {
        validateFields(title, content);
        try (Connection connection = DriverManager.getConnection(jdbcUrl);
             PreparedStatement statement = connection.prepareStatement(
                     "INSERT INTO documents(title, content) VALUES(?, ?)")) {
            statement.setString(1, title.trim());
            statement.setString(2, content);
            statement.executeUpdate();
        }
    }

    public List<Document> getAllDocuments() throws SQLException {
        List<Document> documents = new ArrayList<>();
        try (Connection connection = DriverManager.getConnection(jdbcUrl);
             PreparedStatement statement = connection.prepareStatement(
                     "SELECT id, title, content FROM documents ORDER BY id");
             ResultSet rows = statement.executeQuery()) {
            while (rows.next()) {
                documents.add(new Document(rows.getInt("id"), rows.getString("title"), rows.getString("content")));
            }
        }
        return documents;
    }

    public void updateDocument(int id, String title, String content) throws SQLException {
        validateFields(title, content);
        try (Connection connection = DriverManager.getConnection(jdbcUrl);
             PreparedStatement statement = connection.prepareStatement(
                     "UPDATE documents SET title=?, content=? WHERE id=?")) {
            statement.setString(1, title.trim());
            statement.setString(2, content);
            statement.setInt(3, id);
            if (statement.executeUpdate() == 0) {
                throw new SQLException("The selected document no longer exists.");
            }
        }
    }

    public void deleteDocument(int id) throws SQLException {
        try (Connection connection = DriverManager.getConnection(jdbcUrl);
             PreparedStatement statement = connection.prepareStatement(
                     "DELETE FROM documents WHERE id=?")) {
            statement.setInt(1, id);
            if (statement.executeUpdate() == 0) {
                throw new SQLException("The selected document no longer exists.");
            }
        }
    }

    private void validateFields(String title, String content) {
        if (title == null || title.isBlank() || content == null || content.isBlank()) {
            throw new IllegalArgumentException("Enter both a title and document text.");
        }
    }
}

