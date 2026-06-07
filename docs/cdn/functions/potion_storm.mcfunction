# Potion Storm Chaos Event
# Random potion effects rain down on everyone
title @a title {"text":"Potion Storm!","color":"light_purple","bold":true}
title @a subtitle {"text":"Random potions rain from above!","color":"dark_purple"}
tellraw @a [{"text":"[Chaos]","color":"light_purple","bold":true},{"text":" A potion storm is brewing! Random effects incoming!","color":"white"}]
execute at @s run summon lingering_potion ~5 ~15 ~ {Potion:"minecraft:long_swiftness"}
execute at @s run summon lingering_potion ~-5 ~18 ~ {Potion:"minecraft:strong_strength"}
execute at @s run summon lingering_potion ~ ~16 ~5 {Potion:"minecraft:long_invisibility"}
execute at @s run summon lingering_potion ~-8 ~20 ~-3 {Potion:"minecraft:strong_poison"}
execute at @s run summon lingering_potion ~8 ~14 ~ {Potion:"minecraft:long_slow_falling"}
execute at @s run summon lingering_potion ~-3 ~17 ~-8 {Potion:"minecraft:strong_healing"}
execute at @s run summon lingering_potion ~3 ~19 ~8 {Potion:"minecraft:long_leaping"}
execute at @s run particle minecraft:effect ~ ~15 ~ 10 10 10 0.1 80
execute at @s run particle minecraft:dragon_breath ~ ~10 ~ 12 15 12 0.05 60
playsound minecraft:entity.witch.throw master @a ~ ~ ~ 1 1
