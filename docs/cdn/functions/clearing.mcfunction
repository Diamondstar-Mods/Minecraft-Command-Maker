# Clearing Utility
# Clears weather, sets time, and cleans up mobs
title @a title {"text":"World Cleared!","color":"green","bold":true}
title @a subtitle {"text":"Weather and time have been reset.","color":"yellow"}
tellraw @a [{"text":"[Utility]","color":"green","bold":true},{"text":" Weather cleared, time set to day, hostile mobs removed.","color":"white"}]
weather clear
time set 1000
execute at @s run kill @e[type=minecraft:creeper,distance=..50]
execute at @s run kill @e[type=minecraft:skeleton,distance=..50]
execute at @s run kill @e[type=minecraft:zombie,distance=..50]
execute at @s run kill @e[type=minecraft:spider,distance=..50]
execute at @s run kill @e[type=minecraft:enderman,distance=..50]
execute at @s run kill @e[type=minecraft:witch,distance=..50]
execute at @s run kill @e[type=minecraft:slime,distance=..50]
execute at @s run kill @e[type=minecraft:phantom,distance=..50]
execute at @s run kill @e[type=minecraft:drowned,distance=..50]
execute at @s run kill @e[type=minecraft:husk,distance=..50]
execute at @s run kill @e[type=minecraft:stray,distance=..50]
playsound minecraft:entity.player.levelup master @a ~ ~ ~ 1 2
