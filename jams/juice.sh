#!/bin/sh

VM=java
OPTIONS='-Xms128M -Xmx1024M -Dsun.java2d.d3d=false -Djava.library.path=lib/lib'
$VM $OPTIONS -jar lib/juice-starter.jar $*

