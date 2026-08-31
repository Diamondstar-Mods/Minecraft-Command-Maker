# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Build Commands

The Gradle wrapper scripts (`gradlew` / `gradlew.bat`) use Gradle 9.5.1. The project uses **Fabric Loom 1.16.2**. `build.bat` is a convenience wrapper for `gradlew.bat build`. There are **no automated tests** — `./gradlew check` only validates compilation. Output JARs land in `<subproject>/build/libs/`.

### Subprojects registered in `settings.gradle`

```bash
./gradlew :server-1.21:build    # Fabric server, MC 1.21.11, Java 17 (primary 1.21 module)
./gradlew :server-1.22:build    # Fabric server, MC 26.1.2, Java 25 (primary 26.x module)
./gradlew :client:build         # Fabric client-only, MC 1.21.11, Java 17
./gradlew :client-1.22:build    # Fabric client-only, MC 26.1.2, Java 25
./gradlew build                 # Build all registered subprojects
```

Also registered: `server-1.21-paper`, `server-1.21-forge-neoforge`, `server-rift` (1.21 loader ports), `client-legacy`, `server-legacy` (Legacy Fabric, MC 1.8-1.13.2, Java 8), `server-litloader`, `server-nilloader`, `server-ornithe` (old loader ports).

On disk but **not** in `settings.gradle`: `server-1.17`, `server-1.18-1.19-1.20`, `quilt-client`, `quilt-server`. Add them to `settings.gradle` before building.

Run the game: `./gradlew :client:runClient` or `./gradlew :client-1.22:runClient`.

Known Gradle quirk: if a module's `:jar` task fails with `java.nio.file.ClosedFileSystemException` right after a compile error, run `./gradlew --stop` and rebuild — a crashed build leaves poisoned zipfs state in the daemon.

## CRITICAL: MC 26.x has no Yarn / intermediary mappings

Fabric for MC 26.1+ (the new versioning scheme after 1.21.11) uses **Mojang official (Mojmap) names natively**. There is no intermediary for any 26.x version (Fabric's meta API returns a `0.0.0` placeholder; intermediary maven artifacts stop at `25w46a`).

Consequences for the 26.x modules (`server-1.22`, `client-1.22`):
- Build config must **not** declare `mappings` (no Yarn), and uses `implementation` instead of `modImplementation` for fabric-loader/fabric-api.
- Source is written in Mojmap names (`net.minecraft.client.Minecraft`, `net.minecraft.network.chat.Component`), not Yarn names.
- A jar built with the 1.21-style Yarn pipeline for 26.x contains `net/minecraft/class_NNNN` references that cannot be resolved at runtime → `NoClassDefFoundError` on startup. This was the root cause of issue #33 (client edition crash on MC 26.2).
- `fabric.mod.json` must constrain `"minecraft": ">=26.1"` — never a 1.x floor like `>=1.20`, which lets the jar install on incompatible versions.

## Project Architecture

This is **Minecraft Command Maker** (v4.0.0 line; root `mod_version=4.0.0-rc.1`), a Fabric mod that adds custom aliases, parameterized command syntax, and downloadable `.mcfunction` execution. Gradle multi-module monorepo with per-version source trees; every module has a full copy of the Java source under `src/main/java/com/example/`.

### Version / module matrix

Root `gradle.properties`: `minecraft_version=1.21.11`, `yarn_mappings=1.21.11+build.6`, `mod_version=4.0.0-rc.1` — used by all 1.21 modules. The 26.x modules have their own `gradle.properties` (`minecraft_version=26.1.2`, `loader_version=0.19.2`, `fabric_version=0.149.1+26.1.2`) and Java 25 toolchains.

| Module | Loader | MC | Side |
|--------|--------|-----|------|
| `server-1.21/` | Fabric | 1.21.11 | Server |
| `server-1.22/` | Fabric | 26.1.2 | Server |
| `client/` | Fabric | 1.21.11 | Client-only |
| `client-1.22/` | Fabric | 26.1.2 | Client-only |
| `client-legacy/`, `server-legacy/` | Legacy Fabric | 1.8-1.13.2 | Both |

Mod IDs: server edition `nekkycommandmaker` (entrypoint `com.example.CommandMaker`, `ModInitializer`), client edition `nekkycommandmakerclient` (entrypoint `com.example.CMDMakerClient`, `ClientModInitializer`).

### Core manager classes (static singletons in `com.example`)

- **`CommandMaker` / `CMDMakerClient`** — Entry points; register all commands and delegate to managers.
- **`AliasManager`** — Alias CRUD in `config/CommandMaker/aliases.json`; auto-registers `.mcfunction` files as `function:` aliases.
- **`SyntaxManager`** + **`CommandSyntax`** — Pattern matching: `/tpa <player>` patterns in `syntax.json` become regex capture groups; `${syntaxName_param}` placeholders are substituted.
- **`VariableManager`** — Two-layer substitution: built-in `${player}`, `${x}`, `${y}`, `${z}` + per-player custom variables.
- **`PermissionManager`** — `NONE → ALIAS_USER → COMMAND_USER → MODERATOR → ADMIN`; falls back through vanilla op level → LuckPerms (compileOnly) → `permissions.json`.
- **`FunctionManager`** — Downloads and executes `.mcfunction` files from the GitHub CDN manifest; local files under `config/CommandMaker/Functions/`.
- **`TelemetryManager`** — No-op stub (telemetry removed; old code in `oldtel/`).

`server-1.22` additionally has the v4.0 managers: `ModuleManager`, `ConditionManager`, `CooldownManager`, `EventManager`, `PlaceholderManager`, `ScoreboardManager`. Its build.gradle excludes `com/example/gui/**` and `CommandMakerClient.java` from compilation.

### Client edition specifics (`client-1.22`)

- **No GUI.** The Function Manager screen was removed — it crashed the game (issue #33 follow-up). There is no `/cmd gui` command in this edition. (`client/`, the 1.21 edition, still has `gui/FunctionManagerScreen.java`.)
- **Command execution** goes through `CMDMakerClient.executeClientCommand()`: commands registered client-side (fabric client commands, including aliases that target other aliases) run locally through `ClientCommands.getActiveDispatcher().execute()`; everything else is sent to the server with `ClientPacketListener.sendCommand()` so it *executes* instead of being typed into chat. Note: the fabric client dispatcher mirrors server commands only for tab-completion — their local nodes are no-ops returning 0, so executing them locally does nothing.
- `.mcfunction` lines run through the same `executeClientCommand()` helper.

### MC 26.1 API differences vs 1.21 (for migrating source between modules)

- Fabric client commands: `ClientCommandManager` → `ClientCommands` (same package, static `literal()`/`argument()`).
- Screens: `Screen.render(DrawContext, ...)` replaced by `extractRenderState(GuiGraphicsExtractor, ...)` (background is drawn by the framework's `extractRenderStateWithTooltipAndSubtitles` wrapper); `close()` → `onClose()`; tooltips via `GuiGraphicsExtractor.setTooltipForNextFrame(...)`.
- Mouse events: `net.minecraft.client.gui.Click` record → `net.minecraft.client.input.MouseButtonEvent` (`mouseClicked(MouseButtonEvent, boolean)`).
- Yarn → Mojmap renames: `MinecraftClient` → `Minecraft`, `Text` → `Component`, `DrawContext` → `GuiGraphicsExtractor`, `textRenderer` → `font`, `client` field → `minecraft`, `networkHandler` → `connection`, `sendMessage(Text, boolean)` → `sendSystemMessage(Component)`, `getUuid()` → `getUUID()`.

### GUI system (server modules)

In `gui/`: `FunctionChestHandler`/`FunctionChestScreen` (54-slot chest GUI, tabs, pagination), `AliasDeleteScreen` (click-to-delete aliases), `ModScreens`. Not compiled in `server-1.22`.

### Config file layout (runtime, under `config/CommandMaker/`)

- `aliases.json` — alias name → command target
- `syntax.json` — syntax name → { pattern, description }
- `permissions.json` — permission configuration
- `Functions/*.mcfunction` — multi-line function files

### Other

- `CompiledFiles/` — prebuilt release jars at the repo root (release artifacts, e.g. `CMDMaker-Fabric-client-26.1.jar`).
- **Website**: pure static HTML in `docs/` — `components.js` injects nav/sidebar/footer, `styles.css` is the design system, `script.js` handles theme/search/ToC. No build step.

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

## Mixins

Each module has `modid.mixins.json` (and some have `modid.client.mixins.json`) in `src/main/resources/`, referenced by `fabric.mod.json`. Currently empty mixin lists — check these configs when dealing with class-transformation work.
