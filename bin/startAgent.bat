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
REM By default, the test script files should be placed at same directory with properties file
set GRINDER_PROPERTIES=%cd%\..\src\grinder.properties

REM add all jar files to classpath, refer to http://stackoverflow.com/questions/524081/bat-file-to-create-java-classpath
setLocal EnableDelayedExpansion
set CP=
for /R %GRINDER_HOME%/lib %%a in (*.jar) do (
	set CP=!CP!;%%a
)
set CP=../src;%CP%

REM echo "use CLASSPATH=%CP%"
java %JAVA_OPTS% -classpath %CP% net.grinder.Grinder %GRINDER_PROPERTIES%
