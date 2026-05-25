# Halloween Spook - Transforms the area into a spooky halloween scene!
# Darkness, pumpkins, bats, cobwebs, and spooky sounds.

# --- Announce ---
title @a title {"text":"HAPPY HALLOWEEN!","color":"dark_purple","bold":true}
title @a subtitle {"text":"Something spooky this way comes...","color":"dark_red"}
tellraw @a [{"text":"[Spooky] ","color":"gold","bold":true},{"text":"The world grows dark and spooky...","color":"dark_purple"}]

# --- Give everyone night vision so they can see the effects ---
effect give @a minecraft:night_vision 300 0 true
effect give @a minecraft:blindness 5 0 true

# --- Set time to midnight ---
time set 18000

# --- Spooky darkness particles ---
particle minecraft:smoke ~0 ~3 ~0 8 2 8 0.05 100
particle minecraft:large_smoke ~0 ~3 ~0 6 2 6 0.03 60
particle minecraft:witch ~0 ~2 ~0 4 2 4 0.05 40

# --- Place jack-o-lanterns around the player ---
execute at @s run setblock ~3 ~0 ~3 minecraft:jack_o_lantern
execute at @s run setblock ~-3 ~0 ~3 minecraft:jack_o_lantern
execute at @s run setblock ~3 ~0 ~-3 minecraft:jack_o_lantern
execute at @s run setblock ~-3 ~0 ~-3 minecraft:jack_o_lantern
execute at @s run setblock ~5 ~0 ~0 minecraft:carved_pumpkin
execute at @s run setblock ~-5 ~0 ~0 minecraft:carved_pumpkin
execute at @s run setblock ~0 ~0 ~5 minecraft:jack_o_lantern
execute at @s run setblock ~0 ~0 ~-5 minecraft:jack_o_lantern

# --- Cobwebs scattered around ---
execute at @s run setblock ~4 ~1 ~2 minecraft:cobweb
execute at @s run setblock ~-4 ~1 ~-2 minecraft:cobweb
execute at @s run setblock ~2 ~1 ~4 minecraft:cobweb
execute at @s run setblock ~-2 ~1 ~-4 minecraft:cobweb
execute at @s run setblock ~0 ~2 ~4 minecraft:cobweb
execute at @s run setblock ~4 ~2 ~0 minecraft:cobweb

# --- Spawn bats ---
execute at @s run summon minecraft:bat ~3 ~3 ~3
execute at @s run summon minecraft:bat ~-3 ~2 ~3
execute at @s run summon minecraft:bat ~3 ~3 ~-3
execute at @s run summon minecraft:bat ~-3 ~2 ~-3
execute at @s run summon minecraft:bat ~0 ~4 ~5
execute at @s run summon minecraft:bat ~5 ~4 ~0
execute at @s run summon minecraft:bat ~-5 ~3 ~0
execute at @s run summon minecraft:bat ~0 ~4 ~-5

# --- Spawn a few spooky mobs (named for flavor) ---
execute at @s run summon minecraft:skeleton ~7 ~0 ~7 {CustomName:'{"text":"Spooky Scary Skeleton","color":"white"}',ArmorItems:[{},{},{},{id:"minecraft:carved_pumpkin",Count:1b}]}
execute at @s run summon minecraft:skeleton ~-7 ~0 ~-7 {CustomName:'{"text":"Spooky Scary Skeleton","color":"white"}',ArmorItems:[{},{},{},{id:"minecraft:carved_pumpkin",Count:1b}]}

# --- Spooky sounds ---
playsound minecraft:ambient.cave master @a ~ ~ ~ 1 0.5
playsound minecraft:entity.phantom.swoop master @a ~ ~ ~ 0.5 1
playsound minecraft:entity.witch.ambient master @a ~ ~ ~ 1 1
playsound minecraft:entity.ghast.ambient master @a ~ ~ ~ 0.3 2
playsound minecraft:block.note_block.didgeridoo master @a ~ ~ ~ 1 0

# --- Lightning bolt for dramatic effect (safely in the distance) ---
execute at @s run summon minecraft:lightning_bolt ~10 ~0 ~10

# --- Give halloween treats ---
give @a minecraft:pumpkin_pie 5
give @a minecraft:cookie 16
give @a minecraft:bone 1

tellraw @a [{"text":"[Spooky] ","color":"gold","bold":true},{"text":"HAPPY HALLOWEEN! Trick or treat!","color":"dark_purple","bold":true}]
