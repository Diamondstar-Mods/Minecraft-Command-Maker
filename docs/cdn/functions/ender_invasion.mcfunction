# Ender Invasion Chaos Event
# Endermen teleport everywhere causing chaos
title @a title {"text":"Ender Invasion!","color":"dark_purple","bold":true}
title @a subtitle {"text":"Endermen are invading the overworld!","color":"purple"}
tellraw @a [{"text":"[Chaos]","color":"dark_purple","bold":true},{"text":" Endermen are invading! Don't look them in the eyes!","color":"white"}]
execute at @s run summon enderman ~8 ~ ~ {carriedBlockState:{Name:"minecraft:grass_block"}}
execute at @s run summon enderman ~-8 ~ ~ {carriedBlockState:{Name:"minecraft:dirt"}}
execute at @s run summon enderman ~ ~ ~8 {carriedBlockState:{Name:"minecraft:sand"}}
execute at @s run summon enderman ~ ~ ~-8 {carriedBlockState:{Name:"minecraft:gravel"}}
execute at @s run summon enderman ~5 ~ ~5 {}
execute at @s run summon enderman ~-5 ~ ~-5 {}
execute at @s run summon enderman ~3 ~ ~-7 {}
execute at @s run summon enderman ~-3 ~ ~7 {}
execute at @s run particle minecraft:portal ~ ~2 ~ 10 5 10 0.1 100
execute at @s run particle minecraft:portal ~ ~10 ~ 8 3 8 0.1 60
effect give @a night_vision 300 0 true
playsound minecraft:entity.enderman.scream master @a ~ ~ ~ 1 0.5
playsound minecraft:entity.enderman.teleport master @a ~ ~ ~ 1 1
