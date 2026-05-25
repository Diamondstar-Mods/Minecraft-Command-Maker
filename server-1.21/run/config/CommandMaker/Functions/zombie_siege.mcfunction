# Zombie Siege - Fortify and survive the undead onslaught!
# Builds temporary walls, then sends 5 waves of escalating zombie hordes.

# --- Announce ---
title @a title {"text":"ZOMBIE SIEGE!","color":"dark_green","bold":true}
title @a subtitle {"text":"They're coming... fortify your position!","color":"red"}
tellraw @a [{"text":"[Siege] ","color":"dark_green","bold":true},{"text":"Zombie siege initiating! Fortifying position...","color":"red"}]

# --- Build defensive walls around the player (7x7 fort, 3 tall) ---
execute at @s run fill ~-5 ~-1 ~-5 ~5 ~3 ~-5 minecraft:oak_log
execute at @s run fill ~-5 ~-1 ~5 ~5 ~3 ~5 minecraft:oak_log
execute at @s run fill ~-5 ~-1 ~-5 ~-5 ~3 ~5 minecraft:oak_log
execute at @s run fill ~5 ~-1 ~-5 ~5 ~3 ~5 minecraft:oak_log
execute at @s run fill ~-4 ~3 ~-4 ~4 ~3 ~4 minecraft:oak_planks

# --- Add arrow slit windows ---
execute at @s run setblock ~-5 ~1 ~2 minecraft:air
execute at @s run setblock ~-5 ~1 ~-2 minecraft:air
execute at @s run setblock ~5 ~1 ~2 minecraft:air
execute at @s run setblock ~5 ~1 ~-2 minecraft:air
execute at @s run setblock ~2 ~1 ~5 minecraft:air
execute at @s run setblock ~-2 ~1 ~5 minecraft:air

# --- Torches around the fort ---
execute at @s run setblock ~-5 ~2 ~0 minecraft:torch
execute at @s run setblock ~5 ~2 ~0 minecraft:torch
execute at @s run setblock ~0 ~2 ~5 minecraft:torch
execute at @s run setblock ~0 ~2 ~-5 minecraft:torch

# --- Supply chest inside ---
execute at @s run setblock ~0 ~0 ~0 minecraft:chest
execute at @s run setblock ~0 ~1 ~0 minecraft:crafting_table

# --- Buff the player ---
effect give @a minecraft:strength 300 1 true
effect give @a minecraft:resistance 300 0 true
effect give @a minecraft:regeneration 300 0 true
effect give @a minecraft:hero_of_the_village 300 1 true

# --- Give combat gear ---
give @a minecraft:iron_sword 1
give @a minecraft:bow 1
give @a minecraft:arrow 128
give @a minecraft:shield 1
give @a minecraft:golden_apple 10
give @a minecraft:cooked_beef 32

# --- War horn ---
playsound minecraft:event.raid.horn master @a ~ ~ ~ 2 0.5
playsound minecraft:entity.ravager.roar master @a ~ ~ ~ 1 1

# --- Wave 1: The Scouts (basic zombies, outside walls) ---
tellraw @a [{"text":"[Siege] ","color":"dark_green","bold":true},{"text":"Wave 1: Scouts approaching!","color":"yellow"}]
execute at @s run summon minecraft:zombie ~7 ~ ~7
execute at @s run summon minecraft:zombie ~-7 ~ ~7
execute at @s run summon minecraft:zombie ~7 ~ ~-7
execute at @s run summon minecraft:zombie ~-7 ~ ~-7
execute at @s run summon minecraft:zombie ~10 ~ ~0
execute at @s run summon minecraft:zombie ~-10 ~ ~0
execute at @s run summon minecraft:zombie ~0 ~ ~10
execute at @s run summon minecraft:zombie ~0 ~ ~-10

# --- Wave 2: Armored zombies ---
tellraw @a [{"text":"[Siege] ","color":"dark_green","bold":true},{"text":"Wave 2: Armored zombies!","color":"gold"}]
execute at @s run summon minecraft:zombie ~8 ~ ~5 {HandItems:[{id:"minecraft:iron_sword",Count:1b}],ArmorItems:[{id:"minecraft:iron_boots",Count:1b},{id:"minecraft:iron_leggings",Count:1b},{id:"minecraft:iron_chestplate",Count:1b},{id:"minecraft:iron_helmet",Count:1b}]}
execute at @s run summon minecraft:zombie ~-8 ~ ~-5 {HandItems:[{id:"minecraft:iron_axe",Count:1b}],ArmorItems:[{id:"minecraft:chainmail_boots",Count:1b},{id:"minecraft:chainmail_leggings",Count:1b},{id:"minecraft:chainmail_chestplate",Count:1b},{id:"minecraft:chainmail_helmet",Count:1b}]}
execute at @s run summon minecraft:zombie ~5 ~ ~8 {HandItems:[{id:"minecraft:iron_sword",Count:1b}],ArmorItems:[{id:"minecraft:iron_boots",Count:1b},{id:"minecraft:iron_leggings",Count:1b},{id:"minecraft:iron_chestplate",Count:1b},{id:"minecraft:iron_helmet",Count:1b}]}
execute at @s run summon minecraft:zombie ~-5 ~ ~-8
execute at @s run summon minecraft:zombie ~8 ~ ~-5
execute at @s run summon minecraft:zombie ~-8 ~ ~5

# --- Wave 3: Baby zombie rush (fast and small!) ---
tellraw @a [{"text":"[Siege] ","color":"dark_green","bold":true},{"text":"Wave 3: Baby zombie rush! They're FAST!","color":"red"}]
execute at @s run summon minecraft:zombie ~6 ~ ~6 {IsBaby:1b}
execute at @s run summon minecraft:zombie ~-6 ~ ~6 {IsBaby:1b}
execute at @s run summon minecraft:zombie ~6 ~ ~-6 {IsBaby:1b}
execute at @s run summon minecraft:zombie ~-6 ~ ~-6 {IsBaby:1b}
execute at @s run summon minecraft:zombie ~9 ~ ~3 {IsBaby:1b}
execute at @s run summon minecraft:zombie ~-9 ~ ~-3 {IsBaby:1b}
execute at @s run summon minecraft:zombie ~3 ~ ~9 {IsBaby:1b}
execute at @s run summon minecraft:zombie ~-3 ~ ~-9 {IsBaby:1b}
execute at @s run summon minecraft:zombie ~0 ~ ~10 {IsBaby:1b}
execute at @s run summon minecraft:zombie ~10 ~ ~0 {IsBaby:1b}

# --- Wave 4: Husks and drowned (variety!) ---
tellraw @a [{"text":"[Siege] ","color":"dark_green","bold":true},{"text":"Wave 4: Husks and Drowned join the fight!","color":"dark_red"}]
execute at @s run summon minecraft:husk ~8 ~ ~8
execute at @s run summon minecraft:husk ~-8 ~ ~-8
execute at @s run summon minecraft:husk ~8 ~ ~-8
execute at @s run summon minecraft:husk ~-8 ~ ~8
execute at @s run summon minecraft:drowned ~7 ~2 ~7
execute at @s run summon minecraft:drowned ~-7 ~2 ~7
execute at @s run summon minecraft:drowned ~7 ~2 ~-7
execute at @s run summon minecraft:drowned ~-7 ~2 ~-7

# --- Wave 5: BOSS WAVE - Zombie with reinforcement leader ---
tellraw @a [{"text":"[Siege] ","color":"dark_green","bold":true},{"text":"WAVE 5: THE GENERAL ARRIVES!","color":"dark_red","bold":true}]
execute at @s run summon minecraft:zombie ~5 ~ ~0 {CustomName:'{"text":"General Zombo","color":"red","bold":true}',HandItems:[{id:"minecraft:diamond_axe",Count:1b}],ArmorItems:[{id:"minecraft:diamond_boots",Count:1b},{id:"minecraft:diamond_leggings",Count:1b},{id:"minecraft:diamond_chestplate",Count:1b},{id:"minecraft:diamond_helmet",Count:1b}],Glowing:1b,PersistenceRequired:1b}
execute at @s run summon minecraft:zombie ~-5 ~ ~0
execute at @s run summon minecraft:zombie ~0 ~ ~5
execute at @s run summon minecraft:zombie ~0 ~ ~-5
execute at @s run summon minecraft:zombie ~3 ~ ~3
execute at @s run summon minecraft:zombie ~-3 ~ ~-3
execute at @s run summon minecraft:zombie ~3 ~ ~-3
execute at @s run summon minecraft:zombie ~-3 ~ ~3
execute at @s run summon minecraft:zombie ~7 ~ ~2
execute at @s run summon minecraft:zombie ~-7 ~ ~-2

# --- Battle ambiance ---
playsound minecraft:entity.wither.spawn master @a ~ ~ ~ 0.3 1
particle minecraft:smoke ~0 ~3 ~0 5 0 5 0.05 100
particle minecraft:flame ~0 ~1 ~0 2 0 2 0.02 40

tellraw @a [{"text":"[Siege] ","color":"dark_green","bold":true},{"text":"5 waves spawned! 30+ zombies inbound. Defeat General Zombo to win!","color":"gold"}]
