package com.voba.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.nio.file.Paths;

import com.voba.model.FileNode;
import com.voba.model.ScanOptions;
import com.voba.service.DirectoryService;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

/** Unit-Tests für den FileSystemController. */
@WebMvcTest(FileSystemController.class)
class FileSystemControllerTest {

  @Autowired
  private MockMvc mockMvc;

  @MockitoBean
  private DirectoryService directoryService;

  @Test
  void testScanDirectory() throws Exception {
    FileNode mockNode = new FileNode(Paths.get("test"), true);
    mockNode.setSize(1000);

    when(directoryService.scanDirectory(anyString(), any(ScanOptions.class))).thenReturn(mockNode);

    mockMvc
        .perform(get("/api/scan").param("path", "test/path"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.name").value("test"))
        .andExpect(jsonPath("$.path").value(mockNode.getPath()))
        .andExpect(jsonPath("$.directory").value(true))
        .andExpect(jsonPath("$.size").value(1000));

    verify(directoryService, times(1)).scanDirectory(anyString(), any(ScanOptions.class));
  }

  @Test
  void testScanDirectoryWithException() throws Exception {
    when(directoryService.scanDirectory(anyString(), any(ScanOptions.class)))
        .thenThrow(new IllegalArgumentException("Invalid path"));

    mockMvc
        .perform(get("/api/scan").param("path", "invalid/path"))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.error").value("Invalid path"));

    verify(directoryService, times(1)).scanDirectory(anyString(), any(ScanOptions.class));
  }

  @Test
  void testGetAvailableDrives() throws Exception {
    mockMvc
        .perform(get("/api/drives"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$").isArray());
  }

  @Test
  void testScanDirectoryWithFile() throws Exception {
    FileNode fileNode = new FileNode(Paths.get("path", "file.txt"), false);
    fileNode.setSize(500);

    when(directoryService.scanDirectory(anyString(), any(ScanOptions.class))).thenReturn(fileNode);

    mockMvc
        .perform(get("/api/scan").param("path", "path"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.name").value("file.txt"))
        .andExpect(jsonPath("$.directory").value(false))
        .andExpect(jsonPath("$.extension").value("txt"));

    verify(directoryService, times(1)).scanDirectory(anyString(), any(ScanOptions.class));
  }

  @Test
  void testScanDirectoryWithOptions() throws Exception {
    FileNode mockNode = new FileNode(Paths.get("test"), true);
    mockNode.setSize(1000);

    when(directoryService.scanDirectory(anyString(), any(ScanOptions.class))).thenReturn(mockNode);

    mockMvc
        .perform(
            get("/api/scan")
                .param("path", "test/path")
                .param("includeHidden", "true")
                .param("parallel", "true")
                .param("maxThreads", "4"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.directory").value(true));

    verify(directoryService, times(1)).scanDirectory(anyString(), any(ScanOptions.class));
  }
}
