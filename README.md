# DriveVisualizer

A disk space visualization tool built with Spring Boot and JavaScript that helps you understand your storage usage through interactive treemap visualizations.

![Java](https://img.shields.io/badge/Java-17-orange)
![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.5.6-brightgreen)
![License](https://img.shields.io/badge/license-MIT-blue)

## Table of Contents

- [Features](#features)
- [Technology Stack](#technology-stack)
- [Prerequisites](#prerequisites)
- [Installation](#installation)
- [Usage](#usage)
- [Keyboard Shortcuts](#keyboard-shortcuts)
- [Configuration](#configuration)
- [API Documentation](#api-documentation)
- [Project Structure](#project-structure)
- [Contributing](#contributing)
- [License](#license)

## Features

### Interactive Treemap Visualization
- **Visual Disk Analysis**: See your disk space usage at a glance with an interactive treemap
- **Color-coded Files**: Different file types are shown in different colors for easy identification
- **Zoom Navigation**: Click to zoom into directories, navigate back with keyboard shortcuts
- **Real-time Updates**: Instant visualization updates when scanning new directories

### Keyboard Navigation
- **Arrow Keys**: Navigate between siblings (↑↓) and parent/child nodes (←→)
- **Space/Backspace**: Zoom in and out of directories
- **Escape**: Reset to root view
- **Ctrl Shortcuts**: Quick access to directory input, settings, and help

### Customizable Color Mappings
- **Dynamic Color Assignment**: Assign custom colors to any file extension
- **User-Specific Settings**: Each user's color preferences are saved independently
- **Default Mappings**: Pre-configured colors for common file types
- **Easy Management**: Add, remove, or reset color mappings through the UI

### Advanced Features
- **File Tree View**: Alternative hierarchical view of your directory structure
- **Size Calculations**: Accurate recursive size calculation for directories
- **File/Folder Count**: Track the number of items in each directory
- **Responsive Design**: Works seamlessly on desktop and mobile devices

## Technology Stack

### Backend
- **Java 17**: Modern Java platform
- **Spring Boot 3.5.6**: Application framework
- **Spring Web**: RESTful API endpoints
- **Thymeleaf**: Server-side template engine
- **Jackson**: JSON processing
- **Maven**: Dependency management and build tool

### Frontend
- **HTML5/CSS3**: Modern web standards
- **JavaScript (ES6+)**: Interactive functionality
- **Bootstrap 5**: Responsive UI framework
- **D3.js**: Data-driven treemap visualizations

## Prerequisites

Before you begin, ensure you have the following installed:

- **Java Development Kit (JDK) 17** or higher
  - Download from [Eclipse Adoptium](https://adoptium.net/) or [Oracle](https://www.oracle.com/java/technologies/downloads/)
- **Apache Maven 3.6+**
  - Download from [Maven Official Site](https://maven.apache.org/download.cgi)
- **Git** (for cloning the repository)

Verify your installations:

```powershell
java -version
mvn -version
```

## Installation

### 1. Clone the Repository

```powershell
git clone https://github.com/HendrikHoemberg/DriveVisualizer.git
cd DriveVisualizer
```

### 2. Build the Project

```powershell
mvn clean install
```

This will:
- Download all dependencies
- Compile the Java source code
- Run all unit tests
- Package the application as a JAR file

### 3. Run the Application

```powershell
mvn spring-boot:run
```

Or run the packaged JAR:

```powershell
java -jar target/drive-visualizer-0.0.1-SNAPSHOT.jar
```

### 4. Access the Application

Open your web browser and navigate to:

```
http://localhost:8080
```

## Usage

### Basic Usage

1. **Enter a Directory Path**
   - Type or paste a directory path in the input field (e.g., `C:\Users\YourName\Documents`)
   - Press Enter or click "Visualisieren" to scan the directory

2. **Navigate the Treemap**
   - Click on any directory to zoom into it
   - Use keyboard shortcuts (see below) for faster navigation
   - Hover over elements to see size and path information

3. **Customize Colors**
   - Click the "Einstellungen" (Settings) button
   - Add, modify, or remove color mappings for file extensions
   - Click "Speichern" to save your preferences

4. **Switch Views**
   - Toggle between Treemap and File Tree views using the navigation bar
   - Both views show the same data in different formats

### Keyboard Shortcuts

For a complete list of keyboard shortcuts, see [KEYBOARD_LEGEND_README.md](KEYBOARD_LEGEND_README.md).

**Quick Reference:**

| Shortcut | Action |
|----------|--------|
| `↑` `↓` | Navigate siblings |
| `←` | Go to parent |
| `→` | Go to first child |
| `Space` | Zoom in |
| `Backspace` | Zoom out |
| `Esc` | Reset to root |
| `Ctrl+O` | Focus directory input |
| `Ctrl+S` | Open settings |
| `Ctrl+H` | Toggle keyboard legend |

## Configuration

### Application Properties

The application can be configured via `src/main/resources/application.properties`:

```properties
# Server Configuration
server.port=8080

# Application Name
spring.application.name=drive-visualizer

# Thymeleaf Template Engine
spring.thymeleaf.cache=false
spring.thymeleaf.prefix=classpath:/templates/
spring.thymeleaf.suffix=.html

# Static Resources
spring.web.resources.static-locations=classpath:/static/

# Logging Levels
logging.level.com.drivevisualizer=INFO
logging.level.root=INFO
```

### Color Mappings

For detailed information about color mapping configuration, see [COLOR_MAPPINGS_README.md](COLOR_MAPPINGS_README.md).

**Configuration Files:**
- **Default mappings**: `src/main/resources/color-mappings.json`
- **User mappings**: `~/.drivevisualizer/color-mappings.json`

User-specific mappings are stored in the user's home directory and persist across application restarts.

## API Documentation

### File System Endpoints

#### Scan Directory
```http
GET /api/scan?path={directoryPath}
```

**Parameters:**
- `path` (required): Absolute path to the directory to scan

**Response:**
```json
{
  "name": "Documents",
  "path": "C:\\Users\\YourName\\Documents",
  "size": 1234567890,
  "isDirectory": true,
  "children": [...]
}
```

### Color Mapping Endpoints

#### Get Color Mappings
```http
GET /api/color-mappings
```

**Response:** Array of color mapping objects

#### Save Color Mappings
```http
POST /api/color-mappings
Content-Type: application/json

[
  {
    "extension": "js",
    "color": "#f7df1e",
    "name": "JavaScript"
  }
]
```

#### Reset to Defaults
```http
POST /api/color-mappings/reset
```

For more API details, see the source code in `src/main/java/com/voba/controller/`.

## Project Structure

```
DriveVisualizer/
├── src/
│   ├── main/
│   │   ├── java/com/voba/
│   │   │   ├── DriveVisualizerApplication.java  # Main application
│   │   │   ├── controller/                       # REST controllers
│   │   │   │   ├── ColorMappingController.java
│   │   │   │   ├── FileSystemController.java
│   │   │   │   └── HomeController.java
│   │   │   ├── model/                            # Data models
│   │   │   │   ├── ColorMapping.java
│   │   │   │   └── FileNode.java
│   │   │   └── service/                          # Business logic
│   │   │       ├── ColorMappingService.java
│   │   │       └── DirectoryService.java
│   │   └── resources/
│   │       ├── application.properties            # App configuration
│   │       ├── color-mappings.json               # Default colors
│   │       ├── static/
│   │       │   ├── css/style.css                 # Styles
│   │       │   └── js/
│   │       │       ├── app.js                    # Main app logic
│   │       │       ├── filetree.js               # Tree view
│   │       │       └── treemap.js                # Treemap visualization
│   │       └── templates/
│   │           └── index.html                    # Main page
│   └── test/                                     # Unit tests
│       └── java/com/voba/
├── pom.xml                                       # Maven configuration
├── README.md                                     # This file
├── COLOR_MAPPINGS_README.md                      # Color feature docs
└── KEYBOARD_LEGEND_README.md                     # Keyboard shortcuts docs
```

## Testing

Run all tests:

```powershell
mvn test
```

Run tests with coverage:

```powershell
mvn clean test
```

Test reports are generated in `target/surefire-reports/`.

## Contributing

Contributions are welcome! Please follow these steps:

1. **Fork the repository**
2. **Create a feature branch**
   ```powershell
   git checkout -b feature/amazing-feature
   ```
3. **Commit your changes**
   ```powershell
   git commit -m "Add some amazing feature"
   ```
4. **Push to the branch**
   ```powershell
   git push origin feature/amazing-feature
   ```
5. **Open a Pull Request**

### Development Guidelines

- Follow Java coding conventions
- Write unit tests for new features
- Update documentation as needed
- Keep commits focused and descriptive

## Troubleshooting

### Common Issues

**Port 8080 already in use:**
```powershell
# Change port in application.properties
server.port=8081
```

**Java version mismatch:**
```powershell
# Check your Java version
java -version
# Ensure JDK 17 or higher is installed
```

**Maven build fails:**
```powershell
# Clean and rebuild
mvn clean install -U
```

## Author

**Hendrik Hoemberg**
- GitHub: [@HendrikHoemberg](https://github.com/HendrikHoemberg)
