package com.voba.model;

/**
 * Modellklasse für die Zuordnung von Dateierweiterungen zu Farben.
 * Enthält Informationen über Dateierweiterung, Farbwert und Anzeigenamen.
 */
public class ColorMapping {
    private String extension;
    private String color;
    private String name;
    
    public ColorMapping() {
    }
    
    public ColorMapping(String extension, String color, String name) {
        this.extension = extension;
        this.color = color;
        this.name = name;
    }
    
    public String getExtension() {
        return extension;
    }
    
    public void setExtension(String extension) {
        this.extension = extension;
    }
    
    public String getColor() {
        return color;
    }
    
    public void setColor(String color) {
        this.color = color;
    }
    
    public String getName() {
        return name;
    }
    
    public void setName(String name) {
        this.name = name;
    }
}
