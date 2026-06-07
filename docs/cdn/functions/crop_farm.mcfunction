# Crop Farm Nature Build
# Creates an automated-looking crop farm
title @a title {"text":"Farm Built!","color":"green","bold":true}
tellraw @a [{"text":"[Nature]","color":"green","bold":true},{"text":" A crop farm has been planted and set up!","color":"white"}]
execute at @s run fill ~-8 ~-1 ~-8 ~8 ~-1 ~8 minecraft:farmland
execute at @s run fill ~-7 ~-1 ~-7 ~7 ~-1 ~7 minecraft:wheat[age=7]
execute at @s run setblock ~-5 ~-1 ~ minecraft:carrots[age=7]
execute at @s run setblock ~5 ~-1 ~ minecraft:potatoes[age=7]
execute at @s run setblock ~ ~-1 ~-5 minecraft:beetroots[age=3]
execute at @s run setblock ~ ~-1 ~5 minecraft:carrots[age=7]
execute at @s run setblock ~-8 ~-1 ~-8 minecraft:water
execute at @s run setblock ~8 ~-1 ~-8 minecraft:water
execute at @s run setblock ~-8 ~-1 ~8 minecraft:water
execute at @s run setblock ~8 ~-1 ~8 minecraft:water
# Scarecrow
execute at @s run setblock ~-4 ~-1 ~-4 minecraft:oak_fence
execute at @s run setblock ~-4 ~ ~-4 minecraft:carved_pumpkin
# Compost bin
execute at @s run setblock ~-6 ~-1 ~-6 minecraft:composter
execute at @s run setblock ~6 ~-1 ~-6 minecraft:barrel
give @a minecraft:bone_meal 16
playsound minecraft:item.crop.plant master @a ~ ~ ~ 1 1
