#!/bin/sh
#
# Starts JAMS with its graphical launcher (corresponds to the old
# NetBeans run configuration "JAMS Launcher").
#
# Command-line use without GUI, e.g.:
#   ./jams.sh -n -h model.jam
# (-n = no GUI, -h = show help; see jamsui.cmdline.JAMSCmdLine)
#
# Override heap/JVM options:
#   JAVA_OPTS="-Xmx4g" ./jams.sh
#
cd "$(dirname "$0")"
VM_OPTS="-Xms128M -Xmx1024M -splash:JAMSsplash.png -Djavax.accessibility.assistive_technologies= $JAVA_OPTS"
exec java $VM_OPTS -jar jams-starter.jar "$@"
