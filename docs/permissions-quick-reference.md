# CMDMaker Permissions - Quick Reference

## Cheat Sheet

### Basic Concepts

| Term | Meaning |
|------|---------|
| **Ops** | Players with permission level 2+, always bypass all checks |
| **Permission Node** | A string like `cmdmaker.cmd` that grants a specific ability |
| **Wildcard** | `*` matches everything (e.g., `cmdmaker.*` = all cmdmaker permissions) |
| **LuckPerms** | Optional plugin for advanced group-based permissions |
| **permissions.json** | Built-in config file (used when LuckPerms not installed) |

---

## Quick Setup Scenarios

### Scenario 1: Everyone Can Use Everything

**With LuckPerms:**
```bash
/lp group default permission set cmdmaker.*
```

**With Config Only:**
- Leave `permissions.json` as default (no restrictions)

---

### Scenario 2: Only Ops Can Use Commands

**With LuckPerms:**
- Do nothing (already ops-only by default)

**With Config Only:**
- Don't add any permissions to `permissions.json`

---

### Scenario 3: Players Can Use Specific Aliases

**With LuckPerms:**
```bash
/lp group member permission set cmdmaker.alias.home
/lp group member permission set cmdmaker.alias.spawn
/lp user PlayerName parent set member
```

**With Config Only:**
Edit `permissions.json`:
```json
{
  "players": {
    "permissions": {
      "uuid-of-player": [
        "cmdmaker.alias.home",
        "cmdmaker.alias.spawn"
      ]
    }
  }
}
```

---

### Scenario 4: Moderators Can Manage Aliases

**With LuckPerms:**
```bash
/lp group moderator permission set cmdmaker.cmd
/lp group moderator permission set cmdmaker.manage.alias
/lp user ModeratorName parent set moderator
```

---

### Scenario 5: Staff Can Use Everything

**With LuckPerms:**
```bash
/lp group staff permission set cmdmaker.*
/lp user StaffMemberName parent set staff
```

---

## Permission Reference

### Command Permissions
| Permission | Effect |
|-----------|--------|
| `cmdmaker.cmd` | Allow `/cmd` command |
| `cmdmaker.addcommand` | Allow `/addcommand` command |
| `cmdmaker.manage.alias` | Allow adding/deleting aliases |
| `cmdmaker.manage.permissions` | Allow `/cmmakerperm` command |

### Alias Permissions
| Permission | Effect |
|-----------|--------|
| `cmdmaker.alias.*` | Allow all aliases |
| `cmdmaker.alias.home` | Allow `/home` alias |
| `cmdmaker.alias.spawn` | Allow `/spawn` alias |
| `cmdmaker.alias.{name}` | Allow specific alias |

### Wildcards
| Permission | Grants |
|-----------|--------|
| `cmdmaker.*` | All CMDMaker permissions |
| `cmdmaker.alias.*` | All aliases |

---

## Common LuckPerms Commands

```bash
# View player permissions
/lp user PlayerName info

# Add permission to player
/lp user PlayerName permission set cmdmaker.alias.home

# Add permission to group
/lp group admin permission set cmdmaker.*

# Remove permission
/lp user PlayerName permission unset cmdmaker.alias.home

# List permissions containing "cmdmaker"
/lp search cmdmaker

# Add player to group
/lp user PlayerName parent set admin

# Create permission group
/lp creategroup groupname

# Promote/demote with track
/lp user PlayerName track set promotiontrack member
```

---

## Config File Locations

- **Fabric**: `config/CommandMaker/permissions.json`
- **Quilt**: `config/CommandMaker/permissions.json`

---

## Default Behavior

| Scenario | Result |
|----------|--------|
| Operator player | ✅ Full access (bypasses all checks) |
| Non-Op + LuckPerms | ❓ Checked via LuckPerms |
| Non-Op + No LuckPerms | ❌ No access (unless in permissions.json) |
| Console | ✅ Full access always |

---

## Troubleshooting Checklist

- [ ] Is player an operator? (`/op PlayerName` if yes)
- [ ] Is LuckPerms installed? (`/lp info` to check)
- [ ] Does player have permission? (`/lp user PlayerName info`)
- [ ] Is `permissions.json` valid JSON?
- [ ] Did you reload permissions? (`/cmmakerperm reload`)
- [ ] Did you restart the server after changing config?

---

## Support

- **For LuckPerms help**: `/lp`
- **To reload permissions**: `/cmmakerperm reload`  
- **To check player perms**: `/lp user {name} info`
- **Wiki**: https://commandmakerwiki.lucasgeitgey.com
