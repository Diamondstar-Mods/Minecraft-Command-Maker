# Meteor Shower - Raining fire from the sky!
# Summons falling blocks with flame trails overhead. Dramatic but mostly safe.

# --- Announce ---
title @a title {"text":"METEOR SHOWER!","color":"red","bold":true}
title @a subtitle {"text":"Look up!","color":"gold"}
tellraw @a [{"text":"[Sky] ","color":"dark_red","bold":true},{"text":"The sky is falling!","color":"gold"}]

# --- Set the mood ---
time set midnight
weather thunder

# --- Give players fire resistance so they don't die ---
effect give @a minecraft:fire_resistance 60 0 true

# --- Wave 1: Magma meteors falling from the sky ---
execute at @s run summon minecraft:falling_block ~3 ~40 ~5 {BlockState:{Name:"minecraft:magma_block"},Time:1,Motion:[0.0d,-1.0d,0.0d]}
execute at @s run summon minecraft:falling_block ~-7 ~45 ~2 {BlockState:{Name:"minecraft:magma_block"},Time:1,Motion:[0.1d,-1.2d,-0.1d]}
execute at @s run summon minecraft:falling_block ~10 ~42 ~-8 {BlockState:{Name:"minecraft:magma_block"},Time:1,Motion:[-0.1d,-1.1d,0.1d]}
execute at @s run summon minecraft:falling_block ~-4 ~48 ~-10 {BlockState:{Name:"minecraft:magma_block"},Time:1,Motion:[0.05d,-1.3d,0.05d]}
execute at @s run summon minecraft:falling_block ~8 ~44 ~8 {BlockState:{Name:"minecraft:magma_block"},Time:1,Motion:[-0.05d,-1.0d,-0.05d]}

# --- Wave 2: Glowstone meteors (bright streaks) ---
execute at @s run summon minecraft:falling_block ~-12 ~50 ~6 {BlockState:{Name:"minecraft:glowstone"},Time:1,Motion:[0.15d,-1.5d,-0.1d]}
execute at @s run summon minecraft:falling_block ~6 ~46 ~-12 {BlockState:{Name:"minecraft:glowstone"},Time:1,Motion:[-0.1d,-1.4d,0.15d]}
execute at @s run summon minecraft:falling_block ~0 ~52 ~0 {BlockState:{Name:"minecraft:glowstone"},Time:1,Motion:[0.0d,-1.6d,0.0d]}
execute at @s run summon minecraft:falling_block ~-9 ~47 ~9 {BlockState:{Name:"minecraft:glowstone"},Time:1,Motion:[0.1d,-1.3d,-0.1d]}

# --- Wave 3: Netherrack fireballs ---
execute at @s run summon minecraft:falling_block ~5 ~55 ~-3 {BlockState:{Name:"minecraft:netherrack"},Time:1,Motion:[-0.05d,-1.8d,0.05d]}
execute at @s run summon minecraft:falling_block ~-6 ~53 ~7 {BlockState:{Name:"minecraft:netherrack"},Time:1,Motion:[0.08d,-1.7d,-0.08d]}
execute at @s run summon minecraft:falling_block ~11 ~50 ~11 {BlockState:{Name:"minecraft:netherrack"},Time:1,Motion:[-0.12d,-1.5d,-0.12d]}
execute at @s run summon minecraft:falling_block ~-11 ~56 ~-5 {BlockState:{Name:"minecraft:netherrack"},Time:1,Motion:[0.1d,-2.0d,0.06d]}

# --- Flame particles in the sky for trail effect ---
execute at @s run particle minecraft:flame ~3 ~30 ~5 5 10 5 0.1 200
execute at @s run particle minecraft:flame ~-7 ~35 ~2 5 10 5 0.1 200
execute at @s run particle minecraft:lava ~0 ~25 ~0 10 5 10 0.01 100
execute at @s run particle minecraft:smoke ~0 ~30 ~0 8 8 8 0.05 150
execute at @s run particle minecraft:large_smoke ~5 ~35 ~-5 6 6 6 0.05 80

# --- Impact explosions (harmless particle-only booms) ---
execute at @s run particle minecraft:explosion ~3 ~ ~5 2 1 2 0.1 10
execute at @s run particle minecraft:explosion ~-7 ~ ~2 2 1 2 0.1 10
execute at @s run particle minecraft:explosion ~10 ~ ~-8 2 1 2 0.1 10

# --- Dramatic sounds ---
playsound minecraft:entity.generic.explode master @a ~ ~ ~ 1 0.5
playsound minecraft:entity.lightning_bolt.thunder master @a ~ ~ ~ 1 0.8
playsound minecraft:entity.ender_dragon.growl master @a ~ ~ ~ 0.5 0.5

# --- Scatter some fire where they land (for visual effect) ---
execute at @s run setblock ~3 ~1 ~5 minecraft:fire
execute at @s run setblock ~-7 ~1 ~2 minecraft:fire
execute at @s run setblock ~10 ~1 ~-8 minecraft:fire
execute at @s run setblock ~-4 ~1 ~-10 minecraft:fire

tellraw @a [{"text":"[Sky] ","color":"dark_red","bold":true},{"text":"Brace for impact! Fire Resistance has been applied.","color":"yellow"}]
