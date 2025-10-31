package com.voba.model;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit-Tests für die ColorMapping-Klasse.
 */
class ColorMappingTest {

    @Test
    void testDefaultConstructor() {
        ColorMapping mapping = new ColorMapping();
        assertNotNull(mapping);
    }

    @Test
    void testParameterizedConstructor() {
        ColorMapping mapping = new ColorMapping("java", "#FF5733");
        
        assertEquals("java", mapping.getExtension());
        assertEquals("#FF5733", mapping.getColor());
    }

    @Test
    void testSettersAndGetters() {
        ColorMapping mapping = new ColorMapping();
        
        mapping.setExtension("txt");
        mapping.setColor("#00FF00");
        
        assertEquals("txt", mapping.getExtension());
        assertEquals("#00FF00", mapping.getColor());
    }

    @Test
    void testNullValues() {
        ColorMapping mapping = new ColorMapping(null, null);
        
        assertNull(mapping.getExtension());
        assertNull(mapping.getColor());
    }
}
