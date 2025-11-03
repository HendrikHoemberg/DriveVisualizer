package com.voba.model;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Modellklasse für einen Knoten in der Dateisystem-Hierarchie. Repräsentiert eine Datei oder ein
 * Verzeichnis mit allen relevanten Informationen.
 */
public class FileNode {
  private final Path path;
  private final List<FileNode> children;
  private final String extension;
  private long size;

  /**
   * Konstruktor mit Parametern.
   *
   * @param path Pfad zur Datei oder zum Verzeichnis
   * @param isDirectory true, wenn es sich um ein Verzeichnis handelt
   */
  public FileNode(Path path, boolean isDirectory) {
    this.path = path;
    this.children = isDirectory ? new ArrayList<>() : null;
    this.size = 0;

    // Extrahiere Extension aus dem Dateinamen
    if (!isDirectory && path.getFileName() != null) {
      String fileName = path.getFileName().toString();
      if (fileName.contains(".")) {
        int lastDot = fileName.lastIndexOf(".");
        this.extension = fileName.substring(lastDot + 1).toLowerCase();
      } else {
        this.extension = null;
      }
    } else {
      this.extension = null;
    }
  }

  /**
   * Fügt ein Kind-Element hinzu und aktualisiert die Größe.
   *
   * @param child Hinzuzufügendes Kind-Element
   */
  public void addChild(FileNode child) {
    children.add(child);
    updateParentSizes(child.size);
  }

  private void updateParentSizes(long additionalSize) {
    this.size += additionalSize;
  }

  /** Sortiert die Kind-Elemente nach Größe und Name (rekursiv). */
  public void sortChildren() {
    if (children == null) {
      return;
    }

    children.sort(
        (firstChild, secondChild) -> {
          int sizeCompare = Long.compare(secondChild.size, firstChild.size);
          if (sizeCompare != 0) {
            return sizeCompare;
          }
          return firstChild.getName().compareToIgnoreCase(secondChild.getName());
        });

    for (FileNode child : children) {
      if (child.isDirectory()) {
        child.sortChildren();
      }
    }
  }

  public String getName() {
    return path.getFileName() != null ? path.getFileName().toString() : path.toString();
  }

  public String getPath() {
    return path.toString();
  }

  public Path getPathObject() {
    return path;
  }

  public long getSize() {
    return size;
  }

  public boolean isDirectory() {
    return children != null;
  }

  public List<FileNode> getChildren() {
    return children != null ? children : Collections.emptyList();
  }

  public String getExtension() {
    return extension;
  }

  public void setSize(long size) {
    this.size = size;
  }
}
