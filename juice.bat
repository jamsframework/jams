@echo off

SET VM=java
SET OPTIONS=-Xms128M -Xmx1024M -splash:

@echo on
%VM% %OPTIONS% -jar jams-bin/juice-starter.jar %*
