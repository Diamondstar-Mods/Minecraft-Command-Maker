# Piston Elevator Redstone Build
# Creates a 10-block piston elevator shaft
title @a title {"text":"Elevator Built!","color":"gray","bold":true}
tellraw @a [{"text":"[Redstone]","color":"gray","bold":true},{"text":" A piston elevator has been constructed! Step inside to ride.","color":"white"}]
execute at @s run fill ~-2 ~-1 ~-1 ~2 ~10 ~1 minecraft:stone_bricks
execute at @s run fill ~-1 ~ ~-1 ~1 ~9 ~1 minecraft:air
execute at @s run fill ~-1 ~-1 ~-1 ~1 ~9 ~-1 minecraft:air
execute at @s run setblock ~ ~-1 ~ minecraft:sticky_piston[facing=up]
execute at @s run setblock ~-1 ~-1 ~ minecraft:redstone_block
execute at @s run setblock ~1 ~-1 ~ minecraft:redstone_block
execute at @s run setblock ~ ~-1 ~1 minecraft:stone_button[face=floor]
execute at @s run setblock ~-2 ~-1 ~ minecraft:stone_button[face=floor]
execute at @s run setblock ~2 ~-1 ~ minecraft:stone_button[face=floor]
execute at @s run setblock ~-1 ~10 ~ minecraft:glass
execute at @s run setblock ~1 ~10 ~ minecraft:glass
playsound minecraft:block.piston.extend master @a ~ ~ ~ 1 0.8
