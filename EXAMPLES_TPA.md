# TPA System Example - Copy & Paste Ready

This is a complete, ready-to-use TPA (Teleport Request) system using custom syntax!

## Setup Instructions

### Step 1: Copy into `config/CommandMaker/syntax.json`

```json
{
  "tpa": {
    "pattern": "/tpa <player>",
    "description": "Send teleport request to another player"
  },
  "tpaccept": {
    "pattern": "/tpaccept <player>",
    "description": "Accept teleport request from a player"
  },
  "tpadeny": {
    "pattern": "/tpadeny <player>",
    "description": "Deny teleport request from a player"
  }
}
```

### Step 2: Copy into `config/CommandMaker/aliases.json`

```json
{
  "tpa": "tellraw ${tpa_player} {\"text\":\"${player} wants to teleport to you! Type /tpaccept ${player} to accept or /tpadeny ${player} to deny.\",\"color\":\"yellow\"}",

  "tpaccept": "execute as ${tpaccept_player} at @s run tp ${player} @s",

  "tpadeny": "tellraw ${tpadeny_player} {\"text\":\"Teleport request from ${player} denied.\",\"color\":\"red\"}"
}
```

## Usage in-Game

### Player A requests TP to Player B:

```
/tpa B
```

→ B gets message: "A wants to teleport to you! Type /tpaccept A to accept or /tpadeny A to deny."

### Player B accepts:

```
/tpaccept A
```

→ A gets teleported to B's location

### Or Player B denies:

```
/tpadeny A
```

→ A gets message: "Teleport request from A denied."

---

## How It Works

1. `/tpa B` matches syntax pattern `/tpa <player>` with `player=B`
2. Alias executes: `tellraw B {"text":"A wants to teleport to you!..."}`
3. `/tpaccept A` matches pattern `/tpaccept <player>` with `player=A`
4. Alias executes: `execute as A at @s run tp YOUR_NAME @s`

---

## Customization Ideas

### Add Permission Checks

```json
{
  "tpa": "execute if predicate your_tpa_allowed run tellraw ${tpa_player} {\"text\":\"${player} wants to teleport to you!\",\"color\":\"yellow\"}"
}
```

### Add Confirmation Messages

```json
{
  "tpa": "tellraw ${player} {\"text\":\"Request sent to ${tpa_player}!\",\"color\":\"green\"} && tellraw ${tpa_player} {\"text\":\"${player} wants to teleport to you!\",\"color\":\"yellow\"}"
}
```

### Add Delay

```json
{
  "tpaccept": "schedule function my_namespace:accept_tp 5t"
}
```

### Combine with Built-in Variables

```json
{
  "tpa": "say ${player} at ${x} ${y} ${z} wants to teleport to ${tpa_player}!"
}
```

---

## Testing Checklist

- [ ] Reload config: `/cmd reload`
- [ ] View syntaxes: `/syntax` (should show tpa, tpaccept, tpadeny)
- [ ] Test TPA request: `/tpa <player>`
- [ ] Check message appears to target player
- [ ] Test accept: `/tpaccept <player>`
- [ ] Verify teleport works
- [ ] Test deny: `/tpadeny <player>`
- [ ] Verify deny message appears

---

## Advanced: Multi-step TPA with Variables

You can track TPA requests using custom variables!

```json
// In syntax.json
{
  "tpa": {
    "pattern": "/tpa <player>",
    "description": "Send TPA request"
  }
}
```

```json
// In aliases.json
{
  "tpa": "setcmdvariable tpa_request ${tpa_player} && tellraw ${tpa_player} {\"text\":\"TPA from ${player}\",\"color\":\"yellow\"}"
}
```

Now you can track who requested using `/setcmdvariable` and `${tpa_request}`!

---

## Common Issues

**Issue:** Message doesn't appear  
**Solution:** Check syntax.json has correct pattern, run `/cmd reload`

**Issue:** Player teleports to wrong location  
**Solution:** Verify `@s` is used to teleport to the target player

**Issue:** Variables not substituting  
**Solution:** Ensure format is `${tpa_player}` (syntax_name_parameter)

---

Ready to deploy! Good luck! 🚀
