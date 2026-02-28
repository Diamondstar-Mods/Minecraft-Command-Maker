# Starter Kit - Everything a new player needs to get going!
# Gives armor, tools, food, and basic supplies.

# --- Announce ---
tellraw @s [{"text":"[Starter Kit] ","color":"green","bold":true},{"text":"Welcome! Here's everything you need to get started.","color":"white"}]

# --- Iron Armor Set ---
give @s minecraft:iron_helmet 1
give @s minecraft:iron_chestplate 1
give @s minecraft:iron_leggings 1
give @s minecraft:iron_boots 1

# --- Iron Tools ---
give @s minecraft:iron_sword 1
give @s minecraft:iron_pickaxe 1
give @s minecraft:iron_axe 1
give @s minecraft:iron_shovel 1

# --- Food ---
give @s minecraft:cooked_beef 32
give @s minecraft:bread 16
give @s minecraft:golden_apple 2

# --- Basic Supplies ---
give @s minecraft:torch 64
give @s minecraft:oak_planks 64
give @s minecraft:cobblestone 64
give @s minecraft:crafting_table 1
give @s minecraft:furnace 1
give @s minecraft:chest 2
give @s minecraft:white_bed 1
give @s minecraft:shield 1
give @s minecraft:bow 1
give @s minecraft:arrow 32
give @s minecraft:bucket 1
give @s minecraft:compass 1

# --- Bonus: a little XP to get started ---
experience add @s 100 points

# --- Welcome effects ---
playsound minecraft:entity.player.levelup master @s ~ ~ ~ 1 1
particle minecraft:happy_villager ~ ~1 ~ 1 1 1 0.1 30

tellraw @s [{"text":"[Starter Kit] ","color":"green","bold":true},{"text":"You're all set! Go explore!","color":"yellow"}]
