@echo off

SET ARGUMENTS=
SET VM=java
SET OPTIONS=-Xms128M -Xmx512M
SET LIBS=bin/jams-api.jar;bin/jams-main.jar;bin/jams-common.jar;bin/jams-explorer.jar;bin/jams-ui.jar

@echo on
%VM% %OPTIONS% -cp %LIBS% jamsui.juice.JUICE %ARGUMENTS%
