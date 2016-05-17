#!/bin/sh

cd "$(dirname "$0")"
VM=java
OPTIONS='-Xms128M -Xmx1024M -splash:'
$VM $OPTIONS -jar jams-bin/jams-starter.jar $*

