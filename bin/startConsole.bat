@echo off
setlocal
call ./setGrinderEnv.bat
echo CLASSPATH=%CP%
java -classpath %CP% -Dswing.defaultlaf=javax.swing.plaf.metal.MetalLookAndFeel net.grinder.Console