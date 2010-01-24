@echo off

SET VM=java
SET OPTIONS=-Xms128M -Xmx512M -Dsun.java2d.d3d=false -Djava.library.path=bin/win32

@echo on
%VM% %OPTIONS% -jar lib/juice-starter.jar %1 %2 %3 %4
