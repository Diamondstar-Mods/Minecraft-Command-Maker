# Castle Gate Building
# Constructs a grand castle entrance
title @a title {"text":"Castle Gate Built!","color":"gray","bold":true}
tellraw @a [{"text":"[Build]","color":"gray","bold":true},{"text":" A grand castle gate has risen from the ground!","color":"white"}]
execute at @s run fill ~-8 ~-1 ~-4 ~8 ~12 ~4 minecraft:stone_bricks
execute at @s run fill ~-7 ~ ~-3 ~7 ~10 ~3 minecraft:air
# Towers
execute at @s run fill ~-6 ~-1 ~-3 ~-8 ~8 ~-3 minecraft:stone_bricks
execute at @s run fill ~6 ~-1 ~-3 ~8 ~8 ~-3 minecraft:stone_bricks
execute at @s run fill ~-6 ~-1 ~3 ~-8 ~8 ~3 minecraft:stone_bricks
execute at @s run fill ~6 ~-1 ~3 ~8 ~8 ~3 minecraft:stone_bricks
# Gate
execute at @s run fill ~-3 ~ ~-1 ~3 ~5 ~1 minecraft:dark_oak_fence
execute at @s run setblock ~-3 ~6 ~ minecraft:dark_oak_fence_gate
execute at @s run setblock ~3 ~6 ~ minecraft:dark_oak_fence_gate
# Portcullis
execute at @s run setblock ~-3 ~1 ~ minecraft:iron_bars
execute at @s run setblock ~-2 ~6 ~ minecraft:iron_bars
execute at @s run setblock ~3 ~1 ~ minecraft:iron_bars
execute at @s run setblock ~2 ~6 ~ minecraft:iron_bars
# Decorations
execute at @s run setblock ~-9 ~3 ~ minecraft:lantern
execute at @s run setblock ~9 ~3 ~ minecraft:lantern
execute at @s run setblock ~-9 ~3 ~-3 minecraft:lantern
execute at @s run setblock ~9 ~3 ~3 minecraft:lantern
playsound minecraft:block.anvil.place master @a ~ ~ ~ 1 1
