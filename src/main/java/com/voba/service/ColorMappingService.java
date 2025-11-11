package com.voba.service;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.Collections;
import java.util.List;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.voba.model.ColorMapping;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

/**
 * Service für die Verwaltung von Farbzuordnungen. Verwaltet das Laden,
 * Speichern und Zurücksetzen
 * von Farbzuordnungen aus Benutzer- und Standard-Konfigurationsdateien.
 */
@Service
public class ColorMappingService {

  private static final Logger logger = LoggerFactory.getLogger(ColorMappingService.class);
  private static final String USER_CONFIG_FILE = System.getProperty("user.home")
      + "/.drivevisualizer/color-mappings.json";
  private static final String DEFAULT_CONFIG_FILE = "color-mappings.json";
  private static final ObjectMapper objectMapper = new ObjectMapper();

  /**
   * Ruft die Farbzuordnungen ab - versucht zuerst die Benutzerkonfiguration,
   * fällt auf
   * Standardwerte zurück.
   *
   * @return Liste der Farbzuordnungen
   */
  public List<ColorMapping> getColorMappings() {
    try {
      File userConfigFile = new File(USER_CONFIG_FILE);
      if (userConfigFile.exists()) {
        return objectMapper.readValue(userConfigFile, new TypeReference<List<ColorMapping>>() {
        });
      }

      return loadDefaultColorMappings();

    } catch (IOException ioException) {
      logger.error("Fehler beim Laden der Farbzuordnungen", ioException);
      return Collections.emptyList();
    }
  }

  /**
   * Speichert die Farbzuordnungen in der Benutzerkonfigurationsdatei.
   *
   * @param mappings Liste der zu speichernden Farbzuordnungen
   * @throws IOException wenn ein Fehler beim Speichern auftritt
   */
  public void saveColorMappings(List<ColorMapping> mappings) throws IOException {
    File configDir = new File(System.getProperty("user.home"), ".drivevisualizer");
    if (!configDir.exists()) {
      configDir.mkdirs();
    }

    String json = objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(mappings);
    Files.write(
        Paths.get(USER_CONFIG_FILE),
        json.getBytes(),
        StandardOpenOption.CREATE,
        StandardOpenOption.TRUNCATE_EXISTING);
  }

  /**
   * Setzt die Farbzuordnungen auf die Standardwerte zurück.
   *
   * @return Liste der Standard-Farbzuordnungen
   * @throws IOException wenn ein Fehler beim Laden oder Speichern auftritt
   */
  public List<ColorMapping> resetToDefaults() throws IOException {
    List<ColorMapping> defaults = loadDefaultColorMappings();
    saveColorMappings(defaults);
    return defaults;
  }

  /**
   * Lädt die Standard-Farbzuordnungen aus der Classpath-Ressource.
   *
   * @return Liste der Standard-Farbzuordnungen
   * @throws IOException wenn ein Fehler beim Laden auftritt
   */
  private List<ColorMapping> loadDefaultColorMappings() throws IOException {
    ClassPathResource resource = new ClassPathResource(DEFAULT_CONFIG_FILE);
    return objectMapper.readValue(
        resource.getInputStream(), new TypeReference<List<ColorMapping>>() {
        });
  }
}
