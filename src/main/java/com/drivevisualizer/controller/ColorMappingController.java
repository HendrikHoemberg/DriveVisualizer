package com.drivevisualizer.controller;

import com.drivevisualizer.model.ColorMapping;
import com.drivevisualizer.service.ColorMappingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/color-mappings")
@CrossOrigin
public class ColorMappingController {
    
    @Autowired
    private ColorMappingService colorMappingService;
    
    @GetMapping
    public ResponseEntity<List<ColorMapping>> getColorMappings() {
        try {
            List<ColorMapping> mappings = colorMappingService.getColorMappings();
            return ResponseEntity.ok(mappings);
        } catch (Exception exception) {
            return ResponseEntity.internalServerError().build();
        }
    }
    
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
