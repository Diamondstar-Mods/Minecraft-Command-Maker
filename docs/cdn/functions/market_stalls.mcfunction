# Market Stalls Building
# Creates a colorful marketplace row
title @a title {"text":"Market Stalls Built!","color":"gold","bold":true}
tellraw @a [{"text":"[Build]","color":"gold","bold":true},{"text":" A row of colorful market stalls has been set up!","color":"white"}]
# Stall 1 - Red (Food)
execute at @s run fill ~-10 ~-1 ~-2 ~-4 ~3 ~2 minecraft:red_wool
execute at @s run fill ~-9 ~ ~-1 ~-5 ~2 ~1 minecraft:air
execute at @s run setblock ~-7 ~ ~ minecraft:barrel
execute at @s run setblock ~-10 ~2 ~ minecraft:red_banner
# Stall 2 - Blue (Tools)
execute at @s run fill ~-2 ~-1 ~-2 ~4 ~3 ~2 minecraft:blue_wool
execute at @s run fill ~-1 ~ ~-1 ~3 ~2 ~1 minecraft:air
execute at @s run setblock ~1 ~ ~ minecraft:barrel
execute at @s run setblock ~-2 ~2 ~ minecraft:blue_banner
# Stall 3 - Yellow (Armor)
execute at @s run fill ~6 ~-1 ~-2 ~12 ~3 ~2 minecraft:yellow_wool
execute at @s run fill ~7 ~ ~-1 ~11 ~2 ~1 minecraft:air
execute at @s run setblock ~9 ~ ~ minecraft:barrel
execute at @s run setblock ~6 ~2 ~ minecraft:yellow_banner
# Decorations
execute at @s run fill ~-11 ~-1 ~-3 ~13 ~-1 ~3 minecraft:oak_planks
execute at @s run setblock ~-10 ~-1 ~-3 minecraft:lantern
execute at @s run setblock ~-4 ~-1 ~-3 minecraft:lantern
execute at @s run setblock ~6 ~-1 ~-3 minecraft:lantern
execute at @s run setblock ~12 ~-1 ~-3 minecraft:lantern
playsound minecraft:entity.villager.ambient master @a ~ ~ ~ 1 1
