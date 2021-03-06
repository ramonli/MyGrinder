@echo off
setlocal

REM -----------------------------------------------------------
REM Adjust below variable to meet your needs
REM -----------------------------------------------------------
set GRINDER_HOME=D:\project\grinder-3.11
set JAVA_HOME=%JAVA_HOME%

REM -----------------------------------------------------------
REM Do not change below part
REM -----------------------------------------------------------
set PATH=%JAVA_HOME%\bin;%PATH%
REM By default, the test script files should be placed at same directory with properties file
set GRINDER_PROPERTIES=%cd%\..\conf\grinder.properties

REM add all jars under GRINDER_HOME/lib to classpath, refer to http://stackoverflow.com/questions/524081/bat-file-to-create-java-classpath
setLocal EnableDelayedExpansion
set CP=
REM EXT_LIB will contain all 3rd party libraries.
set EXT_LIB=%cd%/../lib
for /R %EXT_LIB% %%a in (*.jar) do (
	set CP=!CP!;%%a
)
set CP=../conf;%CP%;%GRINDER_HOME%/lib/grinder.jar

REM echo "use CLASSPATH=%CP%"
echo "java %JAVA_OPTS% -classpath %CP% net.grinder.Grinder %GRINDER_PROPERTIES%"
java %JAVA_OPTS% -classpath %CP% net.grinder.Grinder %GRINDER_PROPERTIES%
