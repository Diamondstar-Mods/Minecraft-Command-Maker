# Mob Army - Summon a massive swarm of mobs around a player!
# Spawns waves of different mob types for absolute chaos.

# --- Announce ---
title @a title {"text":"MOB ARMY!","color":"dark_green","bold":true}
title @a subtitle {"text":"They're coming...","color":"red"}
tellraw @a [{"text":"[Army] ","color":"dark_green","bold":true},{"text":"A horde approaches from all directions!","color":"red"}]

# --- War horn ---
playsound minecraft:event.raid.horn master @a ~ ~ ~ 2 1
playsound minecraft:entity.ravager.roar master @a ~ ~ ~ 1 1

# --- Wave 1: Chicken Flood (the absurd warmup) ---
tellraw @a [{"text":"[Army] ","color":"dark_green","bold":true},{"text":"Wave 1: THE CHICKENS!","color":"yellow"}]
execute at @s run summon minecraft:chicken ~3 ~ ~2
execute at @s run summon minecraft:chicken ~-2 ~ ~4
execute at @s run summon minecraft:chicken ~5 ~ ~-1
execute at @s run summon minecraft:chicken ~-4 ~ ~-3
execute at @s run summon minecraft:chicken ~1 ~ ~5
execute at @s run summon minecraft:chicken ~-5 ~ ~1
execute at @s run summon minecraft:chicken ~4 ~ ~-4
execute at @s run summon minecraft:chicken ~-1 ~ ~3
execute at @s run summon minecraft:chicken ~2 ~ ~-5
execute at @s run summon minecraft:chicken ~-3 ~ ~-2
execute at @s run summon minecraft:chicken ~6 ~ ~0
execute at @s run summon minecraft:chicken ~0 ~ ~6
execute at @s run summon minecraft:chicken ~-6 ~ ~0
execute at @s run summon minecraft:chicken ~0 ~ ~-6
execute at @s run summon minecraft:chicken ~4 ~ ~4
execute at @s run summon minecraft:chicken ~-4 ~ ~4
execute at @s run summon minecraft:chicken ~4 ~ ~-4
execute at @s run summon minecraft:chicken ~-4 ~ ~-4
execute at @s run summon minecraft:chicken ~2 ~ ~2
execute at @s run summon minecraft:chicken ~-2 ~ ~-2

# --- Wave 2: Zombie Horde ---
tellraw @a [{"text":"[Army] ","color":"dark_green","bold":true},{"text":"Wave 2: ZOMBIES!","color":"dark_red"}]
execute at @s run summon minecraft:zombie ~8 ~ ~0
execute at @s run summon minecraft:zombie ~-8 ~ ~0
execute at @s run summon minecraft:zombie ~0 ~ ~8
execute at @s run summon minecraft:zombie ~0 ~ ~-8
execute at @s run summon minecraft:zombie ~6 ~ ~6
execute at @s run summon minecraft:zombie ~-6 ~ ~6
execute at @s run summon minecraft:zombie ~6 ~ ~-6
execute at @s run summon minecraft:zombie ~-6 ~ ~-6
execute at @s run summon minecraft:zombie ~10 ~ ~3
execute at @s run summon minecraft:zombie ~-10 ~ ~-3
execute at @s run summon minecraft:zombie ~3 ~ ~10
execute at @s run summon minecraft:zombie ~-3 ~ ~-10
execute at @s run summon minecraft:zombie ~7 ~ ~-5
execute at @s run summon minecraft:zombie ~-7 ~ ~5
execute at @s run summon minecraft:zombie ~5 ~ ~7

# --- Wave 3: Skeleton Archers ---
tellraw @a [{"text":"[Army] ","color":"dark_green","bold":true},{"text":"Wave 3: SKELETONS!","color":"white"}]
execute at @s run summon minecraft:skeleton ~12 ~ ~0
execute at @s run summon minecraft:skeleton ~-12 ~ ~0
execute at @s run summon minecraft:skeleton ~0 ~ ~12
execute at @s run summon minecraft:skeleton ~0 ~ ~-12
execute at @s run summon minecraft:skeleton ~9 ~ ~9
execute at @s run summon minecraft:skeleton ~-9 ~ ~9
execute at @s run summon minecraft:skeleton ~9 ~ ~-9
execute at @s run summon minecraft:skeleton ~-9 ~ ~-9
execute at @s run summon minecraft:skeleton ~11 ~ ~5
execute at @s run summon minecraft:skeleton ~-11 ~ ~-5

# --- Wave 4: Creeper Finale ---
tellraw @a [{"text":"[Army] ","color":"dark_green","bold":true},{"text":"Wave 4: CREEPERS! RUN!","color":"green","bold":true}]
execute at @s run summon minecraft:creeper ~10 ~ ~2
execute at @s run summon minecraft:creeper ~-10 ~ ~-2
execute at @s run summon minecraft:creeper ~2 ~ ~10
execute at @s run summon minecraft:creeper ~-2 ~ ~-10
execute at @s run summon minecraft:creeper ~8 ~ ~8
execute at @s run summon minecraft:creeper ~-8 ~ ~8
execute at @s run summon minecraft:creeper ~8 ~ ~-8
execute at @s run summon minecraft:creeper ~-8 ~ ~-8

# --- Give the player a fighting chance ---
effect give @s minecraft:strength 30 2 true
effect give @s minecraft:speed 30 1 true
effect give @s minecraft:resistance 30 1 true
give @s minecraft:diamond_sword 1

# --- Battle sounds ---
playsound minecraft:entity.ender_dragon.growl master @a ~ ~ ~ 0.5 0.5

tellraw @a [{"text":"[Army] ","color":"dark_green","bold":true},{"text":"50+ mobs spawned! You have Strength III and a diamond sword. Good luck!","color":"gold"}]
