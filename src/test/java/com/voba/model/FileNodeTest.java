package com.voba.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Collectors;

import org.junit.jupiter.api.Test;

/** Unit-Tests für die FileNode-Klasse. */
class FileNodeTest {

  /**
   * Hilfsfunktion zum Konvertieren einer Liste von FileNodes in einen String.
   *
   * @param nodes Liste der FileNodes
   * @return String mit den Namen der Nodes, getrennt durch Leerzeichen
   */
  private String toString(List<FileNode> nodes) {
    return nodes.stream().map(FileNode::getName).collect(Collectors.joining(" "));
  }

  @Test
  void testConstructor() {
    Path path = Paths.get("path", "to", "test.txt");
    FileNode node = new FileNode(path, false);

    assertNotNull(node);
    assertNotNull(node.getChildren());
    assertEquals(0, node.getSize());
    assertTrue(node.getChildren().isEmpty());
    assertEquals("test.txt", node.getName());
    assertEquals(path.toString(), node.getPath());
    assertFalse(node.isDirectory());
    assertEquals("txt", node.getExtension());
  }

  @Test
  void testDirectoryNode() {
    Path path = Paths.get("path", "to", "folder");
    FileNode node = new FileNode(path, true);

    assertEquals("folder", node.getName());
    assertTrue(node.isDirectory());
    assertNull(node.getExtension());
  }

  @Test
  void testExtensionExtraction() {
    FileNode node1 = new FileNode(Paths.get("path", "file.java"), false);
    assertEquals("java", node1.getExtension());

    FileNode node2 = new FileNode(Paths.get("path", "archive.tar.gz"), false);
    assertEquals("gz", node2.getExtension());

    FileNode node3 = new FileNode(Paths.get("path", "noextension"), false);
    assertNull(node3.getExtension());
  }

  @Test
  void testAddChild() {
    FileNode parent = new FileNode(Paths.get("parent"), true);
    FileNode child = new FileNode(Paths.get("parent", "child.txt"), false);
    child.setSize(100);

    parent.addChild(child);

    assertEquals(1, parent.getChildren().size());
    assertEquals(100, parent.getSize());
    assertEquals(child, parent.getChildren().get(0));
  }

  @Test
  void testAddMultipleChildren() {
    FileNode parent = new FileNode(Paths.get("parent"), true);

    FileNode child1 = new FileNode(Paths.get("parent", "file1.txt"), false);
    child1.setSize(100);

    FileNode child2 = new FileNode(Paths.get("parent", "file2.txt"), false);
    child2.setSize(200);

    parent.addChild(child1);
    parent.addChild(child2);

    assertEquals(2, parent.getChildren().size());
    assertEquals(300, parent.getSize());
  }

  @Test
  void testSortChildren() {
    FileNode parent = new FileNode(Paths.get("parent"), true);

    FileNode small = new FileNode(Paths.get("parent", "small.txt"), false);
    small.setSize(100);

    FileNode large = new FileNode(Paths.get("parent", "large.txt"), false);
    large.setSize(1000);

    FileNode medium = new FileNode(Paths.get("parent", "medium.txt"), false);
    medium.setSize(500);

    parent.addChild(small);
    parent.addChild(large);
    parent.addChild(medium);

    parent.sortChildren();

    assertEquals("large.txt medium.txt small.txt", toString(parent.getChildren()));
  }

  @Test
  void testSortChildrenByNameWhenSameSizes() {
    FileNode parent = new FileNode(Paths.get("parent"), true);

    FileNode fileB = new FileNode(Paths.get("parent", "b.txt"), false);
    fileB.setSize(100);

    FileNode fileA = new FileNode(Paths.get("parent", "a.txt"), false);
    fileA.setSize(100);

    parent.addChild(fileB);
    parent.addChild(fileA);

    parent.sortChildren();

    assertEquals("a.txt b.txt", toString(parent.getChildren()));
  }

  @Test
  void testGetPathObject() {
    Path expectedPath = Paths.get("path", "to", "test.txt");
    FileNode node = new FileNode(expectedPath, false);

    assertEquals(expectedPath, node.getPathObject());
  }
}
