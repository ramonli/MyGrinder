// HTTP Test
//
// Sample script to test HTTP server

import static net.grinder.script.Grinder.grinder
import net.grinder.script.Test
import net.grinder.plugin.http.HTTPRequest

// A TestRunner instance is created for each thread. It can be used to
// store thread-specific data.
// NOTE: the groovy test script must be wrapper in a class.
class HttpTest{
	def HTTPRequest httpRequest;

	HttpTest(){
		httpRequest = new HTTPRequest()
		// Create a Test with a test number and a description. The test will be
		// automatically registered with The Grinder console if you are using
		// it.
		def test = new Test(2, "HTTP Test")
		test.record(httpRequest)
	}

    // There must be closure named 'testRunner', which will be ran once for each 'grinder run'.
	def testRunner = { 
		def result = httpRequest.GET("http://www.apache.org/")
		// write result to a file
		File file = new File("log\\result.log")
		file << result.text
	}
}
