# Skeleton Siege Combat Event
# Waves of skeletons attack a prepared defensive position
title @a title {"text":"Skeleton Siege!","color":"gray","bold":true}
title @a subtitle {"text":"Defend against waves of skeletons!","color":"white"}
tellraw @a [{"text":"[Siege]","color":"gray","bold":true},{"text":" Skeletons are attacking! 5 waves incoming. Hold the fort!","color":"white"}]
execute at @s run fill ~-10 ~-1 ~-10 ~10 ~4 ~10 minecraft:cobblestone hollow
execute at @s run fill ~-9 ~ ~-9 ~9 ~2 ~9 minecraft:air
execute at @s run setblock ~ ~ ~ minecraft:iron_block
# Wave 1: Basic skeletons
execute at @s run summon skeleton ~5 ~ ~5 {HandItems:[{id:"minecraft:bow",Count:1b}]}
execute at @s run summon skeleton ~-5 ~ ~5 {HandItems:[{id:"minecraft:bow",Count:1b}]}
execute at @s run summon skeleton ~5 ~ ~-5 {HandItems:[{id:"minecraft:bow",Count:1b}]}
# Wave 2: Armored skeletons
execute at @s run summon skeleton ~7 ~ ~ {ArmorItems:[{},{},{id:"minecraft:chainmail_chestplate",Count:1b},{}]}
execute at @s run summon skeleton ~-7 ~ ~ {ArmorItems:[{},{},{id:"minecraft:chainmail_chestplate",Count:1b},{}]}
execute at @s run summon skeleton ~ ~ ~7 {ArmorItems:[{},{},{id:"minecraft:chainmail_chestplate",Count:1b},{}]}
give @a[sort=nearest,limit=5] minecraft:bow{Damage:0} 1
give @a[sort=nearest,limit=5] minecraft:arrow 64
effect give @a[sort=nearest,limit=5] resistance 300 0 true
playsound minecraft:entity.skeleton.ambient master @a ~ ~ ~ 1 0.5
