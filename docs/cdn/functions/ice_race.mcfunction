# Ice Race Mini-Game
# Creates an ice track with speed boosts and checkpoints
title @a title {"text":"Ice Race!","color":"aqua","bold":true}
title @a subtitle {"text":"Race around the ice track!","color":"white"}
tellraw @a [{"text":"[Race]","color":"aqua","bold":true},{"text":" An ice racing track has been built. First one to complete 3 laps wins!","color":"white"}]
execute at @s run fill ~-20 ~-1 ~-20 ~20 ~-1 ~20 minecraft:blue_ice
execute at @s run fill ~-20 ~-1 ~-20 ~20 ~2 ~-20 minecraft:glass
execute at @s run fill ~-20 ~-1 ~20 ~20 ~2 ~20 minecraft:glass
execute at @s run fill ~-20 ~-1 ~-20 ~-20 ~2 ~20 minecraft:glass
execute at @s run fill ~20 ~-1 ~-20 ~20 ~2 ~20 minecraft:glass
execute at @s run setblock ~-15 ~ ~ minecraft:sea_lantern
execute at @s run setblock ~15 ~ ~ minecraft:sea_lantern
execute at @s run setblock ~ ~ ~-15 minecraft:sea_lantern
execute at @s run setblock ~ ~ ~15 minecraft:sea_lantern
execute at @s run give @p minecraft:oak_boat 1
effect give @a speed 120 2 true
playsound minecraft:block.note_block.chime master @a ~ ~ ~ 1 1.5
