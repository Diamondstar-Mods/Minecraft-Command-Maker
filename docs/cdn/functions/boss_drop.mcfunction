# Boss Drop Adventure System
# Summons a custom boss that drops rare loot
title @a title {"text":"Boss Summoned!","color":"dark_red","bold":true}
title @a subtitle {"text":"Defeat the boss for epic loot!","color":"gold"}
tellraw @a [{"text":"[RPG]","color":"dark_red","bold":true},{"text":" A powerful boss has appeared! Defeat it to claim the rare loot.","color":"white"}]
execute at @s run fill ~-5 ~-1 ~-5 ~5 ~5 ~5 minecraft:obsidian hollow
execute at @s run setblock ~ ~ ~ minecraft:beacon
execute at @s run setblock ~ ~-1 ~ minecraft:netherite_block
# Boss: Supercharged zombie with full diamond and axe
execute at @s run summon zombie ~ ~1 ~ {CustomName:'{"text":"Loot Guardian","color":"gold","bold":true}',HandItems:[{id:"minecraft:diamond_axe",Count:1b}],ArmorItems:[{id:"minecraft:diamond_boots",Count:1b},{id:"minecraft:diamond_leggings",Count:1b},{id:"minecraft:diamond_chestplate",Count:1b},{id:"minecraft:diamond_helmet",Count:1b}],Attributes:[{Name:"generic.max_health",Base:100},{Name:"generic.attack_damage",Base:12},{Name:"generic.movement_speed",Base:0.3}],PersistenceRequired:1b}
# Reward chest (place in center)
execute at @s run setblock ~ ~ ~ minecraft:chest{Items:[{id:"minecraft:diamond",Count:5},{id:"minecraft:netherite_scrap",Count:2},{id:"minecraft:enchanted_golden_apple",Count:1},{id:"minecraft:totem_of_undying",Count:1}]}
give @a minecraft:diamond_sword{Damage:0} 1
effect give @a strength 600 0 true
playsound minecraft:entity.wither.spawn master @a ~ ~ ~ 1 0.5
