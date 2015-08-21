#!/bin/sh

cd "$(dirname "$0")"
VM=java
OPTIONS='-Xms128M -Xmx1024M -Dsun.java2d.d3d=false -Djava.library.path=lib/lib -splash:'
$VM $OPTIONS -jar lib/jams-starter.jar $*

