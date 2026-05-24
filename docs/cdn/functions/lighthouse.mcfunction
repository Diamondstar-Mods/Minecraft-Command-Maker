# Lighthouse - Builds a majestic lighthouse with a rotating beacon!
# Great for coastal bases and harbor builds.

# --- Announce ---
title @a title {"text":"LIGHTHOUSE!","color":"yellow","bold":true}
title @a subtitle {"text":"Guiding ships home...","color":"gold"}
tellraw @a [{"text":"[Build] ","color":"yellow","bold":true},{"text":"Constructing a lighthouse...","color":"white"}]

# --- Foundation (15x15 stone brick platform) ---
execute at @s run fill ~-7 ~-1 ~-7 ~7 ~-1 ~7 minecraft:stone_bricks
execute at @s run fill ~-7 ~-1 ~-7 ~-7 ~-1 ~7 minecraft:chiseled_stone_bricks
execute at @s run fill ~7 ~-1 ~-7 ~7 ~-1 ~7 minecraft:chiseled_stone_bricks
execute at @s run fill ~-7 ~-1 ~-7 ~7 ~-1 ~-7 minecraft:chiseled_stone_bricks
execute at @s run fill ~-7 ~-1 ~7 ~7 ~-1 ~7 minecraft:chiseled_stone_bricks

# --- Base level (7x7, 4 blocks tall, red and white stripes) ---
execute at @s run fill ~-3 ~0 ~-3 ~3 ~0 ~3 minecraft:red_concrete
execute at @s run fill ~-3 ~1 ~-3 ~3 ~1 ~3 minecraft:white_concrete
execute at @s run fill ~-3 ~2 ~-3 ~3 ~2 ~3 minecraft:red_concrete
execute at @s run fill ~-3 ~3 ~-3 ~3 ~3 ~3 minecraft:white_concrete

# --- Middle section (5x5, 4 blocks tall) ---
execute at @s run fill ~-2 ~4 ~-2 ~2 ~4 ~2 minecraft:white_concrete
execute at @s run fill ~-2 ~5 ~-2 ~2 ~5 ~2 minecraft:red_concrete
execute at @s run fill ~-2 ~6 ~-2 ~2 ~6 ~2 minecraft:white_concrete
execute at @s run fill ~-2 ~7 ~-2 ~2 ~7 ~2 minecraft:red_concrete

# --- Upper section (3x3, 3 blocks tall) ---
execute at @s run fill ~-1 ~8 ~-1 ~1 ~8 ~1 minecraft:red_concrete
execute at @s run fill ~-1 ~9 ~-1 ~1 ~9 ~1 minecraft:white_concrete
execute at @s run fill ~-1 ~10 ~-1 ~1 ~10 ~1 minecraft:red_concrete

# --- Lantern room (glass viewing deck) ---
execute at @s run fill ~-2 ~11 ~-2 ~2 ~13 ~2 minecraft:white_stained_glass

# --- Roof ---
execute at @s run fill ~-2 ~14 ~-2 ~2 ~14 ~2 minecraft:dark_oak_planks

# --- The beacon and light ---
execute at @s run setblock ~0 ~11 ~0 minecraft:beacon
execute at @s run setblock ~0 ~15 ~0 minecraft:sea_lantern
execute at @s run setblock ~0 ~16 ~0 minecraft:lightning_rod

# --- Glowstone accents ---
execute at @s run setblock ~0 ~0 ~3 minecraft:glowstone
execute at @s run setblock ~0 ~0 ~-3 minecraft:glowstone
execute at @s run setblock ~3 ~0 ~0 minecraft:glowstone
execute at @s run setblock ~-3 ~0 ~0 minecraft:glowstone

# --- Door ---
execute at @s run setblock ~0 ~0 ~3 minecraft:dark_oak_door

# --- Beam particles (simulating the rotating light) ---
particle minecraft:end_rod ~0 ~15 ~0 1 1 1 0.01 30
particle minecraft:firework ~0 ~16 ~0 0.5 0.5 0.5 0.01 10

playsound minecraft:block.beacon.activate master @a ~ ~ ~ 1 2

tellraw @a [{"text":"[Build] ","color":"yellow","bold":true},{"text":"Lighthouse complete! 16 blocks tall with beacon light.","color":"green"}]
