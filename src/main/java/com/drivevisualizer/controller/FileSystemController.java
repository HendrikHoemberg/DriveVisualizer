package com.drivevisualizer.controller;

import com.drivevisualizer.model.FileNode;
import com.drivevisualizer.service.DirectoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api")
@CrossOrigin
public class FileSystemController {
    
    @Autowired
    private DirectoryService directoryService;
    
    @GetMapping("/scan")
    public ResponseEntity<?> scanDirectory(@RequestParam String path) {
        try {
            FileNode result = directoryService.scanDirectory(path);
            return ResponseEntity.ok(result);
        } catch (Exception exception) {
            Map<String, String> error = new HashMap<>();
            error.put("error", exception.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }
    
    @GetMapping("/drives")
    public ResponseEntity<List<Map<String, String>>> getAvailableDrives() {
        List<Map<String, String>> drives = new ArrayList<>();
        
        // For Windows
        File[] roots = File.listRoots();
        for (File root : roots) {
            if (root.exists() && root.canRead()) {
                Map<String, String> drive = new HashMap<>();
                drive.put("path", root.getAbsolutePath());
                drive.put("name", root.getAbsolutePath());
                drive.put("totalSpace", String.valueOf(root.getTotalSpace()));
                drive.put("freeSpace", String.valueOf(root.getFreeSpace()));
                drive.put("usableSpace", String.valueOf(root.getUsableSpace()));
                drives.add(drive);
            }
        }
        
        // For Unix-like systems, add home directory
        String os = System.getProperty("os.name").toLowerCase();
        if (os.contains("nix") || os.contains("nux") || os.contains("mac")) {
            Map<String, String> home = new HashMap<>();
            home.put("path", System.getProperty("user.home"));
            home.put("name", "Home Directory");
            drives.add(home);
        }
        
        return ResponseEntity.ok(drives);
    }
}
