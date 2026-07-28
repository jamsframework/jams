@echo off
rem Starts JAMS with its graphical launcher.
rem Command-line use without GUI, e.g.: jams.bat -n -h model.jam
rem Override heap/JVM options: set JAVA_OPTS=-Xmx4g

cd /d "%~dp0"

rem In no-GUI mode (-n/--nogui) run truly headless: drop the splash (needs a
rem display) and force java.awt.headless, so it works on servers without graphics.
set "SPLASH=-splash:JAMSsplash.png"
set "HEADLESS="
for %%A in (%*) do (
    if /I "%%~A"=="-n"      ( set "SPLASH=" & set "HEADLESS=-Djava.awt.headless=true" )
    if /I "%%~A"=="--nogui" ( set "SPLASH=" & set "HEADLESS=-Djava.awt.headless=true" )
)

set "VM_OPTS=-Xms128M -Xmx10G %SPLASH% %HEADLESS% %JAVA_OPTS%"

java %VM_OPTS% -jar jams-starter.jar %*
