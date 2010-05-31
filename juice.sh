#!/bin/sh

PLATFORM=linux64
VM=java
OPTIONS="-Xms128M -Xmx512M -Dsun.java2d.d3d=false -Djava.library.path=bin/$PLATFORM -splash:"
$VM $OPTIONS -jar lib/juice-starter.jar $1 $2 $3 $4

