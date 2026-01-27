# Fabric & Quilt Multi-Loader Setup

This project now supports both **Fabric** and **Quilt** loaders. Each loader has its own separate modules to keep things organized and allow independent development.

## Project Structure

```
Minecraft-Command-Maker/
├── client/              # Fabric Client Edition
├── server/              # Fabric Server Edition
├── quilt-client/        # Quilt Client Edition
├── quilt-server/        # Quilt Server Edition
└── [other files]
```

## Loader Specifications

### Fabric Modules
- **Module**: `client` and `server`
- **Loader**: Fabric Loader v0.17.0
- **Dependencies**:
  - Fabric API v0.134.0+1.21.9
  - Minecraft 1.21.9
  - Java 21+

### Quilt Modules
- **Module**: `quilt-client` and `quilt-server`
- **Loader**: Quilt Loader v0.27.3+1.21.9
- **Dependencies**:
  - Quilted Fabric API v9.0.0-1.21.9 (provides Fabric API compatibility)
  - Quilt Standard Libraries v11.0.0-1.21.9
  - Minecraft 1.21.9
  - Java 21+

## Running the Mod

### Fabric Versions
- **Client**: `./gradlew :client:runClient` or run `runCEclient.bat`
- **Server**: `./gradlew :server:runClient` or run `runSEclient.bat`

### Quilt Versions
- **Client**: `./gradlew :quilt-client:runClient` or run `runQEclient.bat`
- **Server**: `./gradlew :quilt-server:runClient` or run `runQuiltServerClient.bat`

## Building the Mod

### Build All Modules
```bash
./gradlew build
```

### Build Specific Module
```bash
./gradlew :client:build          # Fabric Client
./gradlew :server:build          # Fabric Server
./gradlew :quilt-client:build    # Quilt Client
./gradlew :quilt-server:build    # Quilt Server
```

## Important Notes

1. **Code Sharing**: To share code between Fabric and Quilt versions, you'll need to either:
   - Duplicate the source files
   - Create a shared library module
   - Use symbolic links or symlinks
   - Set up a multi-version build script

2. **Mod Metadata**:
   - Fabric uses `fabric.mod.json` in `src/main/resources/`
   - Quilt uses `quilt.mod.json` in `src/main/resources/`
   - Keep both updated when making changes to mod metadata

3. **Version Compatibility**:
   - Both loaders target Minecraft 1.21.9
   - Quilted Fabric API ensures compatibility with Fabric API
   - Quilt Standard Libraries provide additional utilities

4. **Source Code**:
   - Currently, each loader has its own directory
   - Main entrypoints are in `src/main/java/com/example/`
   - Update these with your actual mod classes

## Dependencies

### Gradle Properties
All versions are managed in `gradle.properties`:
- `minecraft_version`: The Minecraft version
- `loader_version`: Fabric Loader version
- `quilt_loader_version`: Quilt Loader version
- `fabric_version`: Fabric API version
- `quilted_fabric_api_version`: Quilted Fabric API version
- `quilt_standard_libraries_version`: QSL version

## Next Steps

1. Copy your mod source files to each module:
   - `client/src/main/java/com/example/`
   - `server/src/main/java/com/example/`
   - `quilt-client/src/main/java/com/example/`
   - `quilt-server/src/main/java/com/example/`

2. Update the entrypoints in each `quilt.mod.json` and `fabric.mod.json`

3. Test each version independently:
   ```bash
   ./gradlew :client:runClient
   ./gradlew :quilt-client:runClient
   ```

4. For publishing, each module builds independently:
   - `build/libs/CMDMaker-Fabric-client.jar`
   - `build/libs/CMDMaker-Fabric-server.jar`
   - `build/libs/CMDMaker-Fabric-quilt-client.jar`
   - `build/libs/CMDMaker-Fabric-quilt-server.jar`

## Support

For issues with specific loaders:
- **Fabric**: https://discord.gg/v6v4pMv
- **Quilt**: https://discord.gg/quilt

