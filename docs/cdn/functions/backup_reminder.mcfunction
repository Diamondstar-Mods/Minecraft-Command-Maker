# Backup Reminder Utility
# Announces a server backup reminder to all players
title @a title {"text":"Backup Reminder!","color":"gold","bold":true}
title @a subtitle {"text":"It's time to back up your server!","color":"yellow"}
tellraw @a [{"text":"[Backup]","color":"gold","bold":true},{"text":" Regular server backups prevent data loss. Back up now!","color":"white"}]
tellraw @a [{"text":"[Backup]","color":"gold","bold":true},{"text":" 1. Stop the server","color":"white"}]
tellraw @a [{"text":"[Backup]","color":"gold","bold":true},{"text":" 2. Copy the world folder to a safe location","color":"white"}]
tellraw @a [{"text":"[Backup]","color":"gold","bold":true},{"text":" 3. Also back up config/CommandMaker/ for your aliases and variables","color":"white"}]
tellraw @a [{"text":"[Backup]","color":"gold","bold":true},{"text":" 4. Restart the server once the backup is complete","color":"white"}]
execute at @s run setblock ~ ~ ~ minecraft:ender_chest
playsound minecraft:block.note_block.pling master @a ~ ~ ~ 1 2
say [Server] Backup reminder! Make sure your server data is safe.
