package io.github.verils.gotemplate.internal;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.StringReader;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for IOUtils class to improve coverage
 */
class IOUtilsTest {

    @Test
    void testReadSimpleString() throws IOException {
        String content = "Hello, World!";
        StringReader reader = new StringReader(content);
        
        String result = IOUtils.read(reader);
        assertEquals(content, result);
    }

    @Test
    void testReadEmptyString() throws IOException {
        String content = "";
        StringReader reader = new StringReader(content);
        
        String result = IOUtils.read(reader);
        assertEquals("", result);
    }

    @Test
    void testReadMultiLineString() throws IOException {
        String content = "Line 1\nLine 2\nLine 3";
        StringReader reader = new StringReader(content);
        
        String result = IOUtils.read(reader);
        assertEquals(content, result);
    }

    @Test
    void testReadLargeContent() throws IOException {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 10000; i++) {
            sb.append("x");
        }
        String content = sb.toString();
        StringReader reader = new StringReader(content);
        
        String result = IOUtils.read(reader);
        assertEquals(content, result);
    }

    @Test
    void testReadWithSpecialCharacters() throws IOException {
        String content = "Special chars: \u0000\u0001\u0002\t\r\n";
        StringReader reader = new StringReader(content);
        
        String result = IOUtils.read(reader);
        assertEquals(content, result);
    }

    @Test
    void testReadWithUnicode() throws IOException {
        String content = "Unicode: 世界こんにちは";
        StringReader reader = new StringReader(content);
        
        String result = IOUtils.read(reader);
        assertEquals(content, result);
    }

    @Test
    void testIOUtilsConstructorIsPrivate() {
        // Verify that the constructor is private by checking we can't instantiate it
        try {
            java.lang.reflect.Constructor<IOUtils> constructor = IOUtils.class.getDeclaredConstructor();
            assertTrue(java.lang.reflect.Modifier.isPrivate(constructor.getModifiers()));
        } catch (NoSuchMethodException e) {
            fail("IOUtils should have a default constructor");
        }
    }
}
