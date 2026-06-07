# Survival Island Event
# Strands players on a small island with limited resources
title @a title {"text":"Survival Island!","color":"green","bold":true}
title @a subtitle {"text":"Stranded! Survive with what you can find.","color":"white"}
tellraw @a [{"text":"[Island]","color":"green","bold":true},{"text":" You've been stranded on a desert island! Survive with limited resources.","color":"white"}]
execute at @s run fill ~-8 ~-3 ~-8 ~8 ~-1 ~8 minecraft:sand
execute at @s run fill ~-3 ~-1 ~-3 ~3 ~-1 ~3 minecraft:grass_block
execute at @s run setblock ~ ~-1 ~ minecraft:chest{Items:[{id:"minecraft:iron_ingot",Count:3},{id:"minecraft:bread",Count:5},{id:"minecraft:oak_sapling",Count:2},{id:"minecraft:fishing_rod",Count:1},{id:"minecraft:bone_meal",Count:8}]}
execute at @s run setblock ~2 ~-1 ~ minecraft:oak_log
execute at @s run setblock ~-2 ~-1 ~ minecraft:oak_log
execute at @s run setblock ~ ~-1 ~2 minecraft:crafting_table
execute at @s run setblock ~ ~-1 ~-2 minecraft:furnace
execute at @s run fill ~2 ~-3 ~2 ~8 ~-3 ~8 minecraft:water
execute at @s run fill ~2 ~-2 ~2 ~8 ~-2 ~8 minecraft:water
execute at @s run fill ~-2 ~-3 ~2 ~-8 ~-3 ~8 minecraft:water
execute at @s run fill ~-2 ~-2 ~2 ~-8 ~-2 ~8 minecraft:water
execute at @s run setblock ~-8 ~-3 ~-8 minecraft:oak_boat
gamerule doDaylightCycle true
time set 0
weather clear
playsound minecraft:entity.player.splash master @a ~ ~ ~ 1 1
