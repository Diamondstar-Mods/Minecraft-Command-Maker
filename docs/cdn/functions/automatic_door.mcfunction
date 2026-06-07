# Automatic Door Redstone Build
# Creates a pressure-plate activated piston door
title @a title {"text":"Auto Door Built!","color":"red","bold":true}
tellraw @a [{"text":"[Redstone]","color":"red","bold":true},{"text":" An automatic piston door has been installed. Step on the pressure plates!","color":"white"}]
execute at @s run fill ~-2 ~-1 ~-2 ~2 ~3 ~2 minecraft:stone_bricks
execute at @s run fill ~-1 ~ ~-1 ~1 ~2 ~1 minecraft:air
execute at @s run setblock ~-1 ~-1 ~ minecraft:sticky_piston[facing=north]
execute at @s run setblock ~1 ~-1 ~ minecraft:sticky_piston[facing=north]
execute at @s run setblock ~-1 ~ ~ minecraft:iron_block
execute at @s run setblock ~1 ~ ~ minecraft:iron_block
execute at @s run setblock ~ ~-1 ~ minecraft:redstone_block
execute at @s run setblock ~ ~-1 ~1 minecraft:stone_pressure_plate
execute at @s run setblock ~ ~-1 ~-1 minecraft:stone_pressure_plate
playsound minecraft:block.piston.extend master @a ~ ~ ~ 1 1
