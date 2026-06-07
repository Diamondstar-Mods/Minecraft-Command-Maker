# Chicken Plague Chaos Event
# Floods the area with chickens and eggs
title @a title {"text":"Chicken Plague!","color":"white","bold":true}
title @a subtitle {"text":"The chickens have taken over!","color":"yellow"}
tellraw @a [{"text":"[Chaos]","color":"white","bold":true},{"text":" A chicken plague has broken out! They're everywhere!","color":"white"}]
execute at @s run summon chicken ~3 ~ ~3 {Age:-99999999}
execute at @s run summon chicken ~-3 ~ ~3 {Age:-99999999}
execute at @s run summon chicken ~3 ~ ~-3 {Age:-99999999}
execute at @s run summon chicken ~-3 ~ ~-3 {Age:-99999999}
execute at @s run summon chicken ~5 ~ ~ {Age:-99999999}
execute at @s run summon chicken ~-5 ~ ~ {Age:-99999999}
execute at @s run summon chicken ~ ~ ~5 {Age:-99999999}
execute at @s run summon chicken ~ ~ ~-5 {Age:-99999999}
execute at @s run summon chicken ~2 ~2 ~2 {Age:-99999999}
execute at @s run summon chicken ~-2 ~2 ~-2 {Age:-99999999}
execute at @s run particle minecraft:item_slime ~ ~1 ~ 5 3 5 0.1 50
playsound minecraft:entity.chicken.ambient master @a ~ ~ ~ 1 1
playsound minecraft:entity.chicken.egg master @a ~ ~ ~ 1 0.5
