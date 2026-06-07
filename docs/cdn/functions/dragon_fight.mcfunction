# Dragon Fight Event
# Summons the Ender Dragon in the overworld for an epic battle
title @a title {"text":"Dragon Fight!","color":"dark_purple","bold":true}
title @a subtitle {"text":"The Ender Dragon has been summoned!","color":"purple"}
tellraw @a [{"text":"[Dragon]","color":"dark_purple","bold":true},{"text":" An Ender Dragon has appeared in the overworld! Prepare for battle!","color":"white"}]
execute at @s run fill ~-20 ~-5 ~-20 ~20 ~15 ~20 minecraft:end_stone hollow
execute at @s run fill ~-5 ~-1 ~-5 ~5 ~-1 ~5 minecraft:obsidian
execute at @s run setblock ~ ~ ~ minecraft:end_crystal
execute at @s run setblock ~3 ~ ~ minecraft:end_crystal
execute at @s run setblock ~-3 ~ ~ minecraft:end_crystal
execute at @s run setblock ~ ~ ~3 minecraft:end_crystal
execute at @s run summon ender_dragon ~ ~5 ~
effect give @a strength 600 1 true
effect give @a slow_falling 600 0 true
give @a minecraft:diamond_sword{Damage:0,Enchantments:[{id:"sharpness",lvl:3}]} 1
give @a minecraft:bow{Damage:0,Enchantments:[{id:"power",lvl:3}]} 1
give @a minecraft:arrow 64
playsound minecraft:entity.ender_dragon.growl master @a ~ ~ ~ 1 1
