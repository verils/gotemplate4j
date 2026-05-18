package io.github.verils.gotemplate.internal.lang;

import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for ErrorUtils similarity matching and suggestion functionality.
 */
public class ErrorUtilsTest {

    @Test
    public void testLevenshteinDistance() {
        // Identical strings
        assertEquals(0, ErrorUtils.levenshteinDistance("hello", "hello"));
        
        // Empty string
        assertEquals(5, ErrorUtils.levenshteinDistance("", "hello"));
        assertEquals(5, ErrorUtils.levenshteinDistance("hello", ""));
        
        // Single character difference
        assertEquals(1, ErrorUtils.levenshteinDistance("hello", "helo"));
        assertEquals(1, ErrorUtils.levenshteinDistance("hello", "hellp"));
        
        // Multiple differences
        assertEquals(3, ErrorUtils.levenshteinDistance("kitten", "sitting"));
        assertEquals(5, ErrorUtils.levenshteinDistance("Name", "FristName"));
        
        // Null check
        assertThrows(IllegalArgumentException.class, 
            () -> ErrorUtils.levenshteinDistance(null, "test"));
        assertThrows(IllegalArgumentException.class, 
            () -> ErrorUtils.levenshteinDistance("test", null));
    }

    @Test
    public void testSimilarityScore() {
        // Identical strings
        assertEquals(1.0, ErrorUtils.similarityScore("hello", "hello"), 0.001);
        
        // Completely different
        assertTrue(ErrorUtils.similarityScore("abc", "xyz") < 0.5);
        
        // Similar strings
        double score = ErrorUtils.similarityScore("FirstName", "FristName");
        assertTrue(score > 0.7); // Should be quite similar
        
        // Empty strings
        assertEquals(1.0, ErrorUtils.similarityScore("", ""), 0.001);
        
        // Null check
        assertThrows(IllegalArgumentException.class, 
            () -> ErrorUtils.similarityScore(null, "test"));
    }

    @Test
    public void testFindClosestMatch() {
        List<String> candidates = Arrays.asList("FirstName", "LastName", "Email", "Age");
        
        // Exact match
        assertEquals("FirstName", ErrorUtils.findClosestMatch("FirstName", candidates));
        
        // Typo - should suggest FirstName
        String closest = ErrorUtils.findClosestMatch("FristName", candidates);
        assertEquals("FirstName", closest);
        
        // Another typo
        closest = ErrorUtils.findClosestMatch("Emial", candidates);
        assertEquals("Email", closest);
        
        // No good match - will still return something (the best of bad options)
        closest = ErrorUtils.findClosestMatch("XYZ", candidates);
        assertNotNull(closest); // Will return the least bad match
        
        // Edge cases
        assertNull(ErrorUtils.findClosestMatch(null, candidates));
        assertNull(ErrorUtils.findClosestMatch("test", null));
        assertNull(ErrorUtils.findClosestMatch("test", Collections.emptyList()));
    }

    @Test
    public void testFindTopMatches() {
        List<String> candidates = Arrays.asList("Name", "FirstName", "LastName", "Email", "Age");
        
        // Get top 3 matches for a typo
        List<String> matches = ErrorUtils.findTopMatches("FristName", candidates, 3);
        assertEquals(3, matches.size());
        assertEquals("FirstName", matches.get(0)); // Should be the best match
        
        // Get all matches
        matches = ErrorUtils.findTopMatches("Name", candidates, 10);
        assertTrue(matches.size() <= 5);
        
        // Edge cases
        assertTrue(ErrorUtils.findTopMatches(null, candidates, 3).isEmpty());
        assertTrue(ErrorUtils.findTopMatches("test", null, 3).isEmpty());
        assertTrue(ErrorUtils.findTopMatches("test", Collections.emptyList(), 3).isEmpty());
    }

    @Test
    public void testIsLikelyTypo() {
        List<String> candidates = Arrays.asList("FirstName", "LastName", "Email");
        
        // Clear typo with high similarity
        assertTrue(ErrorUtils.isLikelyTypo("FristName", candidates, 0.6));
        assertTrue(ErrorUtils.isLikelyTypo("Emial", candidates, 0.6));
        
        // Not a typo (too different)
        assertFalse(ErrorUtils.isLikelyTypo("XYZ", candidates, 0.8));
        
        // Lower threshold catches more
        assertTrue(ErrorUtils.isLikelyTypo("Nam", candidates, 0.3));
        assertFalse(ErrorUtils.isLikelyTypo("Nam", candidates, 0.9));
        
        // Edge cases
        assertFalse(ErrorUtils.isLikelyTypo(null, candidates, 0.6));
        assertFalse(ErrorUtils.isLikelyTypo("test", null, 0.6));
        assertFalse(ErrorUtils.isLikelyTypo("test", Collections.emptyList(), 0.6));
    }

    @Test
    public void testGenerateSuggestion() {
        List<String> candidates = Arrays.asList("FirstName", "LastName", "Email", "Age");
        
        // Good match - should provide suggestion
        String suggestion = ErrorUtils.generateSuggestion("FristName", candidates);
        assertTrue(suggestion.contains("FirstName"));
        assertTrue(suggestion.startsWith(" Did you mean"));
        
        // Another good match
        suggestion = ErrorUtils.generateSuggestion("Emial", candidates);
        assertTrue(suggestion.contains("Email"));
        
        // Poor match - should not suggest
        suggestion = ErrorUtils.generateSuggestion("XYZ", candidates);
        assertEquals("", suggestion);
        
        // Edge cases
        assertEquals("", ErrorUtils.generateSuggestion(null, candidates));
        assertEquals("", ErrorUtils.generateSuggestion("test", null));
        assertEquals("", ErrorUtils.generateSuggestion("test", Collections.emptyList()));
    }

    @Test
    public void testCaseSensitivity() {
        List<String> candidates = Arrays.asList("FirstName", "lastName");
        
        // Case matters in Levenshtein distance
        String closest = ErrorUtils.findClosestMatch("firstname", candidates);
        // Should find the closest based on character differences
        assertNotNull(closest);
    }

    @Test
    public void testRealWorldScenarios() {
        // Simulate field name typos
        List<String> fields = Arrays.asList("UserName", "EmailAddress", "PhoneNumber", "DateOfBirth");
        
        // Common typos
        assertEquals("UserName", ErrorUtils.findClosestMatch("UserNmae", fields));
        assertEquals("EmailAddress", ErrorUtils.findClosestMatch("EmialAddress", fields));
        assertEquals("PhoneNumber", ErrorUtils.findClosestMatch("PhonNumber", fields));
        
        // Generate helpful error messages
        String typo = "UserNmae";
        String suggestion = ErrorUtils.generateSuggestion(typo, fields);
        assertTrue(suggestion.contains("UserName"));
        
        // Verify the suggestion format is user-friendly
        assertTrue(suggestion.matches(" Did you mean '[^']+'\\?"));
    }

    @Test
    public void testPerformanceWithLargeCandidateSet() {
        // Create a large list of candidates
        List<String> candidates = Arrays.asList(
            "Field1", "Field2", "Field3", "Field4", "Field5",
            "Field6", "Field7", "Field8", "Field9", "Field10",
            "Name", "Value", "Type", "Status", "Id"
        );
        
        // Should still be fast
        long start = System.currentTimeMillis();
        String closest = ErrorUtils.findClosestMatch("Fiel1", candidates);
        long elapsed = System.currentTimeMillis() - start;
        
        assertEquals("Field1", closest);
        assertTrue(elapsed < 100, "Should complete in less than 100ms"); // Very generous timeout
    }
}
