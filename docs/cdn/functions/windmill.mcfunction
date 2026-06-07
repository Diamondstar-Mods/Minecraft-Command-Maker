# Windmill Building
# Constructs a decorative windmill with rotating blades
title @a title {"text":"Windmill Built!","color":"white","bold":true}
tellraw @a [{"text":"[Build]","color":"white","bold":true},{"text":" A charming windmill has appeared!","color":"white"}]
execute at @s run fill ~-4 ~-1 ~-4 ~4 ~8 ~4 minecraft:oak_planks
execute at @s run fill ~-3 ~ ~-3 ~3 ~7 ~3 minecraft:air
# Blades
execute at @s run fill ~ ~8 ~-3 ~ ~8 ~3 minecraft:spruce_fence
execute at @s run fill ~-3 ~8 ~ ~3 ~8 ~ minecraft:spruce_fence
execute at @s run fill ~-2 ~9 ~-2 ~2 ~9 ~2 minecraft:white_wool
execute at @s run setblock ~-2 ~9 ~ minecraft:white_wool
execute at @s run setblock ~2 ~9 ~ minecraft:white_wool
execute at @s run setblock ~ ~9 ~-2 minecraft:white_wool
execute at @s run setblock ~ ~9 ~2 minecraft:white_wool
# Base
execute at @s run fill ~-5 ~-1 ~-5 ~5 ~-1 ~5 minecraft:cobblestone
execute at @s run fill ~-3 ~-1 ~-3 ~3 ~-1 ~3 minecraft:stone_bricks
# Door and windows
execute at @s run setblock ~-2 ~ ~-4 minecraft:oak_door
execute at @s run setblock ~-1 ~1 ~-4 minecraft:oak_door
execute at @s run setblock ~3 ~2 ~ minecraft:glass_pane
execute at @s run setblock ~-3 ~2 ~ minecraft:glass_pane
execute at @s run setblock ~ ~2 ~3 minecraft:glass_pane
playsound minecraft:block.wood.place master @a ~ ~ ~ 1 1
