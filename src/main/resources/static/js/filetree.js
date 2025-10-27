// =============================================================================
// FILE TREE EXPLORER
// =============================================================================

class FileTreeExplorer {
    constructor(containerElement) {
        this.container = containerElement;
        this.data = null;
        this.expandedNodes = new Set();
        this.selectedNode = null;
        this.nodeSelectCallback = null;
        this.nodeElements = new Map(); // Maps node objects to DOM elements
    }
    
    // =============================================================================
    // DATA MANAGEMENT
    // =============================================================================
    
    setData(data) {
        this.data = data;
        this.expandedNodes.clear();
        this.nodeElements.clear();
        
        // Expand root node by default
        if (data) {
            this.expandedNodes.add(data);
            this.selectedNode = data;
        }
        
        this.render();
    }
    
    // =============================================================================
    // RENDERING
    // =============================================================================
    
    render() {
        this.container.innerHTML = '';
        
        if (!this.data) {
            this.container.innerHTML = '<div class="text-muted text-center mt-5">No directory loaded</div>';
            return;
        }
        
        const tree = document.createElement('div');
        tree.className = 'file-tree';
        this.renderNode(this.data, tree, 0);
        this.container.appendChild(tree);
    }
    
    renderNode(node, parentElement, level) {
        const nodeItem = document.createElement('div');
        nodeItem.className = 'tree-node';
        nodeItem.dataset.level = level;
        
        // Store reference
        this.nodeElements.set(node, nodeItem);
        
        // Node header
        const nodeHeader = document.createElement('div');
        nodeHeader.className = 'tree-node-header';
        nodeHeader.style.paddingLeft = (level * 20 + 5) + 'px';
        
        if (this.selectedNode === node) {
            nodeHeader.classList.add('selected');
        }
        
        // Expand/collapse icon
        const expandIcon = document.createElement('span');
        expandIcon.className = 'expand-icon';
        
        if (node.directory && node.children && node.children.length > 0) {
            expandIcon.textContent = this.expandedNodes.has(node) ? '▼' : '▶';
            expandIcon.style.cursor = 'pointer';
            expandIcon.addEventListener('click', (e) => {
                e.stopPropagation();
                this.toggleExpand(node);
            });
        } else {
            expandIcon.textContent = '';
            expandIcon.style.visibility = 'hidden';
        }
        
        // Icon
        const icon = document.createElement('span');
        icon.className = 'node-icon';
        icon.textContent = node.directory ? '📁' : '📄';
        
        // Name
        const name = document.createElement('span');
        name.className = 'node-name';
        name.textContent = node.name;
        name.title = node.path;
        
        // Calculate percentage for size bar
        let percentage = 0;
        if (this.data) {
            const parent = this.findParentNode(this.data, node);
            const referenceSize = parent ? parent.size : this.data.size;
            percentage = Math.min(100, (node.size / referenceSize) * 100);
        }
        
        // Size bar container
        const sizeBarContainer = document.createElement('div');
        sizeBarContainer.className = 'size-bar-inline-container';
        
        const sizeBarFill = document.createElement('div');
        sizeBarFill.className = 'size-bar-fill';
        sizeBarFill.style.width = percentage + '%';
        const color = this.getSizeColor(percentage);
        sizeBarFill.style.backgroundColor = color;
        
        sizeBarContainer.appendChild(sizeBarFill);
        
        // Size
        const size = document.createElement('span');
        size.className = 'node-size';
        size.textContent = this.formatSize(node.size);
        
        // Copy path button
        const copyBtn = document.createElement('button');
        copyBtn.className = 'copy-path-btn';
        copyBtn.textContent = 'copy path';
        copyBtn.title = 'Copy path to clipboard';
        copyBtn.addEventListener('click', (e) => {
            e.stopPropagation();
            this.copyPathToClipboard(node.path);
        });
        
        nodeHeader.appendChild(expandIcon);
        nodeHeader.appendChild(icon);
        nodeHeader.appendChild(name);
        nodeHeader.appendChild(sizeBarContainer);
        nodeHeader.appendChild(size);
        nodeHeader.appendChild(copyBtn);
        
        // Click handler for selection
        nodeHeader.addEventListener('click', () => {
            this.selectNode(node);
        });
        
        // Double-click to expand/collapse
        nodeHeader.addEventListener('dblclick', (e) => {
            e.stopPropagation();
            if (node.directory && node.children && node.children.length > 0) {
                this.toggleExpand(node);
            }
        });
        
        nodeItem.appendChild(nodeHeader);
        
        // Children container
        if (node.directory && node.children && node.children.length > 0) {
            const childrenContainer = document.createElement('div');
            childrenContainer.className = 'tree-node-children';
            
            if (this.expandedNodes.has(node)) {
                childrenContainer.style.display = 'block';
                
                // Sort children: directories first, then by size
                const sortedChildren = [...node.children].sort((a, b) => {
                    if (a.directory !== b.directory) {
                        return a.directory ? -1 : 1;
                    }
                    return b.size - a.size;
                });
                
                sortedChildren.forEach(child => {
                    this.renderNode(child, childrenContainer, level + 1);
                });
            } else {
                childrenContainer.style.display = 'none';
            }
            
            nodeItem.appendChild(childrenContainer);
        }
        
        parentElement.appendChild(nodeItem);
    }
    
    // =============================================================================
    // NODE OPERATIONS
    // =============================================================================
    
    toggleExpand(node) {
        if (this.expandedNodes.has(node)) {
            this.expandedNodes.delete(node);
        } else {
            this.expandedNodes.add(node);
        }
        this.render();
    }
    
    selectNode(node) {
        this.selectedNode = node;
        this.render();
        
        // Scroll to selected node
        const nodeElement = this.nodeElements.get(node);
        if (nodeElement) {
            nodeElement.scrollIntoView({ behavior: 'smooth', block: 'nearest' });
        }
        
        if (this.nodeSelectCallback) {
            this.nodeSelectCallback(node);
        }
    }
    
    expandAll() {
        this.expandAllNodes(this.data);
        this.render();
    }
    
    expandAllNodes(node) {
        if (node.directory && node.children && node.children.length > 0) {
            this.expandedNodes.add(node);
            node.children.forEach(child => this.expandAllNodes(child));
        }
    }
    
    collapseAll() {
        this.expandedNodes.clear();
        if (this.data) {
            this.expandedNodes.add(this.data);
        }
        this.render();
    }
    
    // =============================================================================
    // EXTERNAL SELECTION (from visualization)
    // =============================================================================
    
    selectNodeByPath(path) {
        const node = this.findNodeByPath(this.data, path);
        if (node) {
            this.expandToNode(node);
            this.selectNode(node);
        }
    }
    
    selectNodeExternal(node) {
        if (node) {
            this.expandToNode(node);
            this.selectNode(node);
        }
    }
    
    expandToNode(targetNode) {
        // Find path from root to target node and expand all ancestors
        const path = this.findPathToNode(this.data, targetNode);
        if (path) {
            path.forEach(node => {
                if (node.directory && node.children && node.children.length > 0) {
                    this.expandedNodes.add(node);
                }
            });
        }
    }
    
    findPathToNode(currentNode, targetNode, path = []) {
        path.push(currentNode);
        
        if (currentNode === targetNode) {
            return path;
        }
        
        if (currentNode.children) {
            for (let child of currentNode.children) {
                const result = this.findPathToNode(child, targetNode, [...path]);
                if (result) {
                    return result;
                }
            }
        }
        
        return null;
    }
    
    findNodeByPath(currentNode, path) {
        if (currentNode.path === path) {
            return currentNode;
        }
        
        if (currentNode.children) {
            for (let child of currentNode.children) {
                const result = this.findNodeByPath(child, path);
                if (result) {
                    return result;
                }
            }
        }
        
        return null;
    }
    
    // =============================================================================
    // UTILITY FUNCTIONS
    // =============================================================================
    
    formatSize(bytes) {
        if (bytes === 0) return '0 B';
        
        const units = ['B', 'KB', 'MB', 'GB', 'TB'];
        const kilobyte = 1024;
        const unitIndex = Math.floor(Math.log(bytes) / Math.log(kilobyte));
        
        return parseFloat((bytes / Math.pow(kilobyte, unitIndex)).toFixed(2)) + ' ' + units[unitIndex];
    }
    
    findParentNode(root, targetNode) {
        if (!root || !root.children) return null;
        
        for (let child of root.children) {
            if (child === targetNode) {
                return root;
            }
            const found = this.findParentNode(child, targetNode);
            if (found) return found;
        }
        
        return null;
    }
    
    getSizeColor(percentage) {
        // Color gradient from green (small) to yellow to red (large)
        if (percentage < 25) {
            return '#28a745'; // Green
        } else if (percentage < 50) {
            return '#7bc043'; // Light green
        } else if (percentage < 75) {
            return '#ffc107'; // Yellow/Orange
        } else if (percentage < 90) {
            return '#fd7e14'; // Orange
        } else {
            return '#dc3545'; // Red
        }
    }
    
    async copyPathToClipboard(path) {
        try {
            await navigator.clipboard.writeText(path);
            // Show a brief notification
            this.showCopyNotification();
        } catch (err) {
            console.error('Failed to copy path:', err);
            // Fallback for older browsers
            const textArea = document.createElement('textarea');
            textArea.value = path;
            textArea.style.position = 'fixed';
            textArea.style.opacity = '0';
            document.body.appendChild(textArea);
            textArea.focus();
            textArea.select();
            try {
                document.execCommand('copy');
                this.showCopyNotification();
            } catch (err) {
                console.error('Fallback copy failed:', err);
            }
            document.body.removeChild(textArea);
        }
    }
    
    showCopyNotification() {
        // Create or update notification
        let notification = document.getElementById('copyNotification');
        if (!notification) {
            notification = document.createElement('div');
            notification.id = 'copyNotification';
            notification.className = 'copy-notification';
            notification.textContent = 'Path copied to clipboard!';
            document.body.appendChild(notification);
        }
        
        // Show notification
        notification.classList.add('show');
        
        // Hide after 2 seconds
        setTimeout(() => {
            notification.classList.remove('show');
        }, 2000);
    }
    
    // =============================================================================
    // CALLBACKS
    // =============================================================================
    
    setNodeSelectCallback(callback) {
        this.nodeSelectCallback = callback;
    }
}

// =============================================================================
// RESIZE HANDLE
// =============================================================================

class ResizeHandle {
    constructor(handleElement, topPanel, bottomPanel) {
        this.handle = handleElement;
        this.topPanel = topPanel;
        this.bottomPanel = bottomPanel;
        this.isDragging = false;
        this.startY = 0;
        this.startHeight = 0;
        
        this.setupEventListeners();
    }
    
    setupEventListeners() {
        this.handle.addEventListener('mousedown', (e) => this.startDragging(e));
        document.addEventListener('mousemove', (e) => this.drag(e));
        document.addEventListener('mouseup', () => this.stopDragging());
        
        // Touch support for mobile
        this.handle.addEventListener('touchstart', (e) => this.startDragging(e.touches[0]));
        document.addEventListener('touchmove', (e) => this.drag(e.touches[0]));
        document.addEventListener('touchend', () => this.stopDragging());
    }
    
    startDragging(e) {
        this.isDragging = true;
        this.startY = e.clientY;
        this.startHeight = this.topPanel.offsetHeight;
        
        this.handle.classList.add('dragging');
        document.body.style.cursor = 'ns-resize';
        document.body.style.userSelect = 'none';
        
        e.preventDefault();
    }
    
    drag(e) {
        if (!this.isDragging) return;
        
        const deltaY = e.clientY - this.startY;
        const newHeight = this.startHeight + deltaY;
        
        const container = this.topPanel.parentElement;
        const containerHeight = container.offsetHeight;
        const minHeight = 100;
        const maxHeight = containerHeight - minHeight - this.handle.offsetHeight;
        
        if (newHeight >= minHeight && newHeight <= maxHeight) {
            this.topPanel.style.height = newHeight + 'px';
            
            // Save preference
            const percentage = (newHeight / containerHeight) * 100;
            localStorage.setItem('explorerPanelHeightPercent', percentage);
            
            // Trigger resize event for canvas
            window.dispatchEvent(new Event('resize'));
        }
    }
    
    stopDragging() {
        if (!this.isDragging) return;
        
        this.isDragging = false;
        this.handle.classList.remove('dragging');
        document.body.style.cursor = '';
        document.body.style.userSelect = '';
    }
    
    loadSavedSize() {
        const saved = localStorage.getItem('explorerPanelHeightPercent');
        if (saved) {
            const container = this.topPanel.parentElement;
            const containerHeight = container.offsetHeight;
            const newHeight = (parseFloat(saved) / 100) * containerHeight;
            
            const minHeight = 100;
            const maxHeight = containerHeight - minHeight - this.handle.offsetHeight;
            
            if (newHeight >= minHeight && newHeight <= maxHeight) {
                this.topPanel.style.height = newHeight + 'px';
            }
        }
    }
}
