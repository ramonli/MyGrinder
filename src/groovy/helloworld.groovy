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
// NOTE: the groovy test script must be wrapper in a class.
class HelloWorld{
	def log;

	HelloWorld(){
		// A shorter alias for the grinder.logger.output() method.
		log = grinder.logger
		// Create a Test with a test number and a description. The test will be
		// automatically registered with The Grinder console if you are using
		// it.
		Test test = new Test(1, "TE Sale(L590)[" + grinder.properties.getInt("grinder.threads", 1) + "X" + grinder.properties.getInt("grinder.runs", 1) + "]")
		test.record(log)
	}

    // This method is called for every run.
	def testRunner = { 
		grinder.statistics.delayReports = 1

		log.info("hello, world")	// this is the last test
		
		// The setting of custom statistics must be stated after the test method, here is 'log.info()', otherwise the forLastTest will get null.
		grinder.statistics.forLastTest.setLong("userLong0", 150)
		println  grinder.statistics.forLastTest.getLong("userLong0")

		grinder.statistics.forLastTest.setLong("userLong1", grinder.runNumber)
		println grinder.runNumber

		if (grinder.runNumber == 1 || grinder.runNumber == 0){
			// if set the last test as false, all statistics of the last test won't be reported.
			grinder.statistics.forLastTest.setSuccess(false)   
		}

		// The sum() and count() can only be applied on sample statistics, and at present there is only one sample statistics 'timedTests'(refer to
		// http://grinder.sourceforge.net/g3/script-javadoc/net/grinder/script/Statistics.html), and also the explanation from author of grinder(refer
		// to http://grinder.996249.n3.nabble.com/User-defined-mean-test-times-td8712.html)
		//
		// Grinder has some built-in statistis for custom usage, such as userLong0, userDouble0 etc. The non-sample statistics will be cumulated on 
		// each successful test. 
		grinder.statistics.registerSummaryExpression("Time to assemble", "(/ userLong0 userLong1)")
	}
}
