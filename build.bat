echo Building CMDMaker Server version...
call ./gradlew :server:build
powershell cp server/build/libs/* CompiledFiles/
call ./gradlew :quilt-server:build
powershell cp quilt-server/build/libs/* CompiledFiles/
echo Building CMDMaker Client version...
call ./gradlew :client:build
powershell cp client/build/libs/* CompiledFiles/
call ./gradlew :quilt-client:build
powershell cp quilt-client/build/libs/* CompiledFiles/
echo Copying resources...
powershell cp CompiledFiles/* D:\Minecraft-Command-Maker\docs\cdn\files
echo Build complete! Press any key to exit...
pause > nul 