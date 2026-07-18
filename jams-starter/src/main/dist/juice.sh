#!/bin/sh
#
# Starts JUICE (the model editor) directly, skipping the JAMS launcher
# dialog (corresponds to the old NetBeans run configuration "JUICE").
#
# For coupling with R/JRI (run configuration "JUICE + JRI"), e.g.:
#   JAVA_OPTS="-Djava.library.path=/path/to/rJava/jri" ./juice.sh
#
# Override heap/JVM options:
#   JAVA_OPTS="-Xmx8g" ./juice.sh
#
cd "$(dirname "$0")"
VM_OPTS="-Xms128M -Xmx1024M -splash:JAMSsplash.png -Djavax.accessibility.assistive_technologies= $JAVA_OPTS"
exec java $VM_OPTS -jar juice-starter.jar "$@"
