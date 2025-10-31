package com.voba.model;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

/** Unit-Tests für die FileNode-Klasse. */
class FileNodeTest {

  @Test
  void testDefaultConstructor() {
    FileNode node = new FileNode();

    assertNotNull(node);
    assertNotNull(node.getChildren());
    assertEquals(0, node.getSize());
    assertTrue(node.getChildren().isEmpty());
  }

  @Test
  void testParameterizedConstructor() {
    FileNode node = new FileNode("test.txt", "/path/to/test.txt", false);

    assertEquals("test.txt", node.getName());
    assertEquals("/path/to/test.txt", node.getPath());
    assertFalse(node.isDirectory());
    assertEquals("txt", node.getExtension());
  }

  @Test
  void testDirectoryNode() {
    FileNode node = new FileNode("folder", "/path/to/folder", true);

    assertEquals("folder", node.getName());
    assertTrue(node.isDirectory());
    assertNull(node.getExtension());
  }

  @Test
  void testExtensionExtraction() {
    FileNode node1 = new FileNode("file.java", "/path/file.java", false);
    assertEquals("java", node1.getExtension());

    FileNode node2 = new FileNode("archive.tar.gz", "/path/archive.tar.gz", false);
    assertEquals("gz", node2.getExtension());

    FileNode node3 = new FileNode("noextension", "/path/noextension", false);
    assertNull(node3.getExtension());
  }

  @Test
  void testAddChild() {
    FileNode parent = new FileNode("parent", "/parent", true);
    FileNode child = new FileNode("child.txt", "/parent/child.txt", false);
    child.setSize(100);

    parent.addChild(child);

    assertEquals(1, parent.getChildren().size());
    assertEquals(100, parent.getSize());
    assertEquals(child, parent.getChildren().get(0));
  }

  @Test
  void testAddMultipleChildren() {
    FileNode parent = new FileNode("parent", "/parent", true);

    FileNode child1 = new FileNode("file1.txt", "/parent/file1.txt", false);
    child1.setSize(100);

    FileNode child2 = new FileNode("file2.txt", "/parent/file2.txt", false);
    child2.setSize(200);

    parent.addChild(child1);
    parent.addChild(child2);

    assertEquals(2, parent.getChildren().size());
    assertEquals(300, parent.getSize());
  }

  @Test
  void testSortChildren() {
    FileNode parent = new FileNode("parent", "/parent", true);

    FileNode small = new FileNode("small.txt", "/parent/small.txt", false);
    small.setSize(100);

    FileNode large = new FileNode("large.txt", "/parent/large.txt", false);
    large.setSize(1000);

    FileNode medium = new FileNode("medium.txt", "/parent/medium.txt", false);
    medium.setSize(500);

    parent.addChild(small);
    parent.addChild(large);
    parent.addChild(medium);

    parent.sortChildren();

    assertEquals("large.txt", parent.getChildren().get(0).getName());
    assertEquals("medium.txt", parent.getChildren().get(1).getName());
    assertEquals("small.txt", parent.getChildren().get(2).getName());
  }

  @Test
  void testSortChildrenByNameWhenSameSizes() {
    FileNode parent = new FileNode("parent", "/parent", true);

    FileNode fileB = new FileNode("b.txt", "/parent/b.txt", false);
    fileB.setSize(100);

    FileNode fileA = new FileNode("a.txt", "/parent/a.txt", false);
    fileA.setSize(100);

    parent.addChild(fileB);
    parent.addChild(fileA);

    parent.sortChildren();

    assertEquals("a.txt", parent.getChildren().get(0).getName());
    assertEquals("b.txt", parent.getChildren().get(1).getName());
  }
}
