# Dance Party - A one-shot dance party for the whole server!
# Sets the mood, drops the beat, and gets everyone grooving.

# --- Announce the party ---
title @a title {"text":"DANCE PARTY!","color":"light_purple","bold":true}
title @a subtitle {"text":"Get your groove on!","color":"gold"}
tellraw @a [{"text":"[DJ] ","color":"yellow","bold":true},{"text":"The dance floor is open! ","color":"aqua"},{"text":"LET'S GO!","color":"light_purple","bold":true}]

# --- Set the vibe: nighttime, clear skies ---
time set midnight
weather clear
gamerule doDaylightCycle false

# --- Drop the beat ---
playsound minecraft:music_disc.otherside master @a ~ ~ ~ 2 1
execute as @a at @s run playsound minecraft:entity.firework_rocket.blast master @s ~ ~ ~ 0.5 0.5

# --- Give everyone dance buffs ---
effect give @a minecraft:speed 60 2 true
effect give @a minecraft:jump_boost 60 3 true
effect give @a minecraft:haste 60 2 true
effect give @a minecraft:night_vision 60 0 true
effect give @a minecraft:glowing 60 0 true

# --- Build the dance floor: 5x5 colored glass platform under each player ---
execute as @a at @s run fill ~-2 ~-1 ~-2 ~2 ~-1 ~2 minecraft:magenta_stained_glass
execute as @a at @s run setblock ~0 ~-1 ~0 minecraft:glowstone
execute as @a at @s run setblock ~2 ~-1 ~2 minecraft:sea_lantern
execute as @a at @s run setblock ~-2 ~-1 ~-2 minecraft:sea_lantern
execute as @a at @s run setblock ~2 ~-1 ~-2 minecraft:jack_o_lantern
execute as @a at @s run setblock ~-2 ~-1 ~2 minecraft:jack_o_lantern

# --- Spawn backup dancers (armor stands with fun poses) ---
execute as @a at @s run summon minecraft:armor_stand ~2 ~ ~0 {CustomName:"DANCE",CustomNameVisible:1b,ShowArms:1b,Pose:{LeftArm:[-10f,0f,-110f],RightArm:[200f,0f,110f],LeftLeg:[20f,0f,0f],RightLeg:[-20f,0f,0f]}}
execute as @a at @s run summon minecraft:armor_stand ~-2 ~ ~0 {CustomName:"DANCE",CustomNameVisible:1b,ShowArms:1b,Pose:{LeftArm:[200f,0f,110f],RightArm:[-10f,0f,-110f],LeftLeg:[-20f,0f,0f],RightLeg:[20f,0f,0f]}}
execute as @a at @s run summon minecraft:armor_stand ~0 ~ ~2 {CustomName:"DANCE",CustomNameVisible:1b,ShowArms:1b,Pose:{LeftArm:[-90f,20f,0f],RightArm:[-90f,-20f,0f],LeftLeg:[40f,0f,0f],RightLeg:[-40f,0f,0f]}}
execute as @a at @s run summon minecraft:armor_stand ~0 ~ ~-2 {CustomName:"DANCE",CustomNameVisible:1b,ShowArms:1b,Pose:{LeftArm:[0f,0f,160f],RightArm:[0f,0f,-160f],LeftLeg:[-30f,0f,0f],RightLeg:[30f,0f,0f]}}

# --- Disco particles around each player ---
execute as @a at @s run particle minecraft:note ~ ~2.5 ~ 2 0.5 2 1 30
execute as @a at @s run particle minecraft:end_rod ~ ~1 ~ 3 2 3 0.1 80
execute as @a at @s run particle minecraft:totem_of_undying ~ ~1 ~ 1 1 1 0.8 60
execute as @a at @s run particle minecraft:firework ~ ~1 ~ 2 1 2 0.2 50
execute as @a at @s run particle minecraft:witch ~ ~0.5 ~ 2 0.2 2 0.1 40

# --- Fireworks burst in a ring around each player ---
execute as @a at @s run summon minecraft:firework_rocket ~4 ~1 ~0 {LifeTime:10,FireworksItem:{id:"minecraft:firework_rocket",Count:1b,tag:{Fireworks:{Flight:1,Explosions:[{Type:4b,Flicker:1b,Trail:1b,Colors:[I;16711935,65535,16776960],FadeColors:[I;16777215]}]}}}}
execute as @a at @s run summon minecraft:firework_rocket ~-4 ~1 ~0 {LifeTime:12,FireworksItem:{id:"minecraft:firework_rocket",Count:1b,tag:{Fireworks:{Flight:1,Explosions:[{Type:1b,Flicker:1b,Trail:1b,Colors:[I;16711680,65280,255],FadeColors:[I;16776960]}]}}}}
execute as @a at @s run summon minecraft:firework_rocket ~0 ~1 ~4 {LifeTime:14,FireworksItem:{id:"minecraft:firework_rocket",Count:1b,tag:{Fireworks:{Flight:2,Explosions:[{Type:0b,Flicker:1b,Trail:1b,Colors:[I;16753920,16761035],FadeColors:[I;11141290]}]}}}}
execute as @a at @s run summon minecraft:firework_rocket ~0 ~1 ~-4 {LifeTime:16,FireworksItem:{id:"minecraft:firework_rocket",Count:1b,tag:{Fireworks:{Flight:2,Explosions:[{Type:3b,Flicker:1b,Trail:1b,Colors:[I;5636095,5592575],FadeColors:[I;16777045]}]}}}}
execute as @a at @s run summon minecraft:firework_rocket ~3 ~1 ~3 {LifeTime:18,FireworksItem:{id:"minecraft:firework_rocket",Count:1b,tag:{Fireworks:{Flight:1,Explosions:[{Type:2b,Flicker:1b,Trail:1b,Colors:[I;16711935,16776960,65535],FadeColors:[I;16711680]}]}}}}
execute as @a at @s run summon minecraft:firework_rocket ~-3 ~1 ~-3 {LifeTime:20,FireworksItem:{id:"minecraft:firework_rocket",Count:1b,tag:{Fireworks:{Flight:2,Explosions:[{Type:4b,Flicker:1b,Trail:1b,Colors:[I;65280,16711680,255],FadeColors:[I;16753920]}]}}}}

# --- Extra vibes: random fun sounds ---
execute as @a at @s run playsound minecraft:entity.player.levelup master @s ~ ~ ~ 0.8 1.5
execute as @a at @s run playsound minecraft:block.note_block.pling master @s ~ ~ ~ 1 2
execute as @a at @s run playsound minecraft:block.note_block.bass master @s ~ ~ ~ 1 0.5
execute as @a at @s run playsound minecraft:block.note_block.snare master @s ~ ~ ~ 1 1

# --- DJ shoutout ---
tellraw @a [{"text":"[DJ] ","color":"yellow","bold":true},{"text":"Don't forget to ","color":"white"},{"text":"/jump","color":"green","bold":true},{"text":" around! You've got Jump Boost III!","color":"white"}]
