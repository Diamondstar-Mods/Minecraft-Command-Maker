# Night Vision Utility
# Gives all players permanent night vision for building
title @a title {"text":"Night Vision","color":"dark_blue","bold":true}
title @a subtitle {"text":"See clearly in the dark!","color":"aqua"}
tellraw @a [{"text":"[Utility]","color":"dark_blue","bold":true},{"text":" Night vision has been activated for all players. Build safely!","color":"white"}]
effect give @a night_vision 999999 0 true
effect give @a water_breathing 999999 0 true
time set midnight
weather clear
playsound minecraft:entity.witch.drink master @a ~ ~ ~ 1 1.5
