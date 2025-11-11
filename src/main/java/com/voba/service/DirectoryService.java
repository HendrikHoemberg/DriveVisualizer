package com.voba.service;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.ForkJoinTask;
import java.util.concurrent.RecursiveTask;

import com.voba.model.FileNode;
import com.voba.model.ScanOptions;

import org.springframework.stereotype.Service;

/**
 * Service für das Scannen von Verzeichnissen.
 *
 * <p>
 * Unterstützt sowohl sequentielles als auch paralleles Scannen.
 * Parallelverarbeitung ist
 * optional und sollte bewusst aktiviert werden, da sie zusätzliche Komplexität
 * und Systemlast mit
 * sich bringt.
 *
 * <p>
 * Die Anzahl der verwendeten Threads ist konfigurierbar, um die Systemlast zu
 * begrenzen.
 */
@Service
public class DirectoryService {

  private static final int MIN_PARALLEL_SIZE = 100; // Minimum files for parallel processing

  /**
   * Scannt ein Verzeichnis mit Standard-Optionen (sequentiell, ohne versteckte
   * Dateien).
   *
   * @param rootPath Pfad zum Wurzelverzeichnis
   * @return FileNode-Objekt mit der Verzeichnisstruktur
   * @throws IllegalArgumentException wenn der Pfad ungültig ist
   */
  public FileNode scanDirectory(String rootPath) {
    return scanDirectory(rootPath, new ScanOptions());
  }

  /**
   * Scannt ein Verzeichnis mit angegebenen Optionen.
   *
   * <p>
   * Die ScanOptions ermöglichen:
   *
   * <ul>
   * <li>Konfiguration, ob versteckte Dateien inkludiert werden sollen
   * <li>Aktivierung/Deaktivierung der Parallelverarbeitung
   * <li>Begrenzung der Anzahl verwendeter Threads
   * </ul>
   *
   * @param rootPath Pfad zum Wurzelverzeichnis
   * @param options  Scan-Optionen (null = Standard-Optionen)
   * @return FileNode-Objekt mit der Verzeichnisstruktur
   * @throws IllegalArgumentException wenn der Pfad ungültig ist
   */
  public FileNode scanDirectory(String rootPath, ScanOptions options) {
    File rootFile = new File(rootPath);
    if (!rootFile.exists() || !rootFile.isDirectory()) {
      throw new IllegalArgumentException("Invalid directory path: " + rootPath);
    }

    if (options == null) {
      options = new ScanOptions();
    }

    FileNode result;

    if (options.isUseParallelProcessing()) {
      // Parallele Verarbeitung mit Thread-Limit
      ForkJoinPool pool = new ForkJoinPool(options.getMaxThreads());
      try {
        result = pool.invoke(new DirectoryScanTask(rootFile, options));
      } finally {
        pool.shutdown();
      }
    } else {
      // Sequentielle Verarbeitung (einfacher, deterministischer, testbarer)
      result = new DirectoryScanTask(rootFile, options).compute();
    }

    result.sortChildren();
    return result;
  }

  /**
   * Rekursive Task-Klasse für das Scannen von Verzeichnissen.
   *
   * <p>
   * Kann sowohl sequentiell als auch parallel (via Fork/Join-Framework)
   * ausgeführt werden.
   * Respektiert die übergebenen ScanOptions.
   */
  private static class DirectoryScanTask extends RecursiveTask<FileNode> {
    private final File file;
    private final ScanOptions options;

    /**
     * Konstruktor für eine Verzeichnis-Scan-Aufgabe.
     *
     * @param file    Zu scannende Datei oder Verzeichnis
     * @param options Scan-Optionen
     */
    public DirectoryScanTask(File file, ScanOptions options) {
      this.file = file;
      this.options = options;
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
          // Entscheide ob parallel oder sequentiell verarbeitet werden soll
          boolean shouldParallelize = options.isUseParallelProcessing() && children.length >= MIN_PARALLEL_SIZE;

          if (shouldParallelize) {
            // Parallele Verarbeitung für große Verzeichnisse
            invokeAll(createSubtasks(children)).stream()
                .map(ForkJoinTask::join)
                .filter(childNode -> childNode != null)
                .forEach(node::addChild);
          } else {
            // Sequentielle Verarbeitung für kleine Verzeichnisse oder wenn deaktiviert
            for (File child : children) {
              if (shouldProcess(child)) {
                FileNode childNode = new DirectoryScanTask(child, options).compute();
                if (childNode != null) {
                  node.addChild(childNode);
                }
              }
            }
          }
        }
      }

      return node;
    }

    /**
     * Erstellt Subtasks für parallele Verarbeitung.
     *
     * @param children Array von zu verarbeitenden Dateien
     * @return Liste von DirectoryScanTask für jede zu verarbeitende Datei
     */
    private java.util.List<DirectoryScanTask> createSubtasks(File[] children) {
      java.util.List<DirectoryScanTask> tasks = new java.util.ArrayList<>();
      for (File child : children) {
        if (shouldProcess(child)) {
          tasks.add(new DirectoryScanTask(child, options));
        }
      }
      return tasks;
    }

    /**
     * Prüft, ob eine Datei verarbeitet werden soll.
     *
     * @param file Zu prüfende Datei
     * @return true wenn die Datei verarbeitet werden soll, false sonst
     */
    private boolean shouldProcess(File file) {
      try {
        Path path = file.toPath();

        // Symlinks immer ausschließen (vermeidet Endlosschleifen)
        if (Files.isSymbolicLink(path)) {
          return false;
        }

        // Versteckte Dateien nur verarbeiten, wenn explizit aktiviert
        if (file.isHidden() && !options.isIncludeHiddenFiles()) {
          return false;
        }

        return true;
      } catch (Exception exception) {
        // Bei Fehler (z.B. Permission denied) Datei nicht verarbeiten
        return false;
      }
    }
  }
}
