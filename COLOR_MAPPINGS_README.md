# Color Mappings Feature

## Overview
The Drive Visualizer now supports a fully modular color mapping system that allows users to customize file extension colors dynamically.

## Features

### 1. **Dynamic Color Mappings**
- Users can add unlimited file extensions and assign custom colors
- Each mapping includes:
  - **Extension**: File extension (e.g., `js`, `java`, `py`)
  - **Name**: Descriptive name (optional, e.g., "JavaScript")
  - **Color**: Hex color code for visualization

### 2. **User Interface**
- **Add Button**: Click "Hinzufügen" (Add) to create new color mappings
- **Remove Button**: Click ✕ on any row to remove that mapping
- **Reset Button**: Click "Zurücksetzen" (Reset) to restore default mappings
- **Save Button**: Click "Speichern" (Save) to persist your changes

### 3. **Persistence**
- Color mappings are stored in a user-specific configuration file
- Location: `~/.drivevisualizer/color-mappings.json` (user's home directory)
- Changes persist across application restarts
- Each user has their own personalized configuration

### 4. **Default Mappings**
Default mappings are included for common file types:
- **Programming Languages**: js, java, py, html, css, json, xml
- **Text Files**: txt
- **Images**: jpg, jpeg, png, gif
- **Media**: mp4, mp3
- **Documents**: pdf
- **Archives**: zip, rar

## API Endpoints

### GET `/api/color-mappings`
Retrieves current color mappings (user config or defaults)

**Response:**
```json
[
  {
    "extension": "js",
    "color": "#f7df1e",
    "name": "JavaScript"
  },
  ...
]
```

### POST `/api/color-mappings`
Saves color mappings to user configuration

**Request Body:**
```json
[
  {
    "extension": "js",
    "color": "#f7df1e",
    "name": "JavaScript"
  },
  ...
]
```

### POST `/api/color-mappings/reset`
Resets color mappings to default values

**Response:** Returns the default mappings

## File Structure

```
src/main/resources/
├── color-mappings.json           # Default color mappings (shipped with app)
└── ...

src/main/java/com/drivevisualizer/
├── model/
│   └── ColorMapping.java          # Data model for color mappings
├── service/
│   └── ColorMappingService.java   # Business logic for managing mappings
└── controller/
    └── ColorMappingController.java # REST API endpoints

~/.drivevisualizer/
└── color-mappings.json            # User-specific color mappings
```

## Technical Details

### Backend (Java/Spring Boot)
- **ColorMapping Model**: POJO representing a single mapping
- **ColorMappingService**: Handles loading/saving from JSON files
- **ColorMappingController**: REST endpoints for frontend integration

### Frontend (JavaScript)
- **Dynamic UI**: Rows are generated dynamically based on loaded mappings
- **Real-time Validation**: Empty extensions are filtered out on save
- **Async Operations**: All API calls are asynchronous with error handling

## Usage Example

1. Click the "Einstellungen" (Settings) button in the navigation bar
2. Scroll to the "Farbzuordnungen" (Color Mappings) section
3. Click "+ Hinzufügen" to add a new mapping:
   - Extension: `rs`
   - Name: `Rust`
   - Color: `#dea584` (or pick from color picker)
4. Click "Speichern" to save
5. Your Rust files will now appear in the chosen color!

## Benefits

✅ **Modular**: Easy to extend with new file types  
✅ **User-Friendly**: Simple UI for non-technical users  
✅ **Persistent**: Settings survive application restarts  
✅ **Flexible**: Unlimited number of mappings  
✅ **Safe**: Default mappings always available via reset
