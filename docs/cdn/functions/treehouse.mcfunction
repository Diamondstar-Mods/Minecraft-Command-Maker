# Treehouse - Instantly build a treehouse!
# Creates a tree trunk, leaf canopy, platform, walls, and ladder.

# --- Announce ---
tellraw @s [{"text":"[Build] ","color":"dark_green","bold":true},{"text":"Building your treehouse...","color":"green"}]

# --- Tree trunk (6 blocks tall) ---
execute at @s run fill ~0 ~0 ~0 ~0 ~5 ~0 minecraft:oak_log

# --- Leaf canopy around the top ---
execute at @s run fill ~-3 ~6 ~-3 ~3 ~6 ~3 minecraft:oak_leaves
execute at @s run fill ~-2 ~7 ~-2 ~2 ~7 ~2 minecraft:oak_leaves
execute at @s run fill ~-1 ~8 ~-1 ~1 ~8 ~1 minecraft:oak_leaves
# Clear out the center of the canopy so the platform sits inside
execute at @s run fill ~-2 ~6 ~-2 ~2 ~6 ~2 minecraft:air

# --- Treehouse platform (5x5 at height 6) ---
execute at @s run fill ~-2 ~5 ~-2 ~2 ~5 ~2 minecraft:oak_planks
# Re-place the trunk through the platform
execute at @s run setblock ~0 ~5 ~0 minecraft:oak_log

# --- Walls (2 blocks tall, with windows) ---
# North wall
execute at @s run fill ~-2 ~6 ~-2 ~2 ~7 ~-2 minecraft:oak_planks
execute at @s run setblock ~0 ~6 ~-2 minecraft:glass_pane
# South wall
execute at @s run fill ~-2 ~6 ~2 ~2 ~7 ~2 minecraft:oak_planks
execute at @s run setblock ~0 ~6 ~2 minecraft:glass_pane
# East wall
execute at @s run fill ~2 ~6 ~-2 ~2 ~7 ~2 minecraft:oak_planks
execute at @s run setblock ~2 ~6 ~0 minecraft:glass_pane
# West wall (with door opening)
execute at @s run fill ~-2 ~6 ~-2 ~-2 ~7 ~2 minecraft:oak_planks
execute at @s run setblock ~-2 ~6 ~0 minecraft:air
execute at @s run setblock ~-2 ~7 ~0 minecraft:air

# --- Roof ---
execute at @s run fill ~-2 ~8 ~-2 ~2 ~8 ~2 minecraft:oak_slab

# --- Ladder going up the trunk ---
execute at @s run setblock ~1 ~0 ~0 minecraft:ladder[facing=west]
execute at @s run setblock ~1 ~1 ~0 minecraft:ladder[facing=west]
execute at @s run setblock ~1 ~2 ~0 minecraft:ladder[facing=west]
execute at @s run setblock ~1 ~3 ~0 minecraft:ladder[facing=west]
execute at @s run setblock ~1 ~4 ~0 minecraft:ladder[facing=west]

# --- Interior furnishing ---
execute at @s run setblock ~-1 ~6 ~-1 minecraft:crafting_table
execute at @s run setblock ~1 ~6 ~-1 minecraft:chest
execute at @s run setblock ~-1 ~6 ~1 minecraft:lantern
execute at @s run setblock ~1 ~6 ~1 minecraft:barrel

# --- Balcony off the door side ---
execute at @s run fill ~-3 ~5 ~-1 ~-3 ~5 ~1 minecraft:oak_slab
execute at @s run setblock ~-3 ~6 ~-1 minecraft:oak_fence
execute at @s run setblock ~-3 ~6 ~1 minecraft:oak_fence

# --- Sound and particles ---
playsound minecraft:block.wood.place master @s ~ ~ ~ 1 1
particle minecraft:composter ~ ~6 ~ 3 2 3 0.01 50

tellraw @s [{"text":"[Build] ","color":"dark_green","bold":true},{"text":"Treehouse complete! Climb the ladder on the east side.","color":"green"}]
