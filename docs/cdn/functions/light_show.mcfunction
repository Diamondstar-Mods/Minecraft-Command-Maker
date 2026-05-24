# Light Show - A synchronized beacon and note block performance!
# Colors, music, and particles in a stunning 30-second show.

# --- Announce ---
title @a title {"text":"LIGHT SHOW!","color":"light_purple","bold":true}
title @a subtitle {"text":"Let there be light...","color":"aqua"}
tellraw @a [{"text":"[Show] ","color":"light_purple","bold":true},{"text":"Starting the light show! Stand back and enjoy!","color":"white"}]

# --- Give night vision for the full effect ---
effect give @a minecraft:night_vision 60 0 true

# --- Build a ring of colored beacons around the player ---
execute at @s run setblock ~3 ~0 ~0 minecraft:red_stained_glass
execute at @s run setblock ~-3 ~0 ~0 minecraft:blue_stained_glass
execute at @s run setblock ~0 ~0 ~3 minecraft:green_stained_glass
execute at @s run setblock ~0 ~0 ~-3 minecraft:yellow_stained_glass
execute at @s run setblock ~2 ~0 ~2 minecraft:purple_stained_glass
execute at @s run setblock ~-2 ~0 ~2 minecraft:orange_stained_glass
execute at @s run setblock ~2 ~0 ~-2 minecraft:lime_stained_glass
execute at @s run setblock ~-2 ~0 ~-2 minecraft:cyan_stained_glass

# --- Note blocks in a ring for music ---
execute at @s run setblock ~4 ~0 ~0 minecraft:note_block
execute at @s run setblock ~-4 ~0 ~0 minecraft:note_block
execute at @s run setblock ~0 ~0 ~4 minecraft:note_block
execute at @s run setblock ~0 ~0 ~-4 minecraft:note_block
execute at @s run setblock ~3 ~0 ~3 minecraft:note_block
execute at @s run setblock ~-3 ~0 ~3 minecraft:note_block
execute at @s run setblock ~3 ~0 ~-3 minecraft:note_block
execute at @s run setblock ~-3 ~0 ~-3 minecraft:note_block

# --- Center beacon pillar ---
execute at @s run setblock ~0 ~0 ~0 minecraft:beacon
execute at @s run setblock ~0 ~-1 ~0 minecraft:iron_block
execute at @s run setblock ~0 ~4 ~0 minecraft:sea_lantern

# --- Act 1: Red pulse ---
playsound minecraft:block.note_block.harp master @a ~ ~ ~ 1 1
particle minecraft:dust 1 0 0 2 ~3 ~1 ~0 1 1 1 0.1 30
particle minecraft:dust 1 0 0 2 ~-3 ~1 ~0 1 1 1 0.1 30

# --- Act 2: Blue shimmer ---
playsound minecraft:block.note_block.chime master @a ~ ~ ~ 1 1.5
particle minecraft:dust 0 0 1 2 ~0 ~1 ~3 1 1 1 0.1 30
particle minecraft:dust 0 0 1 2 ~0 ~1 ~-3 1 1 1 0.1 30

# --- Act 3: Green burst ---
playsound minecraft:block.note_block.bell master @a ~ ~ ~ 1 2
particle minecraft:dust 0 1 0 2 ~2 ~1 ~2 1 1 1 0.1 30
particle minecraft:dust 0 1 0 2 ~-2 ~1 ~-2 1 1 1 0.1 30

# --- Act 4: Yellow cascade ---
playsound minecraft:block.note_block.flute master @a ~ ~ ~ 1 1
particle minecraft:dust 1 1 0 2 ~0 ~2 ~3 1 1 1 0.1 30
particle minecraft:dust 1 1 0 2 ~3 ~2 ~0 1 1 1 0.1 30

# --- Act 5: Purple swirl ---
playsound minecraft:block.note_block.xylophone master @a ~ ~ ~ 1 0.5
particle minecraft:dust 0.5 0 1 2 ~2 ~1 ~-2 1 1 1 0.1 30
particle minecraft:dust 0.5 0 1 2 ~-2 ~1 ~2 1 1 1 0.1 30

# --- Act 6: Orange burst ---
playsound minecraft:block.note_block.iron_xylophone master @a ~ ~ ~ 1 1.5
particle minecraft:dust 1 0.5 0 2 ~0 ~2 ~0 2 0 2 0.1 40

# --- Act 7: Lime finale ---
playsound minecraft:block.note_block.guitar master @a ~ ~ ~ 1 2
particle minecraft:dust 0 1 0 2 ~3 ~1 ~3 1 1 1 0.1 30
particle minecraft:dust 0 1 0 2 ~-3 ~1 ~-3 1 1 1 0.1 30

# --- Act 8: Cyan shimmer ---
playsound minecraft:block.note_block.harp master @a ~ ~ ~ 1 1.5
particle minecraft:dust 0 1 1 2 ~0 ~3 ~0 3 0 3 0.1 40

# --- GRAND FINALE: All colors at once ---
playsound minecraft:block.beacon.power_select master @a ~ ~ ~ 2 1
playsound minecraft:entity.firework_rocket.large_blast master @a ~ ~ ~ 1 1
particle minecraft:dust 1 0 0 2 ~0 ~1 ~0 5 2 5 0.1 50
particle minecraft:dust 0 0 1 2 ~0 ~1 ~0 5 2 5 0.1 50
particle minecraft:dust 0 1 0 2 ~0 ~1 ~0 5 2 5 0.1 50
particle minecraft:dust 1 1 0 2 ~0 ~1 ~0 5 2 5 0.1 50
particle minecraft:end_rod ~0 ~2 ~0 4 0 4 0.05 40
particle minecraft:firework ~0 ~5 ~0 2 0 2 0.02 20

# --- Cleanup: remove the blocks (show is over) ---
execute at @s run fill ~-4 ~0 ~-4 ~4 ~0 ~4 minecraft:air
execute at @s run setblock ~0 ~-1 ~0 minecraft:air

playsound minecraft:entity.player.levelup master @a ~ ~ ~ 1 2

tellraw @a [{"text":"[Show] ","color":"light_purple","bold":true},{"text":"Show complete! Hope you enjoyed the performance!","color":"gold"}]
