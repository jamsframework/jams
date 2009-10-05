@echo off

SET ARGUMENTS=
SET VM=java
SET OPTIONS=-Xms128M -Xmx512M -Djava.library.path=bin/lib

@echo on
%VM% %OPTIONS% -jar bin/juice-starter.jar %ARGUMENTS%
