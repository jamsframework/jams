@echo off
rem Starts JUICE (the model editor) directly, skipping the JAMS launcher dialog.
rem For R/JRI coupling: set JAVA_OPTS=-Djava.library.path=C:\path\to\rJava\jri
rem Override heap/JVM options: set JAVA_OPTS=-Xmx8g

cd /d "%~dp0"
set VM_OPTS=-Xms128M -Xmx10G -splash:JAMSsplash.png %JAVA_OPTS%

java %VM_OPTS% -jar juice-starter.jar %*
