#!/bin/sh

VM=java
OPTIONS='-Xms128M -Xmx1024M -splash:'
$VM $OPTIONS -jar jams-bin/juice-starter.jar $*

