# Boss Arena - Builds a fighting arena and spawns a boss!
# Creates an obsidian arena, buffs all players, and summons a Wither.

# --- Announce ---
title @a title {"text":"BOSS FIGHT!","color":"dark_red","bold":true}
title @a subtitle {"text":"Prepare yourselves...","color":"red"}
tellraw @a [{"text":"[Arena] ","color":"dark_purple","bold":true},{"text":"A boss arena has been summoned! Gear up!","color":"red"}]

# --- Build the arena floor (21x21 obsidian platform) ---
execute at @s run fill ~-10 ~-1 ~-10 ~10 ~-1 ~10 minecraft:obsidian
execute at @s run fill ~-10 ~0 ~-10 ~10 ~0 ~10 minecraft:air

# --- Arena walls (obsidian border, 4 blocks tall) ---
execute at @s run fill ~-11 ~0 ~-11 ~11 ~3 ~-11 minecraft:obsidian
execute at @s run fill ~-11 ~0 ~11 ~11 ~3 ~11 minecraft:obsidian
execute at @s run fill ~-11 ~0 ~-11 ~-11 ~3 ~11 minecraft:obsidian
execute at @s run fill ~11 ~0 ~-11 ~11 ~3 ~11 minecraft:obsidian

# --- Corner pillars with soul lanterns (5 blocks tall) ---
execute at @s run fill ~-11 ~0 ~-11 ~-11 ~5 ~-11 minecraft:deepslate_bricks
execute at @s run fill ~11 ~0 ~-11 ~11 ~5 ~-11 minecraft:deepslate_bricks
execute at @s run fill ~-11 ~0 ~11 ~-11 ~5 ~11 minecraft:deepslate_bricks
execute at @s run fill ~11 ~0 ~11 ~11 ~5 ~11 minecraft:deepslate_bricks
execute at @s run setblock ~-11 ~6 ~-11 minecraft:soul_lantern
execute at @s run setblock ~11 ~6 ~-11 minecraft:soul_lantern
execute at @s run setblock ~-11 ~6 ~11 minecraft:soul_lantern
execute at @s run setblock ~11 ~6 ~11 minecraft:soul_lantern

# --- Weapon supply chests on opposite sides ---
execute at @s run setblock ~-10 ~0 ~0 minecraft:chest
execute at @s run setblock ~10 ~0 ~0 minecraft:chest

# --- Buff all players for the fight ---
effect give @a minecraft:strength 120 1 true
effect give @a minecraft:resistance 120 1 true
effect give @a minecraft:regeneration 120 0 true
effect give @a minecraft:fire_resistance 120 0 true
effect give @a minecraft:night_vision 120 0 true

# --- Give everyone combat supplies ---
give @a minecraft:diamond_sword 1
give @a minecraft:bow 1
give @a minecraft:arrow 64
give @a minecraft:golden_apple 5
give @a minecraft:shield 1

# --- Dramatic buildup ---
playsound minecraft:entity.wither.spawn master @a ~ ~ ~ 1 1
playsound minecraft:entity.ender_dragon.growl master @a ~ ~ ~ 0.5 0.5
particle minecraft:smoke ~ ~2 ~ 5 2 5 0.05 200
particle minecraft:flame ~ ~2 ~ 3 1 3 0.05 100

# --- SPAWN THE BOSS (Wither) ---
execute at @s run summon minecraft:wither ~ ~3 ~

# --- Fight music ---
playsound minecraft:music_disc.pigstep master @a ~ ~ ~ 2 1

tellraw @a [{"text":"[Arena] ","color":"dark_purple","bold":true},{"text":"The Wither has arrived! ","color":"red"},{"text":"FIGHT!","color":"gold","bold":true}]
