# Time Locker - Freezes the world in eternal day!
# Perfect for build servers or creative mode worlds.

# --- Announce ---
title @a title {"text":"TIME LOCKED!","color":"gold","bold":true}
title @a subtitle {"text":"Eternal day has begun...","color":"yellow"}
tellraw @a [{"text":"[Time] ","color":"gold","bold":true},{"text":"Locking the world in eternal daytime!","color":"white"}]

# --- Set time to noon ---
time set 6000

# --- Disable the daylight cycle so it stays at noon forever ---
gamerule doDaylightCycle false

# --- Clear weather ---
weather clear 999999

# --- Disable weather cycle ---
gamerule doWeatherCycle false

# --- Prevent mob spawns in daylight (optional: remove for survival) ---
# gamerule doMobSpawning false

# --- Disable fire spread for safety ---
gamerule doFireTick false

# --- Visual effect ---
particle minecraft:end_rod ~0 ~5 ~0 10 2 10 0.05 50
particle minecraft:happy_villager ~0 ~5 ~0 10 2 10 0.05 30

playsound minecraft:block.beacon.activate master @a ~ ~ ~ 1 2
playsound minecraft:entity.player.levelup master @a ~ ~ ~ 0.5 1

tellraw @a [{"text":"[Time] ","color":"gold","bold":true},{"text":"Time locked to: ","color":"white"},{"text":"NOON - ETERNAL DAY","color":"gold","bold":true}]
tellraw @a [{"text":"[Time] ","color":"gold","bold":true},{"text":"Weather disabled. Fire spread disabled. Happy building!","color":"aqua"}]
tellraw @a [{"text":"[Time] ","color":"gold","bold":true},{"text":"To restore normal time, use: /gamerule doDaylightCycle true","color":"gray"}]
