package com.voba.controller;

import com.voba.model.ColorMapping;
import com.voba.service.ColorMappingService;
import java.io.IOException;
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
    List<ColorMapping> mappings = colorMappingService.getColorMappings();
    return ResponseEntity.ok(mappings);
  }

  /**
   * Speichert die Farbzuordnungen.
   *
   * @param mappings Liste der zu speichernden Farbzuordnungen
   * @return ResponseEntity mit Erfolgsmeldung
   * @throws IOException wenn ein Fehler beim Speichern auftritt
   */
  @PostMapping
  public ResponseEntity<?> saveColorMappings(@RequestBody List<ColorMapping> mappings)
      throws IOException {
    colorMappingService.saveColorMappings(mappings);
    Map<String, String> response = new HashMap<>();
    response.put("message", "Color mappings saved successfully");
    return ResponseEntity.ok(response);
  }

  /**
   * Setzt die Farbzuordnungen auf die Standardwerte zurück.
   *
   * @return ResponseEntity mit den Standard-Farbzuordnungen
   * @throws IOException wenn ein Fehler beim Laden oder Speichern auftritt
   */
  @PostMapping("/reset")
  public ResponseEntity<?> resetToDefaults() throws IOException {
    List<ColorMapping> defaults = colorMappingService.resetToDefaults();
    return ResponseEntity.ok(defaults);
  }
}
