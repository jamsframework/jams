#!/bin/sh
#
# Same as jams.sh, but double-clickable in the macOS Finder.
#
cd "$(dirname "$0")"
VM_OPTS="-Xms128M -Xmx1024M -splash:JAMSsplash.png -Djavax.accessibility.assistive_technologies= $JAVA_OPTS"
exec java $VM_OPTS -jar jams-starter.jar "$@"
