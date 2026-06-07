# Fishing Derby Mini-Game
# Creates a fishing lake with hidden treasure loot tables
title @a title {"text":"Fishing Derby!","color":"blue","bold":true}
title @a subtitle {"text":"Catch the biggest fish to win!","color":"aqua"}
tellraw @a [{"text":"[Fishing]","color":"blue","bold":true},{"text":" A fishing lake has appeared. Fish for 5 minutes - rarest catch wins!","color":"white"}]
execute at @s run fill ~-12 ~-3 ~-12 ~12 ~-1 ~12 minecraft:water
execute at @s run fill ~-12 ~-4 ~-12 ~12 ~-4 ~12 minecraft:sand
execute at @s run fill ~-13 ~-1 ~-13 ~13 ~-1 ~13 minecraft:grass_block hollow
execute at @s run setblock ~-10 ~-1 ~ minecraft:oak_fence
execute at @s run setblock ~10 ~-1 ~ minecraft:oak_fence
execute at @s run setblock ~ ~-1 ~-10 minecraft:oak_fence
execute at @s run setblock ~ ~-1 ~10 minecraft:oak_fence
execute at @s run setblock ~-10 ~-1 ~-10 minecraft:lantern
execute at @s run setblock ~10 ~-1 ~-10 minecraft:lantern
execute at @s run setblock ~-10 ~-1 ~10 minecraft:lantern
execute at @s run setblock ~10 ~-1 ~10 minecraft:lantern
execute at @s run give @p minecraft:fishing_rod{Enchantments:[{id:"luck_of_the_sea",lvl:3}]} 1
playsound minecraft:entity.fishing_bobber.splash master @a ~ ~ ~ 1 1
