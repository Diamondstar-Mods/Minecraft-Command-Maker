# Trivia Challenge Mini-Game
# Sets up a trivia area with question boards and reward system
title @a title {"text":"Trivia Challenge!","color":"yellow","bold":true}
title @a subtitle {"text":"Answer the questions to win prizes!","color":"gold"}
tellraw @a [{"text":"[Trivia]","color":"yellow","bold":true},{"text":" A trivia booth has appeared! Answer correctly on the signs to win.","color":"white"}]
execute at @s run fill ~-5 ~ ~-5 ~5 ~4 ~5 minecraft:oak_planks hollow
execute at @s run fill ~-4 ~1 ~-4 ~4 ~3 ~4 minecraft:bookshelf
execute at @s run setblock ~-4 ~1 ~ minecraft:oak_sign{Text1:'{"text":"Q1: Capital","bold":true}',Text2:'{"text":"of France?"}',Text3:'{"text":"A) Paris"}',Text4:'{"text":"B) London"}'}
execute at @s run setblock ~ ~1 ~ minecraft:oak_sign{Text1:'{"text":"Q2: How many","bold":true}',Text2:'{"text":"blocks in a","bold":false}',Text3:'{"text":"stack?"}',Text4:'{"text":"A) 64"}'}
execute at @s run setblock ~4 ~1 ~ minecraft:oak_sign{Text1:'{"text":"Q3: Diamond","bold":true}',Text2:'{"text":"pickaxe mines","bold":false}',Text3:'{"text":"what ore?"}',Text4:'{"text":"A) Obsidian"}'}
execute at @s run setblock ~ ~ ~ minecraft:lectern
execute at @s run give @p minecraft:writable_book 1
playsound minecraft:block.note_block.bell master @a ~ ~ ~ 1 2
