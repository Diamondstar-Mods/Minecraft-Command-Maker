# Source Code Setup Instructions

This guide explains how to set up your source code for the Fabric and Quilt versions.

## Option 1: Manual Code Copying (Recommended for Development)

If you want independent development per loader:

1. **Copy Fabric Client to Quilt Client**:
   ```bash
   xcopy /E /I "client\src\main\java" "quilt-client\src\main\java"
   xcopy /E /I "client\src\main\resources" "quilt-client\src\main\resources"
   ```

2. **Copy Fabric Server to Quilt Server**:
   ```bash
   xcopy /E /I "server\src\main\java" "quilt-server\src\main\java"
   xcopy /E /I "server\src\main\resources" "quilt-server\src\main\resources"
   ```

3. Update the respective `quilt.mod.json` and `fabric.mod.json` files with your entrypoints.

## Option 2: Shared Source Folder (Advanced)

If you want to use the same source code for both Fabric and Quilt:

1. Create a shared library module
2. Have both Fabric and Quilt modules depend on it
3. Each loader still has its own `build.gradle` with loader-specific dependencies

## Option 3: Symbolic Links (Windows PowerShell)

Create symbolic links to share source code:

```powershell
# From quilt-client, link to fabric client source
New-Item -ItemType SymbolicLink -Path "quilt-client/src/main/java" -Target "../../client/src/main/java"
```

## Updating Mod Metadata

### Fabric Version (fabric.mod.json)
Update in:
- `client/src/main/resources/fabric.mod.json`
- `server/src/main/resources/fabric.mod.json`

### Quilt Version (quilt.mod.json)
Update in:
- `quilt-client/src/main/resources/quilt.mod.json`
- `quilt-server/src/main/resources/quilt.mod.json`

## Testing

After setting up source code:

1. **Test Fabric Client**:
   ```bash
   ./gradlew :client:runClient
   ```

2. **Test Quilt Client**:
   ```bash
   ./gradlew :quilt-client:runClient
   ```

3. **Test Fabric Server**:
   ```bash
   ./gradlew :server:runClient
   ```

4. **Test Quilt Server**:
   ```bash
   ./gradlew :quilt-server:runClient
   ```

## Compatibility Notes

- **Quilted Fabric API** provides full Fabric API compatibility, so most Fabric code should work in Quilt without modifications
- **Quilt Standard Libraries** (QSL) provides additional features not in Fabric
- Test thoroughly in both environments to ensure compatibility

## Troubleshooting

If you get build errors:

1. Ensure all entrypoint classes exist in `src/main/java`
2. Check that `quilt.mod.json` and `fabric.mod.json` have correct class names
3. Verify all dependencies are available (check `gradle.properties`)
4. Run `./gradlew clean` if you encounter caching issues

