# Sun Direction App

A cross-platform application to visualize real-time solar direction (azimuth and elevation) with compass visualization. Available as both a desktop application (Java) and a mobile web app (PWA).

## Features

### Desktop Version (Java)
- Real-time sun position calculation using NOAA algorithm
- Interactive map-based city search (OpenStreetMap Nominatim)
- Native Windows application (.exe) with installer
- Automatic 10-second updates
- Visual compass with day/night rendering

### Mobile Version (PWA - Progressive Web App)
- Responsive design for all screen sizes
- Device GPS location support
- Offline-first caching with Service Worker
- Installable on home screen (iOS/Android)
- Same solar calculation algorithm as desktop
- Canvas-based compass visualization

## Quick Start

### Desktop Application
```powershell
# Build and package the application
.\package-app.ps1

# Output:
# - dist/SunDirectionApp.zip (portable version)
# - dist/SunDirectionApp-Installer.exe (Windows installer)
```

### Mobile Web App (Local Development)
```powershell
# Start local development server
.\start-mobile-web.ps1

# Access at: http://localhost:8080/mobile-web/
```

## Project Structure

```
├── src/                          # Java desktop application
│   ├── App.java                 # Entry point
│   ├── MainFrame.java           # Main UI window
│   ├── SunCalculator.java       # NOAA solar algorithm
│   ├── GeocodingService.java    # Nominatim API client
│   └── SunCompassPanel.java     # Custom compass rendering
│
├── mobile-web/                  # Progressive Web App
│   ├── index.html              # Responsive UI
│   ├── app.js                  # Solar calculation + geolocation
│   ├── styles.css              # Mobile-first styling
│   ├── manifest.json           # PWA metadata
│   └── service-worker.js       # Offline support
│
├── installer/                   # .NET Windows installer
│   ├── InstallerApp.csproj     # .NET 9.0 project
│   └── Program.cs              # Installer logic
│
├── assets/                      # Icons and diagrams
│   ├── sun-direction-icon.png  # App icon (256x256)
│   ├── sun-direction-icon.ico  # Windows icon
│   └── architecture-diagram.svg # System architecture
│
└── scripts/                     # Build automation
    ├── package-app.ps1         # Master build script
    ├── build-dotnet-installer.ps1
    ├── generate-assets.ps1
    └── start-mobile-web.ps1
```

## Technology Stack

### Desktop
- **Language**: Java 21+
- **UI Framework**: Swing/AWT
- **Packaging**: jpackage
- **Installer**: .NET 9.0 (C#)

### Mobile
- **Frontend**: HTML5, CSS3, JavaScript (ES6+)
- **Solar Math**: NOAA Julian date algorithm
- **APIs**: Geolocation, Fetch (Nominatim)
- **Offline**: Service Worker & Cache API
- **Server**: Python http.server (development)

## Solar Calculation

Both desktop and mobile versions use the same NOAA-standard algorithm:
1. Convert date/time to Julian date
2. Calculate solar declination
3. Compute hour angle
4. Calculate azimuth and elevation angles

**Accuracy**: ±0.2° under ideal conditions

## Dependencies

### Desktop Build
- Java 21+
- .NET 9.0 (for installer)
- PowerShell 5.1+

### Mobile Development
- Python 3.7+
- Modern web browser (Chrome, Firefox, Safari, Edge)

## Limitations

- City search requires internet connection
- Solar calculation assumes standard atmosphere (no atmospheric refraction)
- GPS accuracy depends on device/environment
- Mobile web requires HTTPS for production (Geolocation API requirement)

## Links

- [Architecture Documentation](APP_STRUCTURE.txt)
- Architecture Diagram: [SVG](assets/architecture-diagram.svg)

## License

This project is provided as-is for educational and personal use.
