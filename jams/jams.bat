@echo off

SET VM=java
SET OPTIONS=-Xms128M -Xmx1024M -Dsun.java2d.d3d=false -Djava.library.path=lib/lib -splash:

@echo on
%VM% %OPTIONS% -jar lib/jams-starter.jar %*
