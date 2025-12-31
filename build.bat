echo Building CMDMaker Server version...
call ./gradlew :server:build
powershell cp server/build/libs/* CompiledFiles/
echo Building CMDMaker Client version...
call ./gradlew :client:build
powershell cp client/build/libs/* CompiledFiles/

echo Build complete! 