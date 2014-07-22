@echo off

SET VM=java
SET OPTIONS=-Xms128M -Xmx512M -Dsun.java2d.d3d=false -splash:

@echo on
%VM% %OPTIONS% -jar lib/juice-starter.jar %*
