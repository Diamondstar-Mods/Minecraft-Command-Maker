# RPG Shop Adventure System
# Creates a trader shop with categorized items
title @a title {"text":"RPG Shop!","color":"gold","bold":true}
title @a subtitle {"text":"Rare items available for trade!","color":"green"}
tellraw @a [{"text":"[RPG]","color":"gold","bold":true},{"text":" An adventurer's shop has opened! Trade your emeralds for rare gear.","color":"white"}]
execute at @s run fill ~-5 ~-1 ~-5 ~5 ~4 ~5 minecraft:stripped_oak_wood
execute at @s run fill ~-4 ~ ~-4 ~4 ~3 ~4 minecraft:air
# Shopkeeper NPCs
execute at @s run summon villager ~-2 ~ ~-2 {CustomName:'{"text":"Weaponsmith"}',VillagerData:{profession:"minecraft:weaponsmith",level:5,type:"minecraft:plains"},Offers:{Recipes:[{buy:{id:"minecraft:emerald",Count:5},sell:{id:"minecraft:diamond_sword",Count:1,tag:{Enchantments:[{id:"sharpness",lvl:3}]}}}]},PersistenceRequired:1b}
execute at @s run summon villager ~2 ~ ~-2 {CustomName:'{"text":"Armorer"}',VillagerData:{profession:"minecraft:armorer",level:5,type:"minecraft:plains"},PersistenceRequired:1b}
execute at @s run summon villager ~ ~ ~2 {CustomName:'{"text":"Potion Brewer"}',VillagerData:{profession:"minecraft:cleric",level:5,type:"minecraft:plains"},PersistenceRequired:1b}
# Shop counter
execute at @s run setblock ~-3 ~ ~ minecraft:barrel
execute at @s run setblock ~3 ~ ~ minecraft:barrel
execute at @s run setblock ~ ~ ~ minecraft:barrel
# Decor
execute at @s run setblock ~-5 ~2 ~ minecraft:lantern
execute at @s run setblock ~5 ~2 ~ minecraft:lantern
execute at @s run setblock ~ ~4 ~ minecraft:glowstone
playsound minecraft:entity.villager.trade master @a ~ ~ ~ 1 1
