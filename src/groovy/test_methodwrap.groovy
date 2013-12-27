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
	def wrapMethod;

	HelloWorld(){
		// Create a Test with a test number and a description. The test will be
		// automatically registered with The Grinder console if you are using
		// it.
		Test test = new Test(1, "HelloWorld")

		// !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
		// ! Seem the method wrapping doesn't work in groovy script.       !
		// !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
		test.record(this.&testWrap)
	}

    // This method is called for every run.
	def testRunner = { 
		grinder.statistics.delayReports = 1

		testWrap("Lai")
		
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

	def testWrap(String msg) {
		grinder.statistics.forCurrentTest.setLong("userLong0", 15)
		println "hello ${msg}"
	}
}
