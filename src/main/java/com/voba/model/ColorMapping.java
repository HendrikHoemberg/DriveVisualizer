package com.voba.model;

/**
 * Modellklasse für die Zuordnung von Dateierweiterungen zu Farben. Enthält Informationen über
 * Dateierweiterung und Farbwert.
 */
public class ColorMapping {
  private String extension;
  private String color;

  public ColorMapping() {}

  public ColorMapping(String extension, String color) {
    this.extension = extension;
    this.color = color;
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
}
