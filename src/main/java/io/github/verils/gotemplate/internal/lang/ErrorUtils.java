package io.github.verils.gotemplate.internal.lang;

import java.util.*;

/**
 * Utility class for error diagnostics and similarity matching.
 * <p>
 * Provides functions for calculating string similarity using Levenshtein distance
 * algorithm and finding the closest matches from a set of candidates.
 * This is used to provide helpful suggestions when users make typos in field names,
 * map keys, or function names.
 */
public final class ErrorUtils {

    /**
     * Default threshold for considering a match as a likely typo
     */
    @SuppressWarnings("unused")
    private static final double DEFAULT_TYPO_THRESHOLD = 0.5;

    /**
     * Minimum similarity score to provide a suggestion
     */
    private static final double SUGGESTION_THRESHOLD = 0.5;

    private ErrorUtils() {
        // Prevent instantiation
    }

    /**
     * Calculates the Levenshtein distance between two strings.
     * <p>
     * The Levenshtein distance is the minimum number of single-character edits
     * (insertions, deletions, or substitutions) required to change one string into another.
     *
     * @param s1 the first string
     * @param s2 the second string
     * @return the Levenshtein distance between s1 and s2
     */
    public static int levenshteinDistance(String s1, String s2) {
        if (s1 == null || s2 == null) {
            throw new IllegalArgumentException("Strings must not be null");
        }

        int len1 = s1.length();
        int len2 = s2.length();

        // Create a matrix to store distances
        int[][] dp = new int[len1 + 1][len2 + 1];

        // Initialize base cases
        for (int i = 0; i <= len1; i++) {
            dp[i][0] = i;
        }
        for (int j = 0; j <= len2; j++) {
            dp[0][j] = j;
        }

        // Fill in the rest of the matrix
        for (int i = 1; i <= len1; i++) {
            for (int j = 1; j <= len2; j++) {
                int cost = (s1.charAt(i - 1) == s2.charAt(j - 1)) ? 0 : 1;
                dp[i][j] = Math.min(
                        Math.min(dp[i - 1][j] + 1,      // deletion
                                dp[i][j - 1] + 1),      // insertion
                        dp[i - 1][j - 1] + cost          // substitution
                );
            }
        }

        return dp[len1][len2];
    }

    /**
     * Calculates the similarity score between two strings.
     * <p>
     * Returns a value between 0.0 (completely different) and 1.0 (identical).
     *
     * @param s1 the first string
     * @param s2 the second string
     * @return similarity score between 0.0 and 1.0
     */
    public static double similarityScore(String s1, String s2) {
        if (s1 == null || s2 == null) {
            throw new IllegalArgumentException("Strings must not be null");
        }

        if (s1.equals(s2)) {
            return 1.0;
        }

        int maxLen = Math.max(s1.length(), s2.length());
        if (maxLen == 0) {
            return 1.0;
        }

        int distance = levenshteinDistance(s1, s2);
        return 1.0 - ((double) distance / maxLen);
    }

    /**
     * Finds the most similar string from a collection of candidates.
     *
     * @param target     the target string to match against
     * @param candidates collection of candidate strings
     * @return the most similar candidate, or null if candidates is empty
     */
    public static String findTopMatch(String target, Collection<String> candidates) {
        if (target == null || candidates == null || candidates.isEmpty()) {
            return null;
        }

        String bestMatch = null;
        double bestScore = -1.0; // Start with -1 to ensure we pick at least one candidate

        for (String candidate : candidates) {
            double score = similarityScore(target, candidate);
            if (score > bestScore) {
                bestScore = score;
                bestMatch = candidate;
            }
        }

        return bestMatch;
    }

    /**
     * Finds the top N most similar strings from a collection of candidates.
     *
     * @param target     the target string to match against
     * @param candidates collection of candidate strings
     * @param maxResults maximum number of results to return
     * @return list of most similar candidates, sorted by similarity (descending)
     */
    public static List<String> findTopMatches(String target, Collection<String> candidates, int maxResults) {
        if (target == null || candidates == null || candidates.isEmpty()) {
            return Collections.emptyList();
        }

        // Calculate scores for all candidates
        Map<String, Double> scores = new LinkedHashMap<>();
        for (String candidate : candidates) {
            double score = similarityScore(target, candidate);
            scores.put(candidate, score);
        }

        // Sort by score descending
        List<Map.Entry<String, Double>> sortedEntries = new ArrayList<>(scores.entrySet());
        sortedEntries.sort((e1, e2) -> Double.compare(e2.getValue(), e1.getValue()));

        // Take top N results
        List<String> result = new ArrayList<>();
        int count = Math.min(maxResults, sortedEntries.size());
        for (int i = 0; i < count; i++) {
            result.add(sortedEntries.get(i).getKey());
        }

        return result;
    }

    /**
     * Checks if a string is likely a typo of any candidate based on similarity threshold.
     *
     * @param target     the target string to check
     * @param candidates collection of candidate strings
     * @param threshold  minimum similarity score to consider as a potential typo (0.0-1.0)
     * @return true if there's at least one candidate with similarity &gt;= threshold
     */
    public static boolean isLikelyTypo(String target, Collection<String> candidates, double threshold) {
        if (target == null || candidates == null || candidates.isEmpty()) {
            return false;
        }

        for (String candidate : candidates) {
            double score = similarityScore(target, candidate);
            if (score >= threshold) {
                return true;
            }
        }

        return false;
    }

    /**
     * Generates a suggestion message for a potentially misspelled identifier.
     *
     * @param target     the misspelled identifier
     * @param candidates collection of valid identifiers
     * @return a formatted suggestion message, or empty string if no good match found
     */
    public static String generateSuggestion(String target, Collection<String> candidates) {
        if (target == null || candidates == null || candidates.isEmpty()) {
            return "";
        }

        // Try case-insensitive matching first for better typo detection
        for (String candidate : candidates) {
            if (candidate.equalsIgnoreCase(target)) {
                return String.format("Did you mean '%s'?", candidate);
            }
        }

        // Use similarity threshold to catch typos
        String closest = findTopMatch(target, candidates);
        if (closest != null) {
            double score = similarityScore(target, closest);
            if (score >= SUGGESTION_THRESHOLD) {
                return String.format("Did you mean '%s'?", closest);
            }
        }

        return "";
    }
}
