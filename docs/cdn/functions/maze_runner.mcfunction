# Maze Runner Mini-Game
# Builds a hedge maze with hidden rewards
title @a title {"text":"Maze Runner","color":"green","bold":true}
title @a subtitle {"text":"Find the hidden treasure in the maze!","color":"gold"}
tellraw @a [{"text":"[Maze]","color":"green","bold":true},{"text":" A hedge maze has appeared! Find the diamond block at the center for your reward.","color":"white"}]
execute at @s run fill ~-15 ~ ~-15 ~15 ~5 ~15 oak_leaves[persistent=true] hollow
execute at @s run fill ~-14 ~ ~-14 ~14 ~4 ~14 air replace oak_leaves
execute at @s run setblock ~ ~ ~ minecraft:oak_planks
execute at @s run fill ~-13 ~1 ~-13 ~13 ~1 ~13 minecraft:stone_bricks
execute at @s run setblock ~ ~5 ~ minecraft:glowstone
execute at @s run setblock ~ ~1 ~ minecraft:diamond_block
execute at @s run give @p minecraft:golden_apple 3
playsound minecraft:block.bell.use master @a ~ ~ ~ 1 1
