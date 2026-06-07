# Command Chain Template
# Demonstrates chaining multiple command types together
title @a title {"text":"Command Chain","color":"aqua","bold":true}
tellraw @a [{"text":"[Template]","color":"aqua","bold":true},{"text":" Running a chain of commands: teleport, heal, feed, give items, and announce.","color":"white"}]
# Step 1: Teleport all players to the executor
execute at @s run tp @a ~ ~3 ~
# Step 2: Heal and feed everyone
effect give @a instant_health 1 5 true
effect give @a saturation 1 100 true
# Step 3: Give starter gear
give @a minecraft:iron_sword{Damage:0} 1
give @a minecraft:shield 1
give @a minecraft:bread 16
# Step 4: Set time and weather
time set 1000
weather clear
# Step 5: Announce
tellraw @a [{"text":"[Template]","color":"aqua","bold":true},{"text":" Command chain complete! Everyone has been equipped and healed.","color":"white"}]
playsound minecraft:block.note_block.chime master @a ~ ~ ~ 1 2
