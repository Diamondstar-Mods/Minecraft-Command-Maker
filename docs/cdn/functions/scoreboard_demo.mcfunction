# Scoreboard Demo Template
# Demonstrates scoreboard usage with objectives and display
title @a title {"text":"Scoreboard Demo","color":"gold","bold":true}
tellraw @a [{"text":"[Template]","color":"gold","bold":true},{"text":" Scoreboard demonstration has been set up!","color":"white"}]
scoreboard objectives add kills playerKillCount "Kills"
scoreboard objectives add deaths deathCount "Deaths"
scoreboard objectives add score dummy "Score"
scoreboard objectives setdisplay sidebar kills
scoreboard objectives setdisplay list deaths
tellraw @a [{"text":"[Template]","color":"gold","bold":true},{"text":" Objectives created: kills, deaths, score. Check sidebar and player list.","color":"white"}]
give @a minecraft:diamond_sword{Damage:0} 1
playsound minecraft:entity.player.levelup master @a ~ ~ ~ 1 1
