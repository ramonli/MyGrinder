Dependencies
----------------------------------------------
* Groovy - 2.4.4
* Grinder - 3.11
* grinder-groovy - https://github.com/DealerDotCom/grinder-groovy

Setup Grinder
----------------------------------------------
1. Download grinder, and unzip it to a directory.
2. Edit bin/startAgent.bat, set variable 'GRINDER_HOME' to grinder installation directory.

Notes
----------------------------------------------
1. The full path of GRINDER_HOME and GROOVYGRINDER_HOME shouldn't contain space, otherwise 'startAgent.bat' may fail to startup.
2. All 3rd party libraries should be put under lib directory, and they will be added to classpath automatically.

How can grinder support groovy?
----------------------------------------------
Check the lib grinder-groovy-VERSION.jar, the META-INF/net.grinder.scriptengine has declared the script engine, doesn't need any change to grinder.

Logging
----------------------------------------------
Configure logging by editing conf/logback-worker.xml