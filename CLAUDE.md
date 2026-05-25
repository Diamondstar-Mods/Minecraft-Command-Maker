# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Build Commands

The Gradle wrapper scripts (`gradlew` / `gradlew.bat`) use JDK paths from `gradle.properties`. The project uses **Fabric Loom** (v1.11.8) as the Gradle plugin for Minecraft mod development.

### Active subprojects (registered in `settings.gradle`)

```bash
./gradlew :server-1.21:build    # Fabric server, MC 1.21.9, Java 17
./gradlew :server-1.22:build    # Fabric server, MC 26.1, Java 25
./gradlew build                 # Build all registered subprojects
```

### Inactive/standalone subprojects (on disk but not in `settings.gradle`)

These must be built independently by adding them to `settings.gradle` first, or moving into their directory:

```bash
# cd into the subproject directory and run gradlew from root:
./gradlew :client:build          # Fabric client-only mod, MC 1.21.9, Java 17
./gradlew :quilt-client:build    # Quilt client, MC 1.21.9, Java 17
./gradlew :quilt-server:build    # Quilt server, MC 26.1, Java 17
./gradlew :client-legacy:build   # Legacy Fabric client, MC 1.8-1.13.2, Java 8
./gradlew :server-legacy:build   # Legacy Fabric server, MC 1.8-1.13.2, Java 8
```

Output JARs land in `<subproject>/build/libs/`.

### Run Minecraft with the mod

```bash
./gradlew :client:runClient       # Fabric client
./gradlew :server-1.21:runClient  # Fabric server + client
```

### Website (Next.js 14)

```bash
npm run dev    # Dev server with hot reload (runs scripts/copyDocs.js first)
npm run build  # Production build
npm start      # Run production build
```

## Project Architecture

This is **Minecraft Command Maker** (mod version 3.1.0), a Fabric/Quilt mod that adds custom aliases, parameterized command syntax, and downloadable `.mcfunction` execution to Minecraft. It's a Gradle multi-module monorepo with per-version source trees.

### Module matrix

Each module contains a full copy of the Java source under `src/main/java/com/example/`, adapted for its target Minecraft version:

| Module | Loader | MC Version | Java | Side |
|--------|--------|-----------|------|------|
| `server-1.21/` | Fabric | 1.21.9 | 17 | Server |
| `server-1.22/` | Fabric | 26.1 | 25 | Server |
| `server-1.17/` | Fabric | 1.17.1 | 17 | Server |
| `server-1.18-1.19-1.20/` | Fabric | 1.20.1 | 17 | Server |
| `server-legacy/` | Legacy Fabric | 1.8-1.13.2 | 8 | Server |
| `client/` | Fabric | 1.21.9 | 17 | Client-only |
| `client-legacy/` | Legacy Fabric | 1.8-1.13.2 | 8 | Client-only |
| `quilt-server/` | Quilt | 26.1 | 17 | Server |
| `quilt-client/` | Quilt | 1.21.9 | 17 | Client |

`server-1.21/` is the primary/current module. `server-1.22/` is for the next Minecraft version target.

The mod's mod ID is `nekkycommandmaker`. The main Fabric entry point is `CommandMaker` (implements `ModInitializer`), and the client entry point is `CommandMakerClient` (implements `ClientModInitializer`). Quilt modules re-use the same entry point class.

### Core manager classes (feature-based static singletons)

All core logic lives in `com.example` package. Each feature is a static manager class:

- **`CommandMaker`** — Mod entry point. Registers all Brigadier commands (`/cmd`, `/addcommand`, `/setcmdvariable`, `/syntax`, `/cmmakerperm`, `/deletealias`) and delegates alias execution to manager classes.
- **`AliasManager`** — Alias CRUD. Reads/writes `config/CommandMaker/aliases.json`. Registers each alias as a first-class Brigadier literal command. Supports `.mcfunction` file aliases (prefix `function:`).
- **`SyntaxManager`** + **`CommandSyntax`** — Custom pattern matching system. Users define patterns like `/tpa <player>` in `syntax.json`. `CommandSyntax` converts `<param>` placeholders to regex capture groups. `SyntaxManager` matches incoming input against all registered syntaxes and returns extracted parameters.
- **`VariableManager`** — Two-layer variable substitution: built-in (`${player}`, `${x}`, `${y}`, `${z}`) and per-player custom variables set via `/setcmdvariable`.
- **`PermissionManager`** — Hierarchical permission system: `NONE → ALIAS_USER → COMMAND_USER → MODERATOR → ADMIN`. Falls back through: vanilla op level → LuckPerms (optional, compileOnly) → `permissions.json`. Supports per-alias permissions and wildcards.
- **`FunctionManager`** — Downloads and executes `.mcfunction` files from a GitHub CDN manifest. Lists local functions under `config/CommandMaker/Functions/`.
- **`TelemetryManager`** — No-op stub (telemetry was removed; original code archived in `oldtel/`).

### Command execution flow

1. Player runs an alias (registered as a Brigadier literal command).
2. Input is passed to `SyntaxManager.matchInput()` — if it matches a custom syntax pattern, parameters are extracted (e.g., `<player>` → `"steve"`).
3. `CommandSyntax.substituteParameters()` replaces `${syntaxName_paramName}` placeholders in the alias target.
4. `VariableManager.substituteVariables()` replaces built-in and custom variables.
5. The final command string is parsed and executed via Minecraft's `CommandDispatcher`.

### GUI system

Located in `gui/` subpackage:
- `FunctionChestHandler` / `FunctionChestScreen` — Server-side screen handler with 54-slot chest inventory, tabbed interface (Download / My Functions / Create New), pagination, item-based interaction.
- `FunctionManagerScreen` — Client-only full custom screen (~400 lines) used by the `client/` module.
- `AliasDeleteScreen` — Click-to-delete alias browser.

### Config file layout (runtime, under `config/CommandMaker/`)

- `aliases.json` — Alias name → command target
- `syntax.json` — Syntax name → { pattern, description }
- `permissions.json` — Permission configuration
- `Functions/*.mcfunction` — Multi-line function files

### Website

Next.js 14 app serving a static documentation wiki. The catch-all page `pages/[[...slug]].js` serves HTML files from `docs/` (copied to `public/` during build via `scripts/copyDocs.js`). `docs/` contains ~40 hand-authored HTML pages. Deployed on Vercel.

## Branch conventions (from CONTRIBUTING.md)

- `feature/name` — new features
- `fix/description` — bug fixes
- `docs/topic` — documentation
- `refactor/module` — refactoring

Commit messages start with a verb (Add, Fix, Update, Improve).

## Code style

- Java naming: camelCase variables/methods, PascalCase classes
- 4-space indent or 1 tab, max 120 chars per line
- Javadoc on public methods/classes
