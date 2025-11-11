// =============================================================================
// GLOBAL STATE
// =============================================================================

let treemapVisualizer;
let fileTreeExplorer;
let resizeHandle;
let currentData = null;
let colorMap = new Map();
let colorMappings = []; // Array of {extension, color} objects
let minPixelSize = 10;
let syncingSelection = false; // Prevent infinite loops when syncing

// Scan options
let scanOptions = {
    includeHiddenFiles: false,
    useParallelProcessing: false,
    maxThreads: 4
};

// =============================================================================
// INITIALIZATION & SETUP
// =============================================================================

// Initialize application
document.addEventListener('DOMContentLoaded', () => {
    initializeTreemap();
    initializeFileTree();
    initializeResizeHandle();
    initializeEventListeners();
    loadSettings();
});

// Initialize treemap visualizer
function initializeTreemap() {
    const canvas = document.getElementById('treemapCanvas');
    const tooltip = document.getElementById('tooltip');

    treemapVisualizer = new TreemapVisualizer(canvas, {
        tooltip: tooltip,
        colorMap: colorMap,
        minPixelSize: minPixelSize
    });

    treemapVisualizer.setNodeSelectCallback((node, navigationType) => {
        if (!syncingSelection) {
            syncingSelection = true;
            updatePathDisplay(node.path);
            updateSizeDisplay(node.size);

            // Sync with file tree
            if (fileTreeExplorer) {
                if (navigationType === 'navigateToParent') {
                    // When navigating to parent with left arrow, collapse the parent folder
                    fileTreeExplorer.selectAndCollapseNode(node);
                } else if (navigationType === 'zoomIn') {
                    // When zooming in with spacebar, show only that folder in file explorer
                    fileTreeExplorer.setRootNode(node);
                } else if (navigationType === 'zoomOut') {
                    // When zooming out with backspace, update file explorer root
                    fileTreeExplorer.setRootNode(node);
                } else if (navigationType === 'resetView') {
                    // When resetting view with Esc, reset file explorer to original root
                    fileTreeExplorer.resetToOriginalRoot();
                } else {
                    fileTreeExplorer.selectNodeExternal(node);
                }
            }
            syncingSelection = false;
        }
    });
}

// Initialize file tree explorer
function initializeFileTree() {
    const fileTreeContainer = document.getElementById('fileTree');
    fileTreeExplorer = new FileTreeExplorer(fileTreeContainer);

    fileTreeExplorer.setNodeSelectCallback((node) => {
        if (!syncingSelection) {
            syncingSelection = true;
            updatePathDisplay(node.path);
            updateSizeDisplay(node.size);

            // Sync with treemap and focus on selected node
            if (treemapVisualizer) {
                treemapVisualizer.focusOnNode(node);
            }
            syncingSelection = false;
        }
    });
}

// Initialize resize handle
function initializeResizeHandle() {
    const handleElement = document.getElementById('resizeHandle');
    const topPanel = document.getElementById('explorerPanel');
    const bottomPanel = document.getElementById('visualizationPanel');

    resizeHandle = new ResizeHandle(handleElement, topPanel, bottomPanel);

    // Load saved size after a short delay to ensure layout is ready
    setTimeout(() => {
        resizeHandle.loadSavedSize();
        window.dispatchEvent(new Event('resize'));
    }, 100);
}

// Initialize event listeners
function initializeEventListeners() {
    // Scan button
    document.getElementById('scanBtn').addEventListener('click', () => {
        const path = document.getElementById('directoryInput').value.trim();
        if (path) {
            scanDirectory(path);
        } else {
            alert('Bitte geben Sie einen gültigen Verzeichnispfad ein');
        }
    });

    // Settings button
    document.getElementById('settingsBtn').addEventListener('click', () => {
        const modal = new bootstrap.Modal(document.getElementById('settingsModal'));
        modal.show();
    });

    // Save settings button
    document.getElementById('saveSettingsBtn').addEventListener('click', () => {
        saveSettings();
    });

    // Add mapping button
    document.getElementById('addMappingBtn').addEventListener('click', () => {
        addColorMappingRow('', '#808080');
    });

    // Reset mappings button
    document.getElementById('resetMappingsBtn').addEventListener('click', () => {
        if (confirm('Möchten Sie wirklich alle Farbzuordnungen zurücksetzen?')) {
            resetColorMappings();
        }
    });

    // Directory input enter key
    document.getElementById('directoryInput').addEventListener('keypress', (event) => {
        if (event.key === 'Enter' && document.activeElement === document.getElementById('directoryInput')) {
            document.getElementById('scanBtn').click();
        }
    });

    // Prevent arrow key scrolling in file tree
    const fileTree = document.getElementById('fileTree');
    fileTree.addEventListener('keydown', (event) => {
        if (['ArrowUp', 'ArrowDown', 'ArrowLeft', 'ArrowRight'].includes(event.key)) {
            event.preventDefault();
        }
    });
}

// =============================================================================
// DIRECTORY SCANNING
// =============================================================================

// Scan directory
async function scanDirectory(path) {
    showLoading(true);

    try {
        // Build URL with scan options
        const url = new URL('/api/scan', window.location.origin);
        url.searchParams.append('path', path);
        url.searchParams.append('includeHidden', scanOptions.includeHiddenFiles);
        url.searchParams.append('parallel', scanOptions.useParallelProcessing);
        if (scanOptions.useParallelProcessing) {
            url.searchParams.append('maxThreads', scanOptions.maxThreads);
        }

        const response = await fetch(url);

        if (!response.ok) {
            const error = await response.json();
            throw new Error(error.error || 'Failed to scan directory');
        }

        const data = await response.json();
        currentData = data;

        // Update displays
        updatePathDisplay(data.path);
        updateSizeDisplay(data.size);

        // Render treemap and file tree
        treemapVisualizer.setData(data);
        fileTreeExplorer.setData(data);

        // Focus file tree after rendering completes
        setTimeout(() => {
            const fileTree = document.getElementById('fileTree');
            fileTree.focus();
        }, 0);

    } catch (error) {
        alert('Error scanning directory: ' + error.message);
        console.error('Scan error:', error);
    } finally {
        showLoading(false);
    }
}

// =============================================================================
// SETTINGS MANAGEMENT
// =============================================================================

// Load settings
async function loadSettings() {
    // Load settings from localStorage
    const savedSettings = localStorage.getItem('driveVisualizerSettings');
    if (savedSettings) {
        const settings = JSON.parse(savedSettings);
        
        // Load min pixel size
        if (settings.minPixelSize) {
            minPixelSize = settings.minPixelSize;
            document.getElementById('minPixelSize').value = minPixelSize;
        }
        
        // Load scan options
        if (settings.scanOptions) {
            scanOptions = { ...scanOptions, ...settings.scanOptions };
            document.getElementById('includeHiddenFiles').checked = scanOptions.includeHiddenFiles;
            document.getElementById('useParallelProcessing').checked = scanOptions.useParallelProcessing;
            document.getElementById('maxThreads').value = scanOptions.maxThreads;
        }
    }

    // Set default max threads to CPU cores if not set
    const cpuCores = navigator.hardwareConcurrency || 4;
    if (!savedSettings || !savedSettings.scanOptions || !savedSettings.scanOptions.maxThreads) {
        scanOptions.maxThreads = Math.min(cpuCores, 8);
        document.getElementById('maxThreads').value = scanOptions.maxThreads;
    }

    // Update scan options indicator
    updateScanOptionsIndicator();

    // Load color mappings from backend
    await loadColorMappingsFromBackend();
}

// Save settings
async function saveSettings() {
    // Get min pixel size
    minPixelSize = parseInt(document.getElementById('minPixelSize').value) || 10;

    // Get scan options
    scanOptions = {
        includeHiddenFiles: document.getElementById('includeHiddenFiles').checked,
        useParallelProcessing: document.getElementById('useParallelProcessing').checked,
        maxThreads: parseInt(document.getElementById('maxThreads').value) || 4
    };

    // Save settings to localStorage
    const settings = { 
        minPixelSize: minPixelSize,
        scanOptions: scanOptions
    };
    localStorage.setItem('driveVisualizerSettings', JSON.stringify(settings));

    // Get color mappings from UI
    colorMappings = [];
    const rows = document.querySelectorAll('.color-mapping-row');

    rows.forEach(row => {
        const ext = row.querySelector('.ext-input').value.trim().toLowerCase();
        const color = row.querySelector('.color-input').value;

        if (ext) {
            colorMappings.push({
                extension: ext,
                color: color
            });
        }
    });

    // Save color mappings to backend
    try {
        const response = await fetch('/api/color-mappings', {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json'
            },
            body: JSON.stringify(colorMappings)
        });

        if (!response.ok) {
            throw new Error('Failed to save color mappings');
        }

        updateColorMap();
        if (treemapVisualizer) {
            treemapVisualizer.updateMinPixelSize(minPixelSize);
            treemapVisualizer.updateColorMap(colorMap);
        }

        // Update scan options indicator
        updateScanOptionsIndicator();

        bootstrap.Modal.getInstance(document.getElementById('settingsModal')).hide();

        alert('Einstellungen erfolgreich gespeichert!');

    } catch (error) {
        console.error('Error saving color mappings:', error);
        alert('Fehler beim Speichern der Einstellungen: ' + error.message);
    }
}

// =============================================================================
// COLOR MAPPING MANAGEMENT
// =============================================================================

// Load color mappings from backend
async function loadColorMappingsFromBackend() {
    try {
        const response = await fetch('/api/color-mappings');
        if (!response.ok) {
            throw new Error('Failed to load color mappings');
        }

        colorMappings = await response.json();
        updateColorMap();
        renderColorMappingsUI();
    } catch (error) {
        console.error('Error loading color mappings:', error);
        colorMappings = [];
        updateColorMap();
        renderColorMappingsUI();
    }
}

// Update color map from mappings array
function updateColorMap() {
    colorMap.clear();
    colorMappings.forEach(mapping => {
        if (mapping.extension && mapping.color) {
            colorMap.set(mapping.extension.toLowerCase(), mapping.color);
        }
    });
}

// Reset color mappings to defaults
async function resetColorMappings() {
    try {
        const response = await fetch('/api/color-mappings/reset', {
            method: 'POST'
        });

        if (!response.ok) {
            throw new Error('Failed to reset color mappings');
        }

        colorMappings = await response.json();
        updateColorMap();
        renderColorMappingsUI();

        alert('Farbzuordnungen wurden zurückgesetzt!');

    } catch (error) {
        console.error('Error resetting color mappings:', error);
        alert('Fehler beim Zurücksetzen: ' + error.message);
    }
}

// =============================================================================
// UI RENDERING
// =============================================================================

// Render color mappings UI
function renderColorMappingsUI() {
    const container = document.getElementById('colorMappings');
    container.innerHTML = '';

    if (colorMappings.length === 0) {
        container.innerHTML = '<p class="text-muted text-center p-3">Keine Farbzuordnungen. Klicken Sie auf "Hinzufügen", um eine neue hinzuzufügen.</p>';
        return;
    }

    colorMappings.forEach((mapping, index) => {
        addColorMappingRow(mapping.extension, mapping.color, index);
    });
}

// Add a color mapping row
function addColorMappingRow(extension = '', color = '#808080', index = null) {
    const container = document.getElementById('colorMappings');

    // Remove empty message if present
    if (container.querySelector('.text-muted')) {
        container.innerHTML = '';
    }

    const row = document.createElement('div');
    row.className = 'row mb-2 align-items-center color-mapping-row';
    if (index !== null) {
        row.dataset.index = index;
    }

    row.innerHTML = `
        <div class="col-4">
            <input type="text" class="form-control form-control-sm ext-input" 
                   placeholder="z.B. js" value="${extension}" title="Dateierweiterung">
        </div>
        <div class="col-6">
            <input type="color" class="form-control form-control-sm color-input" 
                   value="${color}" title="Farbe">
        </div>
        <div class="col-2">
            <button type="button" class="btn btn-sm btn-danger remove-mapping-btn" title="Entfernen">
                ✕
            </button>
        </div>
    `;

    // Add remove button event listener
    row.querySelector('.remove-mapping-btn').addEventListener('click', () => {
        row.remove();
        // If no more rows, show empty message
        if (container.querySelectorAll('.color-mapping-row').length === 0) {
            container.innerHTML = '<p class="text-muted text-center p-3">Keine Farbzuordnungen. Klicken Sie auf "Hinzufügen", um eine neue hinzuzufügen.</p>';
        }
    });

    container.appendChild(row);
}

// Update path display
function updatePathDisplay(path) {
    const pathElement = document.getElementById('currentPath');
    const displayPath = path || 'No directory selected';
    pathElement.textContent = displayPath;
    pathElement.setAttribute('title', displayPath);
}

// Update size display
function updateSizeDisplay(size) {
    document.getElementById('currentSize').textContent = formatSize(size);
}

// Update scan options indicator
function updateScanOptionsIndicator() {
    const indicator = document.getElementById('scanOptionsIndicator');
    const badges = [];
    
    if (scanOptions.includeHiddenFiles) {
        badges.push('<span class="badge bg-info" title="Versteckte Dateien werden gescannt">👁️ Versteckte</span>');
    }
    
    if (scanOptions.useParallelProcessing) {
        badges.push(`<span class="badge bg-warning text-dark" title="Parallele Verarbeitung mit max. ${scanOptions.maxThreads} Threads">⚡ Parallel (${scanOptions.maxThreads})</span>`);
    }
    
    if (badges.length === 0) {
        badges.push('<span class="badge bg-secondary" title="Standard-Scan: sequentiell, ohne versteckte Dateien">📁 Standard</span>');
    }
    
    indicator.innerHTML = badges.join(' ');
}

// Show/hide loading overlay
function showLoading(show) {
    const overlay = document.getElementById('loadingOverlay');
    if (show) {
        overlay.classList.remove('d-none');
    } else {
        overlay.classList.add('d-none');
    }
}

// =============================================================================
// EVENT HANDLERS
// =============================================================================

// Handle window resize
window.addEventListener('resize', () => {
    if (treemapVisualizer && currentData) {
        treemapVisualizer.render();
    }
});

