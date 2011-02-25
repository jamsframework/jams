@echo off

SET platform=win64
SET VM=java
SET OPTIONS=-Xms128M -Xmx512M -XX:MaxPermSize=128m -Dsun.java2d.d3d=false -Djava.library.path=bin/%platform% 
%VM% %OPTIONS% -jar lib/juice-starter.jar %*
