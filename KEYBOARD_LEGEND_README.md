# Keyboard Shortcuts Legend

## Overview
A comprehensive keyboard shortcuts legend has been added to the bottom of the Drive Visualizer interface to help users navigate the application efficiently using keyboard commands.

## Features

### **Legend Display**
- **Location**: Fixed at the bottom of the page
- **Style**: Dark-themed bar with styled keyboard key badges
- **Content**: Shows all available keyboard shortcuts with descriptions

### **Collapsible Legend**
- **Toggle Button**: Click the ▼/▲ button on the right to hide/show the legend
- **Keyboard Shortcut**: Press `Ctrl+H` to toggle the legend visibility
- **Persistent State**: Legend visibility preference is saved in localStorage
- **Space Optimization**: Hide the legend to maximize visualization space

## Available Keyboard Shortcuts

### **Navigation (Treemap)**
| Shortcut | Description |
|----------|-------------|
| `↑` `↓` | Navigate between sibling nodes |
| `←` | Navigate to parent node (übergeordnet) |
| `→` | Navigate to first child node (untergeordnet) |
| `Space` | Zoom into selected directory |
| `Backspace` | Zoom out to parent directory |
| `Esc` | Reset view to root |

### **Mouse Controls**
| Action | Description |
|----------|-------------|
| `Left Click` | Select the exact element clicked |
| `Ctrl + Left Click` | Select the parent folder of the clicked element |

### **Application Shortcuts**
| Shortcut | Description |
|----------|-------------|
| `Ctrl+O` | Focus directory input field |
| `Ctrl+S` | Open settings modal |
| `Ctrl+H` | Toggle keyboard legend visibility |

## Visual Design

### **Keyboard Key Styling**
- Keys are displayed as styled `<kbd>` elements
- Dark background with border and subtle shadow
- Monospace font for consistency
- Hover effect for interactivity

### **Responsive Design**
- Adapts to smaller screens with reduced font sizes
- Keys remain readable on all device sizes
- Flexible layout wraps gracefully

### **Color Scheme**
- Dark background (#343a40) matches navbar
- Keys have grey background (#495057)
- Muted text for descriptions
- Border-top separator for visual distinction

## Implementation Details

### **HTML Structure**
```html
<div id="keyboardLegend" class="keyboard-legend bg-dark text-white">
    <div class="container-fluid">
        <div class="d-flex justify-content-between align-items-center">
            <div class="row g-2 flex-grow-1">
                <!-- Keyboard shortcuts displayed here -->
            </div>
            <button id="toggleLegendBtn">▼</button>
        </div>
    </div>
</div>
```

### **JavaScript Functions**

#### `toggleLegend()`
- Toggles visibility of the shortcuts row
- Updates button icon (▼/▲) and tooltip
- Saves state to localStorage
- Triggers canvas resize for proper rendering

#### `loadLegendState()`
- Called on page load
- Restores legend visibility from localStorage
- Ensures consistent user experience across sessions

### **CSS Classes**

#### `.keyboard-legend`
- Border-top for separation
- Smooth transition effects
- Responsive font sizing

#### `.keyboard-legend kbd`
- Styled key badges
- Shadow effect for depth
- Hover state for interactivity

## User Benefits

✅ **Discoverability**: Users can easily learn all available shortcuts  
✅ **Always Visible**: No need to remember or search for shortcuts  
✅ **Collapsible**: Can be hidden to maximize visualization space  
✅ **Persistent**: User preference is remembered  
✅ **Professional**: Clean, modern design that matches the UI  
✅ **Accessible**: Clear labels and visual hierarchy

## Future Enhancements

Possible improvements for future versions:
- **Help Modal**: Detailed help with examples
- **Customizable Shortcuts**: Allow users to define their own key bindings
- **Tooltips**: Hover over treemap to show relevant shortcuts
- **Print-friendly**: Keyboard shortcuts reference sheet
- **Animations**: Highlight keys when they're pressed

## Technical Notes

- **localStorage Key**: `legendVisible` (true/false)
- **Default State**: Visible on first load
- **Performance**: No impact on treemap rendering performance
- **Browser Support**: Works on all modern browsers with CSS3 and ES6 support
