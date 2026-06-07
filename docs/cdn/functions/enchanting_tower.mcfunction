# Enchanting Tower Building
# Multi-level enchanting setup with max bookshelves
title @a title {"text":"Enchanting Tower!","color":"light_purple","bold":true}
tellraw @a [{"text":"[Build]","color":"light_purple","bold":true},{"text":" A mystical enchanting tower has appeared. Max-level enchants available!","color":"white"}]
execute at @s run fill ~-4 ~-1 ~-4 ~4 ~6 ~4 minecraft:stone_bricks
execute at @s run fill ~-3 ~ ~-3 ~3 ~5 ~3 minecraft:air
# Enchanting table
execute at @s run setblock ~ ~ ~ minecraft:enchanting_table
# Bookshelves (level 30)
execute at @s run fill ~-2 ~1 ~-3 ~2 ~1 ~-3 minecraft:bookshelf
execute at @s run fill ~-2 ~2 ~-3 ~2 ~2 ~-3 minecraft:bookshelf
execute at @s run fill ~-3 ~1 ~-2 ~-3 ~1 ~2 minecraft:bookshelf
execute at @s run fill ~-3 ~2 ~-2 ~-3 ~2 ~2 minecraft:bookshelf
execute at @s run fill ~3 ~1 ~-2 ~3 ~1 ~2 minecraft:bookshelf
execute at @s run fill ~3 ~2 ~-2 ~3 ~2 ~2 minecraft:bookshelf
execute at @s run fill ~-2 ~1 ~3 ~2 ~1 ~3 minecraft:bookshelf
execute at @s run fill ~-2 ~2 ~3 ~2 ~2 ~3 minecraft:bookshelf
# Anvil and grindstone
execute at @s run setblock ~2 ~ ~ minecraft:anvil
execute at @s run setblock ~-2 ~ ~ minecraft:grindstone
# Lighting
execute at @s run setblock ~ ~4 ~ minecraft:sea_lantern
execute at @s run setblock ~3 ~2 ~-3 minecraft:end_rod
execute at @s run setblock ~-3 ~2 ~-3 minecraft:end_rod
playsound minecraft:block.enchanting_table.use master @a ~ ~ ~ 1 1.5
