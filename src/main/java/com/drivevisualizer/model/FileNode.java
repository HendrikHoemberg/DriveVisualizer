package com.drivevisualizer.model;

import lombok.Data;
import java.util.ArrayList;
import java.util.List;

/**
 * Modellklasse für einen Knoten in der Dateisystem-Hierarchie.
 * Repräsentiert eine Datei oder ein Verzeichnis mit allen relevanten Informationen.
 */
@Data
public class FileNode {
    private String name;
    private String path;
    private long size;
    private boolean isDirectory;
    private List<FileNode> children;
    private String extension;
    
    /**
     * Standardkonstruktor.
     */
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
    
    /**
     * Sortiert die Kind-Elemente nach Größe und Name (rekursiv).
     */
    public void sortChildren() {
        children.sort((a, b) -> {
            int sizeCompare = Long.compare(b.size, a.size);
            if (sizeCompare != 0) {
                return sizeCompare;
            }
            return a.name.compareToIgnoreCase(b.name);
        });
        
        for (FileNode child : children) {
            if (child.isDirectory) {
                child.sortChildren();
            }
        }
    }
}
