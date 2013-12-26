@echo off
setlocal

REM -----------------------------------------------------------
REM Adjust below variable to meet your needs
REM -----------------------------------------------------------
set GRINDER_HOME=E:\project\grinder-3.11
set JAVA_HOME=%JAVA_HOME%

REM -----------------------------------------------------------
REM Do not change below part
REM -----------------------------------------------------------
set PATH=%JAVA_HOME%\bin;%PATH%

set CP=%GRINDER_HOME%/lib/grinder.jar

REM echo "use CLASSPATH=%CP%"
REM refer to http://grinder.sourceforge.net/g3/tcpproxy.html
java %JAVA_OPTS% -classpath %CP% net.grinder.TCPProxy
