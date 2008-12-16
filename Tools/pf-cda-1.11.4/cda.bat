@echo off

set JVM_OPTS=-Dcom.sun.management.jmxremote.port=4242 -Dcom.sun.management.jmxremote.authenticate=false -Dcom.sun.management.jmxremote.ssl=false
set JAVA_HOME=C:\Programme\Java\jdk1.6.0_10
REM "%JAVA_HOME%\bin\java" -Xmx512M %JVM_OPTS% -Xdebug -Xrunjdwp:transport=dt_socket,server=y,suspend=y,address=8000 -jar pf-cda-swing.jar %*
"%JAVA_HOME%\bin\java" -Xmx512M %JVM_OPTS% -jar pf-cda-swing.jar %*
