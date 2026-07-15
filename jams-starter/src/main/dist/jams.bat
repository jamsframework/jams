@echo off
rem Starts JAMS with its graphical launcher.
rem Command-line use without GUI, e.g.: jams.bat -n -h model.jam
rem Override heap/JVM options: set JAVA_OPTS=-Xmx4g

cd /d "%~dp0"
set VM_OPTS=-Xms128M -Xmx1024M -splash:JAMSsplash.png %JAVA_OPTS%

java %VM_OPTS% -jar jams-starter.jar %*
