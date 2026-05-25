# ==========================================
#  RANDOM CHAOS FUNCTION (random_chaos.mcfunction)
#  Minecraft: Java 1.21.x
#  "Just random" events and effects
# ==========================================

# --- Setup: scoreboards and storage (run once, or leave here if you don't mind spam) ---

scoreboard objectives add rc.tick dummy "RandomChaos Ticks"
scoreboard objectives add rc.rng dummy "RandomChaos RNG"
scoreboard objectives add rc.event dummy "RandomChaos Event"
scoreboard objectives add rc.state dummy "RandomChaos State"
scoreboard objectives add rc.combo dummy "RandomChaos Combo"

# global marker entity tag: rc.global
# (we summon and reuse this as a persistent brain)
execute unless entity @e[type=marker,tag=rc.global,limit=1] run summon marker 0 0 0 {Tags:["rc.global"]}

# --- Tick logic for this call ---

# Increase tick counter on the global marker
execute as @e[type=marker,tag=rc.global,limit=1] run scoreboard players add @s rc.tick 1

# Every call, re-roll RNG 0–9
# (this uses "mod 10" logic via division)
scoreboard players random @e[type=marker,tag=rc.global,limit=1] rc.rng 0 9999
execute as @e[type=marker,tag=rc.global,limit=1] store result score @s rc.rng run scoreboard players operation @s rc.rng /= $ten rc.state

# Pre-load constant "10" in rc.state
scoreboard players set $ten rc.state 10

# --- RANDOM EVENT PICKER (0–9) ---

# Clear last event
scoreboard players set @e[type=marker,tag=rc.global,limit=1] rc.event -1

# If rng == 0 -> event 0 (particle burst)
execute as @e[type=marker,tag=rc.global,limit=1] if score @s rc.rng matches 0 run scoreboard players set @s rc.event 0

# If rng == 1 -> event 1 (random item rain)
execute as @e[type=marker,tag=rc.global,limit=1] if score @s rc.rng matches 1 run scoreboard players set @s rc.event 1

# If rng == 2 -> event 2 (night vision + levitation roulette)
execute as @e[type=marker,tag=rc.global,limit=1] if score @s rc.rng matches 2 run scoreboard players set @s rc.event 2

# If rng == 3 -> event 3 (random mob summon)
execute as @e[type=marker,tag=rc.global,limit=1] if score @s rc.rng matches 3 run scoreboard players set @s rc.event 3

# If rng == 4 -> event 4 (world sound glitch)
execute as @e[type=marker,tag=rc.global,limit=1] if score @s rc.rng matches 4 run scoreboard players set @s rc.event 4

# If rng == 5 -> event 5 (XP + fireworks)
execute as @e[type=marker,tag=rc.global,limit=1] if score @s rc.rng matches 5 run scoreboard players set @s rc.event 5

# If rng == 6 -> event 6 (random teleport in small radius)
execute as @e[type=marker,tag=rc.global,limit=1] if score @s rc.rng matches 6 run scoreboard players set @s rc.event 6

# If rng == 7 -> event 7 (health swap roulette)
execute as @e[type=marker,tag=rc.global,limit=1] if score @s rc.rng matches 7 run scoreboard players set @s rc.event 7

# If rng == 8 -> event 8 (random potion chaos)
execute as @e[type=marker,tag=rc.global,limit=1] if score @s rc.rng matches 8 run scoreboard players set @s rc.event 8

# If rng == 9 -> event 9 (gravity flip illusion)
execute as @e[type=marker,tag=rc.global,limit=1] if score @s rc.rng matches 9 run scoreboard players set @s rc.event 9


# ==========================================
#  EVENT 0: PARTICLE BURST AROUND PLAYERS
# ==========================================
execute as @e[type=marker,tag=rc.global,limit=1] if score @s rc.event matches 0 run title @a actionbar {"text":"§a[Chaos] §fParticle Burst!"}
execute as @e[type=marker,tag=rc.global,limit=1] if score @s rc.event matches 0 run playsound minecraft:block.amethyst_block.chime master @a ~ ~ ~ 0.7 1.8

execute if score @e[type=marker,tag=rc.global,limit=1] rc.event matches 0 as @a at @s run particle minecraft:enchanted_hit ~ ~1 ~ 0.8 1.5 0.8 0.02 60
execute if score @e[type=marker,tag=rc.global,limit=1] rc.event matches 0 as @a at @s run particle minecraft:glow ~ ~2 ~ 1 0.5 1 0.01 40
execute if score @e[type=marker,tag=rc.global,limit=1] rc.event matches 0 as @a at @s run particle minecraft:dragon_breath ~ ~1 ~ 1 0.1 1 0.05 80


# ==========================================
#  EVENT 1: RANDOM ITEM RAIN NEAR PLAYERS
# ==========================================
# Pseudo-random set of items around each player

execute if score @e[type=marker,tag=rc.global,limit=1] rc.event matches 1 run title @a actionbar {"text":"§e[Chaos] §fItem Rain!"}
execute if score @e[type=marker,tag=rc.global,limit=1] rc.event matches 1 run playsound minecraft:entity.item.pickup master @a ~ ~ ~ 0.6 1.2

# Drop 3 kinds of items around every player
execute if score @e[type=marker,tag=rc.global,limit=1] rc.event matches 1 as @a at @s run summon item ~1 ~3 ~1 {Item:{id:"minecraft:gold_nugget",Count:3b},PickupDelay:20}
execute if score @e[type=marker,tag=rc.global,limit=1] rc.event matches 1 as @a at @s run summon item ~-2 ~4 ~ {Item:{id:"minecraft:ender_pearl",Count:1b},PickupDelay:30}
execute if score @e[type=marker,tag=rc.global,limit=1] rc.event matches 1 as @a at @s run summon item ~ ~5 ~-1 {Item:{id:"minecraft:cookie",Count:5b},PickupDelay:10}


# ==========================================
#  EVENT 2: NIGHT VISION + LEVITATION ROULETTE
# ==========================================
# 50/50: either friendly boost or mild punishment

execute if score @e[type=marker,tag=rc.global,limit=1] rc.event matches 2 as @a run effect give @s minecraft:night_vision 8 0 true

# extra RNG: reuse rc.combo as flag (0 or 1)
scoreboard players random $flip rc.combo 0 1

# If flip == 0 -> give slow falling + jump boost (fun)
execute if score $flip rc.combo matches 0 if score @e[type=marker,tag=rc.global,limit=1] rc.event matches 2 as @a run effect give @s minecraft:slow_falling 5 1 true
execute if score $flip rc.combo matches 0 if score @e[type=marker,tag=rc.global,limit=1] rc.event matches 2 as @a run effect give @s minecraft:jump_boost 5 2 true
execute if score $flip rc.combo matches 0 if score @e[type=marker,tag=rc.global,limit=1] rc.event matches 2 run title @a actionbar {"text":"§b[Chaos] §fGraceful Falling"}

# If flip == 1 -> levitation + nausea (chaos)
execute if score $flip rc.combo matches 1 if score @e[type=marker,tag=rc.global,limit=1] rc.event matches 2 as @a run effect give @s minecraft:levitation 4 1 true
execute if score $flip rc.combo matches 1 if score @e[type=marker,tag=rc.global,limit=1] rc.event matches 2 as @a run effect give @s minecraft:nausea 6 0 true
execute if score $flip rc.combo matches 1 if score @e[type=marker,tag=rc.global,limit=1] rc.event matches 2 run title @a actionbar {"text":"§5[Chaos] §fLevitation Roulette"}


# ==========================================
#  EVENT 3: RANDOM MOB SUMMON NEAR PLAYERS
# ==========================================
# Friendly or hostile, small radius

scoreboard players random $mob rc.combo 0 3

# 0: Allay
execute if score @e[type=marker,tag=rc.global,limit=1] rc.event matches 3 if score $mob rc.combo matches 0 as @a at @s run summon minecraft:allay ~2 ~ ~ {PersistenceRequired:1b,CustomName:'{"text":"Chaos Allay"}'}

# 1: Bee (angry)
execute if score @e[type=marker,tag=rc.global,limit=1] rc.event matches 3 if score $mob rc.combo matches 1 as @a at @s run summon minecraft:bee ~-2 ~ ~ {AngerTime:200,CustomName:'{"text":"Angry Chaos Bee"}'}

# 2: Slime
execute if score @e[type=marker,tag=rc.global,limit=1] rc.event matches 3 if score $mob rc.combo matches 2 as @a at @s run summon minecraft:slime ~ ~ ~2 {Size:2,CustomName:'{"text":"Bouncy Chaos"}'}

# 3: Phantom (but low health)
execute if score @e[type=marker,tag=rc.global,limit=1] rc.event matches 3 if score $mob rc.combo matches 3 as @a at @s run summon minecraft:phantom ~ ~8 ~ {Health:5f,CustomName:'{"text":"Mini Phantom"}'}

execute if score @e[type=marker,tag=rc.global,limit=1] rc.event matches 3 run title @a actionbar {"text":"§c[Chaos] §fMob Drop!"}
execute if score @e[type=marker,tag=rc.global,limit=1] rc.event matches 3 run playsound minecraft:entity.illusioner.cast_spell master @a ~ ~ ~ 0.8 0.9


# ==========================================
#  EVENT 4: WEIRD SOUNDS EVERYWHERE
# ==========================================
execute if score @e[type=marker,tag=rc.global,limit=1] rc.event matches 4 run title @a actionbar {"text":"§d[Chaos] §fAuditory Distortion"}

execute if score @e[type=marker,tag=rc.global,limit=1] rc.event matches 4 run playsound minecraft:entity.elder_guardian.curse master @a ~ ~ ~ 0.7 0.8
execute if score @e[type=marker,tag=rc.global,limit=1] rc.event matches 4 run playsound minecraft:music_disc.otherside music @a ~ ~ ~ 0.4 1.0
execute if score @e[type=marker,tag=rc.global,limit=1] rc.event matches 4 run playsound minecraft:block.note_block.chime master @a ~ ~ ~ 1 2.0
execute if score @e[type=marker,tag=rc.global,limit=1] rc.event matches 4 run playsound minecraft:block.note_block.bell master @a ~ ~ ~ 1 0.5


# ==========================================
#  EVENT 5: XP ORB + FIREWORK PARTY
# ==========================================
execute if score @e[type=marker,tag=rc.global,limit=1] rc.event matches 5 run title @a actionbar {"text":"§a[Chaos] §fXP & Fireworks!"}
execute if score @e[type=marker,tag=rc.global,limit=1] rc.event matches 5 run playsound minecraft:entity.experience_orb.pickup master @a ~ ~ ~ 1 1.5

# XP for all players
execute if score @e[type=marker,tag=rc.global,limit=1] rc.event matches 5 as @a run xp add @s 7 points

# Fireworks near players
execute if score @e[type=marker,tag=rc.global,limit=1] rc.event matches 5 as @a at @s run summon firework_rocket ~ ~1 ~ {LifeTime:30,FireworksItem:{id:"minecraft:firework_rocket",Count:1b,tag:{Fireworks:{Flight:1,Explosions:[{Type:1,Colors:[I;11743532,15435844],FadeColors:[I;14602026]}]}}}}


# ==========================================
#  EVENT 6: SHORT-RANGE RANDOM TELEPORT
# ==========================================
execute if score @e[type=marker,tag=rc.global,limit=1] rc.event matches 6 run title @a actionbar {"text":"§3[Chaos] §fSmall Random Teleport!"}
execute if score @e[type=marker,tag=rc.global,limit=1] rc.event matches 6 run playsound minecraft:entity.enderman.teleport master @a ~ ~ ~ 1 1.2

# Teleport each player randomly within a small cube (±5 blocks horizontally, ±2 vertically)
execute if score @e[type=marker,tag=rc.global,limit=1] rc.event matches 6 as @a at @s run spreadplayers ~ ~ 3 5 false @s


# ==========================================
#  EVENT 7: HEALTH SWAP ROULETTE (DANGEROUS BUT FUN)
# ==========================================
# Use scoreboards to store health * 2 (to avoid float issues)

# Ensure objective for health storage
scoreboard objectives add rc.health dummy "Chaos Health"

# Store each player's health
execute if score @e[type=marker,tag=rc.global,limit=1] rc.event matches 7 as @a store result score @s rc.health run data get entity @s Health 2

# Shuffle: we will randomly give someone else’s health
# For simplicity, we pair players via "nearest other"

execute if score @e[type=marker,tag=rc.global,limit=1] rc.event matches 7 as @a at @s run execute as @p[distance=1.1..,limit=1,sort=nearest] run data modify entity @s Health set from entity @p Health

execute if score @e[type=marker,tag=rc.global,limit=1] rc.event matches 7 run title @a actionbar {"text":"§4[Chaos] §fHealth Roulette!"}
execute if score @e[type=marker,tag=rc.global,limit=1] rc.event matches 7 run playsound minecraft:item.trident.thunder master @a ~ ~ ~ 0.7 0.8


# ==========================================
#  EVENT 8: RANDOM POTION CHAOS
# ==========================================
execute if score @e[type=marker,tag=rc.global,limit=1] rc.event matches 8 run title @a actionbar {"text":"§9[Chaos] §fPotion Storm"}
execute if score @e[type=marker,tag=rc.global,limit=1] rc.event matches 8 run playsound minecraft:entity.witch.celebrate master @a ~ ~ ~ 0.7 1.3

# Random effect pattern
scoreboard players random $patt rc.combo 0 2

# Pattern 0: Speed + Haste + Regen
execute if score @e[type=marker,tag=rc.global,limit=1] rc.event matches 8 if score $patt rc.combo matches 0 as @a run effect give @s minecraft:speed 10 2 true
execute if score @e[type=marker,tag=rc.global,limit=1] rc.event matches 8 if score $patt rc.combo matches 0 as @a run effect give @s minecraft:haste 10 1 true
execute if score @e[type=marker,tag=rc.global,limit=1] rc.event matches 8 if score $patt rc.combo matches 0 as @a run effect give @s minecraft:regeneration 4 1 true

# Pattern 1: Strength + Resistance + Slowness
execute if score @e[type=marker,tag=rc.global,limit=1] rc.event matches 8 if score $patt rc.combo matches 1 as @a run effect give @s minecraft:strength 8 2 true
execute if score @e[type=marker,tag=rc.global,limit=1] rc.event matches 8 if score $patt rc.combo matches 1 as @a run effect give @s minecraft:resistance 8 1 true
execute if score @e[type=marker,tag=rc.global,limit=1] rc.event matches 8 if score $patt rc.combo matches 1 as @a run effect give @s minecraft:slowness 5 1 true

# Pattern 2: Invisibility + Blindness
execute if score @e[type=marker,tag=rc.global,limit=1] rc.event matches 8 if score $patt rc.combo matches 2 as @a run effect give @s minecraft:invisibility 7 0 true
execute if score @e[type=marker,tag=rc.global,limit=1] rc.event matches 8 if score $patt rc.combo matches 2 as @a run effect give @s minecraft:blindness 4 0 true


# ==========================================
#  EVENT 9: FAKE GRAVITY FLIP (VISUAL ILLUSION)
# ==========================================
# We don't actually flip gravity, but we flip orientation and mess with camera feeling

execute if score @e[type=marker,tag=rc.global,limit=1] rc.event matches 9 run title @a actionbar {"text":"§6[Chaos] §fGravity Glitch!"}
execute if score @e[type=marker,tag=rc.global,limit=1] rc.event matches 9 run playsound minecraft:block.portal.trigger master @a ~ ~ ~ 0.8 1.5

# Spin players quickly: rotate them 180 degrees
execute if score @e[type=marker,tag=rc.global,limit=1] rc.event matches 9 as @a at @s run tp @s ~ ~ ~ ~180 ~180

# Particle ring above heads
execute if score @e[type=marker,tag=rc.global,limit=1] rc.event matches 9 as @a at @s run particle minecraft:portal ~ ~2 ~ 0.5 1 0.5 0.3 80


# ==========================================
#  EXTRA: SIMPLE COMBO COUNTER (PERSISTENT CHAOS)
# ==========================================
# Every time this function runs, we increment a global combo score.
# On certain milestones, we trigger bigger things.

scoreboard players add $combo rc.combo 1

# Every 20 calls -> bonus fireworks
execute if score $combo rc.combo matches 20 run title @a actionbar {"text":"§d[Chaos] §fCOMBO x20 – Bonus Show!"}
execute if score $combo rc.combo matches 20 as @a at @s run summon firework_rocket ~ ~2 ~ {LifeTime:40,FireworksItem:{id:"minecraft:firework_rocket",Count:1b,tag:{Fireworks:{Flight:2,Explosions:[{Type:2,Colors:[I;15790320,16711680],FadeColors:[I;5636095]}]}}}}
execute if score $combo rc.combo matches 20 run scoreboard players set $combo rc.combo 0

