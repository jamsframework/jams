@echo off

SET ARGUMENTS=
SET VM=java
SET OPTIONS=-Xms128M -Xmx512M -Djava.library.path=bin

@echo on
%VM% %OPTIONS% -jar lib/jams-starter.jar %ARGUMENTS%
