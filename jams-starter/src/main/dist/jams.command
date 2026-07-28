#!/bin/sh
#
# Same as jams.sh, but double-clickable in the macOS Finder.
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

VM_OPTS="-Xms128M -Xmx1024M $SPLASH $HEADLESS -Djavax.accessibility.assistive_technologies= $JAVA_OPTS"
exec java $VM_OPTS -jar jams-starter.jar "$@"
