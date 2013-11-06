// HTTP Test
//
// Sample script to test HTTP server

import static net.grinder.script.Grinder.grinder
import net.grinder.script.Test
import net.grinder.plugin.http.HTTPRequest
import groovy.xml.Namespace

// A TestRunner instance is created for each thread. It can be used to
// store thread-specific data.
// NOTE: the groovy test script must be wrapper in a class.
class HttpTest{
	private HTTPRequest httpRequest;
	// Test configuration
	private String wgpeUrl = "http://202.65.213.109:8135/mlottery_wgpe_gm/ServicePort/"
	private long merchantId = 167

	HttpTest(){
		httpRequest = new HTTPRequest()
		// Create a Test with a test number and a description. The test will be
		// automatically registered with The Grinder console if you are using
		// it.
		def test = new Test(2, "WGPE Sale")
		test.record(httpRequest)
	}

    // There must be closure named 'testRunner', which will be ran once for each 'grinder run'.
	def testRunner = { 
        // Normally test results are reported automatically when the test returns. If you want to 
        // alter the statistics after a test has completed, you must set delayReports = 1 to delay 
        // the reporting before performing the test. This only affects the current worker thread.
        grinder.statistics.delayReports = 1

		//println "send request: ${req}"
		//println "agent number: ${grinder.agentNumber}"
		//println "process number: ${grinder.processNumber}"
		//println "thread number: ${grinder.threadNumber}"
		//println "run number: ${grinder.runNumber}"

		String timestamp = genTimestamp()
		String traceMsgId = genTraceMsgId(grinder.agentNumber)
		String req = assembleRequest(merchantId, timestamp, traceMsgId)
		// issue request to remote service
		def response = httpRequest.POST(wgpeUrl, req.bytes)

		// check whether the response is successful
		int respCode = getSuccessfulResponse(response.text)
		if (respCode != 200){
			grinder.logger.warn("Request with traceMsgId($traceMsgId) got unsuccessful response code $respCode")
			// Set success = 0 to mark the test as a failure.
			grinder.statistics.forLastTest.setSuccess(false)   
		}
	}

	def String assembleRequest(long merchantId, String timestamp, String traceMsgId) {

		return """
		<soap:Envelope xmlns:soap="http://schemas.xmlsoap.org/soap/envelope/">
			<soap:Header>
				<ns2:headReq xmlns:ns2="http://www.lottery.mpos.com/wgpe/port/schemas/">
					<ns2:ProtocalVersion>2.1</ns2:ProtocalVersion>
					<ns2:Timestamp>${timestamp}</ns2:Timestamp>
					<ns2:MerchantId>${merchantId}</ns2:MerchantId>
					<ns2:TransMessageID>${traceMsgId}</ns2:TransMessageID>
					<ns2:GameTypeId>8</ns2:GameTypeId>
					<ns2:SystemId>1</ns2:SystemId>
					<ns2:MPOSSignature>11111</ns2:MPOSSignature>
				</ns2:headReq>
			</soap:Header>
			<soap:Body>
				<ns2:SellReq xmlns:ns2="http://www.lottery.mpos.com/wgpe/port/schemas/">
					<ns2:MultipleDraws>1</ns2:MultipleDraws>
					<ns2:PIN>!!!!</ns2:PIN>
					<ns2:GameDraw>
						<ns2:Number>20131105</ns2:Number>
						<ns2:GameId>4028820741e83a240141e84ebcaf0021</ns2:GameId>
					</ns2:GameDraw>
					<ns2:Entry>
						<ns2:SelectedNumber>1,2,3,4,5-4,7</ns2:SelectedNumber>
					</ns2:Entry>
					<ns2:Entry>
						<ns2:SelectedNumber>5N3*</ns2:SelectedNumber>
					</ns2:Entry>
					<ns2:Entry>
						<ns2:SelectedNumber>QP</ns2:SelectedNumber>
					</ns2:Entry>
					<ns2:User mobile="13728994799"/>
				</ns2:SellReq>
			</soap:Body>
		</soap:Envelope>
		"""
	}

	def synchronized String genTraceMsgId(int agentId){
		if (agentId < 0) agentId = 0
		if (agentId > 9) throw new IllegalArgumentException("AgentID cant'be greater than 9")

		// read sequence number from file
		def sequenceFile = new File("sequence.var")
		if (!sequenceFile.exists())
		    sequenceFile.createNewFile();
		int seq = sequenceFile.text.trim()==""?0:sequenceFile.text.trim().toInteger()
		sequenceFile.withWriter {out ->
		    out.writeLine("${seq+1}")
		}

		"0" + agentId + String.format('%012d',seq)
	}

	def String genTimestamp(){
		new Date().format("yyyyMMddHHmmss")
	}

	def getSuccessfulResponse(String xml) {
		def ns2 = new Namespace("http://www.lottery.mpos.com/wgpe/port/schemas/", 'ns2')
		def soap = new Namespace("http://schemas.xmlsoap.org/soap/envelope/", 'soap')
		def envelope = new XmlParser().parseText(xml)
		envelope[soap.Header][ns2.headResp][ns2.ResponseCode].text().toInteger()
	}
}
