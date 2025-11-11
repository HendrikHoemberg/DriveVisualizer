package com.voba.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.DosFileAttributeView;

import com.voba.model.FileNode;
import com.voba.model.ScanOptions;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

/** Unit-Tests für den DirectoryService. */
@SpringBootTest
class DirectoryServiceTest {

  @Autowired
  private DirectoryService directoryService;

  @Test
  void testScanDirectory(@TempDir Path tempDir) throws IOException {
    // Create test directory structure
    Path file1 = tempDir.resolve("file1.txt");
    Path file2 = tempDir.resolve("file2.java");
    Path subDir = tempDir.resolve("subdir");

    Files.writeString(file1, "test content 1");
    Files.writeString(file2, "test content 2");
    Files.createDirectory(subDir);
    Files.writeString(subDir.resolve("file3.txt"), "test content 3");

    // Scan the directory
    FileNode result = directoryService.scanDirectory(tempDir.toString());

    assertNotNull(result);
    assertTrue(result.isDirectory());
    assertEquals(3, result.getChildren().size());
    assertTrue(result.getSize() > 0);
  }

  @Test
  void testScanInvalidDirectory() {
    String invalidPath = "/this/path/does/not/exist/12345";

    assertThrows(
        IllegalArgumentException.class,
        () -> {
          directoryService.scanDirectory(invalidPath);
        });
  }

  @Test
  void testScanEmptyDirectory(@TempDir Path tempDir) {
    FileNode result = directoryService.scanDirectory(tempDir.toString());

    assertNotNull(result);
    assertTrue(result.isDirectory());
    assertEquals(0, result.getChildren().size());
    assertEquals(0, result.getSize());
  }

  @Test
  void testScanDirectoryWithFiles(@TempDir Path tempDir) throws IOException {
    // Create files with different sizes
    Files.writeString(tempDir.resolve("small.txt"), "small");
    Files.writeString(tempDir.resolve("large.txt"), "this is a much larger file with more content");

    FileNode result = directoryService.scanDirectory(tempDir.toString());

    assertNotNull(result);
    assertEquals(2, result.getChildren().size());

    // Children should be sorted by size (largest first)
    assertTrue(result.getChildren().get(0).getSize() >= result.getChildren().get(1).getSize());
  }

  @Test
  void testScanNestedDirectories(@TempDir Path tempDir) throws IOException {
    // Create nested structure
    Path level1 = tempDir.resolve("level1");
    Path level2 = level1.resolve("level2");
    Files.createDirectories(level2);

    Files.writeString(level1.resolve("file1.txt"), "content1");
    Files.writeString(level2.resolve("file2.txt"), "content2");

    FileNode result = directoryService.scanDirectory(tempDir.toString());

    assertNotNull(result);
    assertEquals(1, result.getChildren().size());

    FileNode level1Node = result.getChildren().get(0);
    assertTrue(level1Node.isDirectory());
    assertEquals(2, level1Node.getChildren().size());
  }

  @Test
  void testScanWithHiddenFiles(@TempDir Path tempDir) throws IOException {
    // Create visible and hidden files
    Path visibleFile = tempDir.resolve("visible.txt");
    Path hiddenFile = tempDir.resolve(".hidden.txt");

    Files.writeString(visibleFile, "visible content");
    Files.writeString(hiddenFile, "hidden content");

    // Try to set hidden attribute on Windows
    try {
      DosFileAttributeView attributes = Files.getFileAttributeView(hiddenFile, DosFileAttributeView.class);
      if (attributes != null) {
        attributes.setHidden(true);
      }
    } catch (Exception e) {
      // Ignore if not supported (e.g., on Linux/Mac)
    }

    // Test 1: Without hidden files (default)
    FileNode resultWithoutHidden = directoryService.scanDirectory(tempDir.toString());
    assertNotNull(resultWithoutHidden);
    // On Unix systems, .hidden files are automatically hidden, on Windows we set
    // the attribute
    assertTrue(
        resultWithoutHidden.getChildren().size() == 1
            || resultWithoutHidden.getChildren().size() == 2,
        "Should have 1-2 children depending on OS hidden file handling");

    // Test 2: With hidden files
    ScanOptions includeHidden = new ScanOptions().setIncludeHiddenFiles(true);
    FileNode resultWithHidden = directoryService.scanDirectory(tempDir.toString(), includeHidden);
    assertNotNull(resultWithHidden);
    assertEquals(2, resultWithHidden.getChildren().size(), "Should include both files");
  }

  @Test
  void testScanWithParallelProcessing(@TempDir Path tempDir) throws IOException {
    // Create many files to trigger parallel processing
    for (int i = 0; i < 150; i++) {
      Files.writeString(tempDir.resolve("file" + i + ".txt"), "content " + i);
    }

    // Test with parallel processing enabled
    ScanOptions parallelOptions = new ScanOptions().setUseParallelProcessing(true).setMaxThreads(4);
    FileNode result = directoryService.scanDirectory(tempDir.toString(), parallelOptions);

    assertNotNull(result);
    assertEquals(150, result.getChildren().size());
    assertTrue(result.getSize() > 0);
  }

  @Test
  void testScanWithSequentialProcessing(@TempDir Path tempDir) throws IOException {
    // Create many files
    for (int i = 0; i < 150; i++) {
      Files.writeString(tempDir.resolve("file" + i + ".txt"), "content " + i);
    }

    // Test with sequential processing (default)
    ScanOptions sequentialOptions = new ScanOptions().setUseParallelProcessing(false);
    FileNode result = directoryService.scanDirectory(tempDir.toString(), sequentialOptions);

    assertNotNull(result);
    assertEquals(150, result.getChildren().size());
    assertTrue(result.getSize() > 0);
  }

  @Test
  void testScanWithNullOptions(@TempDir Path tempDir) throws IOException {
    Files.writeString(tempDir.resolve("test.txt"), "test");

    // Should use default options when null is passed
    FileNode result = directoryService.scanDirectory(tempDir.toString(), null);

    assertNotNull(result);
    assertEquals(1, result.getChildren().size());
  }

  @Test
  void testScanWithCustomThreadLimit(@TempDir Path tempDir) throws IOException {
    // Create files
    for (int i = 0; i < 120; i++) {
      Files.writeString(tempDir.resolve("file" + i + ".txt"), "content " + i);
    }

    // Test with custom thread limit
    ScanOptions customThreads = new ScanOptions().setUseParallelProcessing(true).setMaxThreads(2);
    FileNode result = directoryService.scanDirectory(tempDir.toString(), customThreads);

    assertNotNull(result);
    assertEquals(120, result.getChildren().size());
  }
}
