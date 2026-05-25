# --- Funny Prank Script ---
# Trigger: run manually or via /function

# 1. Dramatic overreaction
title @p title {"text":"WHAT HAVE YOU DONE","color":"red","bold":true}
title @p subtitle {"text":"You pressed the funny button.","color":"yellow"}

# 2. Random fart noises (yes, Minecraft has them)
playsound minecraft:entity.pig.ambient master @p ~ ~ ~ 1 0.5
playsound minecraft:entity.puffer_fish.blow_out master @p ~ ~ ~ 1 1

# 3. Spawn a chicken for 2 ticks so it pops in and out of existence
summon chicken ~ ~1 ~ {Tags:["temp_chicken"]}

# 4. Give the player a potato named “The Forbidden Snack”
give @p minecraft:potato{display:{Name:'{"text":"The Forbidden Snack","italic":false}'}}

# 5. Make the player spin a little
tp @p ~ ~ ~ ~10 ~

# 6. Sprinkle particles for comedic effect
particle minecraft:poof ~ ~1 ~ 0.5 0.5 0.5 0.1 20
