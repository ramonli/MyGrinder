@echo off
REM -----------------------------------------------------------
REM Adjust below variable to meet your needs
REM -----------------------------------------------------------
set JYTHON_HOME=D:\jython2.5.2
set GRINDER_HOME=E:\project\grinder-3.11
set GRINDER_PROPERTIES=%cd%\..\src\grinder.properties
REM add the third party libary to classpath, seperated by comma(,).
set JAVA_CP=

REM -----------------------------------------------------------
REM Do not change below part
REM -----------------------------------------------------------
REM add all jar files to classpath...doesn't work
REM for /R . %%i in (*.jar) do call set CLASSPATH=%%i;%CLASSPATH%
REM upgrade to jython2.5.2...looks like grinder3.5 doesn't support jython2.5.x yet, as when I run helloworld.py, a exception thrown out: Caused by: net.grinder.script.NonInstrumentableTypeException: Cannot instrument class net.grinder.engine.process.ExternalLogger. If switch back to jython2.2.1 which is shiped with grinder3.5 distribution, it works!
REM refer to http://grinder.sourceforge.net/g3/jython.html...
REM    - add jython.jar to classpath
REM    - set environment variable python.home
REM    - may remove jython.jar from grinder lib/
set CP=%JAVA_CP%;%JYTHON_HOME%/jython.jar;%GRINDER_HOME%/lib/asm-3.2.jar;%GRINDER_HOME%/lib/grinder-agent.jar;%GRINDER_HOME%/lib/grinder.jar;%GRINDER_HOME%/lib/grinder-xmlbeans.jar;%GRINDER_HOME%/lib/jsr173_1.0_api.jar;%GRINDER_HOME%/lib/picocontainer-2.13.6.jar;%GRINDER_HOME%/lib/xbean.jar

set JAVA_OPTS=-Dgrinder.jvm.arguments=-Dpython.home=%JYTHON_HOME%

