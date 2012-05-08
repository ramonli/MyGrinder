@echo off
setlocal
call ./setGrinderEnv.bat
echo CLASSPATH=%CP%
java %JAVA_OPTS% -classpath %CP% net.grinder.Grinder %GRINDER_PROPERTIES%
