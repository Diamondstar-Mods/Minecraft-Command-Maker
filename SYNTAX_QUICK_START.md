# Custom Command Syntax System - Quick Start

## What You Can Now Do

Create your own **custom command patterns** with parameters that automatically substitute into aliases!

## Quick Example: TPA System

### 1. Define the Syntax Pattern
In `config/CommandMaker/syntax.json`:
```json
{
  "tpa": {
    "pattern": "/tpa <player>",
    "description": "Teleport request"
  }
}
```

### 2. Create an Alias Using the Parameter
In `config/CommandMaker/aliases.json`:
```json
{
  "tpa_request": "say ${tpa_player} wants to teleport to you!"
}
```

### 3. Use It In-Game
```
/tpa_request steve
```
This executes: `say steve wants to teleport to you!`

---

## Key Features

✅ **Data-Driven** - Define patterns in JSON, no code required  
✅ **Flexible Parameters** - Create any number of parameters in any pattern  
✅ **Multi-Parameter** - `/ban <player> <reason>` → `${ban_player}`, `${ban_reason}`  
✅ **Works with Existing Variables** - Still use `${player}`, `${x}`, `${y}`, `${z}`  
✅ **Easy Management** - Edit JSON files, run `/addcommand reload`  

---

## How It Works (Simple)

1. **User enters:** `/tpa_request steve`
2. **Mod matches pattern:** `/tpa <player>` matches with `player=steve`
3. **Substitutes into alias:** `say ${tpa_player} wants to TP to you!`
4. **Final command:** `say steve wants to TP to you!`

---

## Commands

```
/syntax                    # List all syntax patterns
/addcommand reload         # Reload syntax.json and aliases.json
```

---

## Config Files (Auto-Created)

- `config/CommandMaker/syntax.json` - Define custom syntax patterns
- `config/CommandMaker/aliases.json` - Create aliases using syntax variables

---

## Next Steps

1. Download and install updated mod
2. Check `config/CommandMaker/syntax.json` (has examples!)
3. Define your custom patterns
4. Create aliases using `${syntax_name_parameter}` variables
5. Test in-game!

---

See **SYNTAX_GUIDE.md** for comprehensive documentation with examples!
