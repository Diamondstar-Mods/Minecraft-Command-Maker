# Quick Reference: Fabric vs Quilt Setup

## Directory Structure

| Loader | Client | Server |
|--------|--------|--------|
| **Fabric** | `client/` | `server/` |
| **Quilt** | `quilt-client/` | `quilt-server/` |

## Run Commands

### Batch Files (Windows)
```batch
runCEclient.bat         # Fabric Client
runSEclient.bat         # Fabric Server Client
runQEclient.bat         # Quilt Client
runQuiltServerClient.bat # Quilt Server Client
```

### Gradle Commands
```bash
./gradlew :client:runClient             # Fabric Client
./gradlew :server:runClient             # Fabric Server
./gradlew :quilt-client:runClient       # Quilt Client
./gradlew :quilt-server:runClient       # Quilt Server
```

## Build Outputs

Each module produces its own JAR:
- `build/libs/CMDMaker-Fabric-client.jar`
- `build/libs/CMDMaker-Fabric-server.jar`
- `build/libs/CMDMaker-Fabric-quilt-client.jar`
- `build/libs/CMDMaker-Fabric-quilt-server.jar`

## Key Files

### Configuration
- `gradle.properties` - All version settings
- `settings.gradle` - Project structure definition

### Fabric Modules
- `client/src/main/resources/fabric.mod.json`
- `server/src/main/resources/fabric.mod.json`
- `client/build.gradle`
- `server/build.gradle`

### Quilt Modules
- `quilt-client/src/main/resources/quilt.mod.json`
- `quilt-server/src/main/resources/quilt.mod.json`
- `quilt-client/build.gradle`
- `quilt-server/build.gradle`

## Dependencies at a Glance

| Aspect | Fabric | Quilt |
|--------|--------|-------|
| **Loader Version** | 0.17.0 | 0.27.3+1.21.9 |
| **Primary API** | Fabric API 0.134.0 | Quilted Fabric API 9.0.0 |
| **Secondary API** | - | Quilt Standard Libraries 11.0.0 |
| **Minecraft** | 1.21.9 | 1.21.9 |
| **Java** | 21+ | 21+ |

## Mod Metadata Format

- **Fabric**: Uses `fabric.mod.json`
- **Quilt**: Uses `quilt.mod.json` (different schema)

**Important**: Keep both metadata files updated when changing mod information.

## Next Steps

1. Copy your source code to each module (see `SOURCE_CODE_SETUP.md`)
2. Update entrypoints in all `quilt.mod.json` and `fabric.mod.json` files
3. Test each version independently
4. Maintain both versions or set up code sharing (see `FABRIC_QUILT_SETUP.md`)

## Common Tasks

### Clean Build
```bash
./gradlew clean
```

### Build All Modules
```bash
./gradlew build
```

### Run Specific Loader Only
```bash
./gradlew :quilt-client:build  # Just Quilt Client
./gradlew :client:build         # Just Fabric Client
```

### Troubleshooting

| Issue | Solution |
|-------|----------|
| Class not found | Check entrypoint names in JSON metadata files |
| Dependency error | Check `gradle.properties` for correct versions |
| Build cache issues | Run `./gradlew clean` first |
| Gradle sync error | Reload VS Code or run `./gradlew build` manually |

