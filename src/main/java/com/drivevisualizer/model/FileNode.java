package com.drivevisualizer.model;

import lombok.Data;
import java.util.ArrayList;
import java.util.List;

@Data
public class FileNode {
    private String name;
    private String path;
    private long size;
    private boolean isDirectory;
    private List<FileNode> children;
    private String extension;
    
    public FileNode() {
        this.children = new ArrayList<>();
        this.size = 0;
    }
    
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
    
    public void addChild(FileNode child) {
        children.add(child);
        updateParentSizes(child.size);
    }
    
    private void updateParentSizes(long additionalSize) {
        this.size += additionalSize;
    }
    
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
