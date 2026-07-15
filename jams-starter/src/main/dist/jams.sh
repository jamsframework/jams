#!/bin/sh
#
# Startet JAMS mit grafischer Oberflaeche (Standard-Launcher, entspricht der
# alten NetBeans-Run-Konfiguration "JAMS Launcher").
#
# Kommandozeilen-Betrieb ohne GUI, z.B.:
#   ./jams.sh -n -h model.jam
# (-n = keine GUI, -h = Hilfe anzeigen; siehe jamsui.cmdline.JAMSCmdLine)
#
# Heap/JVM-Optionen ueberschreiben:
#   JAVA_OPTS="-Xmx4g" ./jams.sh
#
cd "$(dirname "$0")"
VM_OPTS="-Xms128M -Xmx1024M -splash:JAMSsplash.png $JAVA_OPTS"
exec java $VM_OPTS -jar jams-starter.jar "$@"
