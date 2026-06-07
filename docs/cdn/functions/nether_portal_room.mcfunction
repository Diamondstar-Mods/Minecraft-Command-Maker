# Nether Portal Room Building
# Creates an elaborate nether portal structure
title @a title {"text":"Portal Room Built!","color":"dark_purple","bold":true}
tellraw @a [{"text":"[Build]","color":"dark_purple","bold":true},{"text":" An ominous nether portal room has been constructed.","color":"white"}]
execute at @s run fill ~-5 ~-1 ~-5 ~5 ~6 ~5 minecraft:obsidian
execute at @s run fill ~-4 ~ ~-4 ~4 ~5 ~4 minecraft:air
# Portal frame
execute at @s run fill ~-2 ~ ~-2 ~2 ~4 ~-3 minecraft:obsidian
execute at @s run fill ~-1 ~1 ~-2 ~1 ~3 ~-3 minecraft:air
execute at @s run setblock ~1 ~1 ~-2 minecraft:fire
execute at @s run setblock ~-1 ~1 ~-2 minecraft:fire
execute at @s run setblock ~ ~1 ~-2 minecraft:fire
execute at @s run setblock ~1 ~2 ~-2 minecraft:fire
execute at @s run setblock ~-1 ~2 ~-2 minecraft:fire
execute at @s run setblock ~ ~2 ~-2 minecraft:fire
# Decorations
execute at @s run setblock ~-4 ~1 ~-4 minecraft:glowstone
execute at @s run setblock ~4 ~1 ~-4 minecraft:glowstone
execute at @s run setblock ~-4 ~1 ~4 minecraft:glowstone
execute at @s run setblock ~4 ~1 ~4 minecraft:glowstone
execute at @s run fill ~-4 ~-1 ~4 ~4 ~-1 ~4 minecraft:soul_sand
execute at @s run fill ~-5 ~-1 ~-5 ~5 ~-1 ~5 minecraft:nether_bricks
execute at @s run setblock ~-4 ~ ~ minecraft:nether_wart
playsound minecraft:block.portal.trigger master @a ~ ~ ~ 1 0.5
