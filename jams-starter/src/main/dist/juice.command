#!/bin/sh
#
# Wie juice.sh, aber per Doppelklick im Finder startbar.
#
cd "$(dirname "$0")"
VM_OPTS="-Xms128M -Xmx1024M -splash:JAMSsplash.png $JAVA_OPTS"
exec java $VM_OPTS -jar juice-starter.jar "$@"
