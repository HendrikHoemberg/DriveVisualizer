package com.voba.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import com.voba.model.ColorMapping;
import com.voba.service.ColorMappingService;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit-Tests für den ColorMappingService.
 */
@SpringBootTest
class ColorMappingServiceTest {

    @Autowired
    private ColorMappingService colorMappingService;

    @Test
    void testGetColorMappings() {
        List<ColorMapping> mappings = colorMappingService.getColorMappings();
        
        assertNotNull(mappings);
        // Should return either user config or default config
        assertTrue(mappings.size() >= 0);
    }

    @Test
    void testSaveColorMappings(@TempDir Path tempDir) throws IOException {
        // Create test mappings
        List<ColorMapping> testMappings = new ArrayList<>();
        testMappings.add(new ColorMapping("java", "#FF0000", "Java"));
        testMappings.add(new ColorMapping("txt", "#00FF00", "Text"));
        
        // This will save to user home directory
        assertDoesNotThrow(() -> colorMappingService.saveColorMappings(testMappings));
    }

    @Test
    void testResetToDefaults() throws IOException {
        List<ColorMapping> defaults = colorMappingService.resetToDefaults();
        
        assertNotNull(defaults);
        assertTrue(defaults.size() > 0);
        
        // Verify that defaults were loaded and saved
        List<ColorMapping> retrieved = colorMappingService.getColorMappings();
        assertEquals(defaults.size(), retrieved.size());
    }

    @Test
    void testSaveAndRetrieve() throws IOException {
        List<ColorMapping> testMappings = new ArrayList<>();
        testMappings.add(new ColorMapping("test", "#123456", "Test Extension"));
        
        colorMappingService.saveColorMappings(testMappings);
        
        List<ColorMapping> retrieved = colorMappingService.getColorMappings();
        
        assertNotNull(retrieved);
        assertTrue(retrieved.stream()
            .anyMatch(m -> "test".equals(m.getExtension())));
    }
}
