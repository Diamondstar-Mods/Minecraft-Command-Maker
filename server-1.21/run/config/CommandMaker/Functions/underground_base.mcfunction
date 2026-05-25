# Underground Base - Excavates and furnishes a complete underground bunker!
# Includes crafting room, storage, bedroom, and nether portal frame.

# --- Announce ---
title @a title {"text":"DIGGING IN!","color":"dark_gray","bold":true}
title @a subtitle {"text":"Building underground base...","color":"gray"}
tellraw @a [{"text":"[Base] ","color":"dark_gray","bold":true},{"text":"Excavating and furnishing an underground bunker!","color":"white"}]

# --- Give night vision for the dig ---
effect give @s minecraft:night_vision 30 0 true
effect give @s minecraft:haste 30 1 true

# --- Excavate the main room (9x7x5 interior) ---
execute at @s run fill ~-4 ~-1 ~-4 ~4 ~3 ~4 minecraft:air

# --- Floor (polished andesite with dark oak border) ---
execute at @s run fill ~-4 ~-1 ~-4 ~4 ~-1 ~4 minecraft:polished_andesite
execute at @s run fill ~-4 ~-1 ~-4 ~-4 ~-1 ~4 minecraft:dark_oak_planks
execute at @s run fill ~4 ~-1 ~-4 ~4 ~-1 ~4 minecraft:dark_oak_planks
execute at @s run fill ~-4 ~-1 ~-4 ~4 ~-1 ~-4 minecraft:dark_oak_planks
execute at @s run fill ~-4 ~-1 ~4 ~4 ~-1 ~4 minecraft:dark_oak_planks

# --- Walls (stone bricks) ---
execute at @s run fill ~-4 ~0 ~-4 ~-4 ~3 ~4 minecraft:stone_bricks
execute at @s run fill ~4 ~0 ~-4 ~4 ~3 ~4 minecraft:stone_bricks
execute at @s run fill ~-4 ~0 ~-4 ~4 ~3 ~-4 minecraft:stone_bricks
execute at @s run fill ~-4 ~0 ~4 ~4 ~3 ~4 minecraft:stone_bricks

# --- Ceiling ---
execute at @s run fill ~-4 ~3 ~-4 ~4 ~3 ~4 minecraft:dark_oak_planks

# --- Ceiling lights ---
execute at @s run setblock ~-2 ~3 ~0 minecraft:glowstone
execute at @s run setblock ~2 ~3 ~0 minecraft:glowstone
execute at @s run setblock ~0 ~3 ~-2 minecraft:glowstone
execute at @s run setblock ~0 ~3 ~2 minecraft:glowstone

# --- West wall: Crafting corner ---
execute at @s run setblock ~-3 ~0 ~-3 minecraft:crafting_table
execute at @s run setblock ~-3 ~0 ~-2 minecraft:furnace
execute at @s run setblock ~-3 ~1 ~-3 minecraft:item_frame
execute at @s run setblock ~-3 ~0 ~-1 minecraft:anvil

# --- East wall: Storage wall (4 chests) ---
execute at @s run setblock ~3 ~0 ~-2 minecraft:chest
execute at @s run setblock ~3 ~0 ~-1 minecraft:chest
execute at @s run setblock ~3 ~0 ~1 minecraft:chest
execute at @s run setblock ~3 ~0 ~2 minecraft:chest
execute at @s run setblock ~3 ~0 ~0 minecraft:barrel

# --- North wall: Bedroom corner (2 beds) ---
execute at @s run setblock ~-1 ~0 ~3 minecraft:red_bed
execute at @s run setblock ~1 ~0 ~3 minecraft:blue_bed

# --- South wall: Portal frame + enchanting ---
execute at @s run setblock ~0 ~0 ~-3 minecraft:enchanting_table
execute at @s run fill ~-1 ~0 ~-3 ~-1 ~2 ~-3 minecraft:obsidian
execute at @s run fill ~1 ~0 ~-3 ~1 ~2 ~-3 minecraft:obsidian
execute at @s run setblock ~-1 ~0 ~-2 minecraft:obsidian
execute at @s run setblock ~1 ~0 ~-2 minecraft:obsidian

# --- Center: table with flower ---
execute at @s run setblock ~0 ~0 ~0 minecraft:dark_oak_stairs
execute at @s run setblock ~0 ~1 ~0 minecraft:flower_pot
execute at @s run setblock ~0 ~1 ~0 minecraft:potted_poppy

# --- Entrance ladder to surface ---
execute at @s run fill ~0 ~0 ~4 ~0 ~5 ~4 minecraft:ladder
execute at @s run setblock ~0 ~5 ~4 minecraft:oak_trapdoor

# --- Wall torches ---
execute at @s run setblock ~-4 ~2 ~-2 minecraft:torch
execute at @s run setblock ~-4 ~2 ~2 minecraft:torch
execute at @s run setblock ~4 ~2 ~-2 minecraft:torch
execute at @s run setblock ~4 ~2 ~2 minecraft:torch

# --- Smoke and sound ---
particle minecraft:smoke ~0 ~1 ~0 3 1 3 0.02 30
playsound minecraft:block.stone.place master @s ~ ~ ~ 0.3 1
playsound minecraft:entity.player.levelup master @s ~ ~ ~ 0.5 1.5

tellraw @a [{"text":"[Base] ","color":"dark_gray","bold":true},{"text":"Underground base complete! Crafting, storage, beds, portal frame, enchantment table.","color":"green"}]
