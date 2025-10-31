// =============================================================================
// FILE TREE EXPLORER
// =============================================================================

class FileTreeExplorer {
    constructor(containerElement) {
        this.container = containerElement;
        this.data = null;
        this.originalData = null; // Store the original root for reset
        this.expandedNodes = new Set();
        this.selectedNode = null;
        this.nodeSelectCallback = null;
        this.nodeElements = new Map(); // Maps node objects to DOM elements
        this.parentMap = new WeakMap(); // Cache parent relationships
    }

    // =============================================================================
    // DATA MANAGEMENT
    // =============================================================================

    // Sets the data for the file tree and initializes the view
    setData(data) {
        this.data = data;
        this.originalData = data; // Store the original root
        this.expandedNodes.clear();
        this.nodeElements.clear();
        this.parentMap = new WeakMap();

        // Pre-calculate parent relationships
        this.buildParentMap(data, null);

        // Expand root node by default
        if (data) {
            this.expandedNodes.add(data);
            this.selectedNode = data;
        }

        this.renderFull();
    }

    // Build parent map for parent lookups
    buildParentMap(node, parent) {
        if (parent) {
            this.parentMap.set(node, parent);
        }
        if (node.children) {
            node.children.forEach(child => this.buildParentMap(child, node));
        }
    }

    // =============================================================================
    // RENDERING
    // =============================================================================

    // Performs a full re-render of the file tree
    renderFull() {
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

    // Update only the visual states without re-rendering everything
    updateNodeStates() {
        this.nodeElements.forEach((element, node) => {
            const header = element.querySelector('.tree-node-header');
            if (!header) return;

            // Update selection state
            if (this.selectedNode === node) {
                header.classList.add('selected');
            } else {
                header.classList.remove('selected');
            }

            // Update expand/collapse icon
            const expandIcon = header.querySelector('.expand-icon');
            if (expandIcon && node.directory && node.children && node.children.length > 0) {
                expandIcon.textContent = this.expandedNodes.has(node) ? '▼' : '▶';
            }

            // Update children visibility
            const childrenContainer = element.querySelector('.tree-node-children');
            if (childrenContainer) {
                if (this.expandedNodes.has(node)) {
                    if (childrenContainer.style.display === 'none') {
                        childrenContainer.innerHTML = '';
                        const sortedChildren = [...node.children].sort((a, b) => b.size - a.size);
                        const level = parseInt(element.dataset.level) + 1;
                        sortedChildren.forEach(child => {
                            this.renderNode(child, childrenContainer, level);
                        });
                    }
                    childrenContainer.style.display = 'block';
                } else {
                    childrenContainer.style.display = 'none';
                }
            }
        });
    }

    // Renders a single node in the file tree
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
            expandIcon.addEventListener('click', (event) => {
                event.stopPropagation();
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

        // Calculate percentage for size bar using cached parent
        let percentage = 0;
        if (this.data) {
            const parent = this.parentMap.get(node);
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
        size.textContent = formatSize(node.size);

        // Copy path button
        const copyBtn = document.createElement('button');
        copyBtn.className = 'copy-path-btn';
        copyBtn.textContent = 'copy path';
        copyBtn.title = 'Copy path to clipboard';
        copyBtn.addEventListener('click', (event) => {
            event.stopPropagation();
            this.copyPathToClipboard(node.path);
        });

        nodeHeader.appendChild(expandIcon);
        nodeHeader.appendChild(icon);
        nodeHeader.appendChild(name);
        nodeHeader.appendChild(sizeBarContainer);
        nodeHeader.appendChild(size);
        nodeHeader.appendChild(copyBtn);

        nodeHeader.addEventListener('click', () => {
            this.selectNode(node);
        });

        nodeItem.appendChild(nodeHeader);

        // Children container (only render when expanded)
        if (node.directory && node.children && node.children.length > 0) {
            const childrenContainer = document.createElement('div');
            childrenContainer.className = 'tree-node-children';

            if (this.expandedNodes.has(node)) {
                childrenContainer.style.display = 'block';

                // Sort children by size (largest first)
                const sortedChildren = [...node.children].sort((a, b) => {
                    return b.size - a.size;
                });

                sortedChildren.forEach(child => {
                    this.renderNode(child, childrenContainer, level + 1);
                });
            } else {
                // Don't render children if not expanded
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
        this.updateNodeStates();
    }

    // Selects the specified node in the file tree
    selectNode(node) {
        const previousNode = this.selectedNode;
        this.selectedNode = node;

        // Update only the affected nodes
        if (previousNode) {
            const prevElement = this.nodeElements.get(previousNode);
            if (prevElement) {
                const prevHeader = prevElement.querySelector('.tree-node-header');
                if (prevHeader) {
                    prevHeader.classList.remove('selected');
                }
            }
        }

        const currentElement = this.nodeElements.get(node);
        if (currentElement) {
            const currentHeader = currentElement.querySelector('.tree-node-header');
            if (currentHeader) {
                currentHeader.classList.add('selected');
            }

            // Scroll to selected node
            // currentElement.scrollIntoView({ behavior: 'smooth', block: 'start' });
        }

        if (this.nodeSelectCallback) {
            this.nodeSelectCallback(node);
        }
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

    // Selects a node externally, expanding to it if necessary
    selectNodeExternal(node) {
        if (node) {
            this.expandToNode(node, true);
            this.selectNode(node);
        }
    }

    // Selects and collapses the specified node
    selectAndCollapseNode(node) {
        if (node) {
            // Collapse the node if it's a directory
            if (node.directory && this.expandedNodes.has(node)) {
                this.expandedNodes.delete(node);
            }
            // Expand only ancestors, not the node itself
            this.expandToNode(node, true);
            this.selectNode(node);
        }
    }

    setRootNode(node) {
        if (node) {
            // Change the root of the file tree to the specified node
            this.data = node;
            this.expandedNodes.clear();
            this.nodeElements.clear();

            // Rebuild parent map for new root
            this.parentMap = new WeakMap();
            this.buildParentMap(node, null);

            // Expand the new root node by default
            this.expandedNodes.add(node);
            this.selectedNode = node;

            // Full re-render for root change
            this.renderFull();
        }
    }

    resetToOriginalRoot() {
        if (this.originalData) {
            // Reset to the original root
            this.data = this.originalData;
            this.expandedNodes.clear();
            this.nodeElements.clear();

            // Rebuild parent map for original root
            this.parentMap = new WeakMap();
            this.buildParentMap(this.originalData, null);

            // Expand the original root node
            this.expandedNodes.add(this.originalData);
            this.selectedNode = this.originalData;

            // Full re-render for root change
            this.renderFull();
        }
    }

    // Expands the tree to make the target node visible
    expandToNode(targetNode, expandOnlyAncestors = false) {
        // Find path from root to target node and expand all ancestors
        const path = this.findPathToNode(this.data, targetNode);
        if (path) {
            path.forEach((node, index) => {
                const isTargetNode = (index === path.length - 1);
                if (!expandOnlyAncestors || !isTargetNode) {
                    if (node.directory && node.children && node.children.length > 0) {
                        this.expandedNodes.add(node);
                    }
                }
            });
            this.updateNodeStates();
        }
    }

    // Finds the path from current node to target node
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

    // Finds a node by its path
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

    // Use cached parent map to get parent node
    getParentNode(node) {
        return this.parentMap.get(node) || null;
    }

    // Returns color based on size percentage
    getSizeColor(percentage) {
        // Color gradient from green (small) to yellow to red (large), relative to the parent
        if (percentage < 25) {
            return '#28a745';
        } else if (percentage < 50) {
            return '#7bc043';
        } else if (percentage < 75) {
            return '#ffc107';
        } else if (percentage < 90) {
            return '#fd7e14';
        } else {
            return '#dc3545';
        }
    }

    // Copies the given path to the clipboard
    async copyPathToClipboard(path) {
        try {
            await navigator.clipboard.writeText(path);
            this.showCopyNotification();
        } catch (error) {
            console.error('Failed to copy path:', error);
        }
    }

    // Shows a notification-toast that the path was copied
    showCopyNotification() {
        let notification = document.getElementById('copyNotification');
        if (!notification) {
            notification = document.createElement('div');
            notification.id = 'copyNotification';
            notification.className = 'copy-notification';
            notification.textContent = 'Path copied to clipboard!';
            document.body.appendChild(notification);
        }

        notification.classList.add('show');

        setTimeout(() => {
            notification.classList.remove('show');
        }, 2000);
    }

    // =============================================================================
    // CALLBACKS
    // =============================================================================

    // Sets the callback for node selection events
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

    // Sets up event listeners for dragging
    setupEventListeners() {
        this.handle.addEventListener('mousedown', (event) => this.startDragging(event));
        document.addEventListener('mousemove', (event) => this.drag(event));
        document.addEventListener('mouseup', () => this.stopDragging());
    }

    startDragging(mouseEvent) {
        this.isDragging = true;
        this.startY = mouseEvent.clientY;
        this.startHeight = this.topPanel.offsetHeight;

        this.handle.classList.add('dragging');
        document.body.style.cursor = 'ns-resize';
        document.body.style.userSelect = 'none';

        mouseEvent.preventDefault();
    }

    drag(mouseEvent) {
        if (!this.isDragging) return;

        const deltaY = mouseEvent.clientY - this.startY;
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

    // Loads and applies saved panel size
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
