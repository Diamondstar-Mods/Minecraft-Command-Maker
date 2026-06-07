# Dungeon Entrance Adventure Build
# Creates an ominous dungeon entrance with traps
title @a title {"text":"Dungeon Found!","color":"dark_gray","bold":true}
title @a subtitle {"text":"An ancient dungeon has been revealed...","color":"red"}
tellraw @a [{"text":"[RPG]","color":"dark_gray","bold":true},{"text":" A dungeon entrance has appeared! Dare to explore its depths?","color":"white"}]
execute at @s run fill ~-3 ~-3 ~-3 ~3 ~3 ~3 minecraft:mossy_stone_bricks
execute at @s run fill ~-2 ~-2 ~-2 ~2 ~2 ~2 minecraft:air
execute at @s run fill ~-2 ~-2 ~-3 ~2 ~2 ~-3 minecraft:iron_bars
execute at @s run setblock ~-1 ~-1 ~-3 minecraft:air
execute at @s run setblock ~1 ~-1 ~-3 minecraft:air
execute at @s run setblock ~ ~-1 ~-3 minecraft:air
execute at @s run fill ~ ~-3 ~ ~ ~-5 ~ minecraft:ladder
execute at @s run fill ~-1 ~-5 ~-1 ~1 ~-5 ~1 minecraft:cobblestone
execute at @s run setblock ~ ~-5 ~ minecraft:redstone_torch
execute at @s run setblock ~-2 ~1 ~-2 minecraft:lantern
execute at @s run setblock ~2 ~1 ~-2 minecraft:lantern
execute at @s run setblock ~-2 ~-1 ~-2 minecraft:skeleton_skull
execute at @s run setblock ~2 ~-1 ~-2 minecraft:skeleton_skull
execute at @s run give @p minecraft:torch 16
effect give @a night_vision 600 0 true
playsound minecraft:ambient.cave master @a ~ ~ ~ 1 0.5
playsound minecraft:block.chest.open master @a ~ ~ ~ 0.5 1
