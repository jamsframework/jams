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

# In no-GUI mode (-n/--nogui) run truly headless: drop the splash (needs a
# display) and force java.awt.headless, so it works on servers without graphics.
SPLASH="-splash:JAMSsplash.png"
HEADLESS=""
for arg in "$@"; do
  case "$arg" in
    -n|--nogui) SPLASH=""; HEADLESS="-Djava.awt.headless=true"; break ;;
  esac
done

VM_OPTS="-Xms128M -Xmx10G $SPLASH $HEADLESS -Djavax.accessibility.assistive_technologies= $JAVA_OPTS"
exec java $VM_OPTS -jar jams-starter.jar "$@"
