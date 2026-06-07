# Heal All Utility
# Fully heals all players and clears negative effects
title @a title {"text":"Heal All!","color":"red","bold":true}
title @a subtitle {"text":"Everyone has been healed!","color":"green"}
tellraw @a [{"text":"[Heal]","color":"red","bold":true},{"text":" All players have been fully healed and cured of negative effects.","color":"white"}]
effect give @a instant_health 1 5 true
effect give @a regeneration 10 5 true
effect give @a saturation 1 10 true
effect clear @a minecraft:poison
effect clear @a minecraft:wither
effect clear @a minecraft:slowness
effect clear @a minecraft:weakness
effect clear @a minecraft:blindness
effect clear @a minecraft:nausea
playsound minecraft:entity.player.levelup master @a ~ ~ ~ 1 1.5
