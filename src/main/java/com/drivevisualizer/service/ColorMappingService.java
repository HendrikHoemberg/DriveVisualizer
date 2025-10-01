package com.drivevisualizer.service;

import com.drivevisualizer.model.ColorMapping;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.List;

/**
 * Service für die Verwaltung von Farbzuordnungen.
 * Verwaltet das Laden, Speichern und Zurücksetzen von Farbzuordnungen
 * aus Benutzer- und Standard-Konfigurationsdateien.
 */
@Service
public class ColorMappingService {
    
    private static final String USER_CONFIG_FILE = System.getProperty("user.home") + "/.drivevisualizer/color-mappings.json";
    private static final String DEFAULT_CONFIG_FILE = "color-mappings.json";
    private final ObjectMapper objectMapper = new ObjectMapper();
    
    /**
     * Ruft die Farbzuordnungen ab - versucht zuerst die Benutzerkonfiguration, 
     * fällt auf Standardwerte zurück.
     * 
     * @return Liste der Farbzuordnungen
     */
    public List<ColorMapping> getColorMappings() {
        try {
            // Try to load user config first
            File userConfigFile = new File(USER_CONFIG_FILE);
            if (userConfigFile.exists()) {
                return objectMapper.readValue(userConfigFile, new TypeReference<List<ColorMapping>>() {});
            }
            
            // Fall back to default config from resources
            ClassPathResource resource = new ClassPathResource(DEFAULT_CONFIG_FILE);
            return objectMapper.readValue(resource.getInputStream(), new TypeReference<List<ColorMapping>>() {});
            
        } catch (IOException ioException) {
            // If all else fails, return empty list
            return new ArrayList<>();
        }
    }
    
    /**
     * Speichert die Farbzuordnungen in der Benutzerkonfigurationsdatei.
     * 
     * @param mappings Liste der zu speichernden Farbzuordnungen
     * @throws IOException wenn ein Fehler beim Speichern auftritt
     */
    public void saveColorMappings(List<ColorMapping> mappings) throws IOException {
        // Ensure directory exists
        File configDir = new File(System.getProperty("user.home"), ".drivevisualizer");
        if (!configDir.exists()) {
            configDir.mkdirs();
        }
        
        // Write to user config file
        String json = objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(mappings);
        Files.write(Paths.get(USER_CONFIG_FILE), json.getBytes(), 
                    StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
    }
    
    /**
     * Setzt die Farbzuordnungen auf die Standardwerte zurück.
     * 
     * @return Liste der Standard-Farbzuordnungen
     * @throws IOException wenn ein Fehler beim Laden oder Speichern auftritt
     */
    public List<ColorMapping> resetToDefaults() throws IOException {
        // Load defaults from resources
        ClassPathResource resource = new ClassPathResource(DEFAULT_CONFIG_FILE);
        List<ColorMapping> defaults = objectMapper.readValue(resource.getInputStream(), 
                                                             new TypeReference<List<ColorMapping>>() {});
        
        // Save as user config
        saveColorMappings(defaults);
        
        return defaults;
    }
}
