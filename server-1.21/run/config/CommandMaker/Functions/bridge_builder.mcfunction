# Bridge Builder - Extends a sturdy stone bridge in the direction you're facing!
# Includes railings and torches. Great for crossing ravines or sky gaps.

# --- Announce ---
tellraw @s [{"text":"[Bridge] ","color":"gray","bold":true},{"text":"Building bridge ahead of you...","color":"white"}]

# --- Give speed and slow falling during construction ---
effect give @s minecraft:speed 10 2 true
effect give @s minecraft:slow_falling 10 0 true

# --- Build the 3-wide stone bridge floor (30 blocks forward) ---
# Center lane
execute at @s run fill ^ ^-1 ^1 ^-1 ^-1 ^30 minecraft:stone_bricks
# Left lane
execute at @s run fill ^-1 ^-1 ^1 ^-1 ^-1 ^30 minecraft:stone_bricks
# Right lane
execute at @s run fill ^1 ^-1 ^1 ^1 ^-1 ^30 minecraft:stone_bricks

# --- Railings on both sides ---
execute at @s run fill ^-2 ^0 ^1 ^-2 ^1 ^30 minecraft:stone_brick_wall
execute at @s run fill ^2 ^0 ^1 ^2 ^1 ^30 minecraft:stone_brick_wall

# --- Torches every 6 blocks on both railings ---
execute at @s run setblock ^-2 ^1 ^3 minecraft:torch
execute at @s run setblock ^2 ^1 ^3 minecraft:torch
execute at @s run setblock ^-2 ^1 ^9 minecraft:torch
execute at @s run setblock ^2 ^1 ^9 minecraft:torch
execute at @s run setblock ^-2 ^1 ^15 minecraft:torch
execute at @s run setblock ^2 ^1 ^15 minecraft:torch
execute at @s run setblock ^-2 ^1 ^21 minecraft:torch
execute at @s run setblock ^2 ^1 ^21 minecraft:torch
execute at @s run setblock ^-2 ^1 ^27 minecraft:torch
execute at @s run setblock ^2 ^1 ^27 minecraft:torch

# --- End platform (slightly wider landing) ---
execute at @s run fill ^-2 ^-1 ^31 ^2 ^-1 ^33 minecraft:stone_bricks
execute at @s run setblock ^-2 ^0 ^32 minecraft:glowstone
execute at @s run setblock ^2 ^0 ^32 minecraft:glowstone

# --- Support pillars every 10 blocks (for realism) ---
execute at @s run fill ^-2 ^-3 ^10 ^2 ^-2 ^10 minecraft:cobblestone
execute at @s run fill ^-2 ^-5 ^20 ^2 ^-2 ^20 minecraft:cobblestone
execute at @s run fill ^-2 ^-3 ^30 ^2 ^-2 ^30 minecraft:cobblestone

# --- Particle trail and sound ---
particle minecraft:smoke ^0 ^0 ^15 3 0.5 15 0.01 40
playsound minecraft:block.stone.place master @s ~ ~ ~ 0.5 1
playsound minecraft:block.stone.place master @s ~ ~ ~ 0.5 1.2

tellraw @s [{"text":"[Bridge] ","color":"gray","bold":true},{"text":"30-block stone bridge built! Includes railings and lighting.","color":"green"}]
