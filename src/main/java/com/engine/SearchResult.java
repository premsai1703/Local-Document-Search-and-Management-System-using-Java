package com.engine;

/** A matching document and the total frequency of its matching query words. */
public final class SearchResult {
    private final Document document;
    private final int score;

    public SearchResult(Document document, int score) {
        this.document = document;
        this.score = score;
    }

    public Document getDocument() {
        return document;
    }

    public int getScore() {
        return score;
    }

    @Override
    public String toString() {
        return document.getTitle() + " - Score: " + score;
    }
}

