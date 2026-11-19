package com.engine;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/** An in-memory inverted index with simple word-frequency ranking. */
public class SearchEngine {
    private final Tokenizer tokenizer = new Tokenizer();
    private Map<Integer, Document> documents = new LinkedHashMap<>();

    // "java" -> {1=2, 2=1} means two occurrences in document 1 and one in document 2.
    private Map<String, Map<Integer, Integer>> index = new HashMap<>();

    public void buildIndex(List<Document> sourceDocuments) {
        Map<Integer, Document> newDocuments = new LinkedHashMap<>();
        Map<String, Map<Integer, Integer>> newIndex = new HashMap<>();

        for (Document document : sourceDocuments) {
            int documentId = document.getId();
            newDocuments.put(documentId, document);

            // Search content only. The title is a display label.
            for (String word : tokenizer.tokenize(document.getContent())) {
                Map<Integer, Integer> frequencies = newIndex.get(word);
                if (frequencies == null) {
                    frequencies = new HashMap<>();
                    newIndex.put(word, frequencies);
                }
                int oldFrequency = frequencies.getOrDefault(documentId, 0);
                frequencies.put(documentId, oldFrequency + 1);
            }
        }

        documents = newDocuments;
        index = newIndex;
    }

    public List<SearchResult> search(String query) {
        Set<String> queryWords = new HashSet<>(tokenizer.tokenize(query));
        Map<Integer, Integer> scores = new HashMap<>();

        for (String word : queryWords) {
            Map<Integer, Integer> frequencies = index.get(word);
            if (frequencies == null) {
                continue;
            }
            for (Map.Entry<Integer, Integer> entry : frequencies.entrySet()) {
                int documentId = entry.getKey();
                int oldScore = scores.getOrDefault(documentId, 0);
                scores.put(documentId, oldScore + entry.getValue());
            }
        }

        List<SearchResult> results = new ArrayList<>();
        for (Map.Entry<Integer, Integer> entry : scores.entrySet()) {
            results.add(new SearchResult(documents.get(entry.getKey()), entry.getValue()));
        }
        results.sort(Comparator.comparingInt(SearchResult::getScore).reversed()
                .thenComparingInt(result -> result.getDocument().getId()));
        return results;
    }

    public List<Document> getDocuments() {
        return new ArrayList<>(documents.values());
    }

    public int getVocabularySize() {
        return index.size();
    }
}

