# Advanced Permissions & LuckPerms Setup Guide

## Table of Contents
1. [Overview](#overview)
2. [Installation](#installation)
3. [Configuration](#configuration)
4. [Permission System](#permission-system)
5. [LuckPerms Integration](#luckperms-integration)
6. [Commands](#commands)
7. [Examples](#examples)
8. [Troubleshooting](#troubleshooting)

---

## Overview

CMDMaker now features an advanced permission system that gives server administrators fine-grained control over who can use aliases and commands. The system supports:

- **Per-Alias Permissions**: Control which players can use specific aliases
- **Per-Command Permissions**: Allow/deny `/cmd` and `/addcommand` access separately
- **Operator Bypass**: Operators (permission level 2+) always have full access
- **LuckPerms Integration**: Full integration with the popular LuckPerms permission plugin
- **Configuration-Based Fallback**: If LuckPerms isn't installed, use built-in JSON configuration
- **Per-Player Permissions**: Grant specific permissions directly to player UUIDs

### Permission Hierarchy

1. **Operators (Level 2+)**: Always have full access - no checks needed
2. **LuckPerms Users**: If LuckPerms is installed, permissions are checked via LuckPerms
3. **Config-Based Users**: Fallback to `permissions.json` configuration
4. **Default**: No access unless explicitly granted

---

## Installation

### Step 1: Install the Mod

Install CMDMaker mod to your Minecraft server:
- Fabric: Copy JAR to `mods/` folder
- Quilt: Copy JAR to `mods/` folder

### Step 2: (Optional) Install LuckPerms

For advanced permission management with groups and inheritance, install LuckPerms:

1. Download LuckPerms from [LuckPerms Downloads](https://luckperms.net/download)
2. Place the JAR in your `mods/` folder
3. Restart the server
4. CMDMaker will automatically detect and integrate with LuckPerms

**Note**: LuckPerms is optional. CMDMaker works perfectly without it using built-in configuration.

### Step 3: Server Startup

When the server starts:
- CMDMaker will automatically create `config/CommandMaker/permissions.json`
- Default configuration allows all players to use all commands (no restrictions)
- LuckPerms integration will be enabled if the plugin is detected

---

## Configuration

### Default Configuration Structure

The first time the server runs, CMDMaker creates a default `permissions.json` file:

```json
{
  "version": "1.0",
  "description": "CMDMaker Advanced Permissions Configuration",
  "useLuckPerms": true,
  "enablePerAliasPermissions": true,
  "aliases": {
    "home": {
      "permission": "cmdmaker.alias.home",
      "description": "Allow players to use the home alias",
      "enabled": true
    },
    "tp": {
      "permission": "cmdmaker.alias.tp",
      "description": "Allow players to use the teleport alias",
      "enabled": true
    }
  },
  "globalPermissions": {
    "cmdmaker.cmd": "Allow use of /cmd command",
    "cmdmaker.addcommand": "Allow use of /addcommand",
    "cmdmaker.manage.alias": "Allow managing aliases",
    "cmdmaker.manage.permissions": "Allow managing permissions",
    "cmdmaker.*": "Allow all cmdmaker features"
  },
  "players": {
    "permissions": {}
  },
  "luckperms": {
    "syncInterval": 300,
    "enabled": true,
    "groupPrefix": "cmdmaker"
  }
}
```

### Configuration Locations

- **Fabric Server/Client**: `config/CommandMaker/permissions.json`
- **Quilt Server/Client**: `config/CommandMaker/permissions.json`

### Key Settings

| Setting | Type | Default | Description |
|---------|------|---------|-------------|
| `version` | String | "1.0" | Config version (for future compatibility) |
| `useLuckPerms` | Boolean | true | Enable LuckPerms integration if available |
| `enablePerAliasPermissions` | Boolean | true | Enable per-alias permission checks |
| `aliases` | Object | {} | Per-alias configuration and permissions |
| `luckperms.enabled` | Boolean | true | Allow syncing with LuckPerms |
| `luckperms.syncInterval` | Number | 300 | How often to sync permissions (seconds) |

---

## Permission System

### Permission Nodes

CMDMaker uses hierarchical permission nodes following Minecraft conventions:

#### Global Permissions
- `cmdmaker.*` - Admin wildcard, grants all permissions
- `cmdmaker.cmd` - Allow use of `/cmd` command
- `cmdmaker.addcommand` - Allow use of `/addcommand` command
- `cmdmaker.manage.alias` - Allow adding/deleting/reloading aliases
- `cmdmaker.manage.permissions` - Allow managing permissions (admin command)

#### Per-Alias Permissions
- `cmdmaker.alias.{aliasname}` - Allow use of specific alias

**Examples:**
- `cmdmaker.alias.home` - Use the /home alias
- `cmdmaker.alias.tp` - Use the /tp alias
- `cmdmaker.alias.ban` - Use the /ban alias

### How Permissions Are Checked

1. **Console Commands**: Always pass - console has full access
2. **Operator Players**: Pass if permission level ≥ 2 (default OP level)
3. **LuckPerms Enabled**: Check LuckPerms permission if installed
4. **Configuration Fallback**: Check `permissions.json`
5. **Default Deny**: If no permission found, access is denied

---

## LuckPerms Integration

### Why LuckPerms?

LuckPerms is the most powerful permission plugin for Minecraft. It provides:

- **Group Management**: Create permission groups for easy bulk assignment
- **Inheritance**: Extend permissions from parent groups
- **Tracks**: Create hierarchical advancement tracks (e.g., Member → Moderator → Admin)
- **Prefix/Suffix System**: Custom tags for players
- **Web Editor**: Beautiful online configuration interface
- **Per-World Permissions**: Different permissions on different worlds
- **Server Network Support**: Sync permissions across multiple servers

### Setting Up LuckPerms

#### 1. Install LuckPerms

```bash
# Download from https://luckperms.net/download
# Place in mods/ folder
# Restart server
```

#### 2. Create Permission Groups

```
/lp creategroup member
/lp creategroup moderator
/lp creategroup admin
```

#### 3. Add CMDMaker Permissions

**For Regular Members** (limited alias access):
```
/lp group member permission set cmdmaker.alias.home
/lp group member permission set cmdmaker.alias.spawn
/lp group member permission set cmdmaker.alias.tp
```

**For Moderators** (command access):
```
/lp group moderator permission set cmdmaker.cmd
/lp group moderator permission set cmdmaker.addcommand
/lp group moderator permission set cmdmaker.manage.alias
```

**For Admins** (full access):
```
/lp group admin permission set cmdmaker.*
```

#### 4. Set Up Track Hierarchy

```
/lp createtrack promotion member moderator admin
/lp user {player} track set promotion member   # Assign to track at member level
/lp user {player} track set promotion admin    # Promote to admin
```

#### 5. Assign Players to Groups

```
/lp user {player} parent set member
/lp user {player} parent set moderator
/lp user {player} parent set admin
```

### Common LuckPerms Commands

```bash
# View a player's permissions
/lp user {player} info

# Add permission to player
/lp user {player} permission set cmdmaker.cmd

# Remove permission from player
/lp user {player} permission unset cmdmaker.cmd

# Add permission to group
/lp group {group} permission set cmdmaker.cmd

# View group permissions
/lp group {group} info

# List all CMDMaker permissions
/lp search cmdmaker
```

### LuckPerms Web Editor

For an easier visual interface:

1. Install LuckPerms Web Editor plugin
2. Run `/lp web`
3. Open the provided URL in your browser
4. Configure permissions visually
5. Changes sync automatically to the server

---

## Commands

### /cmd - Manage Aliases (Server-Side)

**Requires**: `cmdmaker.cmd` permission

```
/cmd list                           # List all aliases
/cmd reload                         # Reload aliases from config (requires cmdmaker.manage.alias)
/cmd add <alias> <target>          # Add an alias (requires cmdmaker.manage.alias)
/cmd del <alias>                   # Delete an alias (requires cmdmaker.manage.alias)
/cmd function <function>           # Execute a function
/cmd syntax                        # List custom syntax definitions
```

**Examples**:
```
/cmd add home "tp @s 0 100 0"
/cmd add warp "tp @s %1 %2 %3"
/cmd del home
/cmd reload
```

### /addcommand - Alternative Alias Management

**Requires**: `cmdmaker.addcommand` permission

```
/addcommand add <alias> <command>  # Add an alias
/addcommand del <alias>            # Delete an alias
/addcommand reload                 # Reload aliases
```

### /cmmakerperm - Permission Management (Admin Only)

**Requires**: OP level 4 or `cmdmaker.manage.permissions`

```
/cmmakerperm grant <player> <permission>    # Grant permission (edit permissions.json)
/cmmakerperm revoke <player> <permission>   # Revoke permission (edit permissions.json)
/cmmakerperm check <player> <permission>    # Check if player has permission
/cmmakerperm reload                         # Reload permission configuration
```

**Notes**: 
- The `grant` and `revoke` commands display a note to use LuckPerms or edit permissions.json directly
- `/cmmakerperm reload` reloads the permissions.json file

### Using Aliases

Once aliases are registered, players with permission can execute them:

```
/home           # Execute the home alias
/tp player 0 100 0    # Execute alias with arguments
/spawn          # Execute the spawn alias
```

---

## Examples

### Example 1: Basic Permission Setup (No LuckPerms)

Edit `config/CommandMaker/permissions.json`:

```json
{
  "version": "1.0",
  "aliases": {
    "home": {
      "permission": "cmdmaker.alias.home",
      "description": "Teleport to home",
      "enabled": true
    }
  },
  "globalPermissions": {
    "cmdmaker.cmd": "Basic player access to /cmd"
  },
  "players": {
    "permissions": {
      "a1b2c3d4-e5f6-7890-abcd-ef1234567890": [
        "cmdmaker.cmd",
        "cmdmaker.alias.home",
        "cmdmaker.alias.spawn"
      ]
    }
  }
}
```

### Example 2: LuckPerms Setup with Groups

```bash
# Create groups
/lp creategroup member
/lp creategroup moderator

# Add permissions to member group
/lp group member permission set cmdmaker.alias.home
/lp group member permission set cmdmaker.alias.spawn
/lp group member permission set cmdmaker.alias.warp

# Add permissions to moderator group
/lp group moderator permission set cmdmaker.cmd
/lp group moderator permission set cmdmaker.addcommand
/lp group moderator permission set cmdmaker.*

# Assign player to member group
/lp user Steve parent set member

# Promote player to moderator
/lp user Steve parent set moderator
```

### Example 3: Mix Operators and Permissions

**Vanilla Operators** (permission level 2+):
- Always have full access to all commands and aliases
- No permission checks applied

**Non-Op Players**:
- Required to have specific permissions
- Example: Player needs `cmdmaker.alias.home` to use `/home`

```
/op Steve              # Steve now has full access to everything
/deop Alex            # Alex now needs explicit permissions

# Give Alex limited access:
/lp user Alex permission set cmdmaker.alias.home
/lp user Alex permission set cmdmaker.alias.spawn
```

### Example 4: Function Permissions

Allow players to execute functions but restrict which ones:

```bash
# Create function aliases with per-alias permissions
/cmd add setspawn "function:setspawn"
/cmd add cleanup "function:cleanup"

# Only ops or those with explicit permission can use these
/lp group admin permission set cmdmaker.alias.setspawn
/lp group admin permission set cmdmaker.alias.cleanup
```

---

## Troubleshooting

### Issue: Players can't execute aliases

**Solution Steps**:

1. **Check if player is operator**:
   - Operators bypass all checks
   - Type `/op {player}` to make them an operator

2. **Check LuckPerms status**:
   - Type `/lp info` in game
   - If not installed, skip LuckPerms troubleshooting

3. **Check permissions configuration**:
   - View `config/CommandMaker/permissions.json`
   - Verify the permission node is correct
   - Example: `cmdmaker.alias.home` for /home alias

4. **Check player permissions**:
   ```bash
   # With LuckPerms:
   /lp user {player} info
   
   # With config:
   # Edit permissions.json and verify UUID is in "permissions" section
   ```

5. **Test with console**:
   - Execute the alias from console: `/home`
   - Console always has access if it works in console but not player

### Issue: LuckPerms not integrating

**Solution Steps**:

1. **Verify LuckPerms is installed**:
   - Check `mods/` folder for LuckPerms JAR
   - Restart server if just installed

2. **Check server log for integration message**:
   - Look for: "LuckPerms detected - enabling LuckPerms integration"
   - Or: "LuckPerms not found - using built-in permissions only"

3. **Verify LuckPerms is working**:
   ```bash
   /lp info      # Should show LuckPerms info
   /lp user {player} info    # Should show player info
   ```

4. **Check CMDMaker permissions in LuckPerms**:
   ```bash
   /lp search cmdmaker      # Should show CMDMaker permissions
   ```

### Issue: Permission node not recognized

**Verify node syntax**:
- Should be lowercase: `cmdmaker.*` not `CMDMaker.*`
- Dot notation: `cmdmaker.alias.home` not `cmdmaker-alias-home`
- Wildcard: `cmdmaker.*` applies to all cmdmaker permissions

**Common mistakes**:
```bash
# WRONG:
/lp user Steve permission set CMDMaker.cmd          # Wrong case
/lp user Steve permission set cmdmaker.CMD          # Wrong case  
/lp user Steve permission set cmdmaker-cmd          # Wrong separator

# CORRECT:
/lp user Steve permission set cmdmaker.cmd          # Correct
/lp user Steve permission set cmdmaker.alias.home   # Correct
/lp user Steve permission set cmdmaker.*            # Correct
```

### Issue: Reload not taking effect

**Solution**:

1. **Reload CMDMaker permissions**:
   ```bash
   /cmmakerperm reload
   ```

2. **Reload LuckPerms permissions**:
   ```bash
   /lp sync
   ```

3. **Reload aliases**:
   ```bash
   /cmd reload
   ```

### Issue: Permission.json not being created

**Solution Steps**:

1. **Check mod is installed**:
   - Verify JAR is in `mods/` folder
   - Check server log for "Alias mod initialized!"

2. **Check file permissions**:
   - Ensure server has write access to `config/` directory
   - Try manually creating `config/CommandMaker/` folder

3. **Check for errors in log**:
   - Look for "Failed to create config" messages
   - Check disk space availability

4. **Manually create the file**:
   - Create folder: `config/CommandMaker/`
   - Copy default permissions.json from below
   - Restart server

---

## Advanced Configuration

### Custom Permission Nodes

You can create custom permission nodes in LuckPerms:

```bash
# Command-specific permissions
/lp group builders permission set cmdmaker.alias.building-tools
/lp group builders permission set cmdmaker.alias.voxelsniper
/lp group builders permission set cmdmaker.alias.worldedit

# Context-specific (world-specific)
/lp user Steve permission settemp cmdmaker.cmd world=world_nether 1h
```

### Permission Prefixes

If using LuckPerms with servers, use prefix in permissions:

```bash
# Server 1 only
/lp user Steve permission settemp cmdmaker.* server=server1
```

### Expiring Permissions

Grant temporary permissions:

```bash
# Grant for 1 hour
/lp user Steve permission settemp cmdmaker.alias.admin-commands 1h

# Grant for 1 day
/lp user Steve permission settemp cmdmaker.cmd 1d

# Grant indefinitely (permanent)
/lp user Steve permission set cmdmaker.cmd
```

---

## Best Practices

1. **Use LuckPerms for production servers**: It's more powerful and easier to manage
2. **Create clear group hierarchy**: Avoid giving individual permissions to many players
3. **Document your setup**: Keep notes on what each group can do
4. **Test permissions before distributing**: Use `/lp user test info` before giving access to real players
5. **Regular backups**: If editing permissions.json, keep backups
6. **Use tracks for promotions**: Make it easy to promote players
7. **Monitor permission changes**: Use LuckPerms web editor for audit trails
8. **Principle of least privilege**: Only grant minimum required permissions

---

## Support & Resources

- **CMDMaker Wiki**: https://commandmakerwiki.lucasgeitgey.com
- **LuckPerms Documentation**: https://luckperms.net/wiki
- **LuckPerms Discord**: https://discord.gg/luckperms
- **Report Issues**: Create an issue on GitHub

---

## Version History

### v2.4.0 (Current)
- Added advanced permission system
- Added LuckPerms integration
- Added per-alias permissions
- Added `/cmmakerperm` admin command

### v2.3.6
- Previous release (no permissions)

---

## FAQ

**Q: Do I need LuckPerms?**  
A: No. CMDMaker works with or without LuckPerms. Without it, use the built-in `permissions.json` file.

**Q: Can operators bypass permissions?**  
A: Yes. Players with OP level 2+ bypass all permission checks and can use any command/alias.

**Q: Can I give temporary permissions?**  
A: Yes, with LuckPerms using `/lp user {player} permission settemp`. Without LuckPerms, edit `permissions.json` manually.

**Q: How do I reset permissions?**  
A: Delete or reset the `config/CommandMaker/permissions.json` file, then restart the server.

**Q: Can I use per-world permissions?**  
A: Yes, with LuckPerms. Use `/lp user {player} permission set cmdmaker.cmd world=world_name`.

**Q: What if LuckPerms is installed but not detected?**  
A: Check the server log for errors. Make sure LuckPerms JAR is in the `mods/` folder and the server has been restarted.
