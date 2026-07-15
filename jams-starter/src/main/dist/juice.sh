#!/bin/sh
#
# Startet JUICE (den Modell-Editor) direkt, ohne den JAMS-Launcher-Dialog
# (entspricht der alten NetBeans-Run-Konfiguration "JUICE").
#
# Fuer die Kopplung mit R/JRI (Run-Konfiguration "JUICE + JRI"), z.B.:
#   JAVA_OPTS="-Djava.library.path=/pfad/zu/rJava/jri" ./juice.sh
#
# Heap/JVM-Optionen ueberschreiben:
#   JAVA_OPTS="-Xmx8g" ./juice.sh
#
cd "$(dirname "$0")"
VM_OPTS="-Xms128M -Xmx1024M -splash:JAMSsplash.png $JAVA_OPTS"
exec java $VM_OPTS -jar juice-starter.jar "$@"
