package com.engine;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.SplitPane;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.util.List;

/** A single JavaFX window for managing and searching a small document collection. */
public class Main extends Application {
    private final DatabaseManager database = new DatabaseManager();
    private SearchEngine engine = new SearchEngine();

    private final ListView<Document> documentList = new ListView<>();
    private final TextField titleField = new TextField();
    private final TextArea contentField = new TextArea();
    private final TextField queryField = new TextField();
    private final ListView<SearchResult> resultList = new ListView<>();
    private final TextArea preview = new TextArea();
    private final Label status = new Label("Loading documents...");
    private final Button reloadButton = new Button("Reload");
    private SplitPane workspace;

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage stage) {
        titleField.setPromptText("Document title");
        contentField.setPromptText("Enter the document text here");
        contentField.setWrapText(true);
        queryField.setPromptText("Try java or database");
        preview.setEditable(false);
        preview.setWrapText(true);
        preview.setPromptText("Select a search result to read its text");

        Button newButton = new Button("New document");
        Button addButton = new Button("Add");
        Button updateButton = new Button("Update");
        Button deleteButton = new Button("Delete");
        Button searchButton = new Button("Search");

        newButton.setOnAction(event -> clearEditor());
        addButton.setOnAction(event -> changeDocuments("Add"));
        updateButton.setOnAction(event -> changeDocuments("Update"));
        deleteButton.setOnAction(event -> deleteSelectedDocument());
        searchButton.setOnAction(event -> searchDocuments());
        queryField.setOnAction(event -> searchDocuments());
        reloadButton.setOnAction(event -> changeDocuments("Load"));

        documentList.getSelectionModel().selectedItemProperty().addListener((value, oldDoc, document) -> {
            if (document != null) {
                titleField.setText(document.getTitle());
                contentField.setText(document.getContent());
            }
        });
        resultList.getSelectionModel().selectedItemProperty().addListener((value, oldResult, result) -> {
            preview.setText(result == null ? "" : result.getDocument().getContent());
        });

        VBox editor = new VBox(8,
                new Label("Saved documents"), documentList,
                new Label("Title"), titleField,
                new Label("Content"), contentField,
                new HBox(8, newButton, addButton, updateButton, deleteButton));
        editor.setPadding(new Insets(12));
        editor.setMinWidth(360);
        VBox.setVgrow(documentList, Priority.ALWAYS);
        VBox.setVgrow(contentField, Priority.ALWAYS);
        documentList.setPrefHeight(200);
        contentField.setPrefRowCount(7);

        HBox searchBar = new HBox(8, queryField, searchButton);
        HBox.setHgrow(queryField, Priority.ALWAYS);
        VBox searchPane = new VBox(8, new Label("Search document content"), searchBar,
                new Label("Results (score = matching word count)"), resultList,
                new Label("Selected result"), preview);
        searchPane.setPadding(new Insets(12));
        searchPane.setMinWidth(380);
        VBox.setVgrow(resultList, Priority.ALWAYS);
        VBox.setVgrow(preview, Priority.ALWAYS);
        resultList.setPrefHeight(200);
        preview.setPrefRowCount(7);

        workspace = new SplitPane(editor, searchPane);
        workspace.setDividerPositions(0.5);
        HBox footer = new HBox(12, reloadButton, status);
        footer.setPadding(new Insets(12));
        BorderPane root = new BorderPane();
        root.setCenter(workspace);
        root.setBottom(footer);

        stage.setTitle("Local Document Search and Management System");
        stage.setScene(new Scene(root, 1050, 700));
        stage.setMinWidth(850);
        stage.setMinHeight(600);
        stage.show();
        changeDocuments("Load");
    }

    private void clearEditor() {
        documentList.getSelectionModel().clearSelection();
        titleField.clear();
        contentField.clear();
        titleField.requestFocus();
    }

    private void deleteSelectedDocument() {
        Document selected = documentList.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showMessage("Select a saved document before deleting it.");
            return;
        }
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION,
                "Delete '" + selected.getTitle() + "'?", ButtonType.OK, ButtonType.CANCEL);
        confirm.setHeaderText("Delete document");
        if (confirm.showAndWait().orElse(ButtonType.CANCEL) == ButtonType.OK) {
            changeDocuments("Delete");
        }
    }

    private void changeDocuments(String action) {
        // Read controls on the JavaFX thread before starting the worker.
        Document selected = documentList.getSelectionModel().getSelectedItem();
        String title = titleField.getText().trim();
        String content = contentField.getText().trim();
        if ((action.equals("Add") || action.equals("Update")) && (title.isEmpty() || content.isEmpty())) {
            showMessage("Enter both a title and some document text.");
            return;
        }
        if (action.equals("Update") && selected == null) {
            showMessage("Select a saved document before updating it.");
            return;
        }

        workspace.setDisable(true);
        reloadButton.setDisable(true);
        status.setText("Loading documents and building index...");

        // Only one operation runs at a time because the controls are disabled.
        Thread worker = new Thread(() -> {
            try {
                switch (action) {
                    case "Load" -> database.initialize();
                    case "Add" -> database.insertDocument(title, content);
                    case "Update" -> database.updateDocument(selected.getId(), title, content);
                    case "Delete" -> database.deleteDocument(selected.getId());
                    default -> throw new IllegalArgumentException("Unknown action: " + action);
                }
                List<Document> documents = database.getAllDocuments();
                SearchEngine rebuiltEngine = new SearchEngine();
                rebuiltEngine.buildIndex(documents);

                // Publish the finished index and update controls on the UI thread.
                Platform.runLater(() -> {
                    engine = rebuiltEngine;
                    documentList.getItems().setAll(documents);
                    clearEditor();
                    resultList.getItems().clear();
                    preview.clear();
                    workspace.setDisable(false);
                    reloadButton.setDisable(false);
                    status.setText(documents.size() + " documents | "
                            + engine.getVocabularySize() + " indexed words | Ready");
                    if (!queryField.getText().isBlank()) {
                        searchDocuments();
                    }
                });
            } catch (Exception error) {
                Platform.runLater(() -> {
                    // Reload must succeed before searching again after a failed change.
                    resultList.getItems().clear();
                    preview.clear();
                    reloadButton.setDisable(false);
                    status.setText("Operation failed. Check the database and click Reload.");
                    showMessage(error.getMessage());
                });
            }
        }, "document-worker");
        worker.setDaemon(true);
        worker.start();
    }

    private void searchDocuments() {
        String query = queryField.getText().trim();
        List<SearchResult> results = engine.search(query);
        resultList.getItems().setAll(results);
        preview.clear();
        if (query.isEmpty()) {
            status.setText("Enter a search word.");
        } else {
            status.setText(results.size() + " matching documents | " + engine.getDocuments().size()
                    + " documents stored");
        }
    }

    private void showMessage(String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION, message, ButtonType.OK);
        alert.setHeaderText("Document Search");
        alert.showAndWait();
    }
}
