# Treasure Hunt Adventure Event
# Buries treasure chests around the area
title @a title {"text":"Treasure Hunt!","color":"gold","bold":true}
title @a subtitle {"text":"X marks the spot! Find the buried treasure!","color":"yellow"}
tellraw @a [{"text":"[RPG]","color":"gold","bold":true},{"text":" Treasure has been buried nearby! Follow the clues to find it.","color":"white"}]
# Treasure 1
execute at @s run setblock ~10 ~-1 ~5 minecraft:chest{Items:[{id:"minecraft:diamond",Count:3},{id:"minecraft:emerald",Count:5},{id:"minecraft:golden_apple",Count:2}]}
execute at @s run setblock ~10 ~-2 ~5 minecraft:sand
execute at @s run setblock ~10 ~-1 ~5 minecraft:red_sand
# Treasure 2
execute at @s run setblock ~-8 ~-1 ~-5 minecraft:chest{Items:[{id:"minecraft:gold_ingot",Count:8},{id:"minecraft:iron_ingot",Count:16},{id:"minecraft:enchanted_book",Count:1}]}
execute at @s run setblock ~-8 ~-2 ~-5 minecraft:gravel
# Treasure 3
execute at @s run setblock ~2 ~30 ~8 minecraft:chest{Items:[{id:"minecraft:elytra",Count:1},{id:"minecraft:firework_rocket",Count:16}]}
# Clue markers
execute at @s run setblock ~10 ~ ~5 minecraft:red_banner
execute at @s run setblock ~-8 ~ ~-5 minecraft:red_banner
execute at @s run setblock ~2 ~31 ~8 minecraft:red_banner
give @a minecraft:compass 1
give @a minecraft:iron_shovel{Damage:0} 1
playsound minecraft:entity.player.levelup master @a ~ ~ ~ 1 2
