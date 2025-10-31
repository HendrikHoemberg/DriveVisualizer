package com.voba.service;

import static org.junit.jupiter.api.Assertions.*;

import com.voba.model.FileNode;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

/** Unit-Tests für den DirectoryService. */
@SpringBootTest
class DirectoryServiceTest {

  @Autowired private DirectoryService directoryService;

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
}
