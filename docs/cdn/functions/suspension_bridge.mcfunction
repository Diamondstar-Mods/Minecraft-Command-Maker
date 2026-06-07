# Suspension Bridge Building
# 50-block dramatic suspension bridge
title @a title {"text":"Bridge Built!","color":"yellow","bold":true}
tellraw @a [{"text":"[Build]","color":"yellow","bold":true},{"text":" A dramatic suspension bridge spans the gap!","color":"white"}]
execute at @s run fill ~ ~-2 ~ ~50 ~-2 ~3 minecraft:oak_planks
execute at @s run fill ~ ~-1 ~ ~50 ~-1 ~3 minecraft:oak_fence
execute at @s run fill ~ ~2 ~-1 ~50 ~2 ~-1 minecraft:spruce_fence
execute at @s run fill ~ ~2 ~4 ~50 ~2 ~4 minecraft:spruce_fence
execute at @s run fill ~ ~6 ~-1 ~50 ~6 ~-1 minecraft:spruce_fence
execute at @s run fill ~ ~6 ~4 ~50 ~6 ~4 minecraft:spruce_fence
execute at @s run fill ~-2 ~4 ~ ~ ~-2 ~3 minecraft:stone_bricks
execute at @s run fill ~52 ~4 ~ ~52 ~-2 ~3 minecraft:stone_bricks
execute at @s run setblock ~-2 ~5 ~ minecraft:lantern
execute at @s run setblock ~-2 ~5 ~3 minecraft:lantern
execute at @s run setblock ~52 ~5 ~ minecraft:lantern
execute at @s run setblock ~52 ~5 ~3 minecraft:lantern
playsound minecraft:block.wood.place master @a ~ ~ ~ 1 0.8
