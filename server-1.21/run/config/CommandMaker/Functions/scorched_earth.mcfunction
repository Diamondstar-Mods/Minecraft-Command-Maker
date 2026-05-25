# Scorched Earth - Set the world ablaze without TNT!
# Uses fire blocks, lava, fire charges, and cranked tick speed.

# --- Announce ---
title @a title {"text":"SCORCHED EARTH!","color":"dark_red","bold":true}
title @a subtitle {"text":"Everything burns...","color":"red"}
tellraw @a [{"text":"[FIRE] ","color":"dark_red","bold":true},{"text":"The ground ignites beneath your feet!","color":"gold"}]

# --- Protect the person running it ---
effect give @s minecraft:fire_resistance 120 0 true

# --- Crank up random tick speed so fire spreads fast ---
gamerule randomTickSpeed 100

# --- Ground-level fire blanket (21x21 area) ---
execute at @s run fill ~-10 ~0 ~-10 ~10 ~0 ~10 minecraft:fire replace minecraft:air
execute at @s run fill ~-10 ~1 ~-10 ~10 ~1 ~10 minecraft:fire replace minecraft:air

# --- Secondary ring further out (15-20 blocks) ---
execute at @s run fill ~-20 ~0 ~-20 ~-15 ~0 ~20 minecraft:fire replace minecraft:air
execute at @s run fill ~15 ~0 ~-20 ~20 ~0 ~20 minecraft:fire replace minecraft:air
execute at @s run fill ~-15 ~0 ~-20 ~15 ~0 ~-15 minecraft:fire replace minecraft:air
execute at @s run fill ~-15 ~0 ~15 ~15 ~0 ~20 minecraft:fire replace minecraft:air

# --- Lava drip from above (scattered source blocks at height) ---
execute at @s run setblock ~5 ~12 ~5 minecraft:lava
execute at @s run setblock ~-5 ~14 ~-5 minecraft:lava
execute at @s run setblock ~8 ~13 ~-3 minecraft:lava
execute at @s run setblock ~-8 ~15 ~3 minecraft:lava
execute at @s run setblock ~0 ~16 ~10 minecraft:lava
execute at @s run setblock ~0 ~14 ~-10 minecraft:lava
execute at @s run setblock ~12 ~13 ~0 minecraft:lava
execute at @s run setblock ~-12 ~15 ~0 minecraft:lava

# --- Fire charges (small fireballs) shooting outward in all directions ---
execute at @s run summon minecraft:small_fireball ~ ~2 ~ {Motion:[1.5d,0.2d,0.0d],power:[0.1d,0.0d,0.0d]}
execute at @s run summon minecraft:small_fireball ~ ~2 ~ {Motion:[-1.5d,0.2d,0.0d],power:[-0.1d,0.0d,0.0d]}
execute at @s run summon minecraft:small_fireball ~ ~2 ~ {Motion:[0.0d,0.2d,1.5d],power:[0.0d,0.0d,0.1d]}
execute at @s run summon minecraft:small_fireball ~ ~2 ~ {Motion:[0.0d,0.2d,-1.5d],power:[0.0d,0.0d,-0.1d]}
execute at @s run summon minecraft:small_fireball ~ ~2 ~ {Motion:[1.0d,0.3d,1.0d],power:[0.07d,0.0d,0.07d]}
execute at @s run summon minecraft:small_fireball ~ ~2 ~ {Motion:[-1.0d,0.3d,1.0d],power:[-0.07d,0.0d,0.07d]}
execute at @s run summon minecraft:small_fireball ~ ~2 ~ {Motion:[1.0d,0.3d,-1.0d],power:[0.07d,0.0d,-0.07d]}
execute at @s run summon minecraft:small_fireball ~ ~2 ~ {Motion:[-1.0d,0.3d,-1.0d],power:[-0.07d,0.0d,-0.07d]}

# --- Second volley: higher arc, wider spread ---
execute at @s run summon minecraft:small_fireball ~ ~3 ~ {Motion:[2.0d,0.8d,0.5d],power:[0.12d,0.02d,0.03d]}
execute at @s run summon minecraft:small_fireball ~ ~3 ~ {Motion:[-2.0d,0.8d,-0.5d],power:[-0.12d,0.02d,-0.03d]}
execute at @s run summon minecraft:small_fireball ~ ~3 ~ {Motion:[0.5d,0.8d,2.0d],power:[0.03d,0.02d,0.12d]}
execute at @s run summon minecraft:small_fireball ~ ~3 ~ {Motion:[-0.5d,0.8d,-2.0d],power:[-0.03d,0.02d,-0.12d]}

# --- Flame and smoke particles for atmosphere ---
execute at @s run particle minecraft:flame ~ ~1 ~ 10 2 10 0.1 500
execute at @s run particle minecraft:large_smoke ~ ~2 ~ 10 3 10 0.05 300
execute at @s run particle minecraft:lava ~ ~1 ~ 8 1 8 0.01 200
execute at @s run particle minecraft:campfire_signal_smoke ~ ~3 ~ 8 4 8 0.01 50

# --- Blazing sounds ---
playsound minecraft:entity.blaze.shoot master @a ~ ~ ~ 1 0.5
playsound minecraft:entity.blaze.shoot master @a ~ ~ ~ 1 0.8
playsound minecraft:entity.generic.burn master @a ~ ~ ~ 1 0.5
playsound minecraft:item.firecharge.use master @a ~ ~ ~ 1 1
playsound minecraft:entity.ghast.shoot master @a ~ ~ ~ 0.5 0.5

# --- Set nearby blocks on fire (netherrack burns forever) ---
execute at @s run setblock ~0 ~0 ~0 minecraft:netherrack
execute at @s run setblock ~0 ~1 ~0 minecraft:fire
execute at @s run setblock ~5 ~0 ~0 minecraft:netherrack
execute at @s run setblock ~5 ~1 ~0 minecraft:fire
execute at @s run setblock ~-5 ~0 ~0 minecraft:netherrack
execute at @s run setblock ~-5 ~1 ~0 minecraft:fire
execute at @s run setblock ~0 ~0 ~5 minecraft:netherrack
execute at @s run setblock ~0 ~1 ~5 minecraft:fire
execute at @s run setblock ~0 ~0 ~-5 minecraft:netherrack
execute at @s run setblock ~0 ~1 ~-5 minecraft:fire

tellraw @a [{"text":"[FIRE] ","color":"dark_red","bold":true},{"text":"The world burns! Fire Resistance applied to caster. ","color":"gold"},{"text":"Run /gamerule randomTickSpeed 3 to stop the spread!","color":"yellow"}]
