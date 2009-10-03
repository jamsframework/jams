@echo off

SET ARGUMENTS=
SET VM=java
SET OPTIONS=-Xms128M -Xmx512M

@echo on
%VM% %OPTIONS% -jar bin/jams-starter.jar %ARGUMENTS%
