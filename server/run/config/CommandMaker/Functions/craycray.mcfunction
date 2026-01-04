# Cray Cray Function for Minecraft 1.21.9 with Carpet
# This function does some wild stuff!

# Spawn a bunch of random mobs around the player
execute at @p run summon minecraft:creeper ~ ~ ~ {ExplosionRadius:0,Fuse:0}
execute at @p run summon minecraft:zombie ~2 ~ ~ 
execute at @p run summon minecraft:skeleton ~-2 ~ ~
execute at @p run summon minecraft:spider ~ ~ ~2
execute at @p run summon minecraft:enderman ~ ~ ~-2

# Create some particle effects
execute at @p run particle minecraft:explosion ~ ~ ~ 1 1 1 0.1 50
execute at @p run particle minecraft:flame ~ ~1 ~ 0.5 0.5 0.5 0.1 100
execute at @p run particle minecraft:heart ~ ~2 ~ 1 1 1 0.1 20

# Play crazy sounds
execute at @p run playsound minecraft:entity.generic.explode master @p ~ ~ ~ 1 1
execute at @p run playsound minecraft:entity.ender_dragon.growl master @p ~ ~ ~ 1 0.5
execute at @p run playsound minecraft:ambient.cave.cave master @p ~ ~ ~ 1 2

# Give the player some random effects
effect give @p minecraft:levitation 5 10
effect give @p minecraft:speed 10 5
effect give @p minecraft:jump_boost 10 5

# Spawn some TNT for extra chaos
execute at @p run summon minecraft:tnt ~3 ~ ~ {Fuse:40}
execute at @p run summon minecraft:tnt ~-3 ~ ~
execute at @p run summon minecraft:tnt ~ ~ ~3
execute at @p run summon minecraft:tnt ~ ~ ~-3

# If Carpet is installed, use some Carpet commands


# Teleport the player randomly
execute at @p run tp @p ~10 ~ ~10

# Send a crazy message
tellraw @p {"text":"CRAY CRAY TIME!","color":"red","bold":true}

# End with a bang
execute at @p run summon minecraft:firework_rocket ~ ~ ~ {FireworksItem:{id:"minecraft:firework_rocket",Count:1,tag:{Fireworks:{Explosions:[{Type:1,Colors:[16711680],FadeColors:[16776960]}]}}}}}
