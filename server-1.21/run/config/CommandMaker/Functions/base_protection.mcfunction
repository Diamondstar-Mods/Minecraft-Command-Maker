# Base Protection - Light up the area and spawn guards!
# Places light sources in a radius and summons iron golems.

# --- Announce ---
tellraw @s [{"text":"[Base Protection] ","color":"blue","bold":true},{"text":"Securing the area...","color":"white"}]

# --- Light grid: torches every few blocks in a ring around the player ---
# Inner ring (5 blocks out)
execute at @s run setblock ~5 ~ ~0 minecraft:torch
execute at @s run setblock ~-5 ~ ~0 minecraft:torch
execute at @s run setblock ~0 ~ ~5 minecraft:torch
execute at @s run setblock ~0 ~ ~-5 minecraft:torch
execute at @s run setblock ~4 ~ ~3 minecraft:torch
execute at @s run setblock ~-4 ~ ~3 minecraft:torch
execute at @s run setblock ~4 ~ ~-3 minecraft:torch
execute at @s run setblock ~-4 ~ ~-3 minecraft:torch
execute at @s run setblock ~3 ~ ~4 minecraft:torch
execute at @s run setblock ~-3 ~ ~4 minecraft:torch
execute at @s run setblock ~3 ~ ~-4 minecraft:torch
execute at @s run setblock ~-3 ~ ~-4 minecraft:torch

# Outer ring (10 blocks out)
execute at @s run setblock ~10 ~ ~0 minecraft:torch
execute at @s run setblock ~-10 ~ ~0 minecraft:torch
execute at @s run setblock ~0 ~ ~10 minecraft:torch
execute at @s run setblock ~0 ~ ~-10 minecraft:torch
execute at @s run setblock ~7 ~ ~7 minecraft:torch
execute at @s run setblock ~-7 ~ ~7 minecraft:torch
execute at @s run setblock ~7 ~ ~-7 minecraft:torch
execute at @s run setblock ~-7 ~ ~-7 minecraft:torch

# Far ring (15 blocks out)
execute at @s run setblock ~15 ~ ~0 minecraft:torch
execute at @s run setblock ~-15 ~ ~0 minecraft:torch
execute at @s run setblock ~0 ~ ~15 minecraft:torch
execute at @s run setblock ~0 ~ ~-15 minecraft:torch
execute at @s run setblock ~11 ~ ~11 minecraft:torch
execute at @s run setblock ~-11 ~ ~11 minecraft:torch
execute at @s run setblock ~11 ~ ~-11 minecraft:torch
execute at @s run setblock ~-11 ~ ~-11 minecraft:torch

# --- Place lanterns for extra warmth ---
execute at @s run setblock ~3 ~ ~0 minecraft:lantern
execute at @s run setblock ~-3 ~ ~0 minecraft:lantern
execute at @s run setblock ~0 ~ ~3 minecraft:lantern
execute at @s run setblock ~0 ~ ~-3 minecraft:lantern

# --- Spawn iron golem guards ---
execute at @s run summon minecraft:iron_golem ~6 ~ ~0
execute at @s run summon minecraft:iron_golem ~-6 ~ ~0
execute at @s run summon minecraft:iron_golem ~0 ~ ~6
execute at @s run summon minecraft:iron_golem ~0 ~ ~-6

# --- Place a bell at the center (village alarm) ---
execute at @s run setblock ~ ~ ~ minecraft:bell

# --- Visual/audio feedback ---
playsound minecraft:block.anvil.use master @s ~ ~ ~ 1 1
particle minecraft:composter ~ ~1 ~ 8 1 8 0.01 100

tellraw @s [{"text":"[Base Protection] ","color":"blue","bold":true},{"text":"Area secured! 4 Iron Golems on patrol, lights placed in a 15-block radius.","color":"green"}]
