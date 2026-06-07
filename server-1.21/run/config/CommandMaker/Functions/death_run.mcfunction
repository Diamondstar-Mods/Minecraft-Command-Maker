# Death Run Mini-Game
# Creates a trapped obstacle course
title @a title {"text":"Death Run!","color":"dark_red","bold":true}
title @a subtitle {"text":"Survive the traps and reach the end!","color":"red"}
tellraw @a [{"text":"[DeathRun]","color":"dark_red","bold":true},{"text":" A deadly obstacle course has been built. Make it to the gold block to win!","color":"white"}]
execute at @s run fill ~ ~-1 ~ ~20 ~-1 ~3 minecraft:red_nether_bricks
execute at @s run setblock ~5 ~ ~ minecraft:tripwire_hook
execute at @s run setblock ~10 ~ ~ minecraft:tripwire_hook
execute at @s run setblock ~7 ~ ~ minecraft:tnt
execute at @s run setblock ~12 ~1 ~ minecraft:lava
execute at @s run setblock ~17 ~ ~ minecraft:soul_sand
execute at @s run setblock ~20 ~ ~3 minecraft:gold_block
execute at @s run setblock ~21 ~1 ~3 minecraft:beacon
give @p minecraft:golden_apple 2
effect give @a speed 60 1 true
playsound minecraft:block.note_block.bass master @a ~ ~ ~ 1 0.5
