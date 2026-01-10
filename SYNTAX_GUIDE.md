# Custom Command Syntax System - User Guide

## Overview

The Custom Command Syntax System allows you to define reusable command patterns with parameters that automatically get substituted into your aliases. This enables powerful, data-driven command creation.

## How It Works

### Example: Teleport Request (TPA) System

Instead of users having to know the exact command, they can use a simpler syntax:

**Configuration** (`config/CommandMaker/syntax.json`):

```json
{
  "tpa": {
    "pattern": "/tpa <player>",
    "description": "Teleport request to a player"
  }
}
```

**Alias** (`config/CommandMaker/aliases.json`):

```json
{
  "tpa_accept": "tp ${tpa_player} @s"
}
```

**User Input:**

```
/tpa steve
```

**Result:** Gets converted to: `tp steve @s`

---

## Step-by-Step Setup

### Step 1: Define Your Syntax

Edit `config/CommandMaker/syntax.json`:

```json
{
  "tpa": {
    "pattern": "/tpa <player>",
    "description": "Teleport request to a player"
  },
  "give": {
    "pattern": "/give <item> <amount>",
    "description": "Give an item to sender"
  },
  "msg": {
    "pattern": "/msg <player> <message>",
    "description": "Send a message to a player"
  }
}
```

**Syntax Rules:**

- `pattern`: The command format with `<parameter_name>` placeholders
- `description`: Human-readable description (optional)
- Parameter names can contain letters, numbers, and underscores

### Step 2: Create Aliases Using Syntax Variables

Edit `config/CommandMaker/aliases.json`:

```json
{
  "tpa_request": "execute as ${tpa_player} run say ${player} wants to TP to you!",
  "give_item": "give @s ${give_item} ${give_amount}",
  "message": "tellraw ${msg_player} {\"text\":\"${msg_message}\"}"
}
```

**Variable Format:** `${syntax_name_parameter_name}`

- Syntax name: The key from syntax.json (e.g., "tpa")
- Parameter name: The parameter from the pattern (e.g., "player")

### Step 3: Test with Commands

In-game:

```
/tpa steve              → triggers alias with ${tpa_player} = "steve"
/give diamond 64        → triggers alias with ${give_item} = "diamond", ${give_amount} = "64"
/msg alex hello world   → triggers alias with ${msg_player} = "alex", ${msg_message} = "hello world"
```

---

## Real-World Examples

### Example 1: Server Join Message

**Syntax:**

```json
{
  "announce": {
    "pattern": "/announce <message>",
    "description": "Announce to all players"
  }
}
```

**Alias:**

```json
{
  "announce_msg": "tellraw @a {\"text\":\"§6[ANNOUNCEMENT] ${announce_message}\"}"
}
```

**Usage:**

```
/announce_msg Maintenance in 5 minutes!
```

### Example 2: Ban System

**Syntax:**

```json
{
  "ban": {
    "pattern": "/ban <player> <reason>",
    "description": "Ban a player with reason"
  }
}
```

**Alias:**

```json
{
  "ban_player": "ban ${ban_player} ${ban_reason}"
}
```

**Usage:**

```
/ban_player hacker Cheating
```

### Example 3: Teleport to Coordinates

**Syntax:**

```json
{
  "goto": {
    "pattern": "/goto <x> <y> <z>",
    "description": "Teleport to coordinates"
  }
}
```

**Alias:**

```json
{
  "warp": "tp ${player} ${goto_x} ${goto_y} ${goto_z}"
}
```

**Usage:**

```
/warp 100 64 -200
```

---

## Built-in Variables (Still Work!)

Custom syntax parameters work alongside the existing variables:

- `${player}` - Current player name
- `${x}`, `${y}`, `${z}` - Player coordinates
- `${custom_var}` - Custom variables set with `/setcmdvariable`
- `${syntax_name_parameter}` - Custom syntax parameters (NEW!)

**Combined Example:**

```json
{
  "home_teleport": "tp ${player} ${home_x} ${home_y} ${home_z}"
}
```

Using syntax parameter AND built-in:

```
/home_teleport 0 64 0
```

---

## Commands

### View All Syntaxes

```
/syntax
```

Shows all defined custom syntax patterns and descriptions.

### View All Aliases

```
/deletealias
```

Lists all aliases (unchanged).

### Add New Alias

```
/cmd add <alias> <command>
/cmd add warp_spawn tp ${player} 0 64 0
```

### Delete Alias

```
/cmd del <alias>
```

### Reload Config

```
/cmd reload
```

Reloads both aliases and syntax definitions from files.

---

## Troubleshooting

### Syntax Not Matching

1. Check the pattern in `syntax.json` - it's case-sensitive
2. Verify parameter names only contain letters, numbers, underscores
3. Use `/syntax` command to see defined patterns
4. Parameters are greedy - `/msg <player> <message>` captures everything after first space for message

### Variables Not Substituting

1. Check variable format: `${syntax_name_parameter}` (all lowercase)
2. Verify syntax exists: `config/CommandMaker/syntax.json`
3. Check alias: `config/CommandMaker/aliases.json`
4. Use `/cmd reload` to refresh

### Multiple Parameters with Spaces

```json
{
  "msg": {
    "pattern": "/msg <player> <message>"
  }
}
```

The last parameter is **greedy** and captures everything after the previous space, including spaces.

---

## Configuration Files Location

- **Aliases:** `config/CommandMaker/aliases.json`
- **Custom Syntax:** `config/CommandMaker/syntax.json`

Both are auto-created on first run with examples!

---

## Tips for Advanced Usage

1. **Combine Syntax with Existing Variables:**

   ```json
   {
     "home": "tp ${player} ${home_x} ${home_y} ${home_z}"
   }
   ```

2. **Multi-word Parameters:**
   Make the last parameter greedy to capture sentences/messages

3. **Multiple Syntaxes:**
   You can define as many syntax patterns as you want - just register them in the JSON

4. **Reload Without Restart:**
   ```
   /cmd reload
   ```
   This reloads both aliases and syntax definitions

---

## Performance Notes

- Syntax matching happens in the order parameters are defined
- Each custom syntax adds a regex pattern match - keep patterns simple for best performance
- No performance impact on commands that don't use syntax

---

Enjoy creating powerful, user-friendly commands! 🚀
