@echo off
REM "%JAVA_HOME%\bin\java" -jar "pf-sssgen.jar" -f pf-cda-swing.jar -dynamic "./;./config;pf-cda-*.jar;lib/*.jar;lib/ext/*.jar" %*

"%JAVA_HOME%\bin\java" -jar "pf-sssgen.jar" -f pf-cda-swing.jar %*
