# Fire Tornado Visual Effect
# Creates a dramatic fire tornado
title @a title {"text":"Fire Tornado!","color":"gold","bold":true}
title @a subtitle {"text":"A vortex of flames erupts!","color":"red"}
tellraw @a [{"text":"[Chaos]","color":"gold","bold":true},{"text":" A massive fire tornado has appeared! Stand clear!","color":"white"}]
execute at @s run particle minecraft:flame ~ ~1 ~ 2 15 2 0.05 100
execute at @s run particle minecraft:flame ~ ~8 ~ 1 10 1 0.05 80
execute at @s run particle minecraft:flame ~ ~15 ~ 0.5 5 0.5 0.05 50
execute at @s run particle minecraft:lava ~ ~1 ~ 1 8 1 0.1 30
execute at @s run particle minecraft:soul_fire_flame ~ ~12 ~ 0.5 6 0.5 0.05 20
execute at @s run particle minecraft:large_smoke ~ ~1 ~ 3 18 3 0.02 40
execute at @s run particle minecraft:campfire_cosy_smoke ~ ~-1 ~ 1 1 1 0.1 20
playsound minecraft:entity.blaze.shoot master @a ~ ~ ~ 1 0.5
playsound minecraft:block.fire.ambient master @a ~ ~ ~ 1 0.3
