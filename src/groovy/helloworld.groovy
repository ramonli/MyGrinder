// Hello World
//
// A minimal script that tests The Grinder logging facility.
//
// This script shows the recommended style for scripts, with a
// TestRunner class. The script is executed just once by each worker
// process and defines the TestRunner class. The Grinder creates an
// instance of TestRunner for each worker thread, and repeatedly calls
// the instance for each run of that thread.

import static net.grinder.script.Grinder.grinder
import net.grinder.script.Test

// A TestRunner instance is created for each thread. It can be used to
// store thread-specific data.
class HelloWorld{
	def log;

	HelloWorld(){
		// A shorter alias for the grinder.logger.output() method.
		log = grinder.logger
		// Create a Test with a test number and a description. The test will be
		// automatically registered with The Grinder console if you are using
		// it.
		def test = new Test(1, "Log method")
		test.record(log)
	}

    // This method is called for every run.
	def testRunner = { 
		log.info("hello, world")
	}
}
