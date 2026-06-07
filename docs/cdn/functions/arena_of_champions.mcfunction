# Arena of Champions Combat Event
# Epic arena with varied champions to defeat
title @a title {"text":"Arena of Champions","color":"gold","bold":true}
title @a subtitle {"text":"Defeat the champions to claim glory!","color":"red"}
tellraw @a [{"text":"[Arena]","color":"gold","bold":true},{"text":" The Arena of Champions has opened! Defeat all 3 champions for the grand reward.","color":"white"}]
execute at @s run fill ~-12 ~-1 ~-12 ~12 ~8 ~12 minecraft:stone_bricks hollow
execute at @s run fill ~-11 ~ ~-11 ~11 ~6 ~11 minecraft:air
execute at @s run setblock ~ ~ ~ minecraft:beacon
# Champion 1: Knight
execute at @s run summon zombie ~6 ~ ~ {CustomName:'{"text":"Champion Knight"}',HandItems:[{id:"minecraft:iron_sword",Count:1b},{id:"minecraft:shield",Count:1b}],ArmorItems:[{},{},{id:"minecraft:iron_chestplate",Count:1b},{id:"minecraft:iron_helmet",Count:1b}],PersistenceRequired:1b}
# Champion 2: Mage
execute at @s run summon skeleton ~-6 ~ ~ {CustomName:'{"text":"Champion Mage"}',HandItems:[{id:"minecraft:bow",Count:1b}],ArmorItems:[{},{},{id:"minecraft:golden_chestplate",Count:1b},{id:"minecraft:golden_helmet",Count:1b}],PersistenceRequired:1b}
# Champion 3: Brute
execute at @s run summon vindicator ~ ~ ~6 {CustomName:'{"text":"Champion Brute"}',PersistenceRequired:1b}
give @a minecraft:diamond_sword{Damage:0} 1
give @a minecraft:golden_apple 5
effect give @a regeneration 600 0 true
playsound minecraft:entity.wither.spawn master @a ~ ~ ~ 1 0.5
