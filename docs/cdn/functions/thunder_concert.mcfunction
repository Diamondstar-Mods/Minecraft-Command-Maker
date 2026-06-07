# Thunder Concert Visual Effect
# Lightning storm synchronized with music-like effects
title @a title {"text":"Thunder Concert!","color":"yellow","bold":true}
title @a subtitle {"text":"Nature's orchestra performs!","color":"aqua"}
tellraw @a [{"text":"[Concert]","color":"yellow","bold":true},{"text":" A thunder concert has begun! Watch the lightning dance!","color":"white"}]
weather thunder
execute at @s run summon lightning_bolt ~10 ~ ~
execute at @s run summon lightning_bolt ~-10 ~ ~
execute at @s run summon lightning_bolt ~ ~ ~10
execute at @s run summon lightning_bolt ~ ~ ~-10
execute at @s run particle minecraft:electric_spark ~ ~15 ~ 20 10 20 0.1 30
execute at @s run particle minecraft:electric_spark ~8 ~12 ~-8 15 8 15 0.1 30
execute at @s run particle minecraft:flash ~ ~10 ~ 20 8 20 0.05 20
playsound minecraft:entity.lightning_bolt.thunder master @a ~ ~ ~ 1 1
playsound minecraft:entity.lightning_bolt.impact master @a ~ ~ ~ 1 1
playsound minecraft:block.note_block.bass master @a ~ ~ ~ 1 0.5
