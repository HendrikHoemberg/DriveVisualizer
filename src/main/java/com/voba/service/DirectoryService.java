package com.voba.service;

import com.voba.model.FileNode;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.ForkJoinTask;
import java.util.concurrent.RecursiveTask;
import org.springframework.stereotype.Service;

/**
 * Service für das Scannen von Verzeichnissen. Verwendet parallele Verarbeitung (ForkJoinPool) für
 * effizientes Scannen großer Verzeichnisstrukturen.
 */
@Service
public class DirectoryService {

  private static final int MIN_PARALLEL_SIZE = 100; // Minimum files for parallel processing

  /**
   * Scannt ein Verzeichnis und erstellt eine hierarchische Dateistruktur.
   *
   * @param rootPath Pfad zum Wurzelverzeichnis
   * @return FileNode-Objekt mit der Verzeichnisstruktur
   * @throws IllegalArgumentException wenn der Pfad ungültig ist
   */
  public FileNode scanDirectory(String rootPath) {
    File rootFile = new File(rootPath);
    if (!rootFile.exists() || !rootFile.isDirectory()) {
      throw new IllegalArgumentException("Invalid directory path: " + rootPath);
    }

    ForkJoinPool pool = new ForkJoinPool();
    try {
      FileNode result = pool.invoke(new DirectoryScanTask(rootFile));
      result.sortChildren();
      return result;
    } finally {
      pool.shutdown();
    }
  }

  /**
   * Rekursive Task-Klasse für das parallele Scannen von Verzeichnissen. Nutzt das
   * Fork/Join-Framework für optimale Performance.
   */
  private static class DirectoryScanTask extends RecursiveTask<FileNode> {
    private final File file;

    /**
     * Konstruktor für eine Verzeichnis-Scan-Aufgabe.
     *
     * @param file Zu scannende Datei oder Verzeichnis
     */
    public DirectoryScanTask(File file) {
      this.file = file;
    }

    @Override
    protected FileNode compute() {
      FileNode node = new FileNode(file.toPath(), file.isDirectory());

      if (file.isFile()) {
        node.setSize(file.length());
        return node;
      }

      if (file.isDirectory()) {
        File[] children = file.listFiles();
        if (children != null && children.length > 0) {
          if (children.length < MIN_PARALLEL_SIZE) {
            for (File child : children) {
              if (shouldProcess(child)) {
                FileNode childNode = compute(child, node);
                if (childNode != null) {
                  node.addChild(childNode);
                }
              }
            }
          } else {
            // Process in parallel for large directories
            invokeAll(createSubtasks(children, node)).stream()
                .map(ForkJoinTask::join)
                .filter(childNode -> childNode != null)
                .forEach(node::addChild);
          }
        }
      }

      return node;
    }

    private FileNode compute(File file, FileNode parent) {
      DirectoryScanTask task = new DirectoryScanTask(file);
      return task.compute();
    }

    private java.util.List<DirectoryScanTask> createSubtasks(File[] children, FileNode parent) {
      java.util.List<DirectoryScanTask> tasks = new java.util.ArrayList<>();
      for (File child : children) {
        if (shouldProcess(child)) {
          tasks.add(new DirectoryScanTask(child));
        }
      }
      return tasks;
    }

    private boolean shouldProcess(File file) {
      try {
        Path path = file.toPath();

        if (Files.isSymbolicLink(path)) {
          return false;
        }

        if (file.isHidden()) {
          return false;
        }

        return true;
      } catch (Exception exception) {
        return false;
      }
    }
  }
}
