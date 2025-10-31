package com.voba.controller;

import com.voba.model.ColorMapping;
import com.voba.service.ColorMappingService;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * REST-Controller für die Verwaltung von Farbzuordnungen. Bietet Endpunkte zum Abrufen, Speichern
 * und Zurücksetzen von Farbzuordnungen.
 */
@RestController
@RequestMapping("/api/color-mappings")
@CrossOrigin
public class ColorMappingController {

  @Autowired private ColorMappingService colorMappingService;

  /**
   * Ruft alle Farbzuordnungen ab.
   *
   * @return ResponseEntity mit der Liste der Farbzuordnungen
   */
  @GetMapping
  public ResponseEntity<List<ColorMapping>> getColorMappings() {
    try {
      List<ColorMapping> mappings = colorMappingService.getColorMappings();
      return ResponseEntity.ok(mappings);
    } catch (Exception exception) {
      return ResponseEntity.internalServerError().build();
    }
  }

  /**
   * Speichert die Farbzuordnungen.
   *
   * @param mappings Liste der zu speichernden Farbzuordnungen
   * @return ResponseEntity mit Erfolgsmeldung oder Fehlermeldung
   */
  @PostMapping
  public ResponseEntity<?> saveColorMappings(@RequestBody List<ColorMapping> mappings) {
    try {
      colorMappingService.saveColorMappings(mappings);
      Map<String, String> response = new HashMap<>();
      response.put("message", "Color mappings saved successfully");
      return ResponseEntity.ok(response);
    } catch (Exception exception) {
      Map<String, String> error = new HashMap<>();
      error.put("error", "Failed to save color mappings: " + exception.getMessage());
      return ResponseEntity.badRequest().body(error);
    }
  }

  /**
   * Setzt die Farbzuordnungen auf die Standardwerte zurück.
   *
   * @return ResponseEntity mit den Standard-Farbzuordnungen oder Fehlermeldung
   */
  @PostMapping("/reset")
  public ResponseEntity<?> resetToDefaults() {
    try {
      List<ColorMapping> defaults = colorMappingService.resetToDefaults();
      return ResponseEntity.ok(defaults);
    } catch (Exception exception) {
      Map<String, String> error = new HashMap<>();
      error.put("error", "Failed to reset to defaults: " + exception.getMessage());
      return ResponseEntity.badRequest().body(error);
    }
  }
}
