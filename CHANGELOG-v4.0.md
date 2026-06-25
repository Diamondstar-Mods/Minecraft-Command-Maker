# Command Maker v4.0 — Changelog

**Released June 2026** · See [full docs](https://commandmakerwiki.lucasgeitgey.com/v4.0-changelog.html)

## New Features

### 1. Built-in Cooldown System
Per-player & global cooldowns for any alias with custom messages. Config at `config/CommandMaker/cooldowns.json`.

```json
{ "cooldowns": { "home": { "type": "player", "seconds": 30 } } }
```
**Commands:** `/cmd cooldown set | clear | list`

### 2. Condition Blocks (If/Else)
Inline logic in alias targets. 9 condition types: `xp`, `health`, `perm`, `hasitem`, `var`, `cooldown`, `player`, `dimension`, `op`.

```
if:xp(30) give @s diamond else say Need 30 levels!
```

### 3. Event Triggers
Auto-run commands on join, leave, death, chat, block break, first join, kill, and respawn. Config at `config/CommandMaker/events.json`.

```json
{ "events": { "player_join": { "commands": ["say Welcome ${player}!"] } } }
```
**Commands:** `/cmd event list | reload`

### 4. Custom Scoreboards
JSON-defined dynamic scoreboards with `${placeholders}`, update intervals, and `enabled` toggle. Config at `config/CommandMaker/scoreboards.json`.

**Commands:** `/cmd scoreboard list | reload`

### 5. Placeholder System
15 built-in `%placeholders%` — PAPI-compatible syntax. Works everywhere: aliases, events, scoreboards, cooldowns.

`%player_health%` · `%player_xp%` · `%server_online%` · `%server_tps%` · `%var_name%` · `%cooldown_alias%`

### 6. Shareable Packages (.cmk)
Package everything into a single `.cmk` file. Safe merge on import. Stored in `config/CommandMaker/packages/`.

```
/cmd package MySetup           # one-command export (configs + functions)
/cmd import MySetup            # one-command import
/cmd import MySetup --overwrite # import replacing conflicts
/cmd module list               # browse available packages
/cmd module info MySetup       # preview without importing
```

## Installing v4.0
Replace the JAR. All new config files auto-create on first run with safe defaults (everything disabled). Existing aliases, syntaxes, and permissions are untouched.
