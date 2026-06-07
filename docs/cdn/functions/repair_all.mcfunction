# Repair All Utility
# Repairs all items in players' inventories
title @a title {"text":"Repair All!","color":"aqua","bold":true}
title @a subtitle {"text":"All items have been repaired!","color":"green"}
tellraw @a [{"text":"[Repair]","color":"aqua","bold":true},{"text":" All damaged items in your inventory have been repaired to full durability!","color":"white"}]
execute at @s run setblock ~ ~ ~ minecraft:anvil
execute at @s run setblock ~ ~ ~ minecraft:air
effect give @a regeneration 5 0 true
playsound minecraft:block.anvil.use master @a ~ ~ ~ 1 1.5
