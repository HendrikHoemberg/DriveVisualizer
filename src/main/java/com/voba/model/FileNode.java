package com.voba.model;

import java.util.ArrayList;
import java.util.List;

/**
 * Modellklasse für einen Knoten in der Dateisystem-Hierarchie. Repräsentiert eine Datei oder ein
 * Verzeichnis mit allen relevanten Informationen.
 */
public class FileNode {
  private String name;
  private String path;
  private long size;
  private boolean isDirectory;
  private List<FileNode> children;
  private String extension;

  /** Standardkonstruktor. */
  public FileNode() {
    this.children = new ArrayList<>();
    this.size = 0;
  }

  /**
   * Konstruktor mit Parametern.
   *
   * @param name Name der Datei oder des Verzeichnisses
   * @param path Pfad zur Datei oder zum Verzeichnis
   * @param isDirectory true, wenn es sich um ein Verzeichnis handelt
   */
  public FileNode(String name, String path, boolean isDirectory) {
    this.name = name;
    this.path = path;
    this.isDirectory = isDirectory;
    this.children = new ArrayList<>();
    this.size = 0;

    if (!isDirectory && name.contains(".")) {
      int lastDot = name.lastIndexOf(".");
      this.extension = name.substring(lastDot + 1).toLowerCase();
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
    children.sort(
        (firstChild, secondChild) -> {
          int sizeCompare = Long.compare(secondChild.size, firstChild.size);
          if (sizeCompare != 0) {
            return sizeCompare;
          }
          return firstChild.name.compareToIgnoreCase(secondChild.name);
        });

    for (FileNode child : children) {
      if (child.isDirectory) {
        child.sortChildren();
      }
    }
  }

  public String getName() {
    return name;
  }

  public String getPath() {
    return path;
  }

  public long getSize() {
    return size;
  }

  public boolean isDirectory() {
    return isDirectory;
  }

  public List<FileNode> getChildren() {
    return children;
  }

  public String getExtension() {
    return extension;
  }

  public void setName(String name) {
    this.name = name;
  }

  public void setPath(String path) {
    this.path = path;
  }

  public void setSize(long size) {
    this.size = size;
  }

  public void setDirectory(boolean directory) {
    isDirectory = directory;
  }

  public void setChildren(List<FileNode> children) {
    this.children = children;
  }

  public void setExtension(String extension) {
    this.extension = extension;
  }
}
