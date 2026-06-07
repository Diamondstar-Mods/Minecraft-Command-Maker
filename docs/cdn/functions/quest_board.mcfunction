# Quest Board Adventure System
# Creates a quest hub with notice boards
title @a title {"text":"Quest Board!","color":"gold","bold":true}
title @a subtitle {"text":"New quests are available!","color":"green"}
tellraw @a [{"text":"[RPG]","color":"gold","bold":true},{"text":" A quest board has appeared! Check the signs for available quests.","color":"white"}]
execute at @s run fill ~-4 ~-1 ~-3 ~4 ~4 ~3 minecraft:dark_oak_planks
execute at @s run fill ~-3 ~ ~-2 ~3 ~3 ~2 minecraft:air
# Quest signs
execute at @s run setblock ~-3 ~1 ~ minecraft:oak_sign{Text1:'{"text":"Quest 1:","bold":true}',Text2:'{"text":"Kill 10"}',Text3:'{"text":"Zombies"}',Text4:'{"text":"Reward: 50 XP"}'}
execute at @s run setblock ~-1 ~1 ~ minecraft:oak_sign{Text1:'{"text":"Quest 2:","bold":true}',Text2:'{"text":"Collect 20"}',Text3:'{"text":"Iron Ingots"}',Text4:'{"text":"Reward: 32 XP"}'}
execute at @s run setblock ~1 ~1 ~ minecraft:oak_sign{Text1:'{"text":"Quest 3:","bold":true}',Text2:'{"text":"Explore the"}',Text3:'{"text":"Nether"}',Text4:'{"text":"Reward: 80 XP"}'}
execute at @s run setblock ~3 ~1 ~ minecraft:oak_sign{Text1:'{"text":"Quest 4:","bold":true}',Text2:'{"text":"Build a"}',Text3:'{"text":"Village"}',Text4:'{"text":"Reward: 100 XP"}'}
# Quest giver NPC
execute at @s run summon villager ~ ~1 ~-3 {CustomName:'{"text":"Quest Master"}',VillagerData:{profession:"minecraft:librarian",level:5,type:"minecraft:plains"},PersistenceRequired:1b}
execute at @s run setblock ~ ~-1 ~ minecraft:barrel
playsound minecraft:entity.villager.ambient master @a ~ ~ ~ 1 1
