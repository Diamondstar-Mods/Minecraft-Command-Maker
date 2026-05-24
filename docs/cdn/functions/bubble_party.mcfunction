# Bubble Party - Fill the world with bubbles and good vibes!
# Bubble particles everywhere, dolphin's grace, and upbeat music.

# --- Announce ---
title @a title {"text":"BUBBLE PARTY!","color":"aqua","bold":true}
title @a subtitle {"text":"POP POP POP!","color":"blue"}
tellraw @a [{"text":"[Bubbles] ","color":"aqua","bold":true},{"text":"IT'S A BUBBLE PARTY! EVERYONE DANCE!","color":"white"}]

# --- Give everyone party effects ---
effect give @a minecraft:dolphins_grace 120 2 true
effect give @a minecraft:slow_falling 120 0 true
effect give @a minecraft:speed 120 1 true
effect give @a minecraft:jump_boost 120 2 true
effect give @a minecraft:water_breathing 120 0 true
effect give @a minecraft:luck 120 1 true

# --- Give party items ---
give @a minecraft:tropical_fish 1
give @a minecraft:pufferfish 1
give @a minecraft:sea_pickle 4
give @a minecraft:prismarine_crystals 16
give @a minecraft:heart_of_the_sea 1

# --- Place water for the vibe (temporary) ---
execute at @s run setblock ~0 ~-1 ~0 minecraft:water

# --- Bubble columns ---
execute at @s run setblock ~2 ~-1 ~2 minecraft:soul_sand
execute at @s run setblock ~-2 ~-1 ~-2 minecraft:soul_sand
execute at @s run setblock ~2 ~-1 ~-2 minecraft:soul_sand
execute at @s run setblock ~-2 ~-1 ~2 minecraft:soul_sand

# --- Massive bubble particle shower ---
particle minecraft:bubble ~0 ~1 ~0 5 2 5 0.1 100
particle minecraft:bubble_column_up ~2 ~0 ~2 2 3 2 0.1 50
particle minecraft:bubble_column_up ~-2 ~0 ~-2 2 3 2 0.1 50
particle minecraft:bubble_column_up ~2 ~0 ~-2 2 3 2 0.1 50
particle minecraft:bubble_column_up ~-2 ~0 ~2 2 3 2 0.1 50
particle minecraft:splash ~0 ~1 ~0 4 0 4 0.1 30
particle minecraft:fishing ~0 ~2 ~0 5 1 5 0.1 40

# --- Underwater sparkle ---
particle minecraft:nautilus ~0 ~1 ~0 4 2 4 0.05 30
particle minecraft:dolphin ~0 ~1 ~0 3 1 3 0.02 20

# --- Party music ---
playsound minecraft:music_disc.strad master @a ~ ~ ~ 1 1
playsound minecraft:ambient.underwater.loop master @a ~ ~ ~ 0.3 1
playsound minecraft:entity.dolphin.play master @a ~ ~ ~ 1 1
playsound minecraft:entity.axolotl.splash master @a ~ ~ ~ 1 0.5

# --- More bubbles! ---
particle minecraft:bubble_pop ~0 ~0 ~0 8 3 8 0.2 200
particle minecraft:splash ~3 ~0 ~0 1 1 1 0.1 20
particle minecraft:splash ~-3 ~0 ~0 1 1 1 0.1 20
particle minecraft:splash ~0 ~0 ~3 1 1 1 0.1 20
particle minecraft:splash ~0 ~0 ~-3 1 1 1 0.1 20

# --- Clean up water and soul sand ---
execute at @s run setblock ~0 ~-1 ~0 minecraft:air
execute at @s run setblock ~2 ~-1 ~2 minecraft:air
execute at @s run setblock ~-2 ~-1 ~-2 minecraft:air
execute at @s run setblock ~2 ~-1 ~-2 minecraft:air
execute at @s run setblock ~-2 ~-1 ~2 minecraft:air

tellraw @a [{"text":"[Bubbles] ","color":"aqua","bold":true},{"text":"BUBBLE PARTY! Hope you had a splash!","color":"gold"}]
