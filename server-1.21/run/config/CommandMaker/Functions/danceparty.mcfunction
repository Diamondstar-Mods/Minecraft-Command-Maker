scoreboard objectives add party dummy
scoreboard players add #run party 1
execute unless score #party party matches 1 run scoreboard players set #party party 1

# --- One-time setup (only when #run == 1) ---
execute if score #run party matches 1 run gamerule doDaylightCycle false
execute if score #run party matches 1 run time set night
execute if score #run party matches 1 run weather clear

execute if score #run party matches 1 run gamerule mobGriefing false

# Optional: give everyone night vision so the particles pop
execute if score #run party matches 1 run effect give @a minecraft:night_vision 9999 0 true

# Optional: spawn a few backup dancers near the command source (your position)
execute if score #run party matches 1 run summon minecraft:villager ~ ~ ~ {NoAI:0}
execute if score #run party matches 1 run summon minecraft:villager ~1 ~ ~ {NoAI:0}
execute if score #run party matches 1 run summon minecraft:villager ~-1 ~ ~ {NoAI:0}
execute if score #run party matches 1 run summon minecraft:armor_stand ~ ~ ~1 {ShowArms:1,Small:1,Pose:{LeftLeg:[30f,0f,0f],RightLeg:[-30f,0f,0f],LeftArm:[0f,0f,30f],RightArm:[0f,0f,-30f]}}
execute if score #run party matches 1 run summon minecraft:armor_stand ~ ~ ~-1 {ShowArms:1,Small:1,Pose:{LeftLeg:[-20f,0f,0f],RightLeg:[20f,0f,0f],LeftArm:[0f,0f,-20f],RightArm:[0f,0f,20f]}}

# --- Main loop (runs every time, once per second) ---
# Only do party actions if #party is still 1
execute unless score #party party matches 1 run return 0

# Player dance buffs (refresh each loop to keep it active)
effect give @a minecraft:jump_boost 2 2 true
effect give @a minecraft:speed 2 1 true
effect give @a minecraft:haste 2 1 true

# Make non-player entities bounce a bit (backup dancers / mobs)
effect give @e[type=!player] minecraft:jump_boost 2 3 true

# Music (pick one primary; you can swap discs anytime)
playsound minecraft:music_disc.pigstep master @a ~ ~ ~ 1 1

# Lights & particles around each player


# Fireworks near each player (moderate density; increase if your server can handle it)
execute as @a at @s run summon minecraft:firework_rocket ~ ~1 ~ {LifeTime:20,FireworksItem:{id:firework_rocket,Count:1,tag:{Fireworks:{Flight:1,Explosions:[{Type:1,Colors:[I;16711680,65280,255],FadeColors:[I;16776960],Trail:1,Flicker:1}]}}}}

# Bass-drop “shake” (safe: Fuse 0 but mobGriefing false prevents block damage from TNT entities; still loud/chaotic)
execute as @a at @s run summon minecraft:tnt ~ ~1 ~ {Fuse:0}

# --- Reschedule self (every 1 second) ---
# If you want it faster/slower, change "1s" to e.g. "5t" (5 ticks) or "2s".
# schedule function party:dance_party 1s replace