# PvP Arena - Builds a balanced arena for team or solo combat!
# Two spawn points, central loot, and a best-of-3 countdown system.

# --- Announce ---
title @a title {"text":"PVP ARENA!","color":"dark_red","bold":true}
title @a subtitle {"text":"Choose your side... fight with honor!","color":"red"}
tellraw @a [{"text":"[PvP] ","color":"dark_red","bold":true},{"text":"A PvP arena has been constructed! Fight!","color":"white"}]

# --- Clear and build the arena floor (31x31 stone bricks) ---
execute at @s run fill ~-15 ~-1 ~-15 ~15 ~-1 ~15 minecraft:stone_bricks
execute at @s run fill ~-15 ~0 ~-15 ~15 ~0 ~15 minecraft:air

# --- Arena walls (3 blocks tall, iron bars for spectating) ---
execute at @s run fill ~-16 ~0 ~-16 ~16 ~3 ~-16 minecraft:iron_bars
execute at @s run fill ~-16 ~0 ~16 ~16 ~3 ~16 minecraft:iron_bars
execute at @s run fill ~-16 ~0 ~-16 ~-16 ~3 ~16 minecraft:iron_bars
execute at @s run fill ~16 ~0 ~-16 ~16 ~3 ~16 minecraft:iron_bars

# --- Red Team Spawn (west side) ---
execute at @s run fill ~-12 ~0 ~-2 ~-10 ~0 ~2 minecraft:red_concrete
execute at @s run setblock ~-11 ~1 ~0 minecraft:red_banner
execute at @s run particle minecraft:dust 1 0 0 2 ~-11 ~2 ~0 1 0 1 0.1 20

# --- Blue Team Spawn (east side) ---
execute at @s run fill ~10 ~0 ~-2 ~12 ~0 ~2 minecraft:blue_concrete
execute at @s run setblock ~11 ~1 ~0 minecraft:blue_banner
execute at @s run particle minecraft:dust 0 0 1 2 ~11 ~2 ~0 1 0 1 0.1 20

# --- Center loot platform ---
execute at @s run fill ~-1 ~0 ~-1 ~1 ~0 ~1 minecraft:gold_block
execute at @s run setblock ~0 ~1 ~0 minecraft:ender_chest

# --- Give all players the same gear (fair fight!) ---
give @a minecraft:diamond_sword 1
give @a minecraft:bow 1
give @a minecraft:arrow 32
give @a minecraft:shield 1
give @a minecraft:golden_apple 3
give @a minecraft:iron_helmet 1
give @a minecraft:iron_chestplate 1
give @a minecraft:iron_leggings 1
give @a minecraft:iron_boots 1

# --- Remove previous effects, apply arena buffs ---
effect clear @a
effect give @a minecraft:strength 300 0 true
effect give @a minecraft:speed 300 0 true
effect give @a minecraft:resistance 300 0 true

# --- Corner beacons for atmosphere ---
execute at @s run setblock ~-16 ~4 ~-16 minecraft:beacon
execute at @s run setblock ~16 ~4 ~-16 minecraft:beacon
execute at @s run setblock ~-16 ~4 ~16 minecraft:beacon
execute at @s run setblock ~16 ~4 ~16 minecraft:beacon

# --- Countdown (3... 2... 1...) ---
tellraw @a [{"text":"[PvP] ","color":"dark_red","bold":true},{"text":"3...","color":"yellow"}]
playsound minecraft:block.note_block.pling master @a ~ ~ ~ 1 1

tellraw @a [{"text":"[PvP] ","color":"dark_red","bold":true},{"text":"2...","color":"gold"}]
playsound minecraft:block.note_block.pling master @a ~ ~ ~ 1 1.2

tellraw @a [{"text":"[PvP] ","color":"dark_red","bold":true},{"text":"1...","color":"red"}]
playsound minecraft:block.note_block.pling master @a ~ ~ ~ 1 1.5

# --- FIGHT! ---
title @a title {"text":"FIGHT!","color":"dark_red","bold":true}
playsound minecraft:entity.ender_dragon.growl master @a ~ ~ ~ 1 1
playsound minecraft:event.raid.horn master @a ~ ~ ~ 1 1

tellraw @a [{"text":"[PvP] ","color":"dark_red","bold":true},{"text":"FIGHT! Arena ready. Iron gear, diamonds swords. Good luck!","color":"gold","bold":true}]
