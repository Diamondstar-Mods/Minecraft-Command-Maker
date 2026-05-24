# Spleef Arena - Builds a snow arena in the sky for a classic spleef match!
# Destroys blocks under players as they walk. Last one standing wins!

# --- Announce ---
title @a title {"text":"SPLEEF!","color":"aqua","bold":true}
title @a subtitle {"text":"The floor crumbles beneath you...","color":"red"}
tellraw @a [{"text":"[Spleef] ","color":"aqua","bold":true},{"text":"A spleef arena has appeared in the sky! Don't fall!","color":"white"}]

# --- Build the floating arena platform (25x25 snow) ---
execute at @s run fill ~-12 ~10 ~-12 ~12 ~10 ~12 minecraft:snow_block
execute at @s run fill ~-13 ~9 ~-13 ~13 ~9 ~13 minecraft:barrier
execute at @s run fill ~-13 ~13 ~-13 ~13 ~13 ~13 minecraft:glass

# --- Corner pillars with glowstone ---
execute at @s run fill ~-13 ~10 ~-13 ~-13 ~13 ~-13 minecraft:quartz_block
execute at @s run fill ~13 ~10 ~-13 ~13 ~13 ~-13 minecraft:quartz_block
execute at @s run fill ~-13 ~10 ~13 ~-13 ~13 ~13 minecraft:quartz_block
execute at @s run fill ~13 ~10 ~13 ~13 ~13 ~13 minecraft:quartz_block
execute at @s run setblock ~-13 ~14 ~-13 minecraft:glowstone
execute at @s run setblock ~13 ~14 ~-13 minecraft:glowstone
execute at @s run setblock ~-13 ~14 ~13 minecraft:glowstone
execute at @s run setblock ~13 ~14 ~13 minecraft:glowstone

# --- Teleport everyone to the arena ---
tp @a ~ ~12 ~

# --- Give everyone diamond shovels (spleef tools!) ---
give @a minecraft:diamond_shovel{Enchantments:[{id:"minecraft:efficiency",lvl:5}]} 1
give @a minecraft:iron_shovel 1

# --- Prevent fall damage ---
effect give @a minecraft:slow_falling 120 0 true
effect give @a minecraft:speed 120 1 true

# --- Countdown ---
tellraw @a [{"text":"[Spleef] ","color":"aqua","bold":true},{"text":"3...","color":"yellow"}]
playsound minecraft:block.note_block.pling master @a ~ ~ ~ 1 1

# Wait ~1 sec (20 ticks) - we can't really wait so just stagger
tellraw @a [{"text":"[Spleef] ","color":"aqua","bold":true},{"text":"2...","color":"gold"}]

tellraw @a [{"text":"[Spleef] ","color":"aqua","bold":true},{"text":"1...","color":"red"}]

# --- GO! ---
title @a title {"text":"GO!","color":"green","bold":true}
playsound minecraft:entity.ender_dragon.growl master @a ~ ~ ~ 1 1
tellraw @a [{"text":"[Spleef] ","color":"aqua","bold":true},{"text":"SPLEEF! Break the snow! Last one standing wins!","color":"green","bold":true}]

# --- Remove the floor barrier so you can fall through broken snow ---
execute at @s run fill ~-13 ~9 ~-13 ~13 ~9 ~13 minecraft:air

# --- Arena particles ---
particle minecraft:snowflake ~ ~12 ~ 12 0 12 0.01 200
