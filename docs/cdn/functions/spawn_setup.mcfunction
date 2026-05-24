# Spawn Setup - Creates a beautiful server spawn area!
# Central fountain, welcome signs, rules board, and starter kit dispenser.

# --- Announce ---
title @a title {"text":"SPAWN SETUP!","color":"gold","bold":true}
title @a subtitle {"text":"Building a beautiful spawn...","color":"yellow"}
tellraw @a [{"text":"[Spawn] ","color":"gold","bold":true},{"text":"Constructing spawn area... stand back!","color":"white"}]

# --- Clear the area first ---
execute at @s run fill ~-12 ~-2 ~-12 ~12 ~15 ~12 minecraft:air

# --- Central plaza floor (21x21 polished andesite) ---
execute at @s run fill ~-10 ~-2 ~-10 ~10 ~-2 ~10 minecraft:polished_andesite
execute at @s run fill ~-10 ~-2 ~-10 ~10 ~-2 ~-10 minecraft:stone_bricks
execute at @s run fill ~-10 ~-2 ~10 ~10 ~-2 ~10 minecraft:stone_bricks
execute at @s run fill ~-10 ~-2 ~-10 ~-10 ~-2 ~10 minecraft:stone_bricks
execute at @s run fill ~10 ~-2 ~-10 ~10 ~-2 ~10 minecraft:stone_bricks

# --- Central fountain ---
execute at @s run fill ~-2 ~-2 ~-2 ~2 ~-2 ~2 minecraft:stone_bricks
execute at @s run setblock ~0 ~-1 ~0 minecraft:water
execute at @s run setblock ~1 ~-1 ~0 minecraft:water
execute at @s run setblock ~-1 ~-1 ~0 minecraft:water
execute at @s run setblock ~0 ~-1 ~1 minecraft:water
execute at @s run setblock ~0 ~-1 ~-1 minecraft:water
execute at @s run setblock ~0 ~0 ~0 minecraft:sea_lantern

# --- Fountain pillar and water ---
execute at @s run setblock ~0 ~1 ~0 minecraft:quartz_pillar
execute at @s run setblock ~0 ~2 ~0 minecraft:water

# --- Fountain particles ---
particle minecraft:splash ~0 ~0 ~0 1 1 1 0.1 20
particle minecraft:end_rod ~0 ~1 ~0 1 1 1 0.05 10

# --- Four paths leading to the fountain ---
execute at @s run fill ~0 ~-2 ~-10 ~0 ~-2 ~-3 minecraft:stone_brick_stairs
execute at @s run fill ~0 ~-2 ~3 ~0 ~-2 ~10 minecraft:stone_brick_stairs
execute at @s run fill ~3 ~-2 ~0 ~10 ~-2 ~0 minecraft:stone_brick_stairs
execute at @s run fill ~-10 ~-2 ~0 ~-3 ~-2 ~0 minecraft:stone_brick_stairs

# --- Lamp posts (4 corners) ---
execute at @s run setblock ~-8 ~-2 ~-8 minecraft:dark_oak_fence
execute at @s run setblock ~-8 ~-1 ~-8 minecraft:dark_oak_fence
execute at @s run setblock ~-8 ~0 ~-8 minecraft:glowstone
execute at @s run setblock ~-8 ~1 ~-8 minecraft:dark_oak_fence
execute at @s run setblock ~-8 ~2 ~-8 minecraft:sea_lantern

execute at @s run setblock ~8 ~-2 ~-8 minecraft:dark_oak_fence
execute at @s run setblock ~8 ~-1 ~-8 minecraft:dark_oak_fence
execute at @s run setblock ~8 ~0 ~-8 minecraft:glowstone
execute at @s run setblock ~8 ~1 ~-8 minecraft:dark_oak_fence
execute at @s run setblock ~8 ~2 ~-8 minecraft:sea_lantern

execute at @s run setblock ~-8 ~-2 ~8 minecraft:dark_oak_fence
execute at @s run setblock ~-8 ~-1 ~8 minecraft:dark_oak_fence
execute at @s run setblock ~-8 ~0 ~8 minecraft:glowstone
execute at @s run setblock ~-8 ~1 ~8 minecraft:dark_oak_fence
execute at @s run setblock ~-8 ~2 ~8 minecraft:sea_lantern

execute at @s run setblock ~8 ~-2 ~8 minecraft:dark_oak_fence
execute at @s run setblock ~8 ~-1 ~8 minecraft:dark_oak_fence
execute at @s run setblock ~8 ~0 ~8 minecraft:glowstone
execute at @s run setblock ~8 ~1 ~8 minecraft:dark_oak_fence
execute at @s run setblock ~8 ~2 ~8 minecraft:sea_lantern

# --- Welcome sign (north path) ---
execute at @s run setblock ~0 ~-1 ~-6 minecraft:dark_oak_sign
execute at @s run setblock ~0 ~0 ~-6 minecraft:dark_oak_wall_sign
execute at @s run setblock ~0 ~1 ~-6 minecraft:dark_oak_slab

# --- Rules board (east path) ---
execute at @s run setblock ~6 ~-1 ~0 minecraft:dark_oak_wall_sign
execute at @s run setblock ~6 ~0 ~0 minecraft:dark_oak_wall_sign
execute at @s run setblock ~6 ~1 ~0 minecraft:dark_oak_wall_sign

# --- Starter kit dispenser (west path) ---
execute at @s run setblock ~-6 ~-1 ~0 minecraft:dispenser
execute at @s run setblock ~-6 ~0 ~0 minecraft:barrel
execute at @s run setblock ~-6 ~0 ~-1 minecraft:chest

# --- Community board (south path) ---
execute at @s run setblock ~0 ~-1 ~6 minecraft:dark_oak_wall_sign
execute at @s run setblock ~0 ~0 ~6 minecraft:dark_oak_wall_sign
execute at @s run setblock ~0 ~1 ~6 minecraft:dark_oak_wall_sign

# --- Flower beds around fountain ---
execute at @s run setblock ~3 ~-1 ~3 minecraft:potted_poppy
execute at @s run setblock ~-3 ~-1 ~3 minecraft:potted_dandelion
execute at @s run setblock ~3 ~-1 ~-3 minecraft:potted_blue_orchid
execute at @s run setblock ~-3 ~-1 ~-3 minecraft:potted_azure_bluet

# --- Benches ---
execute at @s run setblock ~3 ~-1 ~5 minecraft:dark_oak_stairs
execute at @s run setblock ~5 ~-1 ~3 minecraft:dark_oak_stairs

# --- Announcement ---
playsound minecraft:entity.player.levelup master @a ~ ~ ~ 1 2
particle minecraft:happy_villager ~0 ~0 ~0 10 2 10 0.05 60

tellraw @a [{"text":"[Spawn] ","color":"gold","bold":true},{"text":"Spawn area complete! Fountain, signs, lamp posts, and starter kit dispenser ready.","color":"green"}]
tellraw @a [{"text":"[Spawn] ","color":"gold","bold":true},{"text":"Customize the signs with your server rules and welcome message!","color":"yellow"}]
