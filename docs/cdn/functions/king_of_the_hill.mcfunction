# King of the Hill Mini-Game
# Creates a hill arena with a control point at the top
title @a title {"text":"King of the Hill!","color":"gold","bold":true}
title @a subtitle {"text":"Control the hilltop to earn points!","color":"red"}
tellraw @a [{"text":"[KotH]","color":"gold","bold":true},{"text":" A hill arena has appeared. Stand on the gold block at the top to score points!","color":"white"}]
execute at @s run fill ~-10 ~-3 ~-10 ~10 ~10 ~10 minecraft:dirt
execute at @s run fill ~-8 ~-3 ~-8 ~8 ~-3 ~8 minecraft:grass_block
execute at @s run fill ~-2 ~-3 ~-2 ~2 ~-3 ~2 minecraft:coarse_dirt
execute at @s run setblock ~ ~11 ~ minecraft:gold_block
execute at @s run fill ~ ~10 ~ ~ ~10 ~ minecraft:glowstone
execute at @s run setblock ~-10 ~-2 ~ minecraft:oak_sign{Text1:'{"text":"Hill"}',Text2:'{"text":"Control"}',Text3:'{"text":"Point"}',Text4:'{"text":"Here!"}'}
execute at @s run fill ~-10 ~-2 ~-10 ~-10 ~2 ~-10 minecraft:ladder
execute at @s run fill ~10 ~-2 ~-10 ~10 ~2 ~-10 minecraft:ladder
execute at @s run fill ~-10 ~-2 ~10 ~-10 ~2 ~10 minecraft:ladder
execute at @s run fill ~10 ~-2 ~10 ~10 ~2 ~10 minecraft:ladder
playsound minecraft:entity.wither.spawn master @a ~ ~ ~ 1 0.5
