package com.voba.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

/** Unit-Tests für ScanOptions. */
class ScanOptionsTest {

    @Test
    void testDefaultOptions() {
        ScanOptions options = new ScanOptions();

        assertFalse(options.isIncludeHiddenFiles(), "Hidden files should be excluded by default");
        assertFalse(
                options.isUseParallelProcessing(), "Parallel processing should be disabled by default");
        assertEquals(
                Runtime.getRuntime().availableProcessors(),
                options.getMaxThreads(),
                "Max threads should default to available processors");
    }

    @Test
    void testSetIncludeHiddenFiles() {
        ScanOptions options = new ScanOptions().setIncludeHiddenFiles(true);

        assertTrue(options.isIncludeHiddenFiles());
    }

    @Test
    void testSetUseParallelProcessing() {
        ScanOptions options = new ScanOptions().setUseParallelProcessing(true);

        assertTrue(options.isUseParallelProcessing());
    }

    @Test
    void testSetMaxThreads() {
        ScanOptions options = new ScanOptions().setMaxThreads(4);

        assertEquals(4, options.getMaxThreads());
    }

    @Test
    void testSetMaxThreadsInvalid() {
        ScanOptions options = new ScanOptions();

        assertThrows(
                IllegalArgumentException.class,
                () -> options.setMaxThreads(0),
                "Should throw exception for 0 threads");
        assertThrows(
                IllegalArgumentException.class,
                () -> options.setMaxThreads(-1),
                "Should throw exception for negative threads");
    }

    @Test
    void testMethodChaining() {
        ScanOptions options = new ScanOptions().setIncludeHiddenFiles(true).setUseParallelProcessing(true)
                .setMaxThreads(2);

        assertTrue(options.isIncludeHiddenFiles());
        assertTrue(options.isUseParallelProcessing());
        assertEquals(2, options.getMaxThreads());
    }
}
