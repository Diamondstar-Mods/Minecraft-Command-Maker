# Item Sorter Redstone Build
# Creates a hopper-based item sorting system
title @a title {"text":"Item Sorter Built!","color":"gold","bold":true}
tellraw @a [{"text":"[Redstone]","color":"gold","bold":true},{"text":" An item sorting system has been set up. Drop items in the top chest!","color":"white"}]
execute at @s run setblock ~-2 ~ ~ minecraft:chest
execute at @s run setblock ~2 ~ ~ minecraft:chest
execute at @s run setblock ~1 ~-1 ~ minecraft:hopper[facing=down]
execute at @s run setblock ~1 ~-2 ~ minecraft:hopper
execute at @s run setblock ~-1 ~-1 ~ minecraft:hopper[facing=down]
execute at @s run setblock ~-1 ~-2 ~ minecraft:hopper
execute at @s run setblock ~ ~-1 ~ minecraft:hopper[facing=west]
execute at @s run setblock ~ ~-2 ~ minecraft:barrel
execute at @s run setblock ~1 ~-2 ~1 minecraft:comparator[facing=south]
execute at @s run setblock ~-1 ~-2 ~-1 minecraft:comparator[facing=north]
execute at @s run fill ~-2 ~-2 ~-2 ~2 ~-2 ~2 minecraft:redstone_dust
execute at @s run setblock ~ ~-2 ~ minecraft:repeater[facing=east]
playsound minecraft:block.metal.place master @a ~ ~ ~ 1 1
