# Confetti Storm Visual Effect
# Massive confetti celebration with color bursts
title @a title {"text":"Confetti Storm!","color":"gold","bold":true}
title @a subtitle {"text":"Celebrate with a storm of colorful confetti!","color":"red"}
tellraw @a [{"text":"[Party]","color":"gold","bold":true},{"text":" A confetti storm is sweeping through! Celebrate!","color":"white"}]
execute at @s run particle minecraft:firework ~ ~20 ~ 10 15 10 0.1 60
execute at @s run particle minecraft:firework ~10 ~15 ~ 5 20 5 0.1 60
execute at @s run particle minecraft:firework ~-10 ~18 ~-5 8 12 8 0.1 60
execute at @s run particle minecraft:rainbow ~ ~25 ~ 15 10 15 0.05 80
execute at @s run particle minecraft:rainbow ~-8 ~22 ~8 10 8 10 0.05 80
execute at @s run particle minecraft:spore_blossom_air ~5 ~10 ~-5 20 20 20 0.02 40
execute at @s run particle minecraft:spore_blossom_air ~-5 ~12 ~5 20 18 20 0.02 40
playsound minecraft:block.note_block.chime master @a ~ ~ ~ 1 2
playsound minecraft:block.note_block.bell master @a ~ ~ ~ 0.5 1
playsound minecraft:entity.firework_rocket.launch master @a ~ ~ ~ 1 1.5
