class TreemapVisualizer {
    // =============================================================================
    // INITIALIZATION & SETUP
    // =============================================================================
    
    constructor(canvas, options = {}) {
        this.canvas = canvas;
        this.context = canvas.getContext('2d');
        this.data = null;
        this.currentRoot = null;
        this.selectedNode = null;
        this.nodeRectMap = new Map();
        this.colorMap = options.colorMap || new Map();
        this.minPixelSize = options.minPixelSize || 10;
        this.tooltip = options.tooltip || null;
        
        // Default colors
        this.defaultDirColor = '#4a90e2';
        this.defaultFileColor = '#95a5a6';
        this.highlightColor = '#ff0000';
        
        this.setupCanvas();
        this.setupEventListeners();
    }
    
    setupCanvas() {
        const resizeCanvas = () => {
            const container = this.canvas.parentElement;
            this.canvas.width = container.clientWidth;
            this.canvas.height = container.clientHeight;
            if (this.data) {
                this.render();
            }
        };
        
        window.addEventListener('resize', resizeCanvas);
        resizeCanvas();
    }
    
    setupEventListeners() {
        this.canvas.addEventListener('mousemove', (event) => this.handleMouseMove(event));
        this.canvas.addEventListener('click', (event) => this.handleClick(event));
        this.canvas.addEventListener('mouseleave', () => this.hideTooltip());
        
        document.addEventListener('keydown', (event) => this.handleKeyPress(event));
    }
    
    setData(data) {
        this.data = data;
        this.currentRoot = data;
        this.selectedNode = data;
        this.render();
    }
    
    // =============================================================================
    // RENDERING
    // =============================================================================
    
    render() {
        if (!this.data) return;
        
        this.context.clearRect(0, 0, this.canvas.width, this.canvas.height);
        this.nodeRectMap.clear();
        
        const rect = {
            x: 0,
            y: 0,
            width: this.canvas.width,
            height: this.canvas.height
        };
        
        this.drawTreemap(this.currentRoot, rect, true);
        
        // Highlight selected node
        if (this.selectedNode) {
            const selectedRect = this.nodeRectMap.get(this.selectedNode);
            if (selectedRect) {
                this.context.strokeStyle = this.highlightColor;
                this.context.lineWidth = 3;
                this.context.strokeRect(selectedRect.x, selectedRect.y, selectedRect.width, selectedRect.height);
            }
        }
    }
    
    drawTreemap(node, rect, horizontal) {
        this.nodeRectMap.set(node, rect);
        
        // Skip if rectangle is too small
        if (rect.width < this.minPixelSize || rect.height < this.minPixelSize) {
            this.drawRect(node, rect);
            return;
        }
        
        // If node has only one child, skip to that child
        if (node.children && node.children.length === 1) {
            this.drawTreemap(node.children[0], rect, horizontal);
            return;
        }
        
        // If node has no children or is a file, just draw it
        if (!node.children || node.children.length === 0) {
            this.drawRect(node, rect);
            return;
        }
        
        const children = [...node.children].sort((a, b) => b.size - a.size);
        let currentPos = horizontal ? rect.x : rect.y;
        
        children.forEach(child => {
            const ratio = child.size / node.size;
            let childRect;
            
            if (horizontal) {
                const width = rect.width * ratio;
                childRect = {
                    x: currentPos,
                    y: rect.y,
                    width: width,
                    height: rect.height
                };
                currentPos += width;
            } else {
                const height = rect.height * ratio;
                childRect = {
                    x: rect.x,
                    y: currentPos,
                    width: rect.width,
                    height: height
                };
                currentPos += height;
            }
            
            // Recursively draw child with alternating direction
            this.drawTreemap(child, childRect, !horizontal);
        });
    }
    
    drawRect(node, rect) {
        let color = this.defaultDirColor;
        if (!node.directory) {
            if (node.extension && this.colorMap.has(node.extension)) {
                color = this.colorMap.get(node.extension);
            } else {
                color = this.defaultFileColor;
            }
        }
        
        this.context.fillStyle = color;
        this.context.fillRect(rect.x, rect.y, rect.width, rect.height);
        
        this.context.strokeStyle = '#ffffff';
        this.context.lineWidth = 1;
        this.context.strokeRect(rect.x, rect.y, rect.width, rect.height);
        
        // Draw text if space permits
        if (rect.width > 30 && rect.height > 20) {
            this.context.fillStyle = '#ffffff';
            this.context.font = '12px Arial';
            this.context.textAlign = 'center';
            this.context.textBaseline = 'middle';
            
            const text = node.name;
            const maxWidth = rect.width - 10;
            const truncatedText = this.truncateText(text, maxWidth);
            
            this.context.fillText(truncatedText, rect.x + rect.width / 2, rect.y + rect.height / 2);
        }
    }
    
    truncateText(text, maxWidth) {
        const metrics = this.context.measureText(text);
        if (metrics.width <= maxWidth) return text;
        
        let truncated = text;
        while (this.context.measureText(truncated + '...').width > maxWidth && truncated.length > 0) {
            truncated = truncated.substring(0, truncated.length - 1);
        }
        return truncated + '...';
    }
    
    // =============================================================================
    // EVENT HANDLERS
    // =============================================================================
    
    handleMouseMove(event) {
        const canvasBounds = this.canvas.getBoundingClientRect();
        const mouseX = event.clientX - canvasBounds.left;
        const mouseY = event.clientY - canvasBounds.top;
        
        const node = this.findSmallestNodeAtPosition(mouseX, mouseY);
        if (node) {
            this.showTooltip(node, event.clientX, event.clientY);
            this.canvas.style.cursor = node.directory ? 'pointer' : 'default';
        } else {
            this.hideTooltip();
            this.canvas.style.cursor = 'default';
        }
    }
    
    handleClick(event) {
        const canvasBounds = this.canvas.getBoundingClientRect();
        const mouseX = event.clientX - canvasBounds.left;
        const mouseY = event.clientY - canvasBounds.top;
        
        const node = this.findSmallestNodeAtPosition(mouseX, mouseY);
        if (!node) return;
        
        if (event.ctrlKey) {
            // Ctrl+Click: Select the parent folder of the clicked element
            const parent = this.findParent(this.currentRoot, node);
            if (parent) {
                this.selectedNode = parent;
                this.render();
                this.onNodeSelect(parent);
            } else {
                this.selectedNode = this.currentRoot;
                this.render();
                this.onNodeSelect(this.currentRoot);
            }
        } else {
            // Regular click: Select the exact element clicked
            this.selectedNode = node;

            this.render();
            this.onNodeSelect(node);
        }
    }
    
    handleKeyPress(event) {
        // Don't interfere with input fields or textareas
        if (event.target.tagName === 'INPUT' || event.target.tagName === 'TEXTAREA') {
            return;
        }
        
        if (!this.selectedNode) return;
        
        switch(event.key) {
            case 'ArrowLeft':
                this.navigateToSibling(-1);
                break;
            case 'ArrowRight':
                this.navigateToSibling(1);
                break;
            case 'ArrowUp':
                this.navigateToParent();
                break;
            case 'ArrowDown':
                this.navigateToFirstChild();
                break;
            case ' ':
                if (this.selectedNode.directory && this.selectedNode.children && this.selectedNode.children.length > 0) {
                    this.zoomIn(this.selectedNode);
                }
                event.preventDefault();
                break;
            case 'Backspace':
                this.zoomOut();
                event.preventDefault();
                break;
            case 'Escape':
                this.resetView();
                break;
        }
    }
    
    // =============================================================================
    // NAVIGATION
    // =============================================================================
    
    navigateToSibling(direction) {
        const parent = this.findParent(this.currentRoot, this.selectedNode);
        if (!parent || !parent.children) return;
        
        const currentIndex = parent.children.indexOf(this.selectedNode);
        const newIndex = currentIndex + direction;
        
        if (newIndex >= 0 && newIndex < parent.children.length) {
            this.selectedNode = parent.children[newIndex];
            this.render();
            this.onNodeSelect(this.selectedNode);
        }
    }
    
    navigateToParent() {
        const parent = this.findParent(this.currentRoot, this.selectedNode);
        if (parent) {
            this.selectedNode = parent;
            this.render();
            this.onNodeSelect(this.selectedNode);
        }
    }
    
    navigateToFirstChild() {
        if (this.selectedNode.children && this.selectedNode.children.length > 0) {
            this.selectedNode = this.selectedNode.children[0];
            this.render();
            this.onNodeSelect(this.selectedNode);
        }
    }
    
    // =============================================================================
    // NODE SEARCH & TRAVERSAL
    // =============================================================================
    
    findParent(root, node) {
        if (!root.children) return null;
        
        for (let child of root.children) {
            if (child === node) return root;
            const found = this.findParent(child, node);
            if (found) return found;
        }
        return null;
    }
    
    findNodeAtPosition(positionX, positionY) {
        for (let [node, rect] of this.nodeRectMap) {
            if (positionX >= rect.x && positionX <= rect.x + rect.width &&
                positionY >= rect.y && positionY <= rect.y + rect.height) {
                return node;
            }
        }
        return null;
    }
    
    findSmallestNodeAtPosition(positionX, positionY) {
        let smallestNode = null;
        let smallestArea = Infinity;
        
        for (let [node, rect] of this.nodeRectMap) {
            if (positionX >= rect.x && positionX <= rect.x + rect.width &&
                positionY >= rect.y && positionY <= rect.y + rect.height) {
                const area = rect.width * rect.height;
                if (area < smallestArea) {
                    smallestArea = area;
                    smallestNode = node;
                }
            }
        }
        return smallestNode;
    }
    
    // =============================================================================
    // ZOOM & VIEW CONTROL
    // =============================================================================
    
    zoomIn(node) {
        if (node.directory && node.children && node.children.length > 0) {
            this.currentRoot = node;
            this.selectedNode = node;
            this.render();
        }
    }
    
    zoomOut() {
        if (this.currentRoot !== this.data) {
            const parent = this.findParentInFullTree(this.data, this.currentRoot);
            if (parent) {
                this.currentRoot = parent;
                this.selectedNode = parent;
                this.render();
                this.onNodeSelect(parent);
            }
        }
    }
    
    resetView() {
        this.currentRoot = this.data;
        this.selectedNode = this.data;
        this.render();
        this.onNodeSelect(this.data);
    }
    
    findParentInFullTree(root, node) {
        if (!root.children) return null;
        
        for (let child of root.children) {
            if (child === node) return root;
            const found = this.findParentInFullTree(child, node);
            if (found) return found;
        }
        return null;
    }
    
    // =============================================================================
    // TOOLTIP
    // =============================================================================
    
    showTooltip(node, mouseX, mouseY) {
        if (!this.tooltip) return;
        
        const size = this.formatSize(node.size);
        const type = node.directory ? 'Directory' : 'File';
        const extension = node.extension ? ` (.${node.extension})` : '';
        
        this.tooltip.innerHTML = `
            <strong>${node.name}${extension}</strong><br>
            Type: ${type}<br>
            Size: ${size}<br>
            Path: ${node.path}
        `;
        
        this.tooltip.style.display = 'block';
        this.tooltip.style.left = mouseX + 10 + 'px';
        this.tooltip.style.top = mouseY + 10 + 'px';
        
        // Adjust position if tooltip goes off screen
        const tooltipBounds = this.tooltip.getBoundingClientRect();
        if (tooltipBounds.right > window.innerWidth) {
            this.tooltip.style.left = (mouseX - tooltipBounds.width - 10) + 'px';
        }
        if (tooltipBounds.bottom > window.innerHeight) {
            this.tooltip.style.top = (mouseY - tooltipBounds.height - 10) + 'px';
        }
    }
    
    hideTooltip() {
        if (this.tooltip) {
            this.tooltip.style.display = 'none';
        }
    }
    
    // =============================================================================
    // UTILITY FUNCTIONS
    // =============================================================================
    
    formatSize(bytes) {
        const units = ['B', 'KB', 'MB', 'GB', 'TB'];
        let size = bytes;
        let unitIndex = 0;
        
        while (size >= 1024 && unitIndex < units.length - 1) {
            size /= 1024;
            unitIndex++;
        }
        
        return `${size.toFixed(2)} ${units[unitIndex]}`;
    }
    
    // =============================================================================
    // CALLBACKS & CONFIGURATION
    // =============================================================================
    
    onNodeSelect(node) {
        if (this.nodeSelectCallback) {
            this.nodeSelectCallback(node);
        }
    }
    
    setNodeSelectCallback(callback) {
        this.nodeSelectCallback = callback;
    }
    
    updateColorMap(colorMap) {
        this.colorMap = colorMap;
        this.render();
    }
    
    updateMinPixelSize(size) {
        this.minPixelSize = size;
        this.render();
    }
    
    // Focus on a specific node (called from file tree)
    focusOnNode(node) {
        // Find the node in the current view
        const nodeInCurrentView = this.findNodeInTree(this.currentRoot, node);
        
        if (nodeInCurrentView) {
            // Node is in current view, just select it
            this.selectedNode = nodeInCurrentView;
            this.render();
            this.onNodeSelect(nodeInCurrentView);
        } else {
            // Node is not in current view, need to navigate to it
            // First, reset to root view
            this.currentRoot = this.data;
            
            // Then select the node
            const nodeInFullTree = this.findNodeInTree(this.data, node);
            if (nodeInFullTree) {
                this.selectedNode = nodeInFullTree;
                this.render();
                this.onNodeSelect(nodeInFullTree);
            }
        }
    }
    
    // Find a node in the tree by comparing paths or references
    findNodeInTree(root, targetNode) {
        if (!root) return null;
        
        // Compare by path for safety
        if (root.path === targetNode.path) {
            return root;
        }
        
        // Also try direct reference comparison
        if (root === targetNode) {
            return root;
        }
        
        // Search in children
        if (root.children) {
            for (let child of root.children) {
                const found = this.findNodeInTree(child, targetNode);
                if (found) return found;
            }
        }
        
        return null;
    }
}
