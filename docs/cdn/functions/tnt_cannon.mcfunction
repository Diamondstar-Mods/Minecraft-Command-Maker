# TNT Cannon Redstone Build
# Builds a directional TNT cannon
title @a title {"text":"TNT Cannon!","color":"red","bold":true}
title @a subtitle {"text":"Aim and fire!","color":"dark_red"}
tellraw @a [{"text":"[Redstone]","color":"red","bold":true},{"text":" A TNT cannon has been built! Right-click the button to fire.","color":"white"}]
execute at @s run fill ~-1 ~-1 ~ ~1 ~-1 ~4 minecraft:obsidian
execute at @s run fill ~ ~-1 ~1 ~ ~-1 ~3 minecraft:air
execute at @s run fill ~-1 ~ ~-2 ~1 ~ ~ minecraft:stone_bricks
execute at @s run setblock ~ ~-1 ~3 minecraft:water
execute at @s run setblock ~ ~-1 ~4 minecraft:stone_button[face=floor]
execute at @s run setblock ~ ~-2 ~4 minecraft:redstone_block
execute at @s run setblock ~ ~-1 ~2 minecraft:dispenser[facing=north]
execute at @s run setblock ~ ~-2 ~2 minecraft:redstone_dust
execute at @s run setblock ~ ~-1 ~1 minecraft:repeater[facing=south,delay=4]
execute at @s run setblock ~ ~-2 ~1 minecraft:redstone_dust
playsound minecraft:entity.tnt.primed master @a ~ ~ ~ 1 1
