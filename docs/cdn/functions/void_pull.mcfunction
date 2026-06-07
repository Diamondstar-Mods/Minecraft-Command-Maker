# Void Pull Chaos Event
# Creates a void-like vortex that pulls players
title @a title {"text":"Void Pull!","color":"black","bold":true}
title @a subtitle {"text":"Something is pulling you into the void!","color":"dark_purple"}
tellraw @a [{"text":"[Chaos]","color":"black","bold":true},{"text":" A void vortex has opened! You feel yourself being pulled in...","color":"white"}]
execute at @s run fill ~-3 ~-3 ~-3 ~3 ~-1 ~3 minecraft:black_concrete
execute at @s run fill ~-2 ~-2 ~-2 ~2 ~-1 ~2 minecraft:air
execute at @s run setblock ~ ~-2 ~ minecraft:end_portal
execute at @s run particle minecraft:portal ~ ~2 ~ 5 10 5 0.1 80
execute at @s run particle minecraft:portal ~ ~5 ~ 3 8 3 0.05 60
execute at @s run particle minecraft:reverse_portal ~ ~-1 ~ 6 3 6 0.1 50
execute at @s run particle minecraft:dragon_breath ~ ~1 ~ 4 5 4 0.05 30
execute at @s run tp @a[distance=..5] ~ ~-1 ~
effect give @a[distance=..10] blindness 5 0 true
effect give @a[distance=..10] slowness 5 2 true
playsound minecraft:block.end_portal.spawn master @a ~ ~ ~ 1 0.3
playsound minecraft:entity.enderman.scream master @a ~ ~ ~ 0.5 0.5
