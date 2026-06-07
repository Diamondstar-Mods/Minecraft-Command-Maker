# Wave Defense Combat Event
# Progressive waves of mobs defend a point
title @a title {"text":"Wave Defense!","color":"red","bold":true}
title @a subtitle {"text":"Survive 8 waves of increasing difficulty!","color":"dark_red"}
tellraw @a [{"text":"[Defense]","color":"red","bold":true},{"text":" Defend the beacon! Waves will increase in difficulty.","color":"white"}]
execute at @s run fill ~-8 ~-1 ~-8 ~8 ~4 ~8 minecraft:cobblestone hollow
execute at @s run setblock ~ ~ ~ minecraft:beacon
# Wave indicators
execute at @s run setblock ~2 ~2 ~ minecraft:redstone_lamp
execute at @s run setblock ~-2 ~2 ~ minecraft:redstone_lamp
execute at @s run setblock ~ ~2 ~2 minecraft:redstone_lamp
execute at @s run setblock ~ ~2 ~-2 minecraft:redstone_lamp
# Initial wave spawns
execute at @s run summon zombie ~8 ~ ~ {PersistenceRequired:1b}
execute at @s run summon zombie ~-8 ~ ~ {PersistenceRequired:1b}
execute at @s run summon spider ~ ~ ~8 {PersistenceRequired:1b}
execute at @s run summon skeleton ~4 ~ ~6 {PersistenceRequired:1b}
execute at @s run summon skeleton ~-4 ~ ~-6 {PersistenceRequired:1b}
give @a[sort=nearest,limit=5] minecraft:iron_sword{Damage:0} 1
give @a[sort=nearest,limit=5] minecraft:shield 1
give @a[sort=nearest,limit=5] minecraft:cooked_beef 16
effect give @a[sort=nearest,limit=5] resistance 120 0 true
playsound minecraft:block.note_block.pling master @a ~ ~ ~ 1 2
