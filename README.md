Dependencies
----------------------------------------------
* Groovy - 2.4.4
* Grinder - 3.11
* grinder-groovy - https://github.com/DealerDotCom/grinder-groovy

Setup Grinder
----------------------------------------------
1. Download grinder, and unzip it to a directory.
2. Create a directory 'ext' under GRINDER_HOME/lib, and put /lib/grinder-groovy-1.0.0-SNAPSHOT.jar and groovy-all-VERSION.jar under it.
3. edit bin/startAgent.bat, set variable 'GRINDER_HOME' to grinder installation directory.
Noted that the full path of GRINDER_HOME and GROOVYGRINDER_HOME can't contain space, otherwise 'startAgent.bat' may can't startup.

Logging
----------------------------------------------
Configure logging by edit conf/logback-worker.xml