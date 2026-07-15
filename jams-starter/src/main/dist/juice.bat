@echo off
rem Startet JUICE (den Modell-Editor) direkt, ohne den JAMS-Launcher-Dialog.
rem Fuer R/JRI-Kopplung: set JAVA_OPTS=-Djava.library.path=C:\pfad\zu\rJava\jri
rem Heap/JVM-Optionen ueberschreiben: set JAVA_OPTS=-Xmx8g

cd /d "%~dp0"
set VM_OPTS=-Xms128M -Xmx1024M -splash:JAMSsplash.png %JAVA_OPTS%

java %VM_OPTS% -jar juice-starter.jar %*
