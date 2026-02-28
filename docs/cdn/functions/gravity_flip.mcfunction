# Gravity Flip - Yo-yo everyone up and down!
# Alternates between levitation and slow falling for a wild ride.

# --- Announce ---
title @a title {"text":"GRAVITY FLIP!","color":"aqua","bold":true}
title @a subtitle {"text":"Hold on tight!","color":"white"}
tellraw @a [{"text":"[Gravity] ","color":"aqua","bold":true},{"text":"Gravity is going haywire! Everyone hold on!","color":"yellow"}]

# --- Safety net: resistance so nobody dies from fall damage ---
effect give @a minecraft:resistance 30 4 true

# --- Phase 1: Launch everyone UP ---
effect give @a minecraft:levitation 3 8 true
playsound minecraft:entity.firework_rocket.launch master @a ~ ~ ~ 1 1
execute as @a at @s run particle minecraft:cloud ~ ~ ~ 1 0 1 0.1 30

# --- Phase 2: Slow drift down (kicks in after levitation wears off) ---
effect give @a minecraft:slow_falling 6 0 true

# --- Phase 3: Launch UP again (after slow fall) ---
effect give @a minecraft:levitation 3 5 true

# --- Yo-yo particles and sounds ---
execute as @a at @s run particle minecraft:end_rod ~ ~1 ~ 0.5 2 0.5 0.05 40
execute as @a at @s run particle minecraft:reverse_portal ~ ~ ~ 1 1 1 0.1 50
playsound minecraft:block.note_block.xylophone master @a ~ ~ ~ 1 2
playsound minecraft:block.note_block.xylophone master @a ~ ~ ~ 1 0.5

# --- Bonus chaos: spin everyone's view slightly with particles ---
execute as @a at @s run particle minecraft:enchant ~ ~1 ~ 2 2 2 1 60
execute as @a at @s run particle minecraft:portal ~ ~ ~ 1 1 1 0.5 40

# --- More sounds for the ride ---
playsound minecraft:entity.enderman.teleport master @a ~ ~ ~ 0.5 1.5
playsound minecraft:entity.shulker.shoot master @a ~ ~ ~ 0.5 0.8

tellraw @a [{"text":"[Gravity] ","color":"aqua","bold":true},{"text":"You have Resistance V so you won't take fall damage. Enjoy the ride!","color":"green"}]
