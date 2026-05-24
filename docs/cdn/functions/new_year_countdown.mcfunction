# New Year Countdown - 10-second countdown followed by a massive firework display!
# Perfect for server events, celebrations, and New Year's Eve parties.

# --- Announce ---
title @a title {"text":"HAPPY NEW YEAR!","color":"gold","bold":true}
title @a subtitle {"text":"Starting countdown...","color":"yellow"}
tellraw @a [{"text":"[NYE] ","color":"gold","bold":true},{"text":"The countdown to the New Year begins!","color":"white"}]

# --- Give everyone party effects ---
effect give @a minecraft:night_vision 120 0 true
effect give @a minecraft:luck 120 5 true
effect give @a minecraft:slow_falling 120 0 true

# --- 10! ---
tellraw @a [{"text":"10","color":"green"}]
playsound minecraft:block.note_block.pling master @a ~ ~ ~ 1 0.5

# --- 9! ---
tellraw @a [{"text":"9","color":"green"}]
playsound minecraft:block.note_block.pling master @a ~ ~ ~ 1 0.55

# --- 8! ---
tellraw @a [{"text":"8","color":"yellow"}]
playsound minecraft:block.note_block.pling master @a ~ ~ ~ 1 0.6

# --- 7! ---
tellraw @a [{"text":"7","color":"yellow"}]
playsound minecraft:block.note_block.pling master @a ~ ~ ~ 1 0.65

# --- 6! ---
tellraw @a [{"text":"6","color":"gold"}]
playsound minecraft:block.note_block.pling master @a ~ ~ ~ 1 0.7

# --- 5! ---
tellraw @a [{"text":"5","color":"gold"}]
playsound minecraft:block.note_block.pling master @a ~ ~ ~ 1 0.75

# --- 4! ---
tellraw @a [{"text":"4","color":"red"}]
playsound minecraft:block.note_block.pling master @a ~ ~ ~ 1 0.8

# --- 3! ---
tellraw @a [{"text":"3","color":"red"}]
playsound minecraft:block.note_block.pling master @a ~ ~ ~ 1 0.9

# --- 2! ---
tellraw @a [{"text":"2","color":"dark_red"}]
playsound minecraft:block.note_block.pling master @a ~ ~ ~ 1 1

# --- 1! ---
tellraw @a [{"text":"1","color":"dark_red"}]
playsound minecraft:block.note_block.pling master @a ~ ~ ~ 1 1.2

# --- HAPPY NEW YEAR!!! ---
title @a title {"text":"HAPPY NEW YEAR!","color":"gold","bold":true}
title @a subtitle {"text":"Let the fireworks begin!","color":"yellow"}
playsound minecraft:entity.firework_rocket.large_blast master @a ~ ~ ~ 2 1
playsound minecraft:entity.firework_rocket.twinkle master @a ~ ~ ~ 2 1
playsound minecraft:entity.ender_dragon.growl master @a ~ ~ ~ 0.5 2

# --- Firework Show: Red burst ---
execute at @s run summon minecraft:firework_rocket ~3 ~5 ~0 {LifeTime:20,FireworksItem:{id:"minecraft:firework_rocket",Count:1,tag:{Fireworks:{Explosions:[{Type:1,Colors:[I;11743532],FadeColors:[I;15790320]}]}}}}
execute at @s run summon minecraft:firework_rocket ~-3 ~5 ~0 {LifeTime:20,FireworksItem:{id:"minecraft:firework_rocket",Count:1,tag:{Fireworks:{Explosions:[{Type:1,Colors:[I;11743532],FadeColors:[I;15790320]}]}}}}

# --- Firework Show: Gold starburst ---
execute at @s run summon minecraft:firework_rocket ~0 ~10 ~3 {LifeTime:25,FireworksItem:{id:"minecraft:firework_rocket",Count:1,tag:{Fireworks:{Explosions:[{Type:2,Colors:[I;16766720],FadeColors:[I;16777045]}]}}}}
execute at @s run summon minecraft:firework_rocket ~0 ~10 ~-3 {LifeTime:25,FireworksItem:{id:"minecraft:firework_rocket",Count:1,tag:{Fireworks:{Explosions:[{Type:2,Colors:[I;16766720],FadeColors:[I;16777045]}]}}}}

# --- Firework Show: Green creeper face ---
execute at @s run summon minecraft:firework_rocket ~5 ~7 ~0 {LifeTime:20,FireworksItem:{id:"minecraft:firework_rocket",Count:1,tag:{Fireworks:{Explosions:[{Type:3,Colors:[I;4312372],FadeColors:[I;6192150]}]}}}}
execute at @s run summon minecraft:firework_rocket ~-5 ~7 ~0 {LifeTime:20,FireworksItem:{id:"minecraft:firework_rocket",Count:1,tag:{Fireworks:{Explosions:[{Type:3,Colors:[I;4312372],FadeColors:[I;6192150]}]}}}}

# --- Firework Show: Blue twinkle ---
execute at @s run summon minecraft:firework_rocket ~2 ~12 ~2 {LifeTime:30,FireworksItem:{id:"minecraft:firework_rocket",Count:1,tag:{Fireworks:{Explosions:[{Type:0,Colors:[I;2437522],FadeColors:[I;3949738],Flicker:1b,Trail:1b}]}}}}
execute at @s run summon minecraft:firework_rocket ~-2 ~12 ~-2 {LifeTime:30,FireworksItem:{id:"minecraft:firework_rocket",Count:1,tag:{Fireworks:{Explosions:[{Type:0,Colors:[I;2437522],FadeColors:[I;3949738],Flicker:1b,Trail:1b}]}}}}

# --- Firework Show: Purple burst ---
execute at @s run summon minecraft:firework_rocket ~0 ~8 ~5 {LifeTime:25,FireworksItem:{id:"minecraft:firework_rocket",Count:1,tag:{Fireworks:{Explosions:[{Type:4,Colors:[I;8991416],FadeColors:[I;12801229]}]}}}}
execute at @s run summon minecraft:firework_rocket ~0 ~8 ~-5 {LifeTime:25,FireworksItem:{id:"minecraft:firework_rocket",Count:1,tag:{Fireworks:{Explosions:[{Type:4,Colors:[I;8991416],FadeColors:[I;12801229]}]}}}}

# --- Firework Show: Multicolor sphere finale ---
execute at @s run summon minecraft:firework_rocket ~4 ~15 ~4 {LifeTime:35,FireworksItem:{id:"minecraft:firework_rocket",Count:1,tag:{Fireworks:{Explosions:[{Type:1,Colors:[I;11743532,16766720,4312372],FadeColors:[I;15790320,16777045,6192150],Flicker:1b,Trail:1b}]}}}}
execute at @s run summon minecraft:firework_rocket ~-4 ~15 ~-4 {LifeTime:35,FireworksItem:{id:"minecraft:firework_rocket",Count:1,tag:{Fireworks:{Explosions:[{Type:1,Colors:[I;15790320,16777045,6192150],FadeColors:[I;11743532,16766720,4312372],Flicker:1b,Trail:1b}]}}}}
execute at @s run summon minecraft:firework_rocket ~4 ~15 ~-4 {LifeTime:35,FireworksItem:{id:"minecraft:firework_rocket",Count:1,tag:{Fireworks:{Explosions:[{Type:2,Colors:[I;11743532,16766720,4312372],Flicker:1b,Trail:1b}]}}}}
execute at @s run summon minecraft:firework_rocket ~-4 ~15 ~4 {LifeTime:35,FireworksItem:{id:"minecraft:firework_rocket",Count:1,tag:{Fireworks:{Explosions:[{Type:2,Colors:[I;15790320,16777045,6192150],Flicker:1b,Trail:1b}]}}}}
execute at @s run summon minecraft:firework_rocket ~0 ~20 ~0 {LifeTime:40,FireworksItem:{id:"minecraft:firework_rocket",Count:1,tag:{Fireworks:{Explosions:[{Type:1,Colors:[I;11743532,16766720,4312372,15790320,12801229],FadeColors:[I;11743532,16766720,4312372],Flicker:1b,Trail:1b},{Type:1,Colors:[I;15790320,16777045,6192150],FadeColors:[I;11743532,16766720,4312372],Flicker:1b,Trail:1b}]}}}}

# --- Confetti particles everywhere ---
particle minecraft:happy_villager ~0 ~5 ~0 10 5 10 0.1 100
particle minecraft:end_rod ~0 ~10 ~0 8 3 8 0.05 50
particle minecraft:firework ~0 ~15 ~0 8 3 8 0.05 40
particle minecraft:totem_of_undying ~0 ~3 ~0 5 2 5 0.1 30

# --- Party favors ---
give @a minecraft:cake 1
give @a minecraft:golden_apple 1
give @a minecraft:firework_rocket 16
give @a minecraft:champagne 1 2>/dev/null || give @a minecraft:potion 1
give @a minecraft:experience_bottle 16

# --- Finale message ---
tellraw @a [{"text":"[NYE] ","color":"gold","bold":true},{"text":"HAPPY NEW YEAR! Wishing you an amazing year ahead!","color":"gold","bold":true}]
tellraw @a [{"text":"[NYE] ","color":"gold","bold":true},{"text":"Enjoy the fireworks, cake, and party favors!","color":"aqua"}]
