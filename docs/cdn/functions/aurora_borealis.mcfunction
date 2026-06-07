# Aurora Borealis Visual Effect
# Stunning northern lights in the night sky
title @a title {"text":"Aurora Borealis!","color":"green","bold":true}
title @a subtitle {"text":"The northern lights dance across the sky!","color":"aqua"}
tellraw @a [{"text":"[Aurora]","color":"green","bold":true},{"text":" The northern lights have appeared! Look up to see the show.","color":"white"}]
time set midnight
weather clear
# Green ribbons
execute at @s run particle minecraft:end_rod ~15 ~30 ~ ~10 ~10 ~10 0.01 80
execute at @s run particle minecraft:end_rod ~-15 ~25 ~ ~10 ~10 ~10 0.01 80
# Blue ribbons
execute at @s run particle minecraft:portal ~ ~35 ~-10 ~15 ~5 ~15 0.01 60
execute at @s run particle minecraft:portal ~5 ~28 ~15 ~10 ~8 ~10 0.01 60
# Purple ribbons
execute at @s run particle minecraft:dragon_breath ~-10 ~32 ~5 ~20 ~5 ~10 0.01 50
execute at @s run particle minecraft:dragon_breath ~10 ~27 ~-5 ~12 ~8 ~10 0.01 50
# Sparkles
execute at @s run particle minecraft:firework ~-5 ~33 ~-15 ~25 ~2 ~25 0.05 40
execute at @s run particle minecraft:firework ~12 ~29 ~8 ~20 ~2 ~20 0.05 40
playsound minecraft:block.beacon.ambient master @a ~ ~ ~ 0.5 1.5
playsound minecraft:block.amethyst_block.chime master @a ~ ~ ~ 0.3 2
