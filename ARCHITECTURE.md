# Custom Command Syntax System - Architecture

## System Architecture Diagram

```
┌─────────────────────────────────────────────────────────────────────┐
│                         USER INPUT                                   │
│                      /tpa_request steve                             │
└──────────────────────────────────┬──────────────────────────────────┘
                                   │
                    ┌──────────────▼──────────────┐
                    │   ExampleMod.registerAlias  │
                    │   with arguments handler    │
                    └──────────────┬──────────────┘
                                   │
                    ┌──────────────▼──────────────┐
                    │ SyntaxManager.matchInput()  │
                    │ Try to match pattern        │
                    └──────────────┬──────────────┘
                                   │
                 ┌─────────────────┴─────────────────┐
                 │                                   │
          ┌──────▼──────┐                   ┌───────▼────────┐
          │   MATCH     │                   │   NO MATCH     │
          │   FOUND!    │                   │  Use default   │
          └──────┬──────┘                   └────────────────┘
                 │
     ┌───────────▼───────────┐
     │ CommandSyntax class   │
     │ extractParameters()   │
     │ {player: "steve"}     │
     └───────────┬───────────┘
                 │
     ┌───────────▼────────────────┐
     │ substituteParameters()      │
     │ ${tpa_player} -> steve      │
     │ Result: say steve wants...  │
     └───────────┬────────────────┘
                 │
     ┌───────────▼───────────────────────┐
     │ substituteVariables()             │
     │ ${player} -> YourName             │
     │ ${x}, ${y}, ${z} -> coords        │
     │ Final: say steve wants YourName.. │
     └───────────┬───────────────────────┘
                 │
     ┌───────────▼──────────────────┐
     │   Execute command via        │
     │   CommandDispatcher          │
     └──────────────────────────────┘
```

## Class Hierarchy

```
┌────────────────────────────────┐
│   SyntaxManager (static)       │
├────────────────────────────────┤
│ - customSyntaxes: Map          │
├────────────────────────────────┤
│ + loadSyntaxDefinitions()      │
│ + getSyntax(name)              │
│ + matchInput(input)            │
│ + getAllSyntaxes()             │
│ + hasSyntax(name)              │
└────────────┬───────────────────┘
             │
             │ manages
             │
             ▼
┌────────────────────────────────┐
│   CommandSyntax (instance)     │
├────────────────────────────────┤
│ - name: String                 │
│ - pattern: String              │
│ - parameters: List<String>     │
│ - description: String          │
├────────────────────────────────┤
│ + extractParameters(input)     │
│ + substituteParameters(cmd)    │
│ + getRegexPattern()            │
│ + getters/setters              │
└────────────────────────────────┘
```

## Data Flow

### Configuration Loading Phase

```
Minecraft Start
    ↓
ExampleMod.onInitialize()
    ↓
SyntaxManager.loadSyntaxDefinitions()
    ↓
Read: config/CommandMaker/syntax.json
    ↓
Parse JSON → Create CommandSyntax objects
    ↓
Store in customSyntaxes Map
    ↓
Log: "Loaded custom syntax: tpa -> /tpa <player>"
```

### Command Execution Phase

```
User Input: /tpa_request steve
    ↓
ExampleMod.registerAlias() argument handler
    ↓
Full input: "tpa_request steve"
    ↓
SyntaxManager.matchInput("tpa_request steve")
    ↓
For each syntax:
  - Get regex pattern from CommandSyntax
  - Match against input
  - If match: extract parameters
    ↓
Found match on "tpa" syntax!
  - Extracted: {player: "steve"}
    ↓
syntax.substituteParameters(
  alias_target: "say ${tpa_player} wants...",
  parameters: {player: "steve"}
)
    ↓
Result: "say steve wants..."
    ↓
ExampleMod.substituteVariables() for built-in vars
    ↓
Execute command via CommandDispatcher
```

## File Organization

```
Project Root
├── src/main/java/com/example/
│   ├── CommandSyntax.java (NEW)
│   │   └── Pattern parsing & parameter extraction
│   ├── SyntaxManager.java (NEW)
│   │   └── Syntax registry & matching
│   ├── ExampleMod.java (UPDATED)
│   │   └── Main mod class + syntax integration
│   ├── ExampleModClient.java
│   │   └── Client-side handling (unchanged)
│   └── mixin/
│       └── ExampleMixin.java (unchanged)
│
├── src/main/resources/
│   ├── fabric.mod.json
│   ├── modid.mixins.json
│   ├── modid.client.mixins.json
│   └── assets/...
│
├── config/CommandMaker/ (RUNTIME CREATED)
│   ├── syntax.json (NEW)
│   │   └── User-defined syntax patterns
│   └── aliases.json
│       └── Aliases with syntax variables
│
└── Documentation/
    ├── SYNTAX_GUIDE.md (NEW)
    ├── SYNTAX_QUICK_START.md (NEW)
    ├── EXAMPLES_TPA.md (NEW)
    ├── IMPLEMENTATION_SUMMARY.md (NEW)
    └── RELEASE_NOTES.md (NEW)
```

## Pattern Matching Algorithm

```
Input: "/msg alex hello world"
Pattern: "/msg <player> <message>"

1. Convert pattern to regex:
   "/msg <player> <message>"
   → "/msg (.+) (.+)"
   → "^/msg (.+) (.+)$"

2. Match input against regex:
   Does "/msg alex hello world" match?
   YES! Capture groups: [1]="alex", [2]="hello world"

3. Extract parameters:
   parameters[0] = "player"
   parameters[1] = "message"

   result = {
     "player": "alex",
     "message": "hello world"
   }

4. Return match result with:
   - syntaxName: "msg"
   - syntax: CommandSyntax object
   - parameters: extracted map
```

## Variable Substitution Layers

```
Original Alias Target:
  "say ${msg_player} said: ${msg_message} from ${player} at ${x} ${y} ${z}"

Layer 1 - Syntax Parameters (from custom syntax):
  result = syntax.substituteParameters(target, {player: "alex", message: "hi"})
  → "say alex said: hi from ${player} at ${x} ${y} ${z}"

Layer 2 - Built-in Variables (from ExampleMod):
  result = substituteVariables(result, ctx)
  - ${player} → YourName
  - ${x} → 123
  - ${y} → 64
  - ${z} → 456
  → "say alex said: hi from YourName at 123 64 456"

Layer 3 - Custom Per-Player Variables (from /setcmdvariable):
  playerVariables[uuid]["custom"] → value
  → substituted as ${custom}

Final Result:
  "say alex said: hi from YourName at 123 64 456"

Execute via CommandDispatcher
```

## JSON Configuration Structure

### syntax.json Schema

```json
{
  "syntax_name": {
    "pattern": "/command <param1> <param2>", // Required
    "description": "What this does" // Optional
  }
}
```

### aliases.json Usage

```json
{
  "alias_name": "target_command_with_${syntax_name_param_name}"
}
```

## Extension Points

### For Future Enhancements:

1. **Conditional Execution**
   - If parameter matches pattern, execute different command

2. **Parameter Validation**
   - Regex validation for parameters (e.g., must be number)

3. **Custom Parameter Types**
   - @player selector, #tag selector, UUID validation

4. **Syntax Inheritance**
   - Create syntaxes that extend other syntaxes

5. **GUI Configuration**
   - In-game GUI to define syntaxes instead of JSON

6. **Parameter Transformations**
   - Convert parameter values (uppercase, lowercase, etc.)

## Performance Characteristics

| Operation       | Complexity | Notes                            |
| --------------- | ---------- | -------------------------------- |
| Load syntaxes   | O(n)       | n = number of syntax definitions |
| Match input     | O(n)       | n = number of syntax definitions |
| Regex compile   | O(m)       | m = pattern length               |
| Extract params  | O(k)       | k = number of parameters         |
| Substitute vars | O(j)       | j = number of variables          |

**Total per command:** O(n) + O(k) + O(j) ≈ **O(n)** (very fast!)

## Memory Impact

```
Per Syntax Definition:
- CommandSyntax object: ~200 bytes
- Regex Pattern object: ~500 bytes
- String storage: ~100-200 bytes
────────────────────────
Total per syntax: ~800-1000 bytes

With 10 syntax definitions: ~10KB
Negligible impact on memory
```

## Error Handling

```
File Not Found
  → Create default syntax.json with examples

Invalid JSON
  → Log error, continue without that syntax

No Match Found
  → Use default alias behavior

Parameter Missing
  → Replace with empty string, log warning
```

---

This architecture ensures:

- ✅ Clean separation of concerns
- ✅ Efficient pattern matching
- ✅ Flexible variable substitution
- ✅ Backward compatibility
- ✅ Easy to extend and maintain
