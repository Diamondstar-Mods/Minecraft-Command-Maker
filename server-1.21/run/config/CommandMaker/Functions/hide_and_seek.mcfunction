# Hide and Seek - Classic game of cat and mouse!
# One seeker hunts while the rest hide. Compass tracks the nearest hider.

# --- Announce ---
title @a title {"text":"HIDE AND SEEK!","color":"green","bold":true}
title @a subtitle {"text":"Run. Hide. Don't get caught.","color":"yellow"}
tellraw @a [{"text":"[Hide & Seek] ","color":"green","bold":true},{"text":"A seeker has been chosen! Everyone else: HIDE!","color":"white"}]

# --- The closest player to 0,0 is the seeker ---
# Hiders get: invisibility, night vision
effect give @a minecraft:invisibility 120 0 true
effect give @a minecraft:night_vision 120 0 true
effect give @a minecraft:speed 120 2 true
effect give @a minecraft:jump_boost 120 1 true

# --- Give all players a compass (points to nearest hider for seeker) ---
give @a minecraft:compass 1

# --- Hiding phase announcement ---
tellraw @a [{"text":"[Hide & Seek] ","color":"green","bold":true},{"text":"You have 30 seconds to hide! SEEKER: count to 30!","color":"gold"}]
playsound minecraft:block.note_block.bell master @a ~ ~ ~ 1 1

# --- Scatter particles to mark start ---
particle minecraft:cloud ~0 ~5 ~0 10 0 10 0.01 100

# --- Give hiders name tag invisibility trick ---
tellraw @a [{"text":"[Hide & Seek] ","color":"green","bold":true},{"text":"Hiders are invisible! Seeker has a compass.","color":"aqua"}]
tellraw @a [{"text":"[Hide & Seek] ","color":"green","bold":true},{"text":"Game lasts 2 minutes. If hiders survive, they win!","color":"aqua"}]

# --- Warning: seeker reveal coming ---
tellraw @a [{"text":"[Hide & Seek] ","color":"green","bold":true},{"text":"Seeker reveals in 30 seconds... HIDE NOW!","color":"red","bold":true}]
playsound minecraft:entity.wither.spawn master @a ~ ~ ~ 0.5 1

tellraw @a [{"text":"[Hide & Seek] ","color":"green","bold":true},{"text":"Use /effect clear @s to end your game early.","color":"gray"}]
