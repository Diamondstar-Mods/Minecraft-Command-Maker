# Death Swap Mini-Game
# Two players swap positions and must trap each other
title @a title {"text":"Death Swap!","color":"dark_purple","bold":true}
title @a subtitle {"text":"Trap your opponent before they trap you!","color":"light_purple"}
tellraw @a [{"text":"[D.Swap]","color":"dark_purple","bold":true},{"text":" Death Swap has begun! You have 15 seconds before the first swap.","color":"white"}]
execute at @a[sort=nearest,limit=1] run setblock ~ ~ ~ minecraft:barrier
execute at @a[sort=furthest,limit=1] run setblock ~ ~ ~ minecraft:barrier
execute at @a run setblock ~ ~-1 ~ minecraft:obsidian
execute at @a run effect give @a glowing 30 0 true
execute at @p[sort=nearest,limit=1] run give @p minecraft:lava_bucket 2
execute at @p[sort=furthest,limit=1] run give @p minecraft:cobweb 8
give @a minecraft:diamond_sword{Damage:0} 1
give @a minecraft:shield 1
playsound minecraft:entity.enderman.teleport master @a ~ ~ ~ 1 1
