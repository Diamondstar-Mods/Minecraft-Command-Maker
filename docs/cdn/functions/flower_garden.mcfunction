# Flower Garden Nature Build
# Creates a beautiful flower garden with paths and benches
title @a title {"text":"Garden Planted!","color":"light_purple","bold":true}
tellraw @a [{"text":"[Nature]","color":"light_purple","bold":true},{"text":" A beautiful flower garden has bloomed!","color":"white"}]
execute at @s run fill ~-8 ~-1 ~-8 ~8 ~-1 ~8 minecraft:grass_block
# Path
execute at @s run fill ~-1 ~-1 ~-8 ~1 ~-1 ~8 minecraft:grass_path
execute at @s run fill ~-8 ~-1 ~-1 ~8 ~-1 ~1 minecraft:grass_path
# Flowers - variety
execute at @s run setblock ~3 ~-1 ~3 minecraft:rose_bush[half=lower]
execute at @s run setblock ~3 ~ ~3 minecraft:rose_bush[half=upper]
execute at @s run setblock ~-3 ~-1 ~4 minecraft:peony[half=lower]
execute at @s run setblock ~-3 ~ ~4 minecraft:peony[half=upper]
execute at @s run setblock ~5 ~-1 ~-2 minecraft:lilac[half=lower]
execute at @s run setblock ~5 ~ ~-2 minecraft:lilac[half=upper]
execute at @s run setblock ~-5 ~-1 ~-5 minecraft:sunflower[half=lower]
execute at @s run setblock ~-5 ~ ~-5 minecraft:sunflower[half=upper]
execute at @s run setblock ~ ~-1 ~5 minecraft:poppy
execute at @s run setblock ~4 ~-1 ~ minecraft:dandelion
execute at @s run setblock ~-4 ~-1 ~ minecraft:blue_orchid
execute at @s run setblock ~ ~-1 ~-4 minecraft:allium
execute at @s run setblock ~-2 ~-1 ~-3 minecraft:oxeye_daisy
execute at @s run setblock ~6 ~-1 ~4 minecraft:azure_bluet
# Bench
execute at @s run setblock ~ ~-1 ~2 minecraft:oak_stairs[facing=east]
execute at @s run setblock ~ ~-1 ~1 minecraft:oak_stairs[facing=west]
# Lamppost
execute at @s run setblock ~-4 ~-1 ~2 minecraft:oak_fence
execute at @s run setblock ~-4 ~ ~2 minecraft:lantern
playsound minecraft:block.grass.place master @a ~ ~ ~ 1 1
