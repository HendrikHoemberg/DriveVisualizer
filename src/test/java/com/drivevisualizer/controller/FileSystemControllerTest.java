package com.drivevisualizer.controller;

import com.drivevisualizer.model.FileNode;
import com.drivevisualizer.service.DirectoryService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Unit-Tests für den FileSystemController.
 */
@WebMvcTest(FileSystemController.class)
class FileSystemControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private DirectoryService directoryService;

    @Test
    void testScanDirectory() throws Exception {
        FileNode mockNode = new FileNode("test", "/test", true);
        mockNode.setSize(1000);

        when(directoryService.scanDirectory(anyString())).thenReturn(mockNode);

        mockMvc.perform(get("/api/scan")
                        .param("path", "/test/path"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("test"))
                .andExpect(jsonPath("$.path").value("/test"))
                .andExpect(jsonPath("$.directory").value(true))
                .andExpect(jsonPath("$.size").value(1000));

        verify(directoryService, times(1)).scanDirectory("/test/path");
    }

    @Test
    void testScanDirectoryWithException() throws Exception {
        when(directoryService.scanDirectory(anyString()))
                .thenThrow(new IllegalArgumentException("Invalid path"));

        mockMvc.perform(get("/api/scan")
                        .param("path", "/invalid/path"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Invalid path"));

        verify(directoryService, times(1)).scanDirectory("/invalid/path");
    }

    @Test
    void testGetAvailableDrives() throws Exception {
        mockMvc.perform(get("/api/drives"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
        
        // No service mock needed as this method directly accesses File.listRoots()
    }

    @Test
    void testScanDirectoryWithFile() throws Exception {
        FileNode fileNode = new FileNode("file.txt", "/path/file.txt", false);
        fileNode.setSize(500);

        when(directoryService.scanDirectory(anyString())).thenReturn(fileNode);

        mockMvc.perform(get("/api/scan")
                        .param("path", "/path"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("file.txt"))
                .andExpect(jsonPath("$.directory").value(false))
                .andExpect(jsonPath("$.extension").value("txt"));

        verify(directoryService, times(1)).scanDirectory("/path");
    }
}
