# Animal Pen Nature Build
# Creates a fenced animal enclosure with animals
title @a title {"text":"Animal Pen Built!","color":"green","bold":true}
tellraw @a [{"text":"[Nature]","color":"green","bold":true},{"text":" An animal pen has been set up with farm animals!","color":"white"}]
execute at @s run fill ~-6 ~-1 ~-6 ~6 ~-1 ~6 minecraft:grass_block
execute at @s run fill ~-6 ~ ~-6 ~6 ~1 ~6 minecraft:oak_fence
# Gate
execute at @s run setblock ~ ~ ~-6 minecraft:oak_fence_gate
# Water trough
execute at @s run setblock ~-3 ~-1 ~ minecraft:water
execute at @s run fill ~-3 ~-1 ~ ~-2 ~-1 ~ minecraft:oak_slab
# Animals
execute at @s run summon cow ~3 ~ ~3 {Age:-99999999}
execute at @s run summon cow ~-3 ~ ~2 {Age:-99999999}
execute at @s run summon sheep ~2 ~ ~-3 {Age:-99999999}
execute at @s run summon sheep ~-2 ~ ~-3 {Age:-99999999}
execute at @s run summon chicken ~ ~ ~3 {Age:-99999999}
execute at @s run summon chicken ~1 ~ ~4 {Age:-99999999}
# Feeding trough
execute at @s run setblock ~ ~-1 ~ minecraft:hay_block
# Hay bales
execute at @s run setblock ~5 ~-1 ~5 minecraft:hay_block
execute at @s run setblock ~-5 ~-1 ~-5 minecraft:hay_block
playsound minecraft:entity.cow.ambient master @a ~ ~ ~ 1 1
