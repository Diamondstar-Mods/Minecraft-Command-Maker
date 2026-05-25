# Campsite - Instant wilderness rest stop!
# Places a campfire, tent, crafting stations, and supplies.

# --- Announce ---
tellraw @s [{"text":"[Camp] ","color":"gold","bold":true},{"text":"Setting up camp...","color":"white"}]

# --- Campfire at the center ---
execute at @s run setblock ~0 ~0 ~0 minecraft:campfire

# --- Tent (wool A-frame, 3 blocks wide) ---
# Floor
execute at @s run fill ~3 ~0 ~-1 ~3 ~0 ~1 minecraft:brown_wool
execute at @s run fill ~4 ~0 ~-1 ~4 ~0 ~1 minecraft:brown_wool
execute at @s run fill ~5 ~0 ~-1 ~5 ~0 ~1 minecraft:brown_wool
# Left wall slope
execute at @s run fill ~3 ~1 ~-1 ~5 ~1 ~-1 minecraft:white_wool
execute at @s run fill ~3 ~2 ~0 ~5 ~2 ~0 minecraft:white_wool
# Right wall slope
execute at @s run fill ~3 ~1 ~1 ~5 ~1 ~1 minecraft:white_wool
# Tent ridge
execute at @s run fill ~3 ~2 ~0 ~5 ~2 ~0 minecraft:white_wool
# Clear inside
execute at @s run setblock ~4 ~1 ~0 minecraft:air
# Bed inside the tent
execute at @s run setblock ~5 ~1 ~0 minecraft:red_bed[facing=west,part=foot]

# --- Crafting stations near the fire ---
execute at @s run setblock ~-2 ~0 ~0 minecraft:crafting_table
execute at @s run setblock ~-2 ~0 ~1 minecraft:furnace[facing=east]
execute at @s run setblock ~-2 ~0 ~-1 minecraft:smoker[facing=east]

# --- Storage ---
execute at @s run setblock ~0 ~0 ~-3 minecraft:barrel
execute at @s run setblock ~1 ~0 ~-3 minecraft:chest

# --- Seating (log stumps around the fire) ---
execute at @s run setblock ~1 ~0 ~1 minecraft:oak_log[axis=y]
execute at @s run setblock ~-1 ~0 ~1 minecraft:oak_log[axis=y]
execute at @s run setblock ~-1 ~0 ~-1 minecraft:oak_log[axis=y]
execute at @s run setblock ~1 ~0 ~-1 minecraft:oak_log[axis=y]

# --- Torches for light and safety ---
execute at @s run setblock ~3 ~0 ~-3 minecraft:torch
execute at @s run setblock ~-3 ~0 ~-3 minecraft:torch
execute at @s run setblock ~3 ~0 ~3 minecraft:torch
execute at @s run setblock ~-3 ~0 ~3 minecraft:torch
execute at @s run setblock ~0 ~0 ~4 minecraft:torch
execute at @s run setblock ~0 ~0 ~-4 minecraft:torch

# --- Supply drop: give the player some camping essentials ---
give @s minecraft:cooked_beef 16
give @s minecraft:bread 8
give @s minecraft:torch 16
give @s minecraft:coal 8

# --- Ambient sounds and particles ---
playsound minecraft:block.campfire.crackle master @s ~ ~ ~ 1 1
playsound minecraft:ambient.soul_sand_valley.mood master @s ~ ~ ~ 0.3 1
particle minecraft:campfire_cosy_smoke ~0 ~1 ~0 0.2 0.5 0.2 0.01 10

tellraw @s [{"text":"[Camp] ","color":"gold","bold":true},{"text":"Campsite ready! Tent, campfire, crafting stations, and supplies.","color":"green"}]
