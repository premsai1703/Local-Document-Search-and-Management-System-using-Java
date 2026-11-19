package com.engine;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Set;

/** Uses the same word-processing rules for documents and queries. */
public class Tokenizer {
    private static final Set<String> STOP_WORDS = Set.of(
            "a", "an", "the", "is", "are", "was", "were", "in", "on", "at",
            "of", "to", "and", "or", "for", "with", "it", "this", "that");

    public List<String> tokenize(String text) {
        List<String> words = new ArrayList<>();
        String[] parts = text.toLowerCase(Locale.ROOT).split("[^a-z0-9]+");

        for (String word : parts) {
            if (!word.isEmpty() && !STOP_WORDS.contains(word)) {
                words.add(word);
            }
        }
        return words;
    }
}
