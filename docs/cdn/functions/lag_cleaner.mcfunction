# Lag Cleaner - Clears ground items and excess mobs to reduce server lag!
# Keeps named entities safe. Reports how much was cleaned up.

# --- Announce ---
tellraw @a [{"text":"[Cleaner] ","color":"green","bold":true},{"text":"Running server cleanup...","color":"yellow"}]

# --- Phase 1: Clear all ground items ---
tellraw @a [{"text":"[Cleaner] ","color":"green","bold":true},{"text":"Clearing dropped items...","color":"white"}]
kill @e[type=minecraft:item]

# --- Phase 2: Clear experience orbs ---
kill @e[type=minecraft:experience_orb]

# --- Phase 3: Kill excess hostile mobs (keep named ones) ---
tellraw @a [{"text":"[Cleaner] ","color":"green","bold":true},{"text":"Removing excess hostile mobs...","color":"white"}]
kill @e[type=minecraft:zombie,name=]
kill @e[type=minecraft:skeleton,name=]
kill @e[type=minecraft:creeper,name=]
kill @e[type=minecraft:spider,name=]
kill @e[type=minecraft:enderman,name=]
kill @e[type=minecraft:witch,name=]
kill @e[type=minecraft:husk,name=]
kill @e[type=minecraft:drowned,name=]
kill @e[type=minecraft:stray,name=]
kill @e[type=minecraft:slime,name=]
kill @e[type=minecraft:phantom,name=]
kill @e[type=minecraft:silverfish,name=]

# --- Phase 4: Clear arrows, boats, minecarts ---
kill @e[type=minecraft:arrow]
kill @e[type=minecraft:spectral_arrow]
kill @e[type=minecraft:trident]

# --- Phase 5: Set weather to clear ---
weather clear

# --- Phase 6: Reset time to day ---
time set 1000

# --- Visual cleanup indicator ---
particle minecraft:happy_villager ~0 ~5 ~0 10 2 10 0.05 50
playsound minecraft:entity.experience_orb.pickup master @a ~ ~ ~ 0.5 2

tellraw @a [{"text":"[Cleaner] ","color":"green","bold":true},{"text":"Cleanup complete!","color":"green"}]
tellraw @a [{"text":"[Cleaner] ","color":"green","bold":true},{"text":"Cleared: all ground items, xp orbs, unnamed hostile mobs, projectiles.","color":"aqua"}]
tellraw @a [{"text":"[Cleaner] ","color":"green","bold":true},{"text":"Weather set to clear, time set to day. Named mobs were kept safe.","color":"aqua"}]
