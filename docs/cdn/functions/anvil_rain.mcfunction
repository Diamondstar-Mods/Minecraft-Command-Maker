# Anvil Rain Chaos Event
# Rains anvils from the sky
title @a title {"text":"Anvil Rain!","color":"dark_gray","bold":true}
title @a subtitle {"text":"Watch your head!","color":"red"}
tellraw @a [{"text":"[Chaos]","color":"dark_gray","bold":true},{"text":" It's raining anvils! Take cover immediately!","color":"white"}]
execute at @s run summon falling_block ~5 ~20 ~ {BlockState:{Name:"minecraft:anvil"},Time:1,DropItem:0b}
execute at @s run summon falling_block ~-5 ~22 ~ {BlockState:{Name:"minecraft:anvil"},Time:1,DropItem:0b}
execute at @s run summon falling_block ~ ~18 ~5 {BlockState:{Name:"minecraft:anvil"},Time:1,DropItem:0b}
execute at @s run summon falling_block ~-3 ~25 ~-8 {BlockState:{Name:"minecraft:anvil"},Time:1,DropItem:0b}
execute at @s run summon falling_block ~8 ~20 ~-3 {BlockState:{Name:"minecraft:anvil"},Time:1,DropItem:0b}
execute at @s run summon falling_block ~-8 ~23 ~3 {BlockState:{Name:"minecraft:anvil"},Time:1,DropItem:0b}
execute at @s run summon falling_block ~3 ~19 ~-5 {BlockState:{Name:"minecraft:chipped_anvil"},Time:1,DropItem:0b}
execute at @s run summon falling_block ~2 ~21 ~8 {BlockState:{Name:"minecraft:damaged_anvil"},Time:1,DropItem:0b}
effect give @a resistance 10 4 true
playsound minecraft:block.anvil.land master @a ~ ~ ~ 1 0.3
