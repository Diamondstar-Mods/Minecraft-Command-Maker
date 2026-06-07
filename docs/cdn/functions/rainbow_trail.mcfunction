# Rainbow Trail Visual Effect
# Leaves a rainbow particle trail behind the player
title @a title {"text":"Rainbow Trail!","color":"gold","bold":true}
title @a subtitle {"text":"Leave a rainbow wherever you go!","color":"light_purple"}
tellraw @a [{"text":"[Rainbow]","color":"gold","bold":true},{"text":" A rainbow trail follows you! Run around and paint the world.","color":"white"}]
execute at @s run particle minecraft:dust 1 0 0 1 ~ ~1 ~ 0.2 0.2 0.2 0.1 10
execute at @s run particle minecraft:dust 1 0.5 0 1 ~ ~0.5 ~ 0.2 0.2 0.2 0.1 10
execute at @s run particle minecraft:dust 1 1 0 1 ~ ~ ~ 0.2 0.2 0.2 0.1 10
execute at @s run particle minecraft:dust 0 1 0 1 ~ ~0.3 ~ 0.2 0.2 0.2 0.1 10
execute at @s run particle minecraft:dust 0 0 1 1 ~ ~0.1 ~ 0.2 0.2 0.2 0.1 10
execute at @s run particle minecraft:dust 0.3 0 1 1 ~ ~0.7 ~ 0.2 0.2 0.2 0.1 10
effect give @a speed 600 1 true
playsound minecraft:block.amethyst_block.chime master @a ~ ~ ~ 0.3 2
