# Archery Range Mini-Game
# Creates a target range with scoring
title @a title {"text":"Archery Range","color":"red","bold":true}
title @a subtitle {"text":"Hit the targets for points!","color":"gold"}
tellraw @a [{"text":"[Archery]","color":"red","bold":true},{"text":" An archery range has spawned. Hit the bullseye for max points!","color":"white"}]
execute at @s run fill ~-10 ~ ~15 ~10 ~4 ~25 minecraft:oak_planks
execute at @s run fill ~-10 ~5 ~15 ~10 ~5 ~25 minecraft:oak_fence
execute at @s run setblock ~-5 ~1 ~20 minecraft:target
execute at @s run setblock ~ ~1 ~22 minecraft:target
execute at @s run setblock ~5 ~1 ~20 minecraft:target
execute at @s run setblock ~ ~1 ~18 minecraft:target
execute at @s run setblock ~-3 ~1 ~24 minecraft:target
execute at @s run setblock ~3 ~1 ~24 minecraft:target
execute at @s run give @p minecraft:bow{Damage:0} 1
execute at @s run give @p minecraft:arrow 64
execute at @s run setblock ~ ~-1 ~ minecraft:barrel
playsound minecraft:entity.arrow.shoot master @a ~ ~ ~ 1 1
