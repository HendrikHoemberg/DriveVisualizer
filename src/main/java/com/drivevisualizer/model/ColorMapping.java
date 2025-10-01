package com.drivevisualizer.model;

/**
 * Modellklasse für die Zuordnung von Dateierweiterungen zu Farben.
 * Enthält Informationen über Dateierweiterung, Farbwert und Anzeigenamen.
 */
public class ColorMapping {
    private String extension;
    private String color;
    private String name;
    
    /**
     * Standardkonstruktor.
     */
    public ColorMapping() {
    }
    
    /**
     * Konstruktor mit allen Parametern.
     * 
     * @param extension Dateierweiterung
     * @param color Farbwert
     * @param name Anzeigename
     */
    public ColorMapping(String extension, String color, String name) {
        this.extension = extension;
        this.color = color;
        this.name = name;
    }
    
    /**
     * Gibt die Dateierweiterung zurück.
     * 
     * @return Dateierweiterung
     */
    public String getExtension() {
        return extension;
    }
    
    /**
     * Setzt die Dateierweiterung.
     * 
     * @param extension Dateierweiterung
     */
    public void setExtension(String extension) {
        this.extension = extension;
    }
    
    /**
     * Gibt den Farbwert zurück.
     * 
     * @return Farbwert
     */
    public String getColor() {
        return color;
    }
    
    /**
     * Setzt den Farbwert.
     * 
     * @param color Farbwert
     */
    public void setColor(String color) {
        this.color = color;
    }
    
    /**
     * Gibt den Anzeigenamen zurück.
     * 
     * @return Anzeigename
     */
    public String getName() {
        return name;
    }
    
    /**
     * Setzt den Anzeigenamen.
     * 
     * @param name Anzeigename
     */
    public void setName(String name) {
        this.name = name;
    }
}
