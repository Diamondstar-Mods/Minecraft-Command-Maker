# Rainbow Road - Generates a colorful glass bridge ahead of you!
# Builds a rainbow path in the direction you're facing with speed boost.

# --- Announce ---
title @a title {"text":"RAINBOW ROAD!","color":"gold","bold":true}
title @a subtitle {"text":"Run like the wind!","color":"aqua"}

# --- Give the runner speed and slow falling ---
effect give @s minecraft:speed 60 3 true
effect give @s minecraft:slow_falling 60 0 true
effect give @s minecraft:night_vision 60 0 true

# --- Build the rainbow bridge extending forward from the player ---
# Row 1: Red
execute at @s run fill ^ ^-1 ^1 ^-2 ^-1 ^1 minecraft:red_stained_glass
execute at @s run fill ^ ^-1 ^2 ^-2 ^-1 ^2 minecraft:red_stained_glass
execute at @s run fill ^ ^-1 ^3 ^-2 ^-1 ^3 minecraft:red_stained_glass
execute at @s run fill ^ ^-1 ^4 ^-2 ^-1 ^4 minecraft:red_stained_glass
execute at @s run fill ^ ^-1 ^5 ^-2 ^-1 ^5 minecraft:red_stained_glass

# Row 2: Orange
execute at @s run fill ^ ^-1 ^6 ^-2 ^-1 ^6 minecraft:orange_stained_glass
execute at @s run fill ^ ^-1 ^7 ^-2 ^-1 ^7 minecraft:orange_stained_glass
execute at @s run fill ^ ^-1 ^8 ^-2 ^-1 ^8 minecraft:orange_stained_glass
execute at @s run fill ^ ^-1 ^9 ^-2 ^-1 ^9 minecraft:orange_stained_glass
execute at @s run fill ^ ^-1 ^10 ^-2 ^-1 ^10 minecraft:orange_stained_glass

# Row 3: Yellow
execute at @s run fill ^ ^-1 ^11 ^-2 ^-1 ^11 minecraft:yellow_stained_glass
execute at @s run fill ^ ^-1 ^12 ^-2 ^-1 ^12 minecraft:yellow_stained_glass
execute at @s run fill ^ ^-1 ^13 ^-2 ^-1 ^13 minecraft:yellow_stained_glass
execute at @s run fill ^ ^-1 ^14 ^-2 ^-1 ^14 minecraft:yellow_stained_glass
execute at @s run fill ^ ^-1 ^15 ^-2 ^-1 ^15 minecraft:yellow_stained_glass

# Row 4: Lime/Green
execute at @s run fill ^ ^-1 ^16 ^-2 ^-1 ^16 minecraft:lime_stained_glass
execute at @s run fill ^ ^-1 ^17 ^-2 ^-1 ^17 minecraft:lime_stained_glass
execute at @s run fill ^ ^-1 ^18 ^-2 ^-1 ^18 minecraft:lime_stained_glass
execute at @s run fill ^ ^-1 ^19 ^-2 ^-1 ^19 minecraft:lime_stained_glass
execute at @s run fill ^ ^-1 ^20 ^-2 ^-1 ^20 minecraft:lime_stained_glass

# Row 5: Light Blue
execute at @s run fill ^ ^-1 ^21 ^-2 ^-1 ^21 minecraft:light_blue_stained_glass
execute at @s run fill ^ ^-1 ^22 ^-2 ^-1 ^22 minecraft:light_blue_stained_glass
execute at @s run fill ^ ^-1 ^23 ^-2 ^-1 ^23 minecraft:light_blue_stained_glass
execute at @s run fill ^ ^-1 ^24 ^-2 ^-1 ^24 minecraft:light_blue_stained_glass
execute at @s run fill ^ ^-1 ^25 ^-2 ^-1 ^25 minecraft:light_blue_stained_glass

# Row 6: Blue
execute at @s run fill ^ ^-1 ^26 ^-2 ^-1 ^26 minecraft:blue_stained_glass
execute at @s run fill ^ ^-1 ^27 ^-2 ^-1 ^27 minecraft:blue_stained_glass
execute at @s run fill ^ ^-1 ^28 ^-2 ^-1 ^28 minecraft:blue_stained_glass
execute at @s run fill ^ ^-1 ^29 ^-2 ^-1 ^29 minecraft:blue_stained_glass
execute at @s run fill ^ ^-1 ^30 ^-2 ^-1 ^30 minecraft:blue_stained_glass

# Row 7: Purple
execute at @s run fill ^ ^-1 ^31 ^-2 ^-1 ^31 minecraft:purple_stained_glass
execute at @s run fill ^ ^-1 ^32 ^-2 ^-1 ^32 minecraft:purple_stained_glass
execute at @s run fill ^ ^-1 ^33 ^-2 ^-1 ^33 minecraft:purple_stained_glass
execute at @s run fill ^ ^-1 ^34 ^-2 ^-1 ^34 minecraft:purple_stained_glass
execute at @s run fill ^ ^-1 ^35 ^-2 ^-1 ^35 minecraft:purple_stained_glass

# --- End rod sparkles along the bridge ---
execute at @s run particle minecraft:end_rod ^ ^0 ^5 0.5 0.5 0.5 0.02 20
execute at @s run particle minecraft:end_rod ^ ^0 ^15 0.5 0.5 0.5 0.02 20
execute at @s run particle minecraft:end_rod ^ ^0 ^25 0.5 0.5 0.5 0.02 20
execute at @s run particle minecraft:end_rod ^ ^0 ^35 0.5 0.5 0.5 0.02 20

# --- Sound effects ---
playsound minecraft:block.note_block.chime master @s ~ ~ ~ 1 1
playsound minecraft:entity.player.levelup master @s ~ ~ ~ 0.5 2

tellraw @s [{"text":"[Rainbow Road] ","color":"gold","bold":true},{"text":"35-block rainbow bridge placed! You have Speed IV and Slow Falling. GO!","color":"white"}]
