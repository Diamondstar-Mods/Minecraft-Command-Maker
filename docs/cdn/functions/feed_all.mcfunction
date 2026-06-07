# Feed All Utility
# Gives all players max saturation and food
title @a title {"text":"Feast Time!","color":"gold","bold":true}
title @a subtitle {"text":"Everyone has been fed!","color":"green"}
tellraw @a [{"text":"[Feed]","color":"gold","bold":true},{"text":" All players have been fed to max hunger and saturation.","color":"white"}]
effect give @a saturation 1 100 true
give @a minecraft:golden_carrot 16
playsound minecraft:entity.generic.eat master @a ~ ~ ~ 1 1
