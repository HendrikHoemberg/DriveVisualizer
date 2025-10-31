package com.voba.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.voba.model.ColorMapping;
import com.voba.service.ColorMappingService;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

/** Unit-Tests für den ColorMappingController. */
@WebMvcTest(ColorMappingController.class)
class ColorMappingControllerTest {

  @Autowired private MockMvc mockMvc;

  @MockitoBean private ColorMappingService colorMappingService;

  @Autowired private ObjectMapper objectMapper;

  @Test
  void testGetColorMappings() throws Exception {
    List<ColorMapping> mockMappings = new ArrayList<>();
    mockMappings.add(new ColorMapping("java", "#FF0000"));
    mockMappings.add(new ColorMapping("txt", "#00FF00"));

    when(colorMappingService.getColorMappings()).thenReturn(mockMappings);

    mockMvc
        .perform(get("/api/color-mappings"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$").isArray())
        .andExpect(jsonPath("$.length()").value(2))
        .andExpect(jsonPath("$[0].extension").value("java"))
        .andExpect(jsonPath("$[1].extension").value("txt"));

    verify(colorMappingService, times(1)).getColorMappings();
  }

  @Test
  void testGetColorMappingsWithException() throws Exception {
    when(colorMappingService.getColorMappings()).thenThrow(new RuntimeException("Test error"));

    mockMvc.perform(get("/api/color-mappings")).andExpect(status().isInternalServerError());

    verify(colorMappingService, times(1)).getColorMappings();
  }

  @Test
  void testSaveColorMappings() throws Exception {
    List<ColorMapping> testMappings = new ArrayList<>();
    testMappings.add(new ColorMapping("java", "#FF0000"));

    doNothing().when(colorMappingService).saveColorMappings(any());

    mockMvc
        .perform(
            post("/api/color-mappings")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(testMappings)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.message").value("Color mappings saved successfully"));

    verify(colorMappingService, times(1)).saveColorMappings(any());
  }

  @Test
  void testSaveColorMappingsWithException() throws Exception {
    List<ColorMapping> testMappings = new ArrayList<>();
    testMappings.add(new ColorMapping("java", "#FF0000"));

    doThrow(new RuntimeException("Save failed")).when(colorMappingService).saveColorMappings(any());

    mockMvc
        .perform(
            post("/api/color-mappings")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(testMappings)))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.error").exists());

    verify(colorMappingService, times(1)).saveColorMappings(any());
  }

  @Test
  void testResetToDefaults() throws Exception {
    List<ColorMapping> defaultMappings = new ArrayList<>();
    defaultMappings.add(new ColorMapping("default", "#000000"));

    when(colorMappingService.resetToDefaults()).thenReturn(defaultMappings);

    mockMvc
        .perform(post("/api/color-mappings/reset"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$").isArray())
        .andExpect(jsonPath("$.length()").value(1))
        .andExpect(jsonPath("$[0].extension").value("default"));

    verify(colorMappingService, times(1)).resetToDefaults();
  }

  @Test
  void testResetToDefaultsWithException() throws Exception {
    when(colorMappingService.resetToDefaults()).thenThrow(new RuntimeException("Reset failed"));

    mockMvc
        .perform(post("/api/color-mappings/reset"))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.error").exists());

    verify(colorMappingService, times(1)).resetToDefaults();
  }
}
