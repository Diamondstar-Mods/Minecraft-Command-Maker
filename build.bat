echo Building CMDMaker Server version...
call ./gradlew :server:build
powershell cp server/build/libs/* CompiledFiles/
echo Building CMDMaker Client version...
call ./gradlew :client:build
powershell cp client/build/libs/* CompiledFiles/
powershell cp ./client/build/libs/CMDMaker-Fabric-client-* "C:/Users/adam/AppData/Roaming/PrismLauncher/instances/CommandMaker Client TestingInstance/minecraft/mods/"
echo Build complete! 