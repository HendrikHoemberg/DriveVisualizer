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
    
    // Set callback for node selection
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
    
    // Set callback for node selection
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
}

// =============================================================================
// DIRECTORY SCANNING
// =============================================================================

// Scan directory
async function scanDirectory(path) {
    showLoading(true);
    
    try {
        const response = await fetch(`/api/scan?path=${encodeURIComponent(path)}`);
        
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
    // Load min pixel size from localStorage
    const savedSettings = localStorage.getItem('driveVisualizerSettings');
    if (savedSettings) {
        const settings = JSON.parse(savedSettings);
        if (settings.minPixelSize) {
            minPixelSize = settings.minPixelSize;
            document.getElementById('minPixelSize').value = minPixelSize;
        }
    }
    
    // Load color mappings from backend
    await loadColorMappingsFromBackend();
}

// Save settings
async function saveSettings() {
    // Get min pixel size
    minPixelSize = parseInt(document.getElementById('minPixelSize').value) || 10;
    
    // Save min pixel size to localStorage
    const settings = { minPixelSize: minPixelSize };
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
    document.getElementById('currentPath').textContent = path || 'No directory selected';
}

// Update size display
function updateSizeDisplay(size) {
    document.getElementById('currentSize').textContent = formatSize(size);
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
// UTILITY FUNCTIONS
// =============================================================================

// Format size
function formatSize(bytes) {
    if (bytes === 0) return '0 B';
    
    const units = ['B', 'KB', 'MB', 'GB', 'TB'];
    const kilobyte = 1024;
    const unitIndex = Math.floor(Math.log(bytes) / Math.log(kilobyte));
    
    return parseFloat((bytes / Math.pow(kilobyte, unitIndex)).toFixed(2)) + ' ' + units[unitIndex];
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

