# Parkour Course - Generates a floating parkour challenge with checkpoints!
# Run through colored platforms and reach the finish line.

# --- Announce ---
title @a title {"text":"PARKOUR!","color":"gold","bold":true}
title @a subtitle {"text":"Can you reach the end?","color":"yellow"}
tellraw @a [{"text":"[Parkour] ","color":"gold","bold":true},{"text":"A parkour course has spawned! Reach the gold platform!","color":"white"}]

# --- Give parkour buffs ---
effect give @a minecraft:jump_boost 300 1 true
effect give @a minecraft:speed 300 1 true
effect give @a minecraft:slow_falling 300 0 true

# --- Build the starting platform ---
execute at @s run fill ~-2 ~1 ~-2 ~2 ~1 ~2 minecraft:emerald_block
execute at @s run setblock ~0 ~2 ~0 minecraft:beacon
execute at @s run particle minecraft:happy_villager ~0 ~3 ~0 1 1 1 0.1 30

# --- Build course Section 1: Straight jumps (red) ---
execute at @s run setblock ^ ^1 ^5 minecraft:red_concrete
execute at @s run setblock ^ ^1 ^8 minecraft:red_concrete
execute at @s run setblock ^ ^2 ^11 minecraft:red_concrete
execute at @s run setblock ^ ^1 ^14 minecraft:red_concrete
execute at @s run setblock ^-1 ^1 ^17 minecraft:red_concrete

# --- Checkpoint 1 ---
execute at @s run setblock ^-1 ^1 ^20 minecraft:gold_block
execute at @s run particle minecraft:end_rod ^-1 ^2 ^20 0.5 1 0.5 0.1 15

# --- Section 2: Side to side (orange) ---
execute at @s run setblock ^-3 ^1 ^22 minecraft:orange_concrete
execute at @s run setblock ^-5 ^2 ^24 minecraft:orange_concrete
execute at @s run setblock ^-3 ^1 ^26 minecraft:orange_concrete
execute at @s run setblock ^-1 ^2 ^28 minecraft:orange_concrete
execute at @s run setblock ^-3 ^1 ^30 minecraft:orange_concrete

# --- Checkpoint 2 ---
execute at @s run setblock ^-3 ^1 ^33 minecraft:gold_block
execute at @s run particle minecraft:end_rod ^-3 ^2 ^33 0.5 1 0.5 0.1 15

# --- Section 3: Upward climb (yellow) ---
execute at @s run setblock ^-1 ^2 ^35 minecraft:yellow_concrete
execute at @s run setblock ^1 ^3 ^37 minecraft:yellow_concrete
execute at @s run setblock ^-2 ^4 ^39 minecraft:yellow_concrete
execute at @s run setblock ^0 ^5 ^41 minecraft:yellow_concrete
execute at @s run setblock ^2 ^6 ^43 minecraft:yellow_concrete

# --- Checkpoint 3 ---
execute at @s run setblock ^2 ^7 ^45 minecraft:gold_block
execute at @s run particle minecraft:end_rod ^2 ^8 ^45 0.5 1 0.5 0.1 15

# --- Section 4: Zigzag (lime) ---
execute at @s run setblock ^4 ^7 ^48 minecraft:lime_concrete
execute at @s run setblock ^2 ^7 ^51 minecraft:lime_concrete
execute at @s run setblock ^4 ^8 ^54 minecraft:lime_concrete
execute at @s run setblock ^2 ^8 ^57 minecraft:lime_concrete

# --- FINISH PLATFORM ---
execute at @s run fill ^2 ^8 ^60 ^4 ^8 ^62 minecraft:diamond_block
execute at @s run fill ^2 ^9 ^60 ^4 ^12 ^60 minecraft:sea_lantern
execute at @s run fill ^4 ^9 ^60 ^4 ^12 ^62 minecraft:sea_lantern
execute at @s run fill ^2 ^9 ^62 ^4 ^12 ^62 minecraft:sea_lantern
execute at @s run setblock ^3 ^10 ^61 minecraft:beacon

# --- Finish effects ---
execute at @s run particle minecraft:firework ^3 ^9 ^61 1 2 1 0.1 50
playsound minecraft:ui.toast.challenge_complete master @a ~ ~ ~ 1 1

tellraw @a [{"text":"[Parkour] ","color":"gold","bold":true},{"text":"Course built! 4 sections, 3 checkpoints. Get to the DIAMOND tower!","color":"green","bold":true}]
