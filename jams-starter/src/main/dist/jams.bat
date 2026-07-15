@echo off
rem Startet JAMS mit grafischer Oberflaeche (Standard-Launcher).
rem Kommandozeilen-Betrieb ohne GUI, z.B.: jams.bat -n -h model.jam
rem Heap/JVM-Optionen ueberschreiben: set JAVA_OPTS=-Xmx4g

cd /d "%~dp0"
set VM_OPTS=-Xms128M -Xmx1024M -splash:JAMSsplash.png %JAVA_OPTS%

java %VM_OPTS% -jar jams-starter.jar %*
