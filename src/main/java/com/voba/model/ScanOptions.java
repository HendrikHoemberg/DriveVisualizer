package com.voba.model;

/**
 * Konfigurationsoptionen für das Scannen von Verzeichnissen.
 * Ermöglicht feingranulare Kontrolle über das Scan-Verhalten.
 */
public class ScanOptions {

    private boolean includeHiddenFiles = false;
    private boolean useParallelProcessing = false;
    private int maxThreads = Runtime.getRuntime().availableProcessors();

    /**
     * Standard-Konstruktor mit sicheren Standardwerten.
     * - Versteckte Dateien werden nicht gescannt (sicherer Standard)
     * - Keine Parallelverarbeitung (einfacher, deterministischer)
     * - Max. Threads = Anzahl der verfügbaren Prozessoren
     */
    public ScanOptions() {
    }

    /**
     * Gibt an, ob versteckte Dateien gescannt werden sollen.
     *
     * @return true wenn versteckte Dateien inkludiert werden sollen
     */
    public boolean isIncludeHiddenFiles() {
        return includeHiddenFiles;
    }

    /**
     * Setzt, ob versteckte Dateien gescannt werden sollen.
     * Hilfreich beim Suchen nach Speicherfressern, die versteckt sind.
     *
     * @param includeHiddenFiles true um versteckte Dateien zu inkludieren
     * @return diese ScanOptions-Instanz für Method-Chaining
     */
    public ScanOptions setIncludeHiddenFiles(boolean includeHiddenFiles) {
        this.includeHiddenFiles = includeHiddenFiles;
        return this;
    }

    /**
     * Gibt an, ob Parallelverarbeitung verwendet werden soll.
     *
     * @return true wenn parallele Verarbeitung aktiviert ist
     */
    public boolean isUseParallelProcessing() {
        return useParallelProcessing;
    }

    /**
     * Setzt, ob Parallelverarbeitung verwendet werden soll.
     * Parallelverarbeitung kann bei großen Verzeichnissen schneller sein,
     * erhöht aber die Komplexität und Systemlast.
     *
     * @param useParallelProcessing true um parallele Verarbeitung zu aktivieren
     * @return diese ScanOptions-Instanz für Method-Chaining
     */
    public ScanOptions setUseParallelProcessing(boolean useParallelProcessing) {
        this.useParallelProcessing = useParallelProcessing;
        return this;
    }

    /**
     * Gibt die maximale Anzahl von Threads für Parallelverarbeitung zurück.
     *
     * @return maximale Anzahl von Threads
     */
    public int getMaxThreads() {
        return maxThreads;
    }

    /**
     * Setzt die maximale Anzahl von Threads für Parallelverarbeitung.
     * Begrenzt die Systemlast bei paralleler Verarbeitung.
     *
     * @param maxThreads maximale Anzahl von Threads (min. 1)
     * @return diese ScanOptions-Instanz für Method-Chaining
     * @throws IllegalArgumentException wenn maxThreads < 1
     */
    public ScanOptions setMaxThreads(int maxThreads) {
        if (maxThreads < 1) {
            throw new IllegalArgumentException("maxThreads must be at least 1");
        }
        this.maxThreads = maxThreads;
        return this;
    }
}
