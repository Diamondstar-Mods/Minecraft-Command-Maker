# Secret Passage Redstone Build
# Creates a hidden bookshelf door with lever activation
title @a title {"text":"Secret Passage!","color":"gray","bold":true}
tellraw @a [{"text":"[Redstone]","color":"gray","bold":true},{"text":" A secret passage has been built. Pull the lever behind the bookshelf!","color":"white"}]
execute at @s run fill ~-3 ~-1 ~-3 ~3 ~3 ~3 minecraft:stone_bricks
execute at @s run fill ~-2 ~ ~-2 ~2 ~2 ~2 minecraft:air
execute at @s run setblock ~-2 ~1 ~ minecraft:lever[face=wall,facing=south]
execute at @s run setblock ~-2 ~ ~ minecraft:bookshelf
execute at @s run setblock ~-1 ~ ~ minecraft:bookshelf
execute at @s run setblock ~ ~ ~ minecraft:bookshelf
execute at @s run setblock ~1 ~ ~ minecraft:bookshelf
execute at @s run setblock ~2 ~ ~ minecraft:bookshelf
execute at @s run setblock ~-2 ~2 ~ minecraft:bookshelf
execute at @s run setblock ~2 ~2 ~ minecraft:bookshelf
execute at @s run fill ~-2 ~-1 ~3 ~2 ~-1 ~3 minecraft:redstone_dust
execute at @s run setblock ~ ~-1 ~3 minecraft:redstone_torch
execute at @s run setblock ~ ~-1 ~1 minecraft:stone
execute at @s run setblock ~ ~-1 ~2 minecraft:sticky_piston[facing=north]
execute at @s run setblock ~ ~-1 ~ minecraft:iron_trapdoor[open=true]
playsound minecraft:block.piston.extend master @a ~ ~ ~ 1 0.5
